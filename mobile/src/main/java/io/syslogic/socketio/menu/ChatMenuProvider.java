package io.syslogic.socketio.menu;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;

import java.lang.ref.WeakReference;

import io.syslogic.socketio.R;
import io.syslogic.socketio.activity.BaseActivity;
import io.syslogic.socketio.activity.MainActivity;

public class ChatMenuProvider implements MenuProvider {

    private final WeakReference<BaseActivity> mContext;

    public ChatMenuProvider(BaseActivity activity) {
        this.mContext = new WeakReference<>(activity);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_chat, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_leave) {
            BaseActivity activity = mContext.get();
            activity.getNavController().navigateUp();
            return true;
        }
        return false;
    }
}
