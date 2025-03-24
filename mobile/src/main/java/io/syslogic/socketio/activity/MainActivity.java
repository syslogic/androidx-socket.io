package io.syslogic.socketio.activity;

import android.os.Bundle;

public class MainActivity extends BaseActivity {
    String socketId = null;
    String userName = null;
    int userCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setSocketId(String value) {
        this.socketId = value;
    }

    public void setUserName(String value) {
        this.userName = value;
    }

    public void setUserCount(int value) {
        this.userCount = value;
    }

    public String getSocketId() {
        return this.socketId;
    }

    public String getUserName() {
        return this.userName;
    }

    public int getUserCount() {
        return this.userCount;
    }
}
