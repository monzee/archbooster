package ph.codeia.archbooster.experiments.transform;

/*
 * This file is a part of the arch-booster project.
 */

import java.util.concurrent.Executor;

import ph.codeia.archbooster.Emitter;
import ph.codeia.archbooster.Operation;

public class Execute<T, U> implements Operation<T, U> {

    public static <T, U> Operations.Composer<T, U>
    on(Executor executor) {
        return source -> new Execute<>(source, executor);
    }

    private final Operation<T, U> source;
    private final Executor executor;

    public Execute(Operation<T, U> source, Executor executor) {
        this.source = source;
        this.executor = executor;
    }

    @Override
    public void run(Emitter<? super T> states, Emitter<? super U> events) {
        executor.execute(() -> source.run(states, events));
    }
}
