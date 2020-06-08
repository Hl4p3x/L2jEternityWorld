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
package l2e.gameserver.network.clientpackets;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javolution.util.FastList;
import l2e.Config;
import l2e.gameserver.data.sql.CharNameHolder;
import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.data.sql.SkillHolder;
import l2e.gameserver.data.xml.CharTemplateParser;
import l2e.gameserver.data.xml.SkillTreesParser;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.L2ShortCut;
import l2e.gameserver.model.L2SkillLearn;
import l2e.gameserver.model.L2World;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.appearance.PcAppearance;
import l2e.gameserver.model.actor.instance.L2PcInstance;
import l2e.gameserver.model.actor.stat.PcStat;
import l2e.gameserver.model.actor.templates.L2PcTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.itemcontainer.PcInventory;
import l2e.gameserver.model.items.PcItemTemplate;
import l2e.gameserver.model.items.instance.L2ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.network.L2GameClient;
import l2e.gameserver.network.serverpackets.CharCreateFail;
import l2e.gameserver.network.serverpackets.CharCreateOk;
import l2e.gameserver.network.serverpackets.CharSelectionInfo;
import l2e.gameserver.scripting.scriptengine.events.PlayerEvent;
import l2e.gameserver.scripting.scriptengine.listeners.player.PlayerListener;
import l2e.gameserver.util.Util;

public final class CharacterCreate extends L2GameClientPacket
{
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	private static final List<PlayerListener> _listeners = new FastList<PlayerListener>().shared();
	
