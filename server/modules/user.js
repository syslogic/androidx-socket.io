const constants = require("./constants");

module.exports = (io, socket) => {

    const getParticipants = () => {
        let data = [];
        io.sockets.sockets.forEach(function(socket, socketId) {
            if (typeof socket.data.username === 'undefined') {socket.data.username='default';} // default socket.
            console.log(`ID: ${socketId} -> Name: ${socket.data.username}`);
            data.push({socketId: socketId, username: socket.data.username});
        });
        return data;
    }

    // When the client emits 'participants'
    const onParticipants = () => {
        let data = getParticipants();
        socket.emit(constants.PARTICIPANTS, {
            usercount: data.length,
            data: data
        });
    };

    // When the client connects
    const onConnect = (username) => {

        // store the username in the socket session for the client
        socket.data.username = username;

        socket.emit(constants.USER_LOGIN, {
            socketId: socket.id,
            data: getParticipants()
        });

        // broadcast globally that the client has joined
        console.log('User %s has connected; socketId %s', socket.data.username, socket.id);
        socket.broadcast.emit(constants.USER_JOINED, {
            socketId: socket.id,
            usercount: io.sockets.sockets.size,
            username: socket.data.username
        });
    }

    // when the client disconnects
    const onDisconnect = () => {

        // broadcast globally that the client has disconnected
        console.log('User %s has disconnected; socketId %s', socket.data.username, socket.id);
        socket.broadcast.emit(constants.USER_LEFT, {
            socketId: socket.id,
            usercount: io.sockets.sockets.size,
            username: socket.data.username
        });
    }

    socket.on(constants.PARTICIPANTS, onParticipants);
    socket.on(constants.DISCONNECT, onDisconnect);
    socket.on(constants.USER_ADD, onConnect);
}