package ph.codeia.archbooster.demo.fizzbuzz;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.UseCase;
import ph.codeia.archbooster.Operation;

/**
 * A slightly different take on the classic interview problem.
 *
 * <p> This is a small step up from the counter demo. In addition to the
 * primitive state, these actions also produce side effects in the form of
 * strings.
 *
 * <p> Like in counter, {@code INCREMENT} takes a number and emits the next
 * number while {@code DECREMENT} emits the previous number. When the emitted
 * number is divisible by 3, the string {@code Fizz} is emitted. When the
 * number is divisible by 5, {@code Buzz} is emitted. When it is divisible by
 * both 3 and 5, the string {@code FizzBuzz} is emitted.
 */
public enum FizzBuzz implements UseCase<Integer, String> {
    INCREMENT {
        @Override
        public Operation<Integer, String> from(Integer state) {
            return super.from(state + 1);
        }
    },

    DECREMENT {
        @Override
        public Operation<Integer, String> from(Integer state) {
            return super.from(state - 1);
        }
    };

    @Override
    public Operation<Integer, String> from(Integer state) {
        return (states, events) -> {
            if (state % 15 == 0) {
                events.emit("FizzBuzz");
            }
            else if (state % 3 == 0) {
                events.emit("Fizz");
            }
            else if (state % 5 == 0) {
                events.emit("Buzz");
            }
            states.emit(state);
        };
    }
}

