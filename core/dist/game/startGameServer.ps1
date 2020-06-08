$OutputEncoding = New-Object -typename System.Text.UTF8Encoding
$a = (Get-Host).UI.RawUI
$a.WindowTitle = "L2J - Game Server Console"

do
{
    switch ($LASTEXITCODE)
    {
        -1 { cls; "Starting L2J Game Server."; break; }
        2 { cls; "Restarting L2J Game Server."; break; }
    }
    ""
    # -------------------------------------
    # Default parameters for a basic server.
    java "-Djava.util.logging.manager=l2e.util.L2LogManager" -Xms1024m -Xmx1024m -cp "./../libs/*;EternityWorld.jar" l2e.gameserver.GameServer
    #
    # If you have a big server and lots of memory, you could experiment for example with
    # java -server -Xmx1536m -Xms1024m -Xmn512m -XX:PermSize=256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts
    # -------------------------------------
}
while ($LASTEXITCODE -like 2)

if ($LASTEXITCODE -like 1)
{
    "Server Terminated Abnormally";
}
else 
{
    "Server Terminated";
}