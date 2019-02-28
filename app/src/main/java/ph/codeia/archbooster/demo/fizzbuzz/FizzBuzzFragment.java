package ph.codeia.archbooster.demo.fizzbuzz;

/*
 * This file is a part of the arch-booster project.
 */

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import ph.codeia.archbooster.LifecycleBinder;
import ph.codeia.archbooster.LiveView;
import ph.codeia.archbooster.Observers;
import ph.codeia.archbooster.PublishLiveData;
import ph.codeia.archbooster.R;


public class FizzBuzzFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.counter, container, false);
        Model vm = ViewModelProviders.of(requireActivity()).get(Model.class);
        LifecycleBinder binder = new LifecycleBinder(getViewLifecycleOwner());
        binder.bind(setup(
                binder,
                vm.state,
                vm.message,
                root.findViewById(R.id.counter),
                root.findViewById(R.id.plus),
                root.findViewById(R.id.minus)
        ), vm);
        return root;
    }

    private static LiveData<FizzBuzz> setup(
            LifecycleBinder live,
            LiveData<Integer> state,
            LiveData<String> toasts,
            TextView counter,
            Button plus,
            Button minus
    ) {
        live.bind(state, Observers.pipe(Object::toString, LiveView.setText(counter)));
        live.bind(toasts, LiveView.showToast(counter.getContext()));
        return LiveView.create(cmd -> {
            live.bind(LiveView.clicks(plus), () -> cmd.emit(FizzBuzz.INCREMENT));
            live.bind(LiveView.clicks(minus), () -> cmd.emit(FizzBuzz.DECREMENT));
        });
    }

    private static class Model extends ViewModel implements Observer<FizzBuzz> {
        final MutableLiveData<Integer> state = new MutableLiveData<>();
        final MutableLiveData<String> message = new PublishLiveData<>();

        Model() {
            state.setValue(0);
        }

        @Override
        public void onChanged(FizzBuzz fizzBuzz) {
            fizzBuzz.from(state.getValue()).run(state::setValue, message::setValue);
        }
    }
}
