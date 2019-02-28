package ph.codeia.archbooster.demo.login;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.Console;
import ph.codeia.archbooster.Operation;
import ph.codeia.archbooster.Ref;
import ph.codeia.archbooster.UseCase;


public class DoLogin implements UseCase.Concurrent<Login.Model, Console> {

    public interface Action {
        Operation<String, Throwable> exec(String username, String password);
    }

    private final Action tryLogin;

    public DoLogin(Action tryLogin) {
        this.tryLogin = tryLogin;
    }

    @Override
    public Operation<Ref<Login.Model>, Console> from(Login.Model state) {
        return (states, events) -> {
            if (state.tag() != Login.State.READY) {
                return;
            }
            state.tag(Login.State.LOGGING_IN);
            states.emit(Ref.notifyChanged());
            tryLogin.exec(state.username(), state.password()).run(token -> {
                events.emit(Console.log("logged in; token='%s'", token));
                states.emit(Ref.deref(current -> {
                    if (current.tag() != Login.State.LOGGING_IN) {
                        return;
                    }
                    current.succeed(token);
                    states.emit(Ref.notifyChanged());
                }));
            }, error -> {
                events.emit(Console.log(error));
                states.emit(Ref.deref(current -> {
                    if (current.tag() != Login.State.LOGGING_IN) {
                        return;
                    }
                    try {
                        throw error;
                    }
                    catch (Login.Rejected | Login.Unavailable e) {
                        events.emit(Console.echo(e.getMessage()));
                        current.tag(Login.State.READY);
                    }
                    catch (Throwable e) {
                        current.fail(e);
                    }
                    finally {
                        states.emit(Ref.notifyChanged());
                    }
                }));
            });
        };
    }
}
