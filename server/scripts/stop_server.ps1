Start-Process -UseNewEnvironment -Wait -NoNewWindow pwsh -args '-Command', 'taskkill /F /im node.exe'
