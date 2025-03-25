
## Socket.IO Chat-Server

A simple chat-server built with the Socket.IO library.

### Features

- Multiple users can join a chat room by each entering a unique username on website load.
- A notification is sent to all users, when a user joins or leaves the chatroom.
- A notification is sent to all users, when a user starts or stops typing.
- Users can send chat messages to the chat room.

### New Features (work in progress)

- A listing of participants is being sent upon login.
- Direct messages from socket to socket are being supported.

### Linux Bash Scripts

- [`scripts/check_port.sh`](https://github.com/syslogic/androidx-socket.io/blob/master/server/scripts/check_port.sh)
- [`scripts/start_server.sh`](https://github.com/syslogic/androidx-socket.io/blob/master/server/scripts/start_server.sh)
- [`scripts/stop_server.sh`](https://github.com/syslogic/androidx-socket.io/blob/master/server/scripts/stop_server.sh)

### Windows Powershell 7 Scripts

- [`scripts/check_port.ps1`](https://github.com/syslogic/androidx-socket.io/blob/master/server/scripts/check_port.ps1)
- [`scripts/start_server.ps1`](https://github.com/syslogic/androidx-socket.io/blob/master/server/scripts/start_server.ps1)
- [`scripts/stop_server.ps1`](https://github.com/syslogic/androidx-socket.io/blob/master/server/scripts/stop_server.ps1)

### Environmental Variables

- `PORT` (default: `3000`) ~ Specify an alternate port.

### Connectivity

- The default URL is `http://localhost:3000`.
- Android connects by IP address or hostname.

### Authors

- [Grant Timmerman](https://github.com/grant) (initial)
- [Martin Zeitler](https://github.com/syslogic) (forked)
