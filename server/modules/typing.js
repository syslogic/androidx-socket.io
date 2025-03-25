const constants = require("./constants");
module.exports = (io, socket) => {

    // When the client emits 'typing'
    const onStartTyping = () => {

        // broadcast globally that the client has started typing
        socket.broadcast.emit('typing', {
            socketId: socket.id,
            username: socket.data.username
        });
    }

    // When the client emits 'stop typing'
    const onStopTyping = () => {

        // broadcast globally that the client has stopped typing
        socket.broadcast.emit('stop typing', {
            socketId: socket.id,
            username: socket.data.username
        });
    }

    socket.on(constants.START_TYPING, onStartTyping);
    socket.on(constants.STOP_TYPING, onStopTyping);
}
