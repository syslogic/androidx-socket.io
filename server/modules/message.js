module.exports = (io, socket) => {

    // when the client emits 'new message'
    const onChatMessage = (data) => {
        console.log('User %s wrote: %s', socket.data.username, data);
        socket.broadcast.emit('new message', {
            username: socket.data.username,
            message: data
        });
    }

    // when the client emits 'direct message'
    const onDirectMessage = (anotherSocketId, message) => {
        console.log('socketId %s wrote to socketId %s: %s', socket.id, anotherSocketId, message);
        socket.to(anotherSocketId).emit("private message", socket.id, message);
    }

    socket.on("chat message", onChatMessage);
    socket.on("direct message", onDirectMessage);
}
