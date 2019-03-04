package ph.codeia.archbooster;

/*
 * This file is a part of the arch-booster project.
 */

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.Observer;


public abstract class AndroidConsole implements Console.Event {

    public static Observer<Console> from(Context context, String tag) {
        return from(context, tag, Log.DEBUG);
    }

    public static Observer<Console> from(Context context, String tag, int logLevel) {
        AndroidConsole console = new AndroidConsole(tag, logLevel) {
            @Override
            public void echo(String text) {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        };
        return console::dispatch;
    }

    private final String tag;
    private final int logLevel;

    public AndroidConsole(String tag, int logLevel) {
        this.tag = tag;
        this.logLevel = logLevel;
    }

    public AndroidConsole(String tag) {
        this(tag, Log.DEBUG);
    }

    @Override
    public void log(Throwable error, String text, Object... fmtArgs) {
        if (Log.isLoggable(tag, logLevel)) {
            Log.e(tag, String.format(text, fmtArgs), error);
        }
    }

    @Override
    public void debug(String text, Object... fmtArgs) {
        if (!Log.isLoggable(tag, logLevel)) {
            return;
        }
        String message = String.format(text, fmtArgs);
        switch (logLevel) {
            case Log.VERBOSE:
                Log.v(tag, message);
                break;
            case Log.DEBUG:
                Log.d(tag, message);
                break;
            case Log.INFO:
                Log.i(tag, message);
                break;
            case Log.WARN:
                Log.w(tag, message);
                break;
            case Log.ERROR:
                Log.e(tag, message);
                break;
            case Log.ASSERT:
                Log.wtf(tag, message);
                break;
        }
    }
}
