Start-Process -UseNewEnvironment -Wait -NoNewWindow pwsh -args '-Command', 'node.exe ../index.js' -Environment @{
    PORT = 3000
    DEBUG = "socket.io*"
}
