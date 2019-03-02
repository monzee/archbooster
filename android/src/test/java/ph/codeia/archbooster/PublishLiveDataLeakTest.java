package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import org.junit.Rule;
import org.junit.Test;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;

import static org.junit.Assert.*;

public class PublishLiveDataLeakTest {
    @Rule
    public final InstantTaskExecutorRule rule = new InstantTaskExecutorRule();
    private final PublishLiveData<Object> sender = new PublishLiveData<>();
    private final Owner owner = new Owner();

    private static Reference<?> gc(ReferenceQueue<?> q) throws InterruptedException {
        assertNull(q.poll());
        Reference<?> ref = null;
        for (; ref == null; ref = q.remove(100)) {
            System.gc();
        }
        return ref;
    }

    @Test(timeout = 1000)
    public void gc_works() throws InterruptedException {
        Object p = new Object();
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        PhantomReference<Object> ref = new PhantomReference<>(p, q);
        p = null;
        assertNotNull(gc(q));
    }

    @Test(timeout = 1000)
    public void drops_the_listener_when_removed() throws InterruptedException {
        Observer<Object> obs = noop();
        ReferenceQueue<Observer<Object>> q = new ReferenceQueue<>();
        PhantomReference<Observer<Object>> ref = new PhantomReference<>(obs, q);

        sender.observe(owner, obs);
        assertTrue(sender.hasObservers());

        sender.removeObserver(obs);
        obs = null;
        assertNotNull(gc(q));
    }

    @Test(timeout = 1000)
    public void drops_permanent_listener_when_removed() throws InterruptedException {
        Observer<Object> obs = noop();
        ReferenceQueue<Observer<Object>> q = new ReferenceQueue<>();
        PhantomReference<Observer<Object>> ref = new PhantomReference<>(obs, q);

        sender.observeForever(obs);
        assertTrue(sender.hasObservers());

        sender.removeObserver(obs);
        obs = null;
        assertNotNull(gc(q));
    }

    @Test(timeout = 1000)
    public void drops_the_listener_when_all_observers_of_its_owner_are_removed() throws InterruptedException {
        Observer<Object> obs = noop();
        ReferenceQueue<Observer<Object>> q = new ReferenceQueue<>();
        PhantomReference<Observer<Object>> ref = new PhantomReference<>(obs, q);

        sender.observe(owner, obs);

        sender.removeObservers(owner);
        obs = null;
        assertNotNull(gc(q));
    }

    @Test(timeout = 1000)
    public void drops_the_listener_when_its_owner_is_destroyed() throws InterruptedException {
        Observer<Object> obs = noop();
        ReferenceQueue<Observer<Object>> q = new ReferenceQueue<>();
        PhantomReference<Observer<Object>> ref = new PhantomReference<>(obs, q);

        owner.life.markState(Lifecycle.State.STARTED);
        sender.observe(owner, obs);

        owner.life.markState(Lifecycle.State.DESTROYED);
        obs = null;
        assertNotNull(gc(q));
    }

    @Test(timeout = 1000)
    public void drops_the_owner_when_it_is_destroyed() throws InterruptedException {
        Owner o = new Owner();
        ReferenceQueue<LifecycleOwner> q = new ReferenceQueue<>();
        PhantomReference<LifecycleOwner> ref = new PhantomReference<>(o, q);

        o.life.markState(Lifecycle.State.STARTED);
        sender.observe(o, noop());

        o.life.markState(Lifecycle.State.DESTROYED);
        o = null;
        assertNotNull(gc(q));
    }

    private static <T> Observer<T> noop() {
        // can't use a lambda because that compiles into a static singleton
        // which won't be GC'd throughout the process' life
        return new Observer<T>() {
            @Override
            public void onChanged(T t) {
            }
        };
    }

    private static class Owner implements LifecycleOwner {
        final LifecycleRegistry life = new LifecycleRegistry(this);

        @NonNull
        @Override
        public Lifecycle getLifecycle() {
            return life;
        }
    }
}