	private String _name;
	protected int _race;
	private byte _sex;
	private int _classId;
	protected int _int;
	protected int _str;
	protected int _con;
	protected int _men;
	protected int _dex;
	protected int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_name.length() < 1) || (_name.length() > 16))
		{
			if (Config.DEBUG)
			{
				_log.fine("Character Creation Failure: Character name " + _name + " is invalid. Message generated: Your title cannot exceed 16 characters in length. Please try again.");
			}
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS));
			return;
		}
		
		if (Config.FORBIDDEN_NAMES.length > 1)
		{
			for (String st : Config.FORBIDDEN_NAMES)
			{
				if (_name.toLowerCase().contains(st.toLowerCase()))
				{
					sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
					return;
				}
			}
		}
		
		if (!Util.isAlphaNumeric(_name) || !isValidName(_name))
		{
			if (Config.DEBUG)
			{
				_log.fine("Character Creation Failure: Character name " + _name + " is invalid. Message generated: Incorrect name. Please try again.");
			}
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
			return;
		}
		
		if ((_face > 2) || (_face < 0))
		{
			_log.warning("Character Creation Failure: Character face " + _face + " is invalid. Possible client hack. " + getClient());
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		if ((_hairStyle < 0) || ((_sex == 0) && (_hairStyle > 4)) || ((_sex != 0) && (_hairStyle > 6)))
		{
			_log.warning("Character Creation Failure: Character hair style " + _hairStyle + " is invalid. Possible client hack. " + getClient());
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		if ((_hairColor > 3) || (_hairColor < 0))
		{
			_log.warning("Character Creation Failure: Character hair color " + _hairColor + " is invalid. Possible client hack. " + getClient());
			
			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		L2PcInstance newChar = null;
		L2PcTemplate template = null;
		
		synchronized (CharNameHolder.getInstance())
		{
			if ((CharNameHolder.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT) && (Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0))
			{
				if (Config.DEBUG)
				{
					_log.fine("Max number of characters reached. Creation failed.");
				}
				
				sendPacket(new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS));
				return;
			}
			else if (CharNameHolder.getInstance().doesCharNameExist(_name))
			{
				if (Config.DEBUG)
				{
					_log.fine("Character Creation Failure: Message generated: You cannot create another character. Please delete the existing character and try again.");
				}
				
				sendPacket(new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS));
				return;
			}
			
			template = CharTemplateParser.getInstance().getTemplate(_classId);
			
			if ((template == null) || (ClassId.getClassId(_classId).level() > 0))
			{
				if (Config.DEBUG)
				{
					_log.fine("Character Creation Failure: " + _name + " classId: " + _classId + " Template: " + template + " Message generated: Your character creation has failed.");
				}
				
				sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}
			final PcAppearance app = new PcAppearance(_face, _hairColor, _hairStyle, _sex != 0);
			newChar = L2PcInstance.create(template, getClient().getAccountName(), _name, app);
		}
		
		newChar.setCurrentHp(newChar.getMaxHp());
		newChar.setCurrentMp(newChar.getMaxMp());
		
		sendPacket(new CharCreateOk());
		
		initNewChar(getClient(), newChar);
		
		LogRecord record = new LogRecord(Level.INFO, "Created new character");
		record.setParameters(new Object[]
		{
			newChar,
			this.getClient()
		});
		_logAccounting.log(record);
	}
	
	private boolean isValidName(String text)
	{
		boolean result = true;
		String test = text;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (PatternSyntaxException e)
		{
			_log.warning("ERROR : Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}
	
	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		if (Config.DEBUG)
		{
			_log.fine("Character init start");
		}
		
		L2World.getInstance().storeObject(newChar);
		
		if (Config.STARTING_ADENA > 0)
		{
			newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		}
		
		final L2PcTemplate template = newChar.getTemplate();
		
		if (Config.ALLOW_NEW_CHAR_CUSTOM_POSITION)
		{
			newChar.getPosition().setXYZInvisible(Config.NEW_CHAR_POSITION_X, Config.NEW_CHAR_POSITION_Y, Config.NEW_CHAR_POSITION_Z);
		}
		else
		{
			Location createLoc = template.getCreationPoint();
			newChar.setXYZInvisible(createLoc.getX(), createLoc.getY(), createLoc.getZ());
		}
		
		for (int[] startingItems : Config.CUSTOM_STARTER_ITEMS)
		{
			PcInventory inv = newChar.getInventory();
			if (ItemHolder.getInstance().createDummyItem(startingItems[0]).isStackable())
			{
				inv.addItem("Starter Items", startingItems[0], startingItems[1], newChar, null);
			}
			else
			{
				for (int i = 0; i < startingItems[1]; i++)
				{
					inv.addItem("Starter Items", startingItems[0], 1, newChar, null);
				}
			}
		}
		
		if (Config.ALLOW_NEW_CHARACTER_TITLE)
		{
			newChar.setTitle(Config.NEW_CHARACTER_TITLE);
		}
		else
		{
			newChar.setTitle("");
		}
		
		if (Config.NEW_CHAR_IS_NOBLE)
		{
			newChar.setNoble(true);
		}
		
		if (Config.ENABLE_VITALITY)
		{
			newChar.setVitalityPoints(Math.min(Config.STARTING_VITALITY_POINTS, PcStat.MAX_VITALITY_POINTS), true);
		}
		if (Config.STARTING_LEVEL > 1)
		{
			newChar.getStat().addLevel((byte) (Config.STARTING_LEVEL - 1));
		}
		if (Config.STARTING_SP > 0)
		{
			newChar.getStat().addSp(Config.STARTING_SP);
		}
		
		L2ShortCut shortcut;
		shortcut = new L2ShortCut(0, 0, 3, 2, 0, 1);
		newChar.registerShortCut(shortcut);
		shortcut = new L2ShortCut(3, 0, 3, 5, 0, 1);
		newChar.registerShortCut(shortcut);
		shortcut = new L2ShortCut(10, 0, 3, 0, 0, 1);
		newChar.registerShortCut(shortcut);
		
		if (template.hasInitialEquipment())
		{
			L2ItemInstance item;
			for (PcItemTemplate ie : template.getInitialEquipment())
			{
				item = newChar.getInventory().addItem("Init", ie.getId(), ie.getCount(), newChar, null);
				if (item == null)
				{
					_log.warning("Could not create item during char creation: itemId " + ie.getId() + ", amount " + ie.getCount() + ".");
					continue;
				}
				
				if (item.getId() == 5588)
				{
					shortcut = new L2ShortCut(11, 0, 1, item.getObjectId(), 0, 1);
					newChar.registerShortCut(shortcut);
				}
				
				if (item.isEquipable() && ie.isEquipped())
				{
					newChar.getInventory().equipItem(item);
				}
			}
		}
		
		for (L2SkillLearn skill : SkillTreesParser.getInstance().getAvailableSkills(newChar, newChar.getClassId(), false, true))
		{
			newChar.addSkill(SkillHolder.getInstance().getInfo(skill.getSkillId(), skill.getSkillLevel()), true);
			if ((skill.getSkillId() == 1001) || (skill.getSkillId() == 1177))
			{
				shortcut = new L2ShortCut(1, 0, 2, skill.getSkillId(), skill.getSkillLevel(), 1);
				newChar.registerShortCut(shortcut);
			}
			if (skill.getSkillId() == 1216)
			{
				shortcut = new L2ShortCut(10, 0, 2, skill.getSkillId(), skill.getSkillLevel(), 1);
				newChar.registerShortCut(shortcut);
			}
			if (Config.DEBUG)
			{
				_log.fine("Adding starter skill:" + skill.getSkillId() + " / " + skill.getSkillLevel());
			}
		}
		
		if (!Config.DISABLE_TUTORIAL)
		{
			startTutorialQuest(newChar);
		}
		
		PlayerEvent event = new PlayerEvent();
		event.setObjectId(newChar.getObjectId());
		event.setName(newChar.getName());
		event.setClient(client);
		firePlayerListener(event);
		
		newChar.setOnlineStatus(true, false);
		newChar.deleteMe();
		
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.setCharSelection(cl.getCharInfo());
		
		if (Config.DEBUG)
		{
			_log.fine("Character init end");
		}
	}
	
	public void startTutorialQuest(L2PcInstance player)
	{
		final QuestState qs = player.getQuestState("_255_Tutorial");
		Quest q = null;
		if (qs == null)
		{
			q = QuestManager.getInstance().getQuest("_255_Tutorial");
		}
		if (q != null)
		{
			q.newQuestState(player).setState(State.STARTED);
		}
	}
	
	private void firePlayerListener(PlayerEvent event)
	{
		for (PlayerListener listener : _listeners)
		{
			listener.onCharCreate(event);
		}
	}
	
	public static void addPlayerListener(PlayerListener listener)
	{
		if (!_listeners.contains(listener))
		{
			_listeners.add(listener);
		}
	}
	
	public static void removePlayerListener(PlayerListener listener)
	{
		_listeners.remove(listener);
	}
}