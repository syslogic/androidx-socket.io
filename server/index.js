var path = require('path');
var express = require('express');
var port = process.env.PORT || 3000;

var app = express();
app.use(express.static(path.join(__dirname, 'public')));
var server = require('http').createServer(app);

// Close the server if listening doesn't fail
// server.once('listening', function() {
    // console.log('Closing port %d...', port);
    // server.close();
// });

server.listen(port, () => {
    console.log('Listening at port %d', port);
});

var io = require('socket.io')(server,  {
    transports: ['polling', 'websocket'],
    path: '/socket.io'
});

// Chatroom
var numUsers = 0;

io.on('connection', (socket) => {

    var addedUser = false;

    socket.join('default');

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

        if (addedUser) {
            console.log('user %s was already added', socket.username);
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
        console.log('user %s has joined; socketId %s', socket.username, socket.id);
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
            console.log('user %s has left; socketId %s', socket.username, socket.id);
            socket.broadcast.emit('user left', {
                username: socket.username,
                usercount: numUsers
            });
        }
    });
});
