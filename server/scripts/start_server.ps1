#!/usr/bin/env pwsh
Set-Variable -Name "PORT" -Value 3000
$process = Start-Process -UseNewEnvironment -Wait -NoNewWindow pwsh -args '-Command', 'node.exe ../index.js' -Environment @{
    PORT = $PORT
    DEBUG = "socket.io*"
}
if ($process.ExitCode -eq 0) {
    exit 0
}
exit 1
