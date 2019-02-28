package ph.codeia.archbooster.demo.login;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.Operation;
import ph.codeia.archbooster.UseCase;
import ph.codeia.archbooster.function.Predicate1;


public class DoActivate implements UseCase<Login.Model, Void> {

    public interface Factory {
        DoActivate with(Login.Field field);
    }

    public static Factory of(Predicate1<String> isEmail) {
        return field -> new DoActivate(field, isEmail);
    }

    private final Login.Field field;
    private final Predicate1<String> isEmail;

    private DoActivate(Login.Field field, Predicate1<String> isEmail) {
        this.field = field;
        this.isEmail = isEmail;
    }

    @Override
    public Operation<Login.Model, Void> from(Login.Model state) {
        return (states, events) -> {
            if (state.isActivated(field)) {
                return;
            }
            state.touch(field);
            switch (field) {
                case USERNAME:
                    state.validateEmailUsing(isEmail);
                    new DoValidate(field).from(state).run(states);
                    break;
                case PASSWORD:
                    new DoValidate(field).from(state).run(states);
                    break;
            }
        };
    }
}
