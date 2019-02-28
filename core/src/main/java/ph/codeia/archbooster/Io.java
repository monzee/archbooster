package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import ph.codeia.archbooster.function.Function1;

public interface Io<T> {
    void run(Emitter<? super T> consumer);

    default <U> Io<U> map(Function1<T, U> transform) {
        return us -> run(t -> us.emit(transform.apply(t)));
    }

    default <U> Io<U> flatMap(Function1<T, Io<U>> transform) {
        return us -> run(t -> transform.apply(t).run(us));
    }

    default T unwrap() throws InterruptedException {
        return (new Emitter<T>() {
            T t;
            CountDownLatch latch = new CountDownLatch(1);

            {
                run(this);
                latch.await();
            }

            @Override
            public void emit(T value) {
                t = value;
                latch.countDown();
            }
        }).t;
    }

    default void unwrapInto(int count, Collection<? super T> values)
    throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(count);
        run(t -> {
            values.add(t);
            latch.countDown();
        });
        latch.await();
    }
}