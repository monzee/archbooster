package ph.codeia.archbooster.function;

/*
 * This file is a part of the arch-booster project.
 */

/**
 * A function from {@code D} to {@code R}.
 *
 * @param <D> The domain of the function
 * @param <R> The range of the function
 */
public interface Function1<D, R> {
    /**
     * @param d The value to transform
     * @return the transformed value
     */
    R apply(D d);
}