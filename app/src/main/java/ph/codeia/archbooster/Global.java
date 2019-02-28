package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import android.app.Application;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.core.util.PatternsCompat;
import ph.codeia.archbooster.demo.login.Login;
import ph.codeia.archbooster.experiments.Try;


public class Global extends Application {
    public interface LoginAdapter {
        Operation<String, Throwable> login(String username, String password);
        boolean isEmail(String text);
    }

    private ContextDependent services;

    @Override
    public void onCreate() {
        super.onCreate();
        services = new ContextDependent();
    }

    public Executor ioExecutor() {
        return IoExecutor.INSTANCE;
    }

    public LoginAdapter loginAdapter() {
        return LoginModule.ADAPTER;
    }

    private class ContextDependent {
    }

    private static class IoExecutor {
        static final Executor INSTANCE = Executors.newCachedThreadPool();
    }

    private static class Rng {
        static final Random INSTANCE = new Random();
    }

    private static class LoginModule {
        static final LoginAdapter ADAPTER = new LoginAdapter() {
            @Override
            public Try<String> login(String username, String password) {
                return c -> {
                    String credentials = username + ":" + password;
                    IoExecutor.INSTANCE.execute(() -> {
                        try {
                            Thread.sleep(2000);
                            if (Rng.INSTANCE.nextInt(10) == 1) {
                                c.failure(new Login.Unavailable());
                            }
                            else if ("foo@example.com:hunter2".equals(credentials)) {
                                c.success("ok.");
                            }
                            else {
                                c.failure(new Login.Rejected());
                            }
                        }
                        catch (InterruptedException e) {
                            c.failure(e);
                        }
                    });
                };
            }

            @Override
            public boolean isEmail(String text) {
                return PatternsCompat.EMAIL_ADDRESS.matcher(text).matches();
            }
        };
    }
}
