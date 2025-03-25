package io.syslogic.socketio.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ChatUser extends BaseObservable {
    public static final int TYPE_DEFAULT  = 0;
    public static final int TYPE_OPERATOR = 1;
    private final Integer userType;
    private final String socketId;
    private final String username;

    public ChatUser(Integer userType, String socketId, String username) {
        this.userType = userType;
        this.socketId = socketId;
        this.username = username;
    }

    @Bindable
    public Integer getUserType() {
        return this.userType;
    }
    @Bindable
    public String getSocketId() {
        return this.socketId;
    }
    @Bindable
    public String getUsername() {
        return this.username;
    }

    /** Builder */
    public static class Builder {
        private Integer userType;
        private String socketId;
        private String username;

        public Builder(Integer userType) {
            this.userType = userType;
        }

        public Builder setUserType(Integer value) {
            this.userType = value;
            return this;
        }

        public Builder setSocketId(String value) {
            this.socketId = value;
            return this;
        }

        public Builder setUsername(String value) {
            this.username = value;
            return this;
        }

        public ChatUser build() {
            return new ChatUser(this.userType, this.socketId, this.username);
        }
    }
}
