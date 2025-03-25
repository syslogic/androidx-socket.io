module.exports = (io, socket) => {

    // when the client emits 'direct message'
    const onDirectMessage = (anotherSocketId, message) => {
        console.log('socketId %s wrote to socketId %s: %s', socket.id, anotherSocketId, message);
        socket.to(anotherSocketId).emit("direct message", socket.id, message);
    }

    // when the client emits 'new message'
    const onChatMessage = (data) => {
        console.log('User %s wrote: %s', socket.data.username, data);
        socket.broadcast.emit('chat message', {
            username: socket.data.username,
            message: data
        });
    }

    socket.on("direct message", onDirectMessage);
    socket.on("chat message", onChatMessage);
}
