#!/usr/bin/env pwsh
Set-Variable -Name "PORT" -Value 3000
$cmdOutput = Get-NetTCPConnection | Where-Object Localport -eq $PORT | Select-Object Localport,@{'Name' = 'ProcessName';'Expression'={(Get-Process -Id $_.OwningProcess).Name}} | Out-String
if (! $cmdOutput.Equals("")) {
    Start-Process -UseNewEnvironment -Wait -NoNewWindow pwsh -args '-Command', 'taskkill /F /im node.exe'
} else {
    Write-Output "server not running"
}
