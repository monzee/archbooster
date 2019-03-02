package ph.codeia.archbooster.demo;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.experiments.Many;
import ph.codeia.archbooster.experiments.typeclass.MonadPlus;

public class ListMonad {
    private static class Triple {
        final int x, y, z;
        Triple(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static void main(String[] args) {
        MonadPlus<Many> m = Many.KIND;
        m.from(Range.from(1))
                .flatMap(z -> m.from(Range.between(2, z))
                        .flatMap(x -> m.from(Range.between(x + 1, z))
                                .filter(y -> x*x + y*y == z*z)
                                .map(y -> new Triple(x, y, z))
                                .yield())
                        .yield())
                .yield(Many::of)
                .forEach(t -> System.out.printf("(%d, %d, %d)%n", t.x, t.y, t.z));
    }
}

class Range implements Many<Integer> {
    static Range from(int start) {
        return new Range(start, Integer.MAX_VALUE, 1);
    }

    static Range from(int start, int step) {
        return new Range(start, (step > 0 ? 0 : 1) + Integer.MAX_VALUE, step);
    }

    static Range between(int start, int endExclusive) {
        return new Range(start, endExclusive, start < endExclusive ? 1 : -1);
    }

    static Range between(int start, int endExclusive, int step) {
        return new Range(start, endExclusive, step);
    }

    private int n;
    private final int endExclusive;
    private final int step;

    private Range(int start, int endExclusive, int step) {
        if (step == 0 ||
                (start < endExclusive && step < 0) ||
                (start > endExclusive && step > 0)
        ) {
            throw new IllegalArgumentException("invalid step");
        }
        n = start;
        this.endExclusive = endExclusive;
        this.step = step;
    }

    @Override
    public void forEach(Case<Integer> c) {
        if (Integer.MAX_VALUE - Math.abs(step) < Math.abs(n)) {
            c.nil();
            return;
        }
        if (step > 0 ? (n >= endExclusive) : (n <= endExclusive)) {
            c.nil();
        }
        else {
            int current = n;
            n += step;
            c.cons(current, this);
        }
    }

}
