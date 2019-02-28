package ph.codeia.archbooster.demo.login;

/*
 * This file is a part of the arch-booster project.
 */

import java.util.HashMap;
import java.util.Map;

import ph.codeia.archbooster.function.Predicate1;

/**
 * Contract for the login screen.
 *
 * <p> Represents the state using a package-mutable tagged union.
 */
public final class Login {

    public enum State {
        /**
         * The initial state. No field validation can be done while in this
         * state. If the field activation happens when the value changes, the
         * login button should be disabled. Otherwise, the activation could
         * only happen on the first click of the login button, so it should be
         * enabled for anything to happen at all.
         */
        INACTIVE,
        /**
         * Some input has been taken in one of the fields and validated. Not all
         * of the fields have been validated yet, so the login button must be
         * disabled. No errors must be shown on the untouched fields.
         */
        ACTIVE,
        /**
         * One or more of the fields are invalid. The error(s) should be
         * displayed and the login button disabled.
         */
        INVALID,
        /**
         * All fields are valid and logging in is possible. The login button
         * should be enabled.
         */
        READY,
        /**
         * Currently logging in and awaiting the result. The fields and the
         * login button should be disabled.
         */
        LOGGING_IN,
        /**
         * Login has succeeded. The screen has served its purpose and should be
         * dismissed and the next one launched.
         */
        LOGGED_IN,
        /**
         * Irrecoverable state. Should crash the application.
         */
        FAILED
    }

    public enum Field {USERNAME, PASSWORD}

    /**
     * Mutators are for the use case classes and tests only.
     */
    public static class Model {
        private volatile State tag = State.INACTIVE;
        private final Map<Field, String> errors = new HashMap<>(2);
        private byte touched = 0;
        private String username;
        private String password;
        private String token;
        private RuntimeException error;
        private Predicate1<String> isValidEmail = s -> false;

        void tag(State tag) {
            this.tag = tag;
        }

        void update(Field field, String value) {
            switch (field) {
                case USERNAME:
                    username = value;
                    break;
                case PASSWORD:
                    password = value;
                    break;
            }
        }

        void touch(Field field) {
            touched |= (1 << field.ordinal());
        }

        boolean isEmail(String text) {
            return isValidEmail.test(text);
        }

        boolean isActivated(Field field) {
            return (touched & (1 << field.ordinal())) != 0;
        }

        void error(Field field, String value) {
            errors.put(field, value);
        }

        void clear(Field field) {
            errors.remove(field);
        }

        void validateEmailUsing(Predicate1<String> predicate) {
            isValidEmail = predicate;
            tag(State.ACTIVE);
        }

        /**
         * Sets the tag according to the validation result. If the errors map
         * is empty, it does not necessarily mean that the user is ready to
         * login. One of the fields might not have been touched yet, in which
         * case the tag is set to active where there are no errors displayed
         * but the login button is disabled.
         */
        void didValidate() {
            tag(!errors.isEmpty() ? State.INVALID
                    : touched != 3 ? State.ACTIVE
                    : State.READY);
        }

        void succeed(String token) {
            this.token = token;
            tag(State.LOGGED_IN);
        }

        void fail(Throwable ex) {
            error = ex instanceof RuntimeException ?
                    (RuntimeException) ex : new RuntimeException(ex);
            tag(State.FAILED);
        }

        public State tag() {
            return tag;
        }

        public String username() {
            return username;
        }

        public String password() {
            return password;
        }

        public String error(Field field) {
            return errors.get(field);
        }

        public String token() {
            return token;
        }

        public RuntimeException failure() {
            return error;
        }

        public boolean isLoggedIn() {
            return tag == State.LOGGED_IN;
        }
    }

    /**
     * List of actions that the user may perform.
     */
    public interface Input {
        /**
         * Performs the initial validation and allows subsequent validations
         * to happen.
         *
         * @param field The field to activate
         */
        void activate(Field field);

        default void activate() {
            activate(Field.USERNAME);
            activate(Field.PASSWORD);
        }

        /**
         * Update and validate a field if possible.
         *
         * @param field The field to test
         * @param value The value to test
         */
        void update(Field field, String value);

        default void setUsername(String value) {
            update(Field.USERNAME, value);
        }

        default void setPassword(String value) {
            update(Field.PASSWORD, value);
        }

        /**
         * Try to login. Login failures due to incorrect credentials and
         * service unavailability should be recoverable.
         */
        void login();
    }

    /**
     * Should be emitted by the login use case when the username + password
     * combination was not accepted by the auth service.
     */
    public static class Rejected extends RuntimeException {
        public Rejected() {
            super("Wrong username or password");
        }
    }

    /**
     * Should be emitted by the login use case when the server is unavailable.
     */
    public static class Unavailable extends RuntimeException {
        public Unavailable() {
            super("Login service unavailable");
        }
    }

    private Login() {}
}
