package io.syslogic.socketio;

class Constants {

    private static final String DEBUG_URL ="http://192.168.2.96:3000/";

    private static final String LIVE_URL = "https://.../";

    /** CHAT_SERVER_URL may cause io.socket.engineio.client.EngineIOException: xhr poll error when down */
    static String CHAT_SERVER_URL = BuildConfig.DEBUG ? DEBUG_URL : LIVE_URL;
}
