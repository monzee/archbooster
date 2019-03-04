package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import static org.junit.Assert.*;

public class PublishLiveDataTest implements LifecycleOwner {
    @Rule
    public final InstantTaskExecutorRule rule = new InstantTaskExecutorRule();
    private final LifecycleRegistry life = new LifecycleRegistry(this);

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return life;
    }

    @Test
    public void can_receive_set_items() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> receiver = new AtomicReference<>();
        sender.observeForever(receiver::set);

        Object o = new Object();
        sender.setValue(o);

        assertEquals(o, receiver.get());
    }

    @Test
    public void can_receive_posted_items() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> receiver = new AtomicReference<>();
        sender.observeForever(receiver::set);

        Object o = new Object();
        sender.postValue(o);

        assertEquals(o, receiver.get());
    }

    @Test
    public void newly_added_observers_do_not_receive_the_current_item() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> receiver = new AtomicReference<>();

        sender.setValue(new Object());
        sender.observeForever(receiver::set);

        assertNull(receiver.get());
    }

    @Test
    public void obs_does_not_receive_value_when_owner_is_inactive() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> receiver = new AtomicReference<>();
        sender.observe(this, receiver::set);

        life.markState(Lifecycle.State.CREATED);
        sender.setValue(new Object());

        assertNull(receiver.get());
    }

    @Test
    public void obs_receives_the_value_posted_while_inactive_when_owner_becomes_active() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> receiver = new AtomicReference<>();
        sender.observe(this, receiver::set);
        Object o = new Object();

        life.markState(Lifecycle.State.CREATED);
        sender.setValue(o);
        assertNull(receiver.get());

        life.markState(Lifecycle.State.STARTED);
        assertEquals(o, receiver.get());
    }

    @Test
    public void can_send_object_to_multiple_receivers() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> r1 = new AtomicReference<>();
        AtomicReference<Object> r2 = new AtomicReference<>();
        AtomicReference<Object> r3 = new AtomicReference<>();
        sender.observe(this, r1::set);
        sender.observe(this, r2::set);
        sender.observeForever(r3::set);
        Object o = new Object();

        life.markState(Lifecycle.State.STARTED);
        sender.setValue(o);

        assertEquals(o, r1.get());
        assertEquals(o, r2.get());
        assertEquals(o, r3.get());
    }

    @Test
    public void can_remove_observer() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> receiver = new AtomicReference<>();
        Observer<Object> obs = receiver::set;
        sender.observe(this, obs);

        life.markState(Lifecycle.State.STARTED);
        assertTrue(sender.hasActiveObservers());
        sender.removeObserver(obs);

        assertFalse(sender.hasActiveObservers());
        assertFalse(sender.hasObservers());
    }

    @Test
    public void can_remove_observers() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> r1 = new AtomicReference<>();
        AtomicReference<Object> r2 = new AtomicReference<>();
        sender.observe(this, r1::set);
        sender.observe(this, r2::set);

        life.markState(Lifecycle.State.STARTED);
        assertTrue(sender.hasActiveObservers());
        sender.removeObservers(this);

        assertFalse(sender.hasActiveObservers());
        assertFalse(sender.hasObservers());
    }

    @Test
    public void can_remove_permanent_observer() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> receiver = new AtomicReference<>();
        Observer<Object> obs = receiver::set;
        sender.observeForever(obs);

        assertTrue(sender.hasActiveObservers());
        sender.removeObserver(obs);

        assertFalse(sender.hasActiveObservers());
        assertFalse(sender.hasObservers());
    }

    @Test
    public void does_not_observe_destroyed_owner() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> receiver = new AtomicReference<>();

        life.markState(Lifecycle.State.DESTROYED);
        sender.observe(this, receiver::set);

        assertFalse(sender.hasObservers());
    }

    @Test
    public void removes_observers_when_owner_is_destroyed() {
        MutableLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> r1 = new AtomicReference<>();
        AtomicReference<Object> r2 = new AtomicReference<>();
        sender.observe(this, r1::set);
        sender.observe(this, r2::set);

        life.markState(Lifecycle.State.CREATED);
        assertTrue(sender.hasObservers());
        life.markState(Lifecycle.State.DESTROYED);

        assertFalse(sender.hasObservers());
    }

    @Test
    public void obs_does_not_receive_the_value_again_on_the_next_active_cycle() {
        PublishLiveData<Object> sender = new PublishLiveData<>();
        AtomicReference<Object> receiver = new AtomicReference<>();
        Object o = new Object();

        life.markState(Lifecycle.State.STARTED);
        sender.observe(this, receiver::set);
        sender.setValue(o);
        assertEquals(o, receiver.get());

        life.markState(Lifecycle.State.CREATED);
        receiver.set(null);
        life.markState(Lifecycle.State.STARTED);
        assertNull(receiver.get());
    }
}
