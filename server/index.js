const path = require('path');
const express = require('express');
const port = process.env.PORT || 3000;

const app = express();
app.use(express.static(path.join(__dirname, 'public')));
const server = require('http').createServer(app);

server.listen(port, () => {
    console.log('Server listening at port %d', port);
});

const io = require('socket.io')(server, {
    transports: ['polling', 'websocket'],
    path: '/socket.io'
});

let numUsers = 0;

const getParticipants = () => {
    let data = [];
    io.sockets.sockets.forEach(function(socket, socketId) {
        console.log(`ID: ${socketId} -> Name: ${socket.data.username}`);
        data.push({socketId: socketId, username: socket.data.username});
    });
    return data;
}

io.on('connection', (socket) => {

    // store the remote connection in the socket session for the client
    socket.remoteAddress = socket.request.connection._peername.address.replace('::ffff:','');
    socket.remotePort = socket.request.connection._peername.port;
    console.info('New client has connected from %s [:%s].', socket.remoteAddress, socket.remotePort);
    // io.sockets.manager.server.connections

    let addedUser = false;
    socket.join('default');

    // when the client emits 'participants'
    socket.on("participants", () => {
        let data = getParticipants();
        socket.emit('participants', {
            usercount: data.length,
            data: data
        });
    });

    // when the client emits 'new message'
    socket.on('new message', (data) => {
        console.log('User %s wrote: %s', socket.data.username, data);
        socket.broadcast.emit('new message', {
            username: socket.data.username,
            message: data
        });
    });

    // when the client emits 'private message'
    socket.on("private message", (anotherSocketId, message) => {
        console.log('socketId %s wrote to socketId %s: %s', socket.id, anotherSocketId, message);
        socket.to(anotherSocketId).emit("private message", socket.id, message);
    });

    // when the client emits 'add user'
    socket.on('add user', (username) => {

        if (addedUser) {
            console.log('User %s was already added, rejoining %s', socket.data.username, socket.id);
            socket.emit('login', {usercount: numUsers, socketId: socket.id, data: getParticipants()});
            return;
        }

        addedUser = true;
        numUsers++;

        // store the username in the socket session for the client
        socket.data.username = username;

        socket.emit('login', {
            usercount: numUsers,
            socketId: socket.id,
            data: getParticipants()
        });

        // broadcast globally that the client has joined
        console.log('User %s has connected; socketId %s', socket.data.username, socket.id);
        socket.broadcast.emit('user joined', {
            username: socket.data.username,
            usercount: numUsers
        });
    });

    // when the client emits 'typing'
    socket.on('typing', () => {

        // broadcast globally that the client has started typing
        socket.broadcast.emit('typing', {
            username: socket.data.username
        });
    });

    // when the client emits 'stop typing'
    socket.on('stop typing', () => {

        // broadcast globally that the client has stopped typing
        socket.broadcast.emit('stop typing', {
            username: socket.data.username
        });
    });

    // when the client disconnects
    socket.on('disconnect', () => {
        if (addedUser) {

            // broadcast globally that the client has disconnected
            console.log('User %s has disconnected; socketId %s', socket.data.username, socket.id);
            socket.broadcast.emit('user left', {
                username: socket.data.username,
                usercount: numUsers
            });
            addedUser = false;
            numUsers--;
        }
    });
});
