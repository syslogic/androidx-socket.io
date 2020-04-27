var port = process.env.PORT || 3000;
var path = require('path');
var express = require('express');
var app = express();
app.use(express.static(path.join(__dirname, 'public')));
var server = require('http').createServer(app);
var io = require('socket.io')(server,  {
    transports: ['polling', 'websocket'],
    path: '/socket.io'
});

var socket = server.listen(port, () => {
    // console.log('Server listening at port %d', port);
});

// Chatroom
var numUsers = 0;

io.on('connection', (socket) => {

    var addedUser = false;

    // when the client emits 'new message', this listens and executes
    socket.on('new message', (data) => {
        console.log('user %s wrote: %s', socket.username, data);
        socket.broadcast.emit('new message', {
            username: socket.username,
            message: data
        });
    });

    // when the client emits 'add user', this listens and executes
    socket.on('add user', (username) => {
        if (addedUser) return;

        // store the username in the socket session for this client
        socket.username = username;
        addedUser = true;
        numUsers++;

        socket.emit('login', {
            numUsers: numUsers,
            socketId: socket.id
        });

        // echo globally that a user has connected
        // console.log('user %s has joined; socket %s', socket.username, socket.id);
        socket.broadcast.emit('user joined', {
            username: socket.username,
            numUsers: numUsers
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
            numUsers--;
            // echo globally that this client has left
            console.log('user %s has left', socket.username);
            socket.broadcast.emit('user left', {
                username: socket.username,
                numUsers: numUsers
            });
        }
    });
});
