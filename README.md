# ArchBooster
[ ![Download](https://api.bintray.com/packages/monzee/jvm/archbooster-android/images/download.svg) ](https://bintray.com/monzee/jvm/archbooster-android/_latestVersion)

## Installation
This package is (will be) available at JCenter. You can simply add these
coordinates to your dependencies:
```groovy
dependencies {
    implementation "ph.codeia.archbooster:android:$version"
}
```
where `$version` is the number shown in the badge above.

## Usage

### PublishLiveData

This is to `MutableLiveData` as Rx's `PublishSubject` is to `BehaviorSubject`.
You might have seen or used `SingleLiveEvent` before. This is a more correct
implementation of that idea.

This class extends `MutableLiveData`, so you can use this exactly as you would
a `MutableLiveData`. Typically you'd have a `ViewModel` with a private field
with a getter exposing it as `LiveData` and you'd have some other methods that
feed values into the field.

```java
public class CounterModel extends ViewModel {
    private final PublishLiveData<Integer> state = new PublishLiveData<>();

    public CounterModel() {
        state.setValue(0);
    }

    public LiveData<Integer> state() {
        return state;
    }

    public void increment() {
        state.setValue(state.getValue() + 1);
    }

    public void decrement() {
        state.setValue(state.getValue() - 1);
    }
}
```

This example doesn't really make sense because the state is something you want
to emit at subscription time. See the `app` module for concrete examples on how
it might be used.


### LiveView

This component is like [RxBinding](https://github.com/JakeWharton/RxBinding),
except it produces `LiveData` and `Observer`s instead of RxJava subjects and
subscribers. It's also not nearly as comprehensive, nor is it planned to be.

```java
class LoginView {
    final LiveData<CharSequence> username;
    final LiveData<CharSequence> password;
    final LiveData<Void> login;
    final Observer<CharSequence> toasts;

    LoginView(TextView username, TextView password, Button login) {
        this.username = LiveView.textChanges(username);
        this.password = LiveView.textChanges(password);
        this.login = LiveView.clicks(login);
        toasts = LiveView.showToast(username.getContext());
    }
}
```


## License
```
MIT License

Copyright (c) 2019 Mon Zafra

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
