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
package l2e.gameserver.model.entity.events.phoenix;

public class Interface
{
	public static boolean areTeammates(Integer player, Integer target)
	{
		return (Boolean) invokeMethod("io.In", "areTeammates", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			player,
			target
		});
	}
	
	public static void bypass(Integer player, String command)
	{
		invokeMethod("io.In", "bypass", new Class<?>[]
		{
			Integer.class,
			String.class
		}, new Object[]
		{
			player,
			command
		});
	}
	
	public static boolean canAttack(Integer player, Integer target)
	{
		return (Boolean) invokeMethod("io.In", "canAttack", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			player,
			target
		});
	}
	
	public static boolean canTargetPlayer(Integer target, Integer self)
	{
		return (Boolean) invokeMethod("io.In", "canTargetPlayer", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			target,
			self
		});
	}
	
	public static boolean canUseSkill(Integer player, Integer skill)
	{
		return (Boolean) invokeMethod("io.In", "canUseSkill", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			player,
			skill
		});
	}
	
	public static boolean doAttack(Integer self, Integer target)
	{
		return (Boolean) invokeMethod("io.In", "doAttack", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			self,
			target
		});
	}
	
	public static void eventOnLogout(Integer player)
	{
		invokeMethod("io.In", "eventOnLogout", new Class<?>[]
		{
			Integer.class
		}, new Object[]
		{
			player
		});
	}
	
	public static boolean getBoolean(String propName, Integer player)
	{
		return (Boolean) (invokeMethod("io.In", "getBoolean", new Class<?>[]
		{
			String.class,
			Integer.class
		}, new Object[]
		{
			propName,
			player
		}));
	}
	
	private static Class<?> getClass(String name)
	{
		Class<?> c = null;
		try
		{
			c = Class.forName("l2e.gameserver.model.entity.events.phoenix." + name);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return c;
	}
	
	public static int getInt(String propName, Integer player)
	{
		return (Integer) (invokeMethod("io.In", "getInt", new Class<?>[]
		{
			String.class,
			Integer.class
		}, new Object[]
		{
			propName,
			player
		}));
	}
	
	private static Object invokeMethod(String className, String methodName)
	{
		try
		{
			return getClass(className).getMethod(methodName).invoke(getClass(className).getMethod("getInstance").invoke(null), (Object[]) null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private static Object invokeMethod(String className, String methodName, Class<?>[] paramTypes, Object[] args)
	{
		try
		{
			return getClass(className).getMethod(methodName, paramTypes).invoke(getClass(className).getMethod("getInstance").invoke(null), args);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean isParticipating(Integer player)
	{
		if (player != null)
		{
			return ((Boolean) (invokeMethod("io.In", "isParticipating", new Class<?>[]
			{
				Integer.class
			}, new Object[]
			{
				player
			})));
		}
		return false;
	}
	
	public static boolean isRegistered(Integer player)
	{
		return (Boolean) invokeMethod("io.In", "isRegistered", new Class<?>[]
		{
			Integer.class
		}, new Object[]
		{
			player
		});
	}
	
	public static boolean logout(Integer player)
	{
		return (Boolean) invokeMethod("io.In", "logout", new Class<?>[]
		{
			Integer.class
		}, new Object[]
		{
			player
		});
	}
	
	public static void onDie(Integer victim, Integer killer)
	{
		invokeMethod("io.In", "onDie", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			victim,
			killer
		});
	}
	
	public static void onHit(Integer actor, Integer target)
	{
		invokeMethod("io.In", "onHit", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			actor,
			target
		});
	}
	
	public static void onKill(Integer victim, Integer killer)
	{
		invokeMethod("io.In", "onKill", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			victim,
			killer
		});
	}
	
	public static void onLogout(Integer player)
	{
		invokeMethod("io.In", "onLogout", new Class<?>[]
		{
			Integer.class
		}, new Object[]
		{
			player
		});
	}
	
	public static void onSay(int type, Integer player, String text)
	{
		invokeMethod("io.In", "onSay", new Class<?>[]
		{
			int.class,
			Integer.class,
			String.class
		}, new Object[]
		{
			type,
			player,
			text
		});
	}
	
	public static boolean onTalkNpc(Integer npc, Integer player)
	{
		return (Boolean) invokeMethod("io.In", "onTalkNpc", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			npc,
			player
		});
	}
	
	public static boolean onUseItem(Integer player, Integer item, Integer objectId)
	{
		return (Boolean) invokeMethod("io.In", "onUseItem", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			player,
			item
		});
	}
	
	public static boolean onUseMagic(Integer player, Integer skill)
	{
		return (Boolean) invokeMethod("io.In", "onUseMagic", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			player,
			skill
		});
	}
	
	public static void showFirstHtml(Integer player, int obj)
	{
		invokeMethod("io.In", "showFirstHtml", new Class<?>[]
		{
			Integer.class,
			int.class
		}, new Object[]
		{
			player,
			obj
		});
	}
	
	public static void shutdown()
	{
		invokeMethod("io.In", "shutdown");
	}
	
	public static void start()
	{
		try
		{
			getClass("PhoenixEngine").getMethod("phoenixStart").invoke(null);
			if ((Boolean) (invokeMethod("io.In", "getBoolean", new Class<?>[]
			{
				String.class,
				Integer.class
			}, new Object[]
			{
				"eventBufferEnabled",
				0
			})))
			{
				getClass("function.Buffer").getMethod("getInstance").invoke(null);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean talkNpc(Integer player, Integer npc)
	{
		return (Boolean) invokeMethod("io.In", "talkNpc", new Class<?>[]
		{
			Integer.class,
			Integer.class
		}, new Object[]
		{
			player,
			npc
		});
	}
}