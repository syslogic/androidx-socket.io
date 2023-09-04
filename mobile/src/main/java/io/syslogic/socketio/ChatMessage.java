package io.syslogic.socketio;

public class ChatMessage {

    static final int TYPE_MESSAGE = 0;
    static final int TYPE_LOG = 1;
    static final int TYPE_ACTION = 2;

    private int mType;
    private String mMessage;
    private String mUsername;

    private ChatMessage() {

    }

    public int getType() {
        return this.mType;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public String getUsername() {
        return this.mUsername;
    }

    static class Builder {

        private final int mType;
        private String mUsername;
        private String mMessage;

        Builder(int type) {
            mType = type;
        }

        Builder username(String username) {
            this.mUsername = username;
            return this;
        }

        Builder message(String message) {
            this.mMessage = message;
            return this;
        }

        ChatMessage build() {
            ChatMessage message = new ChatMessage();
            message.mType = this.mType;
            message.mUsername = this.mUsername;
            message.mMessage = this.mMessage;
            return message;
        }
    }
}
