package ph.codeia.archbooster.demo.login;

/*
 * This file is a part of the arch-booster project.
 */

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.PatternsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import ph.codeia.archbooster.LifecycleBinder;
import ph.codeia.archbooster.LiveView;
import ph.codeia.archbooster.Observers;
import ph.codeia.archbooster.R;


public class LoginFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.login, container, false);
        LifecycleBinder live = new LifecycleBinder(getViewLifecycleOwner());
        ViewModel vm = ViewModelProviders.of(requireActivity()).get(ViewModel.class);
        Form form = new Form(
                root.findViewById(R.id.username),
                root.findViewById(R.id.password),
                root.findViewById(R.id.login)
        );
        live.bind(vm.usernameError, form.usernameError);
        live.bind(vm.passwordError, form.passwordError);
        live.bind(vm.isValid, form.isValid);
        live.bind(vm.isLoggingIn, form.isLoggingIn);
        live.bind(form.username, Observers.pipe(Object::toString, vm::setUsername));
        live.bind(form.password, Observers.pipe(Object::toString, vm::setPassword));
        live.bindOnce(form.login, () -> {
            vm.activate();
            live.bind(form.login, vm::login);
        });
        return root;
    }

    private static class ViewModel extends AndroidViewModel {
        final MutableLiveData<String> usernameError = new MutableLiveData<>();
        final MutableLiveData<String> passwordError = new MutableLiveData<>();
        final MutableLiveData<Boolean> isValid = new MutableLiveData<>();
        final MutableLiveData<Boolean> isLoggingIn = new MutableLiveData<>();
        private String username;
        private String password;
        private boolean active;

        public ViewModel(@NonNull Application app) {
            super(app);
            isValid.setValue(true);
        }

        void activate() {
            active = true;
            validateUsername();
            validatePassword();
        }

        void setUsername(String value) {
            username = value;
            if (active) {
                validateUsername();
            }
        }

        void setPassword(String value) {
            password = value;
            if (active) {
                validatePassword();
            }
        }

        void login() {
            Boolean valid = isValid.getValue();
            if (valid == null || !valid) {
                return;
            }
            isLoggingIn.setValue(true);
            // TODO
        }

        private void validateUsername() {
            String error = null;
            if (username == null || username.isEmpty()) {
                error = "This field is required";
            }
            else if (!PatternsCompat.EMAIL_ADDRESS.matcher(username).matches()) {
                error = "Bad email address";
            }
            usernameError.setValue(error);
            didValidate();
        }

        private void validatePassword() {
            String error = null;
            if (password == null || password.isEmpty()) {
                error = "This field is required";
            }
            passwordError.setValue(error);
            didValidate();
        }

        private void didValidate() {
            isValid.setValue(
                    usernameError.getValue() == null &&
                    passwordError.getValue() == null
            );
        }
    }

    private static class Form {
        final LiveData<CharSequence> username;
        final LiveData<CharSequence> password;
        final LiveData<Void> login;
        final Observer<CharSequence> usernameError;
        final Observer<CharSequence> passwordError;
        final Observer<Boolean> isValid;
        final Observer<Boolean> isLoggingIn;

        Form(TextView username, TextView password, Button login) {
            this.username = LiveView.textChanges(username);
            this.password = LiveView.textChanges(password);
            this.login = LiveView.clicks(login);
            usernameError = LiveView.setError(username);
            passwordError = LiveView.setError(password);
            isValid = LiveView.setEnabled(login);
            isLoggingIn = flag -> {
                username.setEnabled(!flag);
                password.setEnabled(!flag);
                login.setEnabled(!flag);
            };
        }
    }
}
