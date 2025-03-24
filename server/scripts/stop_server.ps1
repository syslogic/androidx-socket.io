#!/usr/bin/env pwsh
Set-Variable -Name "PORT" -Value 3000
$cmdOutput = Get-NetTCPConnection | Where-Object Localport -eq $PORT | Select-Object Localport,@{'Name' = 'ProcessName';'Expression'={(Get-Process -Id $_.OwningProcess).Name}} | Out-String
if (! $cmdOutput.Equals("")) {
    $process = Start-Process -UseNewEnvironment -Wait -NoNewWindow pwsh -args '-Command', 'taskkill /F /im node.exe'
    if ($process.ExitCode -eq 0) {
        exit 0
    }
} else {
    Write-Output "server not running"
}
exit 1
