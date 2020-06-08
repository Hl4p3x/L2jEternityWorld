Copyright 2004-2011 L2J-DataPack team

This file is part of the L2J-DataPack.

L2J-DataPack comes with ABSOLUTELY NO WARRANTY. This is 
free software, and you are welcome to redistribute it under certain conditions.

L2J-DataPack is free software; you can redistribute it and/or modify it under 
the terms of the GNU General Public License as published by the Free Software 
Foundation; either version 3 of the License, or (at your option) any later 
version.

L2J-DataPack is distributed in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
L2J-DataPack; if not, write to the Free Software Foundation, Inc., 51 Franklin 
St, Fifth Floor, Boston, MA  02110-1301  USA


L2J-DataPack SVN Build:  

Project Website: http://www.l2jdp.com
Project Forum: http://www.l2jdp.com/forum
Wiki: http://trac.l2jdp.com/wiki
Download: Our wiki contain directives for you to get the latest DataPack 
revision either from nightly builds or via Subversion.

IRC: irc.freenode.net #l2j

L2J-DataPack is *NOT* L2J-Server. L2J-Server is *NOT* L2J-DataPack. Comments, questions, 
suggestions etc. should be directed to the appropriate forums.

Any given DataPack copy you get, is designed/optimized to work with an specific 
L2J build. Ensure your core and DataPack revisions match each other.

This readme assumes a basic understanding of MySQL commands and internals, SQL 
queries, or at least familiarity with a MySQL frontend. This readme will not 
teach you how to install MySQL nor will it teach you to use MySQL or any MySQL 
frontend. This readme is for the sole purpose of providing a brief overview of 
how to either install or upgrade the data in your database.


Installation:

All users: Copy all the datapack content to your gameserver directory/folder.
(for example C:\L2J\gameserver for Win users, /opt/l2j/gameserver for *nix)
You'd know if you are doing it right if you're being asked about overwriting
the data folder and/or its content, since L2J core includes a basic skeleton of
it. It's safe to answer 'yes' at this point.

For new L2J databases or existing databases where you want to delete character 
and account information: Create your loginserver and gameserver databases so they 
match the loginserver.properties and server.properties settings respectively (the 
default for both is 'l2jdb'.)

Method 1: run database_installer.bat for windows users, or database_installer.sh for
linux/unix users.
Method 2: Select your database and run all the batch scripts in the sql folder

For existing L2J databases where you want to keep character and account 
information: 

Method 1: Run database_installer.bat for windows users, or 
database_installer.sh for linux/unix users. Choose (u)pgrade when asked.
Method 2: Select your database and run all the batch scripts in the 
sql folder that correspond to tables in your database that are missing or you 
want to upgrade.


IMPORTANT: 	There may also be changes altering table structures, if you need  such
an update after some certain changeset, you should run the relevant SQL sequence from
/sql/updates/. Database_installer tool will provide a way for you to execute them all.

-the L2J-DataPack team