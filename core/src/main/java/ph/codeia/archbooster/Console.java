package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */


public interface Console extends Emitter<Console.Event> {

    interface Event {
        void log(Throwable error, String text, Object... fmtArgs);
        void debug(String text, Object... fmtArgs);
        void echo(String text);

        default void dispatch(Console command) {
            if (command != null) {
                command.emit(this);
            }
        }
    }

    static Console debug(String text, Object... fmtArgs) {
        return event -> event.debug(text, fmtArgs);
    }

    static Console log(Throwable error) {
        return log(error, "");
    }

    static Console log(Throwable error, String text, Object... fmtArgs) {
        return event -> event.log(error, text, fmtArgs);
    }

    static Console echo(String text, Object... fmtArgs) {
        return event -> {
            String message = String.format(text, fmtArgs);
            event.echo(message);
        };
    }
}
