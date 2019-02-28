package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;


public final class LiveView<T> extends LiveData<T> {

    public static <T> LiveData<T> create(Io<T> builder) {
        return new LazyLiveData<>(builder, null);
    }

    public static LiveData<Void> clicks(View view) {
        return new LazyLiveData<>(
                on -> view.setOnClickListener(o -> on.emit(null)),
                () -> view.setOnClickListener(null)
        );
    }

    public static LiveData<CharSequence> textChanges(TextView view) {
        return LazyLiveData.of(new TextChanges(view));
    }

    public static LiveData<Boolean> toggles(CompoundButton view) {
        return new LazyLiveData<>(
                on -> view.setOnCheckedChangeListener(
                        (button, isChecked) -> on.emit(isChecked)),
                () -> view.setOnCheckedChangeListener(null)
        );
    }

    public static Observer<Boolean> setEnabled(View view) {
        return view::setEnabled;
    }

    public static Observer<CharSequence> setText(TextView view) {
        return view::setText;
    }

    public static Observer<Integer> setTextResource(TextView view) {
        return view::setText;
    }

    public static Observer<CharSequence> setError(TextView view) {
        return view::setError;
    }

    public static Observer<Integer> setErrorResource(TextView view) {
        return stringRes -> view.setError(view.getResources().getText(stringRes));
    }

    public static Observer<Boolean> setChecked(Checkable view) {
        return view::setChecked;
    }

    public static Observer<Void> click(View view) {
        return o -> view.performClick();
    }

    public static Observer<Void> toggle(Checkable view) {
        return o -> view.toggle();
    }

    public static Observer<CharSequence> showToast(Context context) {
        return showToast(context, Toast.LENGTH_SHORT);
    }

    public static Observer<CharSequence> showToast(Context context, int duration) {
        return text -> Toast.makeText(context, text, duration).show();
    }

    public static Observer<Integer> toastResource(Context context) {
        return toastResource(context, Toast.LENGTH_SHORT);
    }

    public static Observer<Integer> toastResource(Context context, int duration) {
        return stringRes -> Toast.makeText(context, stringRes, duration).show();
    }

    private static class TextChanges implements Io<CharSequence>, Runnable {
        final TextView view;
        TextWatcher watcher;

        TextChanges(TextView view) {
            this.view = view;
        }

        @Override
        public void run(Emitter<? super CharSequence> on) {
            watcher = new OnTextChange(on);
            view.addTextChangedListener(watcher);
        }

        @Override
        public void run() {
            view.removeTextChangedListener(watcher);
        }
    }

    private static class OnTextChange implements TextWatcher {
        final Emitter<? super CharSequence> on;

        OnTextChange(Emitter<? super CharSequence> on) {
            this.on = on;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            on.emit(s);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private LiveView() {}
}
