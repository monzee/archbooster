package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.function.Function1;

/**
 * A procedure that sends many outputs into two channels, possibly at different
 * points in time.
 *
 * @param <State> The type of the primary outputs of the procedure
 * @param <Event> The type of the side effects of the procedure
 */
public interface Operation<State, Event> {
    /**
     * @param states The channel in which the primary outputs are sent
     * @param events The channel in which the side effects are sent
     */
    void run(Emitter<? super State> states, Emitter<? super Event> events);

    /**
     * Executes a procedure to produce outputs, ignoring the side effects.
     *
     * <p> It's not possible to have the opposite (ignore output, receive
     * side effects only) because that method would have the same erasure
     * as this and the compiler wouldn't allow that.
     *
     * @param states The primary output channel
     */
    default void run(Emitter<? super State> states) {
        run(states, Emitter.SINK);
    }

    default Io<State> leftIo() {
        return this::run;
    }

    default Io<Event> rightIo() {
        return events -> run(Emitter.SINK, events);
    }

    default <T> Operation<T, Event>
    leftMap(Function1<? super State, ? extends T> fn) {
        return (ts, events) -> run(state -> ts.emit(fn.apply(state)), events);
    }

    default <T> Operation<State, T>
    rightMap(Function1<? super Event, ? extends T> fn) {
        return (states, ts) -> run(states, event -> ts.emit(fn.apply(event)));
    }

    default <T> Operation<T, Event>
    leftFlatMap(Function1<? super State, Io<? extends T>> fn) {
        return (ts, events) -> run(state -> fn.apply(state).run(ts), events);
    }

    default <T> Operation<State, T>
    rightFlatMap(Function1<? super Event, Io<? extends T>> fn) {
        return (states, ts) -> run(states, event -> fn.apply(event).run(ts));
    }
}