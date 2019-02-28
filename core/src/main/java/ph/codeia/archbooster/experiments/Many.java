package ph.codeia.archbooster.experiments;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.experiments.typeclass.MonadPlus;
import ph.codeia.archbooster.function.Consumer1;
import ph.codeia.archbooster.function.Function1;

public interface Many<T> extends Higher<Many, T> {

    interface Case<T> {
        void cons(T head, Many<T> tail);
        void nil();
    }

    interface Accumulate<T> {
        void next(T t);
        void done();
    }

    void forEach(Case<T> c);

    default void forEach(Accumulate<T> a) {
        forEach(trampoline(a));
    }

    default void forEach(Consumer1<T> c) {
        forEach(new Accumulate<T>() {
            @Override
            public void next(T t) {
                c.accept(t);
            }

            @Override
            public void done() {
            }
        });
    }

    static <T> Many<T> of(Higher<Many, T> hk) {
        return (Many<T>) hk;
    }

    @SafeVarargs
    static <T> Many<T> create(T... ts) {
        Many<T> list = nil();
        for (int i = ts.length - 1; i >= 0; i--) {
            list = cons(ts[i], list);
        }
        return list;
    }

    static <T> Many<T> cons(T head, Many<T> tail) {
        return c -> c.cons(head, tail);
    }

    static <T> Many<T> nil() {
        return Case::nil;
    }

    static <T> Case<T> trampoline(Accumulate<T> delegate) {
        return new Case<T>() {
            @Override
            public void cons(T head, Many<T> tail) {
                Trampoline<T> iter = new Trampoline<>();
                iter.value = head;
                iter.next = tail;
                Many<T> nil = Case::nil;
                while (iter.next != null) {
                    delegate.next(iter.value);
                    iter.next.forEach(iter);
                }
                delegate.done();
            }

            @Override
            public void nil() {
                delegate.done();
            }
        };
    }

    Kind KIND = new Kind();

    final class Kind implements MonadPlus<Many> {
        private Kind() {}

        @Override
        public <T> Higher<Many, T> zero() {
            return nil();
        }

        @Override
        public <T> Higher<Many, T> plus(
                Higher<Many, T> left,
                Higher<Many, T> right
        ) {
            return (Many<T>) c -> of(left).forEach(new Case<T>() {
                @Override
                public void cons(T head, Many<T> tail) {
                    c.cons(head, of(plus(tail, right)));
                }

                @Override
                public void nil() {
                    of(right).forEach(c);
                }
            });
        }

        @Override
        public <T, U> Higher<Many, U> bind(
                Higher<Many, T> source,
                Function1<? super T, ? extends Higher<Many, U>> next
        ) {
            return (Many<U>) c -> of(source).forEach(new Case<T>() {
                @Override
                public void cons(T head, Many<T> tail) {
                    of(plus(next.apply(head), bind(tail, next))).forEach(c);
                }

                @Override
                public void nil() {
                    c.nil();
                }
            });
        }

        @Override
        public <T> Higher<Many, T> unit(T t) {
            return Many.create(t);
        }

        @Override
        public <T, U> Higher<Many, U> map(
                Higher<Many, T> source,
                Function1<? super T, ? extends U> f
        ) {
            return (Many<U>) c -> of(source).forEach(new Case<T>() {
                @Override
                public void cons(T head, Many<T> tail) {
                    c.cons(f.apply(head), of(map(tail, f)));
                }

                @Override
                public void nil() {
                    c.nil();
                }
            });
        }
    }

    final class Trampoline<T> implements Many.Case<T> {
        private Trampoline() {}

        T value;
        Many<T> next;

        @Override
        public void cons(T head, Many<T> tail) {
            value = head;
            next = tail;
        }

        @Override
        public void nil() {
            next = null;
        }
    }
}
