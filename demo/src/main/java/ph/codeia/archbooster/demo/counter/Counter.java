package ph.codeia.archbooster.demo.counter;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.UseCase;
import ph.codeia.archbooster.Operation;

/**
 * The simplest interactive demo, using only a primitive for state and no
 * side effects.
 *
 * <p> {@code INCREMENT} takes a number and emits the next number.
 * {@code DECREMENT} takes a number and emits the previous number.
 */
public enum Counter implements UseCase<Integer, Void> {
    INCREMENT {
        @Override
        public Operation<Integer, Void> from(Integer state) {
            return (states, events) -> states.emit(state + 1);
        }
    },

    DECREMENT {
        @Override
        public Operation<Integer, Void> from(Integer state) {
            return (states, events) -> states.emit(state - 1);
        }
    }
}

