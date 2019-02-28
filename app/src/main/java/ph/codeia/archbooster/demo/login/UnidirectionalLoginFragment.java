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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import ph.codeia.archbooster.AndroidConsole;
import ph.codeia.archbooster.Console;
import ph.codeia.archbooster.Global;
import ph.codeia.archbooster.LifecycleBinder;
import ph.codeia.archbooster.LiveRef;
import ph.codeia.archbooster.LiveView;
import ph.codeia.archbooster.Observers;
import ph.codeia.archbooster.PublishLiveData;
import ph.codeia.archbooster.R;


public class UnidirectionalLoginFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.login, container, false);
        Form form = new Form(
                root.findViewById(R.id.username),
                root.findViewById(R.id.password),
                root.findViewById(R.id.login)
        );
        ViewModel vm = ViewModelProviders.of(requireActivity()).get(ViewModel.class);
        LifecycleBinder live = new LifecycleBinder(getViewLifecycleOwner());
        live.bind(vm.state, form);
        live.bind(vm.state, Observers.where(Login.Model::isLoggedIn, state -> {
            // TODO do something with state.token()
        }));
        live.bind(vm.messages, AndroidConsole.from(requireContext(), "Login"));
        form.connect(live, vm);
        return root;
    }

    private static class ViewModel extends AndroidViewModel implements Login.Input {
        final MutableLiveData<Login.Model> state = new MutableLiveData<>();
        final MutableLiveData<Console> messages = new PublishLiveData<>();
        final LiveRef<Login.Model> stateRef = new LiveRef<>(state);
        final DoActivate.Factory activator;
        final DoLogin doLogin;

        ViewModel(@NonNull Application application) {
            super(application);
            state.setValue(new Login.Model());
            Global locator = getApplication();
            Global.LoginAdapter service = locator.loginAdapter();
            doLogin = new DoLogin(service::login);
            activator = DoActivate.of(service::isEmail);
        }

        @Override
        public void activate(Login.Field field) {
            activator.with(field).from(state.getValue()).run(state::setValue);
        }

        @Override
        public void update(Login.Field field, String value) {
            new DoUpdate(field, value).from(state.getValue()).run(state::setValue);
        }

        @Override
        public void login() {
            doLogin.from(state.getValue()).run(stateRef, messages::postValue);
        }
    }

    private static class Form implements Observer<Login.Model> {
        final TextView username;
        final TextView password;
        final Button login;

        Form(TextView username, TextView password, Button login) {
            this.username = username;
            this.password = password;
            this.login = login;
        }

        void connect(LifecycleBinder live, Login.Input input) {
            live.bind(LiveView.textChanges(username),
                    Observers.pipe(Object::toString, input::setUsername));
            live.bind(LiveView.textChanges(password),
                    Observers.pipe(Object::toString, input::setPassword));
            LiveData loginClicks = LiveView.clicks(login);
            live.bindOnce(loginClicks, () -> {
                input.activate();
                live.bind(loginClicks, input::login);
            });
        }

        @Override
        public void onChanged(Login.Model state) {
            switch (state.tag()) {
                case FAILED:
                    throw state.failure();
                case LOGGED_IN:
                    return;
                case LOGGING_IN:
                    username.setEnabled(false);
                    password.setEnabled(false);
                    // fallthrough
                case ACTIVE:
                case INVALID:
                    login.setEnabled(false);
                    break;
                case INACTIVE:
                case READY:
                    username.setEnabled(true);
                    password.setEnabled(true);
                    login.setEnabled(true);
                    break;
            }
            username.setError(state.error(Login.Field.USERNAME));
            password.setError(state.error(Login.Field.PASSWORD));
        }
    }
}
