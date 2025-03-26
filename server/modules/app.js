const express = require("express");
const { Server } = require("socket.io");
const port = process.env.PORT || 3000;

const app = express();
app.use(express.static(__dirname.replace("modules", "public")));
const server = require("http").createServer(app);

server.listen(port, () => {
    console.log("Server listening at port %d", port);
});

const io = new Server(server, {
    transports: ["polling", "websocket"],
    path: "/socket.io"
});

io.on('connection', (socket) => {
    socket.join('default');
    require("./connection")(socket, io);
    require("./message")(socket, io);
    require("./user")(socket, io);
    require("./typing")(socket, io);
});

module.exports = io;
