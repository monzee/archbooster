package ph.codeia.archbooster.experiments;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.experiments.typeclass.Monad;
import ph.codeia.archbooster.function.Function1;

public final class Id<T> implements Higher<Id, T> {

    public static <T> Id<T> of(Higher<Id, T> hk) {
        return (Id<T>) hk;
    }

    public static final Kind KIND = new Kind();

    public static final class Kind implements Monad<Id> {
        private Kind() {}

        @Override
        public <T, U> Higher<Id, U> bind(
                Higher<Id, T> source,
                Function1<? super T, ? extends Higher<Id, U>> next
        ) {
            return next.apply(of(source).get());
        }

        @Override
        public <T> Higher<Id, T> unit(T t) {
            return new Id<>(t);
        }
    }

    private final T t;

    public Id(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }
}
