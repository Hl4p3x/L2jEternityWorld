$OutputEncoding = New-Object -typename System.Text.UTF8Encoding
$a = (Get-Host).UI.RawUI
$a.WindowTitle = "L2J - Register Game Server"
java "-Djava.util.logging.config.file=console.ini" -cp "./../libs/*;EWLogin.jar" l2e.tools.gsregistering.BaseGameServerRegister -c