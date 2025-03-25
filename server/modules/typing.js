const constants = require("./constants");
module.exports = (io, socket) => {

    // When the client emits 'typing start'.
    const onTypingStart = () => {

        // broadcast globally that the client has started typing
        socket.broadcast.emit('typing', {
            socketId: socket.id,
            username: socket.data.username
        });
    }

    // When the client emits 'typing stop'.
    const onTypingStop = () => {

        // broadcast globally that the client has stopped typing
        socket.broadcast.emit('stop typing', {
            socketId: socket.id,
            username: socket.data.username
        });
    }

    socket.on(constants.TYPING_START, onTypingStart);
    socket.on(constants.TYPING_STOP, onTypingStop);
}
