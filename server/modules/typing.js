module.exports = (io, socket) => {

    // when the client emits 'typing'
    const onStartTyping = () => {

        // broadcast globally that the client has started typing
        socket.broadcast.emit('typing', {
            socketId: socket.id,
            username: socket.data.username
        });
    }

    // when the client emits 'stop typing'
    const onStopTyping = () => {

        // broadcast globally that the client has stopped typing
        socket.broadcast.emit('stop typing', {
            socketId: socket.id,
            username: socket.data.username
        });
    }

    socket.on("typing", onStartTyping);
    socket.on("stop typing", onStopTyping);
}
