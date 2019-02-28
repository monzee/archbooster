package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

/**
 * Wraps a {@link MutableLiveData} to make it usable with a {@link
 * UseCase.Concurrent concurrent use case object}.
 *
 * @param <T> The type of the observable data
 */
public class LiveRef<T> implements Ref.Event<T>, Observer<T> {
    private final MutableLiveData<T> copy = new MutableLiveData<>();
    private final MutableLiveData<T> store;
    private final boolean strict;
    private Emitter<? super T> receiver;

    /**
     * @param store The backing store
     * @param strict If true, {@link #swap(Object)} calls {@link MutableLiveData#setValue(Object)}
     *               instead of {@link MutableLiveData#postValue(Object)}. This
     *               forces the task to use {@link #deref(Emitter)} in a
     *               background thread in order call {@link #notifyChanged()} or
     *               {@link #swap(Object)}. This is to ensure that the task has
     *               access to the latest state and also to confine any state
     *               mutation to the main thread. Defaults to {@code true} if
     *               left out.
     */
    public LiveRef(MutableLiveData<T> store, boolean strict) {
        this.store = store;
        this.strict = strict;
        copy.observeForever(this);
    }

    public LiveRef(MutableLiveData<T> store) {
        this(store, true);
    }

    @Override
    public void notifyChanged() {
        swap(store.getValue());
    }

    @Override
    public void swap(T t) {
        if (strict) {
            store.setValue(t);
        }
        else {
            store.postValue(t);
        }
    }

    @Override
    public void deref(Emitter<? super T> block) {
        receiver = block;
        copy.postValue(store.getValue());
    }

    @Override
    public void onChanged(T t) {
        if (receiver != null) {
            receiver.emit(t);
            receiver = null;
        }
    }
}
