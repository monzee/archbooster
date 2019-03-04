package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * A mutable live data whose observers never receive data older than them.
 *
 * <p> The name comes from RxJava's {@code PublishSubject}. {@link MutableLiveData}
 * normally behaves like {@code BehaviorSubject} from RxJava (minus the error
 * and completion semantics) where newly registered observers are called with
 * the last value that was posted or set. This is bad for things that should
 * only be dispatched once like toasts, logs or navigation events. For more
 * info, search for {@code SingleLiveEvent} on google. This problem has been
 * observed over and over even by googlers themselves, yet AAC still doesn't
 * provide a built-in solution for this.
 *
 * <p> Like {@code MutableLiveData}, this class keeps a reference to the last
 * value set/posted, but observers which are added afterwards are not invoked
 * with it. They will only be notified on the next call to {@link #postValue(Object)}
 * or {@link #setValue(Object)}.
 *
 * <p> If the observer is added and a value is set while the owner is inactive
 * (but not destroyed), the observer will receive the value when the owner
 * becomes active.
 *
 * <p> This implementation is better than the aforementioned {@code SingleLiveEvent}
 * because this allows multiple observers in the same cycle to receive the
 * posted value. If you've seen {@code EventWrapper} in a google developer blog,
 * forget about it. The thing it touts as an advantage (handling vs. peeking)
 * is not. You shouldn't have to make a distinction between those two. It just
 * forces you to arbitrarily assign one observer as _the_ handler and relegate
 * others to an "on-looker" status and it also adds noise to the types and the
 * observer body.
 *
 * @param <T> The type of the observable data
 */
public class PublishLiveData<T> extends MutableLiveData<T> {
    private final Map<Observer, Wrapper> wrapperByObserver = new HashMap<>();
    private final Set<Wrapper> unprimed = new HashSet<>();

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
        Wrapper wrapper = new Wrapper(owner, observer);
        unprimed.add(wrapper);
        super.observe(owner, wrapper);
    }

    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        Wrapper wrapper = new Wrapper(null, observer);
        unprimed.add(wrapper);
        super.observeForever(wrapper);
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
            wrapperByObserver.remove(wrapper.delegate);
        }
        if (wrapper != null) {
            unprimed.remove(wrapper);
            super.removeObserver(wrapper);
        }
    }

    @Override
    public void setValue(T value) {
        if (!unprimed.isEmpty()) {
            for (Wrapper wrapper : unprimed) {
                wrapper.prime();
            }
            unprimed.clear();
        }
        super.setValue(value);
    }

    private class Wrapper implements Observer<T> {
        final LifecycleOwner owner;
        final Observer<? super T> delegate;
        boolean primed = false;

        Wrapper(LifecycleOwner owner, Observer<? super T> delegate) {
            this.owner = owner;
            this.delegate = delegate;
            wrapperByObserver.put(delegate, this);
        }

        void prime() {
            primed = true;
        }

        @Override
        public void onChanged(T t) {
            if (primed) {
                delegate.onChanged(t);
            }
        }
    }
}
