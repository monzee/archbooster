package ph.codeia.archbooster.experiments;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.Emitter;
import ph.codeia.archbooster.Operation;
import ph.codeia.archbooster.experiments.transform.Operations;
import ph.codeia.archbooster.experiments.typeclass.MonadPlus;
import ph.codeia.archbooster.function.Function1;

public interface Maybe<T> extends Operation<T, Void>, Higher<Maybe, T> {

    interface Case<T> {
        void just(T t);
        void nothing();
    }

    void runMaybe(Case<T> c);

    @Override
    default void run(Emitter<? super T> states, Emitter<? super Void> events) {
        runMaybe(new Case<T>() {
            @Override
            public void just(T t) {
                states.emit(t);
            }

            @Override
            public void nothing() {
                events.emit(null);
            }
        });
    }

    default <U> Maybe<U> pipe(Operations.LeftComposer<T, U, Void> transform) {
        return of(transform.fromLeft(this));
    }

    static <T> Maybe<T> of(Higher<Maybe, T> hk) {
        return (Maybe<T>) hk;
    }

    static <T> Maybe<T> of(Operation<T, ?> op) {
        return c -> op.run(c::just, e -> c.nothing());
    }

    static <T> Maybe<T> just(T t) {
        return c -> c.just(t);
    }

    static <T> Maybe<T> nothing() {
        return Case::nothing;
    }

    Kind KIND = new Kind();

    final class Kind implements MonadPlus<Maybe> {
        private Kind() {}

        @Override
        public <T> Higher<Maybe, T> zero() {
            return nothing();
        }

        @Override
        public <T> Higher<Maybe, T> plus(
                Higher<Maybe, T> left,
                Higher<Maybe, T> right
        ) {
            return (Maybe<T>) c -> of(left).runMaybe(new Case<T>() {
                @Override
                public void just(T t) {
                    c.just(t);
                }

                @Override
                public void nothing() {
                    of(right).runMaybe(c);
                }
            });
        }

        @Override
        public <T, U> Higher<Maybe, U> bind(
                Higher<Maybe, T> source,
                Function1<? super T, ? extends Higher<Maybe, U>> next
        ) {
            return (Maybe<U>) c -> of(source).runMaybe(new Case<T>() {
                @Override
                public void just(T t) {
                    of(next.apply(t)).runMaybe(c);
                }

                @Override
                public void nothing() {
                    c.nothing();
                }
            });
        }

        @Override
        public <T> Higher<Maybe, T> unit(T t) {
            return just(t);
        }

        @Override
        public <T, U> Higher<Maybe, U> map(
                Higher<Maybe, T> source,
                Function1<? super T, ? extends U> f
        ) {
            return (Maybe<U>) c -> of(source).runMaybe(new Case<T>() {
                @Override
                public void just(T t) {
                    c.just(f.apply(t));
                }

                @Override
                public void nothing() {
                    c.nothing();
                }
            });
        }
    }
}
