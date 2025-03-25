package io.syslogic.socketio.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ChatUser extends BaseObservable {
    private final String socketId;
    private final String username;

    public ChatUser(String socketId, String username) {
        this.socketId = socketId;
        this.username = username;
    }

    @Bindable
    public String getSocketId() {
        return this.socketId;
    }
    @Bindable
    public String getUsername() {
        return this.username;
    }

    public static class Builder {
        private String socketId;
        private String username;

        public Builder setSocketId(String value) {
            this.socketId = value;
            return this;
        }
        public Builder setUsername(String value) {
            this.username = value;
            return this;
        }

        public ChatUser build() {
            return new ChatUser(this.socketId, this.username);
        }
    }
}
