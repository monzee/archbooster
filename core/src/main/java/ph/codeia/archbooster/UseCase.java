package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */


/**
 * Produces a procedure from some starting state.
 *
 * @param <State> The type of the primary outputs of the use case
 * @param <Event> The type of the side effects of the use case
 */
public interface UseCase<State, Event> {
    /**
     * @param state The current state which may be used for making
     *              decisions in what outputs to emit during the operation
     * @return a procedure that may emit multiple outputs
     */
    Operation<State, Event> from(State state);

    interface Concurrent<State, Event> {
        Operation<Ref<State>, Event> from(State state);
    }
}