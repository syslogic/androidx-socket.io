package io.syslogic.socketio;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {

    private LiveData<NavController> currentNavController = null;
    private String userName = null;
    private int userCount = 0;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.fragment_container);

        try {
            IO.Options opts = new IO.Options();
            this.mSocket = IO.socket(Constants.CHAT_SERVER_URL, opts);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(currentNavController.getValue() != null) {
            return currentNavController.getValue().navigateUp();
        } else {
            return false;
        }
    }

    public void setUserName(String value) {
        this.userName = value;
    }

    public void setUserCount(int value) {
        this.userCount = value;
    }

    public Socket getSocket() {
        return this.mSocket;
    }

    public String getUserName() {
        return this.userName;
    }

    public int getUserCount() {
        return this.userCount;
    }
}
