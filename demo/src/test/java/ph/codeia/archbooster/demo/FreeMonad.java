package ph.codeia.archbooster.demo;

/*
 * This file is a part of the arch-booster project.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Queue;

import ph.codeia.archbooster.experiments.Free;
import ph.codeia.archbooster.experiments.Higher;
import ph.codeia.archbooster.experiments.typeclass.Functor;
import ph.codeia.archbooster.function.Function1;


public class FreeMonad<T>
implements Free.Case<Algebra, T>, Algebra.Case<Free<Algebra, T>> {
    private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    private final Queue<Free<Algebra, T>> steps = new ArrayDeque<>();
    private T result;

    private FreeMonad(Free<Algebra, T> step) {
        steps.add(step);
    }

    @Override
    public void readLine(Function1<String, Free<Algebra, T>> next) {
        try {
            steps.add(next.apply(in.readLine()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printLine(String text, Function1<Void, Free<Algebra, T>> next) {
        System.out.println(text);
        steps.add(next.apply(null));
    }

    @Override
    public void free(Higher<Algebra, Free<Algebra, T>> node) {
        Algebra.of(node).match(this);
    }

    @Override
    public void pure(T t) {
        result = t;
    }

    private static final Free<Algebra, String> readLine = Free.liftF(Algebra::readLine);

    private static Free<Algebra, Void> printLine(String text) {
        return Free.liftF(text, Algebra::printLine);
    }

    private static <T> T interpret(Free<Algebra, T> program) {
        FreeMonad<T> interpreter = new FreeMonad<>(program);
        while (!interpreter.steps.isEmpty()) {
            interpreter.steps.remove().match(interpreter);
        }
        return interpreter.result;
    }

    public static void main(String[] args) {
        /*
        interpret $ do
            printLine "your name?"
            name <- readLine
            printLine $ "hello, " ++ name ++ "!"
            toUpper name
         */
        String result = interpret(Free.of(Algebra.KIND, monad -> monad
                .from(printLine("your name?"))
                .then(readLine)
                .also(name -> printLine("hello, " + name + "!"))
                .map(String::toUpperCase)
                .yield()));
        System.out.println("result = " + result);
    }
}

interface Algebra<T> extends Higher<Algebra, T> {
    interface Case<T> {
        void readLine(Function1<String, T> next);
        void printLine(String text, Function1<Void, T> next);
    }

    void match(Case<T> c);

    Kind KIND = new Kind();

    final class Kind implements Functor<Algebra> {
        @Override
        public <T, U> Algebra<U> map(
                Higher<Algebra, T> source,
                Function1<? super T, ? extends U> f
        ) {
            return c -> of(source).match(new Case<T>() {
                @Override
                public void readLine(Function1<String, T> next) {
                    c.readLine(s -> f.apply(next.apply(s)));
                }

                @Override
                public void printLine(String text, Function1<Void, T> next) {
                    c.printLine(text, o -> f.apply(next.apply(null)));
                }
            });
        }

        private Kind() {}
    }

    static <T> Algebra<T> of(Higher<Algebra, T> hk) {
        return (Algebra<T>) hk;
    }

    static <T> Algebra<T> readLine(Function1<String, T> next) {
        return c -> c.readLine(next);
    }

    static <T> Algebra<T> printLine(String text, Function1<Void, T> next) {
        return c -> c.printLine(text, next);
    }
}
