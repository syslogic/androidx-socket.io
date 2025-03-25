const constants = require("./constants");

module.exports = (io, socket) => {

    // When the client emits 'direct message'
    const onDirectMessage = (anotherSocketId, message) => {
        console.log('socketId %s wrote to socketId %s: %s', socket.id, anotherSocketId, message);
        socket.to(anotherSocketId).emit(constants.DIRECT_MESSAGE, socket.id, message);
    }

    // When the client emits 'chat message'
    const onChatMessage = (data) => {
        console.log('User %s wrote: %s', socket.data.username, data);
        socket.broadcast.emit(constants.CHAT_MESSAGE, {
            username: socket.data.username,
            message: data
        });
    }

    socket.on(constants.DIRECT_MESSAGE, onDirectMessage);
    socket.on(constants.CHAT_MESSAGE, onChatMessage);
}
