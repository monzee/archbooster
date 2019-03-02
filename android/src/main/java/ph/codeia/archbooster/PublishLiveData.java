package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * A mutable live data that only notifies its observers right after a call to
 * {@link #setValue(Object)} or {@link #postValue(Object)}.
 *
 * <p> If the observer is added and a value is set while the owner is inactive
 * (but not destroyed), the observer will receive the value when the owner
 * becomes active.
 *
 * @param <T> The type of the observable data
 */
public class PublishLiveData<T> extends MutableLiveData<T> {
    private final Map<Observer, Wrapper> wrapperByObserver = new HashMap<>();
    private int activeCycle = 0;
    private boolean needsToSync = false;

    @Override
    protected void onInactive() {
        activeCycle += 1;
        needsToSync = true;
    }

    /**
     * Newly added observers will never receive the current value; otherwise
     * behaves the same as below.
     * <p></p>
     * {@inheritDoc}
     */
    @Override
    public void observe(
            @NonNull LifecycleOwner owner,
            @NonNull Observer<? super T> observer
    ) {
        if (owner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        needsToSync = true;
        super.observe(owner, new Wrapper(owner, observer));
    }

    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        needsToSync = true;
        super.observeForever(new Wrapper(null, observer));
    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        Wrapper wrapper;
        // direct call by client
        if (!(observer instanceof PublishLiveData.Wrapper)) {
            wrapper = wrapperByObserver.remove(observer);
        }
        // virtual call by parent
        else {
            //noinspection unchecked
            wrapper = (Wrapper) observer;
            wrapperByObserver.remove(wrapper.observer);
        }
        if (wrapper != null) {
            super.removeObserver(wrapper);
        }
    }

    @Override
    public void setValue(T value) {
        // possible improvement: maintain a queue of out-of-sync wrappers
        // instead of syncing all of them even when only one of them needs to
        if (needsToSync) {
            for (Wrapper wrapper : wrapperByObserver.values()) {
                wrapper.sync();
            }
            needsToSync = false;
        }
        super.setValue(value);
    }

    private class Wrapper implements Observer<T> {
        final LifecycleOwner owner;
        final Observer<? super T> observer;
        int observerCycle = -1;

        Wrapper(LifecycleOwner owner, Observer<? super T> observer) {
            this.owner = owner;
            this.observer = observer;
            wrapperByObserver.put(observer, this);
        }

        void sync() {
            observerCycle = activeCycle;
        }

        @Override
        public void onChanged(T t) {
            if (observerCycle == activeCycle) {
                observer.onChanged(t);
            }
        }
    }
}
