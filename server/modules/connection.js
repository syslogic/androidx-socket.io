module.exports = (io, socket) => {

    // store the remote connection in the socket session for the client
    const onConnection = () => {
        socket.remoteAddress = socket.request.connection._peername.address.replace('::ffff:','');
        socket.remotePort = socket.request.connection._peername.port;
        console.info('New client has connected from %s [:%s].', socket.remoteAddress, socket.remotePort);
        // io.sockets.manager.server.connections
    }

    socket.on("connection", onConnection);
}
