package io.syslogic.socketio.menu;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;

import java.lang.ref.WeakReference;

import io.syslogic.socketio.R;
import io.syslogic.socketio.activity.MainActivity;
import io.syslogic.socketio.fragment.BaseFragment;

/**
 * Chat {@link MenuProvider}
 * @author Martin Zeitler
 */
public class ChatMenuProvider implements MenuProvider {

    private final WeakReference<MainActivity> mContext;

    public ChatMenuProvider(MainActivity activity) {
        this.mContext = new WeakReference<>(activity);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_chat, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_action_leave_chat) {
            MainActivity activity = mContext.get();
            // activity.getNavController().navigateUp();
            activity.getNavController().navigate(R.id.action_chatFragment_to_loginDialogFragment);
            return true;
        } else if (menuItem.getItemId() == R.id.menu_menu_action_sockets) {
            MainActivity activity = mContext.get();
            activity.getNavController().navigate(R.id.action_chatFragment_to_socketsDialogFragment);
            return true;
        }
        return false;
    }
}
