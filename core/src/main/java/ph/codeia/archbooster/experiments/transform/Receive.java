package ph.codeia.archbooster.experiments.transform;

/*
 * This file is a part of the arch-booster project.
 */

import java.util.concurrent.Executor;

import ph.codeia.archbooster.Emitter;
import ph.codeia.archbooster.Operation;

public class Receive<T, U> implements Operation<T, U> {

    public static <T, U> Operations.Composer<T, U>
    on(Executor executor) {
        return source -> new Receive<>(source, executor);
    }

    private final Operation<T, U> source;
    private final Executor executor;

    public Receive(Operation<T, U> source, Executor executor) {
        this.source = source;
        this.executor = executor;
    }

    @Override
    public void run(Emitter<? super T> states, Emitter<? super U> events) {
        source.run(
                new Receiver<>(states, executor),
                new Receiver<>(events, executor)
        );
    }

    private static class Receiver<T> implements Emitter<T> {
        private final Emitter<? super T> delegate;
        private final Executor executor;

        private Receiver(Emitter<? super T> delegate, Executor executor) {
            this.delegate = delegate;
            this.executor = executor;
        }

        @Override
        public void emit(T value) {
            executor.execute(() -> delegate.emit(value));
        }
    }
}
