@echo off
START /max CMD /c "set PORT=3000 && set DEBUG=socket.io* && node.exe index.js"
