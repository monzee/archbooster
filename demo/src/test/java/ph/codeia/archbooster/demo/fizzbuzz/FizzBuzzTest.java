package ph.codeia.archbooster.demo.fizzbuzz;

/*
 * This file is a part of the arch-booster project.
 */

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import ph.codeia.archbooster.Emitter;
import ph.codeia.archbooster.Operation;

import static org.junit.Assert.*;


public class FizzBuzzTest {
    @Test
    public void increments_count() {
        assertEquals(101, output(FizzBuzz.INCREMENT.from(100)));
    }

    @Test
    public void decrements_count() {
        assertEquals(422, output(FizzBuzz.DECREMENT.from(423)));
    }

    @Test
    public void emits_Fizz_after_incrementing_2() {
        assertEquals("Fizz", effect(FizzBuzz.INCREMENT.from(2)));
    }

    @Test
    public void emits_Fizz_after_decrementing_4() {
        assertEquals("Fizz", effect(FizzBuzz.DECREMENT.from(4)));
    }

    @Test
    public void emits_Buzz_after_incrementing_4() {
        assertEquals("Buzz", effect(FizzBuzz.INCREMENT.from(4)));
    }

    @Test
    public void emits_Buzz_after_decrementing_6() {
        assertEquals("Buzz", effect(FizzBuzz.DECREMENT.from(6)));
    }

    @Test
    public void emits_FizzBuzz_after_incrementing_14() {
        assertEquals("FizzBuzz", effect(FizzBuzz.INCREMENT.from(14)));
    }

    @Test
    public void emits_FizzBuzz_after_decrementing_16() {
        assertEquals("FizzBuzz", effect(FizzBuzz.DECREMENT.from(16)));
    }

    private static int output(Operation<Integer, ?> operation) {
        AtomicInteger n = new AtomicInteger(0);
        operation.run(n::set);
        return n.get();
    }

    private static <T> T effect(Operation<?, T> operation) {
        AtomicReference<T> e = new AtomicReference<>();
        operation.run(Emitter.SINK, e::set);
        return e.get();
    }
}

