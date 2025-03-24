#!/usr/bin/env pwsh
Set-Variable -Name "PORT" -Value 3000
$cmdOutput = Get-NetTCPConnection | Where-Object Localport -eq $PORT | Select-Object Localport,@{'Name' = 'ProcessName';'Expression'={(Get-Process -Id $_.OwningProcess).Name}} | Out-String
if ($cmdOutput.Equals("")) {
    Write-Output "port $PORT is available"
    exit 0
} else {
    Write-Output "port $PORT is occupied"
    # Write-Output $cmdOutput
    exit 1
}
