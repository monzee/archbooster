package ph.codeia.archbooster.experiments.typeclass;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.experiments.Higher;
import ph.codeia.archbooster.function.Function1;
import ph.codeia.archbooster.function.Function2;


public interface Applicative<F> extends Functor<F> {
    <T> Higher<F, T> unit(T t);

    default <T, U> Higher<F, U> ap(
            Higher<F, T> ft,
            Higher<F, Function1<? super T, ? extends U>> ff
    ) {
        return map2(ff, ft, Function1::apply);
    }

    default <T, U, V> Higher<F, V> map2(
            Higher<F, T> ft,
            Higher<F, U> fu,
            Function2<? super T, ? super U, ? extends V> f
    ) {
        return ap(fu, map(ft, t -> u -> f.apply(t, u)));
    }

    @Override
    default <T, U> Higher<F, U> map(
            Higher<F, T> source,
            Function1<? super T, ? extends U> f
    ) {
        return ap(source, unit(f));
    }
}