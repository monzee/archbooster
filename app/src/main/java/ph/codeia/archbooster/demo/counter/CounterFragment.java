package ph.codeia.archbooster.demo.counter;

/*
 * This file is a part of the arch-booster project.
 */

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import ph.codeia.archbooster.R;


public class CounterFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.counter, container, false);
        Model vm = ViewModelProviders.of(requireActivity()).get(Model.class);
        LifecycleOwner owner = getViewLifecycleOwner();
        setup(owner, vm.state,
                root.findViewById(R.id.counter),
                root.findViewById(R.id.plus),
                root.findViewById(R.id.minus)
        ).observe(owner, vm);
        return root;
    }

    @SuppressLint("SetTextI18n")
    private static LiveData<Counter> setup(
            LifecycleOwner owner,
            LiveData<Integer> output,
            TextView counter,
            Button plus,
            Button minus
    ) {
        output.observe(owner, n -> counter.setText(n.toString()));
        MutableLiveData<Counter> actions = new MutableLiveData<>();
        plus.setOnClickListener(o -> actions.setValue(Counter.INCREMENT));
        minus.setOnClickListener(o -> actions.setValue(Counter.DECREMENT));
        return actions;
    }

    private static class Model extends ViewModel implements Observer<Counter> {
        final MutableLiveData<Integer> state = new MutableLiveData<>();

        Model() {
            state.setValue(0);
        }

        @Override
        public void onChanged(Counter action) {
            action.from(state.getValue()).run(state::setValue);
        }
    }
}

