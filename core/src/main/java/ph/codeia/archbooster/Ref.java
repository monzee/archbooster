package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

public interface Ref<T> extends Emitter<Ref.Event<T>> {

    interface Event<T> extends Emitter<Ref<T>> {
        /**
         * @see Ref#notifyChanged()
         */
        void notifyChanged();

        /**
         * @see Ref#swap(Object)
         */
        void swap(T t);

        /**
         * @see Ref#deref(Emitter)
         */
        void deref(Emitter<? super T> block);

        @Override
        default void emit(Ref<T> ref) {
            ref.emit(this);
        }
    }

    /**
     * Calls {@link #swap(Object)} with the same wrapped value. Usually done
     * if {@code T} is a mutable type.
     *
     * @param <T> The type of the value
     * @return a command object
     */
    static <T> Ref<T> notifyChanged() {
        return Event::notifyChanged;
    }

    /**
     * Replaces the value being referred to. This must be done in the main
     * thread only.
     *
     * @param t The new value
     * @param <T> The type of the value
     * @return a command object
     */
    static <T> Ref<T> swap(T t) {
        return e -> e.swap(t);
    }

    /**
     * Switches to the main thread and calls a function with the current value
     * being referred to.
     *
     * @param block The function to run in the main thread
     * @param <T> The type of the value
     * @return a command object
     */
    static <T> Ref<T> deref(Emitter<? super T> block) {
        return e -> e.deref(block);
    }
}
