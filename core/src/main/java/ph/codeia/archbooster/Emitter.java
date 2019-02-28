package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

/**
 * A {@code Consumer&lt;T&gt;} interface with a more semantic name.
 *
 * @param <T> The type of the values accepted
 */
public interface Emitter<T> {
    /**
     * A static emitter that does nothing values sent to it. Useful for testing.
     */
    Emitter<Object> SINK = value -> {};

    /**
     * @param value The value to send to the other side
     */
    void emit(T value);
}