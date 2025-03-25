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

    // when the client emits 'participants'
    const onParticipants = () => {
        let data = getParticipants();
        socket.emit('participants', {
            usercount: data.length,
            data: data
        });
    };

    // when the client connects
    const onConnect = (username) => {

        // store the username in the socket session for the client
        socket.data.username = username;

        socket.emit('login', {
            socketId: socket.id,
            data: getParticipants()
        });

        // broadcast globally that the client has joined
        console.log('User %s has connected; socketId %s', socket.data.username, socket.id);
        socket.broadcast.emit('user joined', {
            socketId: socket.id,
            usercount: io.sockets.sockets.size,
            username: socket.data.username
        });
    }

    // when the client disconnects
    const onDisconnect = () => {

        // broadcast globally that the client has disconnected
        console.log('User %s has disconnected; socketId %s', socket.data.username, socket.id);
        socket.broadcast.emit('user left', {
            socketId: socket.id,
            usercount: io.sockets.sockets.size,
            username: socket.data.username
        });
    }

    socket.on("participants", onParticipants);
    socket.on("disconnect", onDisconnect);
    socket.on("add user", onConnect);
}