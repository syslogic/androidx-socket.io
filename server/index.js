// Setup basic express server
var express = require('express');
var app = express();
var path = require('path');
var server = require('http').createServer(app);
var io = require('socket.io')(server);
var port = process.env.PORT || 3000;

server.listen(port, () => {
  console.log('Server listening at port %d', port);
  console.log('Close this window to stop it.');
});

// Routing
app.use(express.static(path.join(__dirname, 'public')));

// Chatroom

var numUsers = 0;

io.on('connection', (socket) => {

  var addedUser = false;

  // when the client emits 'new message', this listens and executes
  socket.on('new message', (data) => {

    // we tell the client to execute 'new message'
    console.log('%s wrote: %s', socket.username, data);
    socket.broadcast.emit('new message', {
      username: socket.username,
      message: data
    });
  });

  // when the client emits 'add user', this listens and executes
  socket.on('add user', (username) => {
    if (addedUser) return;

    // we store the username in the socket session for this client
    socket.username = username;
    numUsers++;
    addedUser = true;

    console.log('user %s logged in', socket.username);
    socket.emit('login', {
      numUsers: numUsers
    });

    // echo globally (all clients) that a person has connected
    console.log('user %s has joined', socket.username);
    socket.broadcast.emit('user joined', {
      username: socket.username,
      numUsers: numUsers
    });

  });

  // when the client emits 'typing', we broadcast it to others
  socket.on('typing', () => {
    console.log('user %s is typing', socket.username);
    socket.broadcast.emit('typing', {
      username: socket.username
    });
  });

  // when the client emits 'stop typing', we broadcast it to others
  socket.on('stop typing', () => {
    console.log('user %s stopped typing', socket.username);
    socket.broadcast.emit('stop typing', {
      username: socket.username
    });
  });

  // when the user disconnects.. perform this
  socket.on('disconnect', () => {
    if (addedUser) {
      numUsers--;

      // echo globally that this client has left
      console.log('user %s left', socket.username);
      socket.broadcast.emit('user left', {
        username: socket.username,
        numUsers: numUsers
      });
    }
  });
});
