package io.syslogic.socketio.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ChatRoom extends BaseObservable {

    private final String socketId;
    private final String username;
    private final Integer usercount;

    public ChatRoom(String socketId, String username, Integer userCount) {
        this.socketId = socketId;
        this.username = username;
        this.usercount = userCount;
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
    public int getUsercount() {
        return this.usercount;
    }

    /** Builder */
    public static class Builder {
        private String socketId;
        private String username;
        private Integer usercount;

        public Builder setSocketId(String value) {
            this.socketId = value;
            return this;
        }
        public Builder setUsername(String value) {
            this.username = value;
            return this;
        }
        public Builder setUsercount(Integer value) {
            this.usercount = value;
            return this;
        }

        public ChatRoom build() {
            return new ChatRoom(this.socketId, this.username, this.usercount);
        }
    }
}
