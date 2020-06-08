/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://eternity-world.ru/>.
 */
package l2e;

import java.util.logging.Logger;

import l2e.protection.Protection;

public class EternityWorld
{
	private static final Logger _log = Logger.getLogger(EternityWorld.class.getName());
	
	public static final int _revision = 977;
	
	public static void info()
	{
		_log.info("-------------------------------------------------------------------------------");
		_log.info("                         Project Owner: LordWinter                             ");
		_log.info("                            Developers: Wing Chun                              ");
		_log.info("-------------------------------------------------------------------------------");
		_log.info("                           Client: High Five                                   ");
		_log.info("                         Revision: " + _revision + "                                        ");
		if (Protection.isProtectionOn())
		{
			_log.info("                       Protection: Guardian System Enabled                     ");
		}
		else
		{
			_log.info("                       Protection: Guardian System Disabled                    ");
		}
		_log.info("-------------------------------------------------------------------------------");
		_log.info("                  Copyright 2010-2014 Eternity-World Team                      ");
		_log.info("                          www.eternity-world.ru                                ");
		_log.info("-------------------------------------------------------------------------------");
	}
}