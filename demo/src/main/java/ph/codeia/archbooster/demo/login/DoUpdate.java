package ph.codeia.archbooster.demo.login;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.Operation;
import ph.codeia.archbooster.UseCase;


public class DoUpdate implements UseCase<Login.Model, Void> {
    private final Login.Field field;
    private final String value;

    DoUpdate(Login.Field field, String value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public Operation<Login.Model, Void> from(Login.Model state) {
        return (states, events) -> {
            state.update(field, value);
            if (state.isActivated(field)) {
                new DoValidate(field).from(state).run(states);
            }
        };
    }
}
