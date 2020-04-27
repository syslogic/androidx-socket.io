package io.syslogic.socketio;

class Constants {

    private static String DEBUG_URL ="http://192.168.1.1:3000/";

    private static String LIVE_URL = "https://.../";

    /** CHAT_SERVER_URL may cause io.socket.engineio.client.EngineIOException: xhr poll error when down */
    static String CHAT_SERVER_URL = BuildConfig.DEBUG ? DEBUG_URL : LIVE_URL;
}
