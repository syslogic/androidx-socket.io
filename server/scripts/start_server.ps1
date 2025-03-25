#!/usr/bin/env pwsh
Set-Variable -Name "PORT" -Value 3000
Start-Process -UseNewEnvironment -Wait -NoNewWindow pwsh -args '-Command', 'node.exe ../index.js' -Environment @{
    PORT = $PORT
    DEBUG = "socket.io*"
}
