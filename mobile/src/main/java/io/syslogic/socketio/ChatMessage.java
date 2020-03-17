package io.syslogic.socketio;

class ChatMessage {

    static final int TYPE_MESSAGE = 0;
    static final int TYPE_LOG = 1;
    static final int TYPE_ACTION = 2;

    private int mType;
    private String mMessage;
    private String mUsername;

    private ChatMessage() {}

    int getType() {
        return mType;
    }

    String getMessage() {
        return mMessage;
    }

    String getUsername() {
        return mUsername;
    }

    static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;

        Builder(int type) {
            mType = type;
        }

        Builder username(String username) {
            mUsername = username;
            return this;
        }

        Builder message(String message) {
            mMessage = message;
            return this;
        }

        ChatMessage build() {
            ChatMessage message = new ChatMessage();
            message.mType = mType;
            message.mUsername = mUsername;
            message.mMessage = mMessage;
            return message;
        }
    }
}
