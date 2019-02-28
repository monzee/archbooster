package ph.codeia.archbooster.experiments;

/*
 * This file is a part of the arch-booster project.
 */

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import ph.codeia.archbooster.Emitter;
import ph.codeia.archbooster.Operation;
import ph.codeia.archbooster.experiments.transform.Operations;
import ph.codeia.archbooster.experiments.typeclass.MonadPlus;
import ph.codeia.archbooster.function.Function1;

public interface Try<T> extends Operation<T, Throwable>, Higher<Try, T> {

    interface Case<T> {
        void success(T t);
        void failure(Throwable t);
    }

    void runTry(Case<? super T> c);

    @Override
    default void run(Emitter<? super T> states, Emitter<? super Throwable> events) {
        runTry(new Case<T>() {
            @Override
            public void success(T t) {
                states.emit(t);
            }

            @Override
            public void failure(Throwable t) {
                events.emit(t);
            }
        });
    }

    default <U> Try<U> pipe(
            Operations.LeftComposer<T, U, Throwable> transform
    ) {
        return of(transform.fromLeft(this));
    }

    static <T> Try<T> of(Higher<Try, T> hk) {
        return (Try<T>) hk;
    }

    static <T> Try<T> of(Operation<T, Throwable> op) {
        return c -> op.run(c::success, c::failure);
    }

    static <T> Try<T> of(Callable<? extends T> block) {
        return c -> {
            T result;
            try {
                result = block.call();
            }
            catch (Exception e) {
                c.failure(e);
                return;
            }
            c.success(result);
        };
    }

    static <T> Try<T> succeed(T t) {
        return c -> c.success(t);
    }

    static <T> Try<T> fail(Throwable t) {
        return c -> c.failure(t);
    }

    Kind KIND = new Kind();

    final class Kind implements MonadPlus<Try> {
        private Kind() {}

        @Override
        public <T> Higher<Try, T> zero() {
            return fail(new NoSuchElementException());
        }

        @Override
        public <T> Higher<Try, T> plus(
                Higher<Try, T> left,
                Higher<Try, T> right
        ) {
            return (Try<T>) c -> of(left).runTry(new Case<T>() {
                @Override
                public void success(T t) {
                    c.success(t);
                }

                @Override
                public void failure(Throwable t) {
                    of(right).runTry(c);
                }
            });
        }

        @Override
        public <T, U> Higher<Try, U> bind(
                Higher<Try, T> source,
                Function1<? super T, ? extends Higher<Try, U>> next
        ) {
            return (Try<U>) c -> of(source).runTry(new Case<T>() {
                @Override
                public void success(T t) {
                    of(next.apply(t)).runTry(c);
                }

                @Override
                public void failure(Throwable t) {
                    c.failure(t);
                }
            });
        }

        @Override
        public <T> Higher<Try, T> unit(T t) {
            return succeed(t);
        }

        @Override
        public <T, U> Higher<Try, U> map(
                Higher<Try, T> source,
                Function1<? super T, ? extends U> f
        ) {
            return (Try<U>) c -> of(source).runTry(new Case<T>() {
                @Override
                public void success(T t) {
                    c.success(f.apply(t));
                }

                @Override
                public void failure(Throwable t) {
                    c.failure(t);
                }
            });
        }
    }
}
