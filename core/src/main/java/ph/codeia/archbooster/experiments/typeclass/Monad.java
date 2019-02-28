package ph.codeia.archbooster.experiments.typeclass;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.experiments.Comprehension;
import ph.codeia.archbooster.experiments.Higher;
import ph.codeia.archbooster.function.Function1;
import ph.codeia.archbooster.function.Function2;


public interface Monad<M> extends Applicative<M> {
    <T, U> Higher<M, U> bind(
            Higher<M, T> source,
            Function1<? super T, ? extends Higher<M, U>> next
    );

    default <T> Comprehension<M, T> from(Higher<M, T> instance) {
        return new Comprehension<>(this, instance);
    }

    default <T> Comprehension<M, T> from(T value) {
        return new Comprehension<>(this, unit(value));
    }

    @Override
    default <T, U> Higher<M, U> map(
            Higher<M, T> source,
            Function1<? super T, ? extends U> f
    ) {
        return bind(source, t -> unit(f.apply(t)));
    }

    @Override
    default <T, U> Higher<M, U> ap(
            Higher<M, T> ft,
            Higher<M, Function1<? super T, ? extends U>> ff
    ) {
        return bind(ff, f -> bind(ft, t -> unit(f.apply(t))));
    }

    @Override
    default <T, U, V> Higher<M, V> map2(
            Higher<M, T> ft,
            Higher<M, U> fu,
            Function2<? super T, ? super U, ? extends V> f
    ) {
        return bind(ft, t -> bind(fu, u -> unit(f.apply(t, u))));
    }
}