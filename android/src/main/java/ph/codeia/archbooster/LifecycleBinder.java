package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import androidx.core.util.Pair;
import androidx.core.util.Supplier;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import ph.codeia.archbooster.function.Function1;
import ph.codeia.archbooster.function.Function2;


public class LifecycleBinder {
    private final LifecycleOwner owner;

    public LifecycleBinder(LifecycleOwner owner) {
        this.owner = owner;
    }

    public void bind(LiveData<?> source, Runnable destination) {
        source.observe(owner, o -> destination.run());
    }

    public void bindOnce(LiveData<?> source, Runnable destination) {
        bindOnce(source, o -> destination.run());
    }

    public <T> void bind(
            LiveData<? extends T> source,
            Observer<? super T> destination
    ) {
        source.observe(owner, destination);
    }

    public <T> void bindOnce(
            LiveData<? extends T> source,
            Observer<? super T> destination
    ) {
        source.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                destination.onChanged(t);
                source.removeObserver(this);
            }
        });
    }

    public <A, B> void bind(
            LiveData<? extends Pair<A, B>> source,
            Observers.Binary<A, B> destination
    ) {
        source.observe(owner, destination);
    }

    public <A, B> void bindOnce(
            LiveData<? extends Pair<A, B>> source,
            Observers.Binary<A, B> destination
    ) {
        bindOnce(source, (Observer<Pair<A, B>>) destination);
    }

    public <A, B> LiveData<Pair<A, B>> zip(LiveData<A> left, LiveData<B> right) {
        return zipWith(left, right, Pair::create);
    }

    public <A, B, T> LiveData<T> zipWith(
            LiveData<? extends A> left,
            LiveData<? extends B> right,
            Function2<? super A, ? super B, ? extends T> transform
    ) {
        return LazyLiveData.of(new Zipper<>(owner, left, right, transform, true));
    }

    public <A, B> LiveData<Pair<A, B>> zipFill(LiveData<A> left, LiveData<B> right) {
        return zipFillWith(left, right, Pair::create);
    }

    public <A, B, T> LiveData<T> zipFillWith(
            LiveData<? extends A> left,
            LiveData<? extends B> right,
            Function2<? super A, ? super B, ? extends T> transform
    ) {
        return LazyLiveData.of(new Zipper<>(owner, left, right, transform, false));
    }

    private static class Zipper<A, B, T> implements Io<T>, Runnable {
        final LifecycleOwner owner;
        final LiveData<? extends A> left;
        final LiveData<? extends B> right;
        final Function2<? super A, ? super B, ? extends T> transform;
        final boolean strictPairing;
        final boolean[] hasValue = {false, false};
        Observer<A> observerA;
        Observer<B> observerB;
        A valueA;
        B valueB;

        Zipper(
                LifecycleOwner owner,
                LiveData<? extends A> left,
                LiveData<? extends B> right,
                Function2<? super A, ? super B, ? extends T> transform,
                boolean strictPairing
        ) {
            this.owner = owner;
            this.left = left;
            this.right = right;
            this.transform = transform;
            this.strictPairing = strictPairing;
        }

        @Override
        public void run(Emitter<? super T> on) {
            observerA = a -> {
                valueA = a;
                if (hasValue[1]) {
                    on.emit(transform.apply(a, valueB));
                    if (strictPairing) {
                        hasValue[1] = false;
                        hasValue[0] = false;
                    }
                    else {
                        hasValue[0] = true;
                    }
                }
                else {
                    hasValue[0] = true;
                }
            };
            observerB = b -> {
                valueB = b;
                if (hasValue[0]) {
                    on.emit(transform.apply(valueA, b));
                    if (strictPairing) {
                        hasValue[0] = false;
                        hasValue[1] = false;
                    }
                    else {
                        hasValue[1] = true;
                    }
                }
                else {
                    hasValue[1] = true;
                }
            };
            left.observe(owner, observerA);
            right.observe(owner, observerB);
        }

        @Override
        public void run() {
            left.removeObserver(observerA);
            right.removeObserver(observerB);
        }
    }

}
