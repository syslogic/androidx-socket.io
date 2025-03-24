package io.syslogic.socketio.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ChatRoom extends BaseObservable {

    private final String socketId;
    private final String username;
    private final int userCount;

    public ChatRoom(String socketId, String username, int userCount) {
        this.socketId = socketId;
        this.username = username;
        this.userCount = userCount;
    }

    @Bindable
    public String getSocketId() {
        return this.socketId;
    }
    @Bindable
    public String getUsername() {
        return this.username;
    }
    @Bindable
    public int getUserCount() {
        return this.userCount;
    }

    public static class Builder {
        private String socketId;
        private String username;
        private int userCount;

        public Builder socketId(String socketId) {
            this.socketId = socketId;
            return this;
        }
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder userCount(int userCount) {
            this.userCount = userCount;
            return this;
        }

        public ChatRoom build() {
            return new ChatRoom(this.socketId, this.username, this.userCount);
        }
    }
}
