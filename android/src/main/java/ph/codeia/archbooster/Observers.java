package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.core.util.Supplier;
import androidx.lifecycle.Observer;
import ph.codeia.archbooster.function.Function1;
import ph.codeia.archbooster.function.Predicate1;

public final class Observers {

    public interface Binary<A, B> extends Observer<Pair<A, B>> {
        void onChanged(A a, B b);

        @Override
        default void onChanged(Pair<A, B> value) {
            onChanged(value.first, value.second);
        }
    }

    public interface Some<T> {
        void onChanged(@NonNull T t);
    }

    public static <T> Observer<T> forSome(Some<? super T> delegate) {
        return t -> {
            if (t != null) {
                delegate.onChanged(t);
            }
        };
    }

    public static <S, T> Observer<T> pipe(
            Function1<? super T, ? extends S> transform,
            Observer<? super S> delegate
    ) {
        return t -> delegate.onChanged(transform.apply(t));
    }

    public static <T> Runnable pipe(
            Supplier<? extends T> supplier,
            Observer<? super T> delegate
    ) {
        return () -> delegate.onChanged(supplier.get());
    }

    public static <T> Runnable just(
            T value,
            Observer<? super T> delegate
    ) {
        return () -> delegate.onChanged(value);
    }

    public static <T> Observer<T> where(
            Predicate1<? super T> predicate,
            Observer<? super T> delegate
    ) {
        return t -> {
            if (predicate.test(t)) {
                delegate.onChanged(t);
            }
        };
    }

    public static <T> Observer<T> where(
            Predicate1<? super T> predicate,
            Runnable delegate
    ) {
        return where(predicate, o -> delegate.run());
    }

    public static <T> Observer<T> throttle(
            long periodMillis,
            Observer<? super T> delegate
    ) {
        if (periodMillis <= 0) {
            throw new IllegalArgumentException("Non-positive period");
        }
        return new ThrottledObserver<>(delegate, periodMillis);
    }

    private static class ThrottledObserver<T> implements Observer<T> {
        final Observer<? super T> delegate;
        final long period;
        long lastChanged = 0;

        ThrottledObserver(Observer<? super T> delegate, long period) {
            this.delegate = delegate;
            this.period = period;
        }

        @Override
        public void onChanged(T t) {
            long now = System.currentTimeMillis();
            if (now - lastChanged > period) {
                lastChanged = now;
                delegate.onChanged(t);
            }
        }
    }

    private Observers() {}
}
