const express = require("express");
const port = process.env.PORT || 3000;
const path = require("path");
const app = express();

app.use(express.static(path.join(__dirname, "public")));
const server = require("http").createServer(app);

server.listen(port, () => {
    console.log("Server listening at port %d", port);
});

const opts = {transports: ["polling", "websocket"], path: "/socket.io"};
const io = require("socket.io")(server, opts);

io.on('connection', (socket) => {
    socket.join('default');
    require("./modules/connection")(io, socket);
    require("./modules/message")(io, socket);
    require("./modules/user")(io, socket);
    require("./modules/typing")(io, socket);
});
