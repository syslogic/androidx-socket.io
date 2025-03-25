import * as constants from "./constants";

module.exports = (io, socket) => {

    const onConnection = () => {
        // Store the remote connection in the socket session for the client
        socket.remoteAddress = socket.request.connection._peername.address.replace('::ffff:','');
        socket.remotePort = socket.request.connection._peername.port;
        console.info('New client has connected from %s [:%s].', socket.remoteAddress, socket.remotePort);
    }

    socket.on(constants.CONNECTION, onConnection);
}
