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

io.on('connection', (socket) => {

    // console.log(socket.request.connection._peername);
    socket.remoteAddress = socket.request.connection._peername.address.replace('::ffff:','');
    socket.remotePort = socket.request.connection._peername.port;
    console.info('New client has connected from %s [:%s].', socket.remoteAddress, socket.remotePort);

    let addedUser = false;
    socket.join('default');

    // when the client emits 'new message', this listens and executes
    socket.on('new message', (data) => {
        console.log('User %s wrote: %s', socket.username, data);
        socket.broadcast.emit('new message', {
            username: socket.username,
            message: data
        });
    });

    socket.on("private message", (anotherSocketId, message) => {
        console.log('socketId %s wrote to socketId %s: %s', socket.id, anotherSocketId, message);
        socket.to(anotherSocketId).emit("private message", socket.id, message);
    });

    // when the client emits 'add user', this listens and executes
    socket.on('add user', (username) => {

        if (addedUser) {
            console.log('User %s was already added', socket.username);
            socket.emit('login', {
                usercount: numUsers,
                socketId: socket.id
            });
            return;
        }

        // store the username in the socket session for this client
        socket.username = username;
        addedUser = true;
        numUsers++;

        socket.emit('login', {
            usercount: numUsers,
            socketId: socket.id
        });

        // broadcast that a user has connected
        console.log('User %s has joined; socketId %s', socket.username, socket.id);
        socket.broadcast.emit('user joined', {
            username: socket.username,
            usercount: numUsers
        });
    });

    // when the client emits 'typing', broadcast it to others
    socket.on('typing', () => {
        socket.broadcast.emit('typing', {
            username: socket.username
        });
    });

    // when the client emits 'stop typing', broadcast it to others
    socket.on('stop typing', () => {
        socket.broadcast.emit('stop typing', {
            username: socket.username
        });
    });

    // when the user disconnects
    socket.on('disconnect', () => {
        if (addedUser) {

            addedUser = false;
            numUsers--;

            // echo globally that this client has left
            console.log('User %s has disconnected; socketId %s', socket.username, socket.id);
            socket.broadcast.emit('user left', {
                username: socket.username,
                usercount: numUsers
            });
        }
    });
});
