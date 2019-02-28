package ph.codeia.archbooster.demo.login;

/*
 * This file is a part of the arch-booster project.
 */

import org.junit.Test;

import ph.codeia.archbooster.Emitter;


public class DoUpdateTest {
    @Test
    public void updates_the_username() {
        Login.Model state = new Login.Model();
        new DoUpdate(Login.Field.USERNAME, "a").from(state).run(Emitter.SINK);
    }
}