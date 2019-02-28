package ph.codeia.archbooster.experiments;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.experiments.typeclass.Monad;
import ph.codeia.archbooster.experiments.typeclass.MonadPlus;
import ph.codeia.archbooster.function.Consumer1;
import ph.codeia.archbooster.function.Consumer2;
import ph.codeia.archbooster.function.Function0;
import ph.codeia.archbooster.function.Function1;
import ph.codeia.archbooster.function.Function2;
import ph.codeia.archbooster.function.Predicate1;


public final class Comprehension<M, T> {
    private final Monad<M> monad;
    private final Higher<M, T> instance;

    public Comprehension(Monad<M> monad, Higher<M, T> instance) {
        this.monad = monad;
        this.instance = instance;
    }

    public Higher<M, T> yield() {
        return instance;
    }

    public <I extends Higher<M, T>> I yield(Function1<Higher<M, T>, I> narrow) {
        return narrow.apply(instance);
    }

    public <U> Comprehension<M, U> lift(U value) {
        return then(monad.unit(value));
    }

    public <S, U> Comprehension<M, U> liftFold(
            S value,
            Function2<? super T, ? super S, ? extends U> accumulate
    ) {
        return mapFold(delay(value), accumulate);
    }

    public <S> Comprehension<M, T> liftFold(
            S value,
            Consumer2<? super T, ? super S> accumulate
    ) {
        return thenFold(monad.unit(value), accumulate);
    }

    public <U> Comprehension<M, U> map(Function0<? extends U> next) {
        return map(o -> next.get());
    }

    public <U> Comprehension<M, U> map(
            Function1<? super T, ? extends U> next
    ) {
        return new Comprehension<>(monad, monad.map(instance, next));
    }

    public <S, U> Comprehension<M, U> mapFold(
            Function1<? super T, ? extends S> next,
            Function2<? super T, ? super S, ? extends U> accumulate
    ) {
        return flatMapFold(t -> monad.unit(next.apply(t)), accumulate);
    }

    public <S> Comprehension<M, T> mapFold(
            Function1<? super T, ? extends S> next,
            Consumer2<? super T, ? super S> accumulate
    ) {
        return flatMapFold(t -> monad.unit(next.apply(t)), accumulate);
    }

    public <U> Comprehension<M, U> then(Higher<M, U> next) {
        return new Comprehension<>(monad, monad.bind(instance, delay(next)));
    }

    public <S, U> Comprehension<M, U> thenFold(
            Higher<M, S> next,
            Function2<? super T, ? super S, ? extends U> accumulate
    ) {
        return new Comprehension<>(monad, monad.bind(
                instance,
                t -> monad.map(next, s -> accumulate.apply(t, s))
        ));
    }

    public <S> Comprehension<M, T> thenFold(
            Higher<M, S> next,
            Consumer2<? super T, ? super S> accumulate
    ) {
        return new Comprehension<>(monad, monad.bind(
                instance,
                t -> monad.map(next, s -> {
                    accumulate.accept(t, s);
                    return t;
                })
        ));
    }

    public <U> Comprehension<M, U> flatMap(
            Function1<? super T, ? extends Higher<M, U>> next
    ) {
        return new Comprehension<>(monad, monad.bind(instance, next));
    }

    public <U> Comprehension<M, U> flatMap(
            Function0<? extends Higher<M, U>> next
    ) {
        return flatMap(o -> next.get());
    }

    public <S, U> Comprehension<M, U> flatMapFold(
            Function1<? super T, Higher<M, S>> next,
            Function2<? super T, ? super S, ? extends U> accumulate
    ) {
        return new Comprehension<>(monad, monad.bind(
                instance,
                t -> monad.map(next.apply(t), s -> accumulate.apply(t, s))
        ));
    }

    public <S> Comprehension<M, T> flatMapFold(
            Function1<? super T, Higher<M, S>> next,
            Consumer2<? super T, ? super S> accumulate
    ) {
        return new Comprehension<>(monad, monad.bind(
                instance,
                t -> monad.map(next.apply(t), s -> {
                    accumulate.accept(t, s);
                    return t;
                })
        ));
    }

    public Comprehension<M, T> also(
            Function1<? super T, ? extends Higher<M, ?>> next
    ) {
        return new Comprehension<>(
                monad,
                monad.bind(instance, t -> monad.map(next.apply(t), delay(t)))
        );
    }

    public Comprehension<M, T> inspect(Consumer1<? super T> block) {
        return new Comprehension<>(monad, monad.map(instance, t -> {
            block.accept(t);
            return t;
        }));
    }

    public Comprehension<M, T> concat(Higher<M, T> that) {
        try {
            MonadPlus<M> mp = (MonadPlus<M>) monad;
            return new Comprehension<>(monad, mp.plus(instance, that));
        }
        catch (ClassCastException e) {
            throw new UnsupportedOperationException("Kind does not implement MonadPlus");
        }
    }

    public Comprehension<M, T> filter(Predicate1<T> pred) {
        try {
            MonadPlus<M> mp = (MonadPlus<M>) monad;
            return flatMap(t -> pred.test(t) ? mp.unit(t) : mp.zero());
        }
        catch (ClassCastException e) {
            throw new UnsupportedOperationException("Kind does not implement MonadPlus");
        }
    }

    private static <T> Function0<T> thunk(T next) {
        return () -> next;
    }

    private static <S, T> Function1<S, T> delay(T t) {
        return o -> t;
    }
}
