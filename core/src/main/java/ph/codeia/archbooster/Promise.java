package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import java.util.concurrent.Callable;

import ph.codeia.archbooster.function.Function1;

public interface Promise<T> extends Operation<T, Exception> {

    static <T> Promise<T> of(Callable<? extends T> block) {
        return (ok, err) -> {
            try {
                ok.emit(block.call());
            }
            catch (Exception e) {
                err.emit(e);
            }
        };
    }

    static <T> Promise<T> just(T t) {
        return (ok, err) -> ok.emit(t);
    }

    static <T> Promise<T> failed(Exception e) {
        return (ok, err) -> err.emit(e);
    }

    interface Case<T> extends Emitter<Promise<T>> {
        void success(T t);
        void failure(Exception e);

        @Override
        default void emit(Promise<T> value) {
            value.select(this);
        }
    }

    default void select(Case<T> matcher) {
        run(matcher::success, matcher::failure);
    }

    default <U> Promise<U> map(Function1<T, U> transform) {
        return (ok, err) -> select(new Case<T>() {
            @Override
            public void success(T t) {
                ok.emit(transform.apply(t));
            }

            @Override
            public void failure(Exception e) {
                err.emit(e);
            }
        });
    }

    default <U> Promise<U> flatMap(Function1<T, Promise<U>> chain) {
        return (ok, err) -> select(new Case<T>() {
            @Override
            public void success(T t) {
                chain.apply(t).run(ok, err);
            }

            @Override
            public void failure(Exception e) {
                err.emit(e);
            }
        });
    }
}
