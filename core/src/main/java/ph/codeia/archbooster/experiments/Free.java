package ph.codeia.archbooster.experiments;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.experiments.typeclass.Functor;
import ph.codeia.archbooster.experiments.typeclass.Monad;
import ph.codeia.archbooster.function.Function1;


public interface Free<F, T> extends Higher<Free, T> {
    interface Case<F, T> {
        void free(Higher<F, Free<F, T>> node);
        void pure(T t);
    }

    void match(Case<F, T> interpreter);

    final class Kind<F> implements Monad<Free> {
        private final Functor<F> functor;

        private Kind(Functor<F> functor) {
            this.functor = functor;
        }

        @Override
        public <T> Free<F, T> unit(T t) {
            return Free.pure(t);
        }

        @Override
        public <T, U> Free<F, U> bind(
                Higher<Free, T> source,
                Function1<? super T, ? extends Higher<Free, U>> next
        ) {
            return c -> Free.<F, T>of(source).match(new Case<F, T>() {
                @Override
                public void free(Higher<F, Free<F, T>> node) {
                    c.free(functor.map(node, ft -> bind(ft, next)));
                }

                @Override
                public void pure(T t) {
                    Free.<F, U>of(next.apply(t)).match(c);
                }
            });
        }
    }

    static <F, T> Free<F, T> of(Higher<Free, T> hk) {
        return (Free<F, T>) hk;
    }

    static <F, T> Free<F, T> of(
            Functor<F> functor,
            Function1<Monad<Free>, Higher<Free, T>> block
    ) {
        return of(block.apply(new Kind<>(functor)));
    }

    static <F, T> Free<F, T> pure(T t) {
        return c -> c.pure(t);
    }

    static <F, T> Free<F, T> free(Higher<F, Free<F, T>> node) {
        return c -> c.free(node);
    }

    interface Factory0<F, T> {
        Higher<F, Free<F, T>> make(Function1<T, Free<F, T>> constructor);
    }

    interface Factory1<F, A, T> {
        Higher<F, Free<F, T>> make(A a, Function1<T, Free<F, T>> constructor);
    }

    interface Factory2<F, A, B, T> {
        Higher<F, Free<F, T>> make(A a, B b, Function1<T, Free<F, T>> constructor);
    }

    static <F, T> Free<F, T> liftF(Factory0<F, T> cons) {
        return free(cons.make(Free::pure));
    }

    static <F, A, T> Free<F, T> liftF(A a, Factory1<F, A, T> cons) {
        return free(cons.make(a, Free::pure));
    }

    static <F, A, B, T> Free<F, T> liftF(A a, B b, Factory2<F, A, B, T> cons) {
        return free(cons.make(a, b, Free::pure));
    }
}
