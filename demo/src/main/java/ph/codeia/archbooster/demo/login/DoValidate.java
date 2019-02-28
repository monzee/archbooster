package ph.codeia.archbooster.demo.login;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.Operation;
import ph.codeia.archbooster.UseCase;


class DoValidate implements UseCase<Login.Model, Void> {
    private final Login.Field field;

    public DoValidate(Login.Field field) {
        this.field = field;
    }

    @Override
    public Operation<Login.Model, Void> from(Login.Model state) {
        return (states, events) -> {
            switch (state.tag()) {
                case INVALID:
                case READY:
                case ACTIVE:
                    break;
                default:
                    return;
            }
            state.clear(field);
            String value;
            switch (field) {
                case USERNAME:
                    value = state.username();
                    if (value == null || value.isEmpty()) {
                        state.error(field, "This field is required");
                    }
                    else if (!state.isEmail(value)) {
                        state.error(field, "Bad email address");
                    }
                    break;
                case PASSWORD:
                    value = state.password();
                    if (value == null || value.isEmpty()) {
                        state.error(field, "This field is required");
                    }
                    break;
            }
            state.didValidate();
            states.emit(state);
        };
    }
}
