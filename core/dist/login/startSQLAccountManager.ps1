$OutputEncoding = New-Object -typename System.Text.UTF8Encoding
$a = (Get-Host).UI.RawUI
$a.WindowTitle = "L2J - SQL Account Manager"
java "-Djava.util.logging.config.file=console.ini" -cp "./../libs/*;EWLogin.jar" l2e.tools.accountmanager.SQLAccountManager 2> $null
if ($LASTEXITCODE -like 0)
{
    ""
    "Execution successful"
    ""
}
else
{
    ""
    "An error has occurred while running the L2J Account Manager!"
    ""
    "Possible reasons for this to happen:"
    ""
    "Missing .jar files or ../libs directory."
    "- MySQL server not running or incorrect MySQL settings:"
    "   check ./config/loginserver.ini"
    "- Wrong data types or values out of range were provided:"
    "   specify correct values for each required field"
    ""
}