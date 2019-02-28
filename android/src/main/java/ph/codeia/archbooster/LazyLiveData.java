package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;


public class LazyLiveData<T> extends LiveData<T> {

    public static <T, E extends Io<T> & Runnable>
    LiveData<T> of(E e) {
        return of(e, false);
    }

    public static <T, E extends Io<T> & Runnable>
    LiveData<T> of(E e, boolean shouldPost) {
        return new LazyLiveData<>(e, e, shouldPost);
    }

    private final Io<T> attach;
    private final Runnable detach;
    private final boolean shouldPost;
    private boolean isAttached = false;

    public LazyLiveData(
            Io<T> attach,
            @Nullable Runnable detach,
            boolean shouldPost
    ) {
        this.attach = attach;
        this.detach = detach;
        this.shouldPost = shouldPost;
    }

    public LazyLiveData(Io<T> attach, @Nullable Runnable detach) {
        this(attach, detach, false);
    }

    @Override
    protected void onActive() {
        if (!isAttached) {
            attach.run(shouldPost ? this::postValue : this::setValue);
            isAttached = true;
        }
    }

    @Override
    protected void onInactive() {
        if (isAttached && detach != null) {
            detach.run();
            isAttached = false;
        }
    }
}
