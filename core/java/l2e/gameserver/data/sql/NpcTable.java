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
package l2e.gameserver.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.Config;
import l2e.L2DatabaseFactory;
import l2e.gameserver.data.xml.SkillLearnParser;
import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.L2DropData;
import l2e.gameserver.model.L2MinionData;
import l2e.gameserver.model.L2NpcAIData;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.actor.templates.L2NpcTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.stats.BaseStats;
import l2e.gameserver.model.stats.MoveType;

public class NpcTable extends DocumentParser
{
	private static final Logger _log = Logger.getLogger(NpcTable.class.getName());
	
	private static final Map<Integer, L2NpcTemplate> _npcs = new HashMap<>();
	
	private static final String SELECT_NPC_ALL = "SELECT * FROM npc ORDER BY id";
	private static final String SELECT_NPC_BY_ID = "SELECT * FROM npc WHERE id = ?";
	
	private static final String SELECT_SKILLS_ALL = "SELECT * FROM npcskills ORDER BY npcid";
	private static final String SELECT_SKILLS_BY_ID = "SELECT * FROM npcskills WHERE npcid = ?";
	
	private static final String SELECT_DROPLIST_ALL = "SELECT * FROM droplist ORDER BY mobId, category, chance";
	private static final String SELECT_DROPLIST_BY_ID = "SELECT * FROM droplist WHERE mobId = ? ORDER BY mobId, category, chance";
	
	private static final String SELECT_NPC_AI_ALL = "SELECT * FROM npcaidata ORDER BY npcId";
	private static final String SELECT_NPC_AI_BY_ID = "SELECT * FROM npcaidata WHERE npcId = ?";
	
	private static final String SELECT_NPC_ELEMENTALS_ALL = "SELECT * FROM npc_elementals ORDER BY npc_id";
	private static final String SELECT_NPC_ELEMENTALS_BY_ID = "SELECT * FROM npc_elementals WHERE npc_id = ?";
	
	private static final String SELECT_MINION_ALL = "SELECT * FROM minions ORDER BY boss_id";
	private static final String SELECT_MINION_BY_ID = "SELECT * FROM minions WHERE boss_id = ?";
	
	private static final String CUSTOM_SELECT_NPC_ALL = "SELECT * FROM custom_npc ORDER BY id";
	private static final String CUSTOM_SELECT_NPC_BY_ID = "SELECT * FROM custom_npc WHERE id = ?";
	
	private static final String CUSTOM_SELECT_SKILLS_ALL = "SELECT * FROM custom_npcskills ORDER BY npcid";
	private static final String CUSTOM_SELECT_SKILLS_BY_ID = "SELECT * FROM custom_npcskills WHERE npcid = ?";
	
	private static final String CUSTOM_SELECT_DROPLIST_ALL = "SELECT * FROM custom_droplist ORDER BY mobId, category, chance";
	private static final String CUSTOM_SELECT_DROPLIST_BY_ID = "SELECT * FROM custom_droplist WHERE mobId = ? ORDER BY mobId, category, chance";
	
	private static final String CUSTOM_SELECT_NPC_AI_ALL = "SELECT * FROM custom_npcaidata ORDER BY npcId";
	private static final String CUSTOM_SELECT_NPC_AI_BY_ID = "SELECT * FROM custom_npcaidata WHERE npcId = ?";
	
	private static final String CUSTOM_SELECT_NPC_ELEMENTALS_ALL = "SELECT * FROM custom_npc_elementals ORDER BY npc_id";
	private static final String CUSTOM_SELECT_NPC_ELEMENTALS_BY_ID = "SELECT * FROM custom_npc_elementals WHERE npc_id = ?";
	
	protected NpcTable()
	{
		_npcs.clear();
		restoreNpcData();
		load();
	}
	
	@Override
	public synchronized void load()
	{
		parseDirectory("data/stats/npcs");
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		StatsSet set;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equals(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("npc".equals(d.getNodeName()))
					{
						attrs = d.getAttributes();
						int id = parseInt(attrs, "id");
						if (_npcs.containsKey(id))
						{
							L2NpcTemplate template = _npcs.get(id);
							set = new StatsSet();
							for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
							{
								if ((c.getNodeName() == null) || c.getNodeName().startsWith("#"))
								{
									continue;
								}
								attrs = c.getAttributes();
								switch (c.getNodeName())
								{
									case "base_attack":
									{
										final String type = parseString(attrs, "type");
										final int range = parseInt(attrs, "range");
										final L2WeaponType weaponType = L2WeaponType.findByName(type);
										template.setBaseAttackType(weaponType);
										template.setBaseAttackRange(range);
										break;
									}
									case "base_attribute":
									{
										for (Node b = c.getFirstChild(); b != null; b = b.getNextSibling())
										{
											attrs = b.getAttributes();
											if ("attack".equals(b.getNodeName()))
											{
												template.setBaseFire(parseInt(attrs, "fire"));
												template.setBaseWater(parseInt(attrs, "water"));
												template.setBaseEarth(parseInt(attrs, "earth"));
												template.setBaseWind(parseInt(attrs, "wind"));
												template.setBaseHoly(parseInt(attrs, "holy"));
												template.setBaseDark(parseInt(attrs, "dark"));
											}
											else if ("defend".equals(b.getNodeName()))
											{
												template.setBaseFireRes(parseInt(attrs, "fire"));
												template.setBaseWaterRes(parseInt(attrs, "water"));
												template.setBaseEarthRes(parseInt(attrs, "earth"));
												template.setBaseWindRes(parseInt(attrs, "wind"));
												template.setBaseHolyRes(parseInt(attrs, "holy"));
												template.setBaseDarkRes(parseInt(attrs, "dark"));
												template.setBaseElementRes(parseInt(attrs, "unknown"));
											}
										}
										break;
									}
									case "npc_ai":
									{
										for (Node b = c.getFirstChild(); b != null; b = b.getNextSibling())
										{
											attrs = b.getAttributes();
											if ("ai_param".equals(b.getNodeName()))
											{
												set.set(parseString(attrs, "name"), parseString(attrs, "val"));
											}
										}
										template.setParameters(set);
										break;
									}
									case "speed":
									{
										for (Node b = c.getFirstChild(); b != null; b = b.getNextSibling())
										{
											attrs = b.getAttributes();
											if ("run".equals(b.getNodeName()))
											{
												template.setBaseMoveSpeed(MoveType.RUN, parseFloat(attrs, "ground"));
												template.setBaseMoveSpeed(MoveType.FAST_SWIM, parseFloat(attrs, "underWater"));
												template.setBaseMoveSpeed(MoveType.FAST_FLY, parseFloat(attrs, "flying"));
											}
											else if ("walk".equals(b.getNodeName()))
											{
												template.setBaseMoveSpeed(MoveType.WALK, parseFloat(attrs, "ground"));
												template.setBaseMoveSpeed(MoveType.SLOW_SWIM, parseFloat(attrs, "underWater"));
												template.setBaseMoveSpeed(MoveType.SLOW_FLY, parseFloat(attrs, "flying"));
											}
										}
										break;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void restoreNpcData()
	{
		loadNpcs(0);
		loadNpcsSkills(0);
		loadNpcsDrop(0);
		loadNpcsSkillLearn(0);
		loadMinions(0);
		loadNpcsAI(0);
		loadNpcsElement(0);
	}
	
	private void fillNpcTable(ResultSet NpcData) throws Exception
	{
		StatsSet npcDat = new StatsSet();
		int id = NpcData.getInt("id");
		int idTemp = NpcData.getInt("idTemplate");
		
		assert idTemp < 1000000;
		
		npcDat.set("npcId", id);
		npcDat.set("idTemplate", idTemp);
		int level = NpcData.getInt("level");
		npcDat.set("level", level);
		npcDat.set("client_class", NpcData.getString("class"));
		
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseCritRate", NpcData.getInt("critical"));
		
		npcDat.set("name", NpcData.getString("name"));
		npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
		npcDat.set("title", NpcData.getString("title"));
		npcDat.set("serverSideTitle", NpcData.getBoolean("serverSideTitle"));
		npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
		npcDat.set("collision_height", NpcData.getDouble("collision_height"));
		npcDat.set("sex", NpcData.getString("sex"));
		npcDat.set("type", NpcData.getString("type"));
		npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
		npcDat.set("rewardExp", NpcData.getInt("exp"));
		npcDat.set("rewardSp", NpcData.getInt("sp"));
		npcDat.set("basePAtkSpd", NpcData.getInt("atkspd"));
		npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
		npcDat.set("rhand", NpcData.getInt("rhand"));
		npcDat.set("lhand", NpcData.getInt("lhand"));
		npcDat.set("enchant", NpcData.getInt("enchant"));
		npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
		npcDat.set("baseRunSpd", NpcData.getInt("runspd"));
		
		npcDat.safeSet("baseSTR", NpcData.getInt("str"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		npcDat.safeSet("baseCON", NpcData.getInt("con"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		npcDat.safeSet("baseDEX", NpcData.getInt("dex"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		npcDat.safeSet("baseINT", NpcData.getInt("int"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		npcDat.safeSet("baseWIT", NpcData.getInt("wit"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		npcDat.safeSet("baseMEN", NpcData.getInt("men"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
		
		npcDat.set("baseHpMax", NpcData.getDouble("hp"));
		npcDat.set("baseCpMax", 0);
		npcDat.set("baseMpMax", NpcData.getDouble("mp"));
		npcDat.set("baseHpReg", NpcData.getFloat("hpreg") > 0 ? NpcData.getFloat("hpreg") : 1.5 + ((level - 1) / 10.0));
		npcDat.set("baseMpReg", NpcData.getFloat("mpreg") > 0 ? NpcData.getFloat("mpreg") : 0.9 + (0.3 * ((level - 1) / 10.0)));
		npcDat.set("basePAtk", NpcData.getInt("patk"));
		npcDat.set("basePDef", NpcData.getInt("pdef"));
		npcDat.set("baseMAtk", NpcData.getInt("matk"));
		npcDat.set("baseMDef", NpcData.getInt("mdef"));
		
		npcDat.set("dropHerbGroup", NpcData.getInt("dropHerbGroup"));
		
		npcDat.set("baseFireRes", 20);
		npcDat.set("baseWindRes", 20);
		npcDat.set("baseWaterRes", 20);
		npcDat.set("baseEarthRes", 20);
		npcDat.set("baseHolyRes", 20);
		npcDat.set("baseDarkRes", 20);
		
		final L2NpcTemplate template = getTemplate(id);
		if (template == null)
		{
			_npcs.put(id, new L2NpcTemplate(npcDat));
		}
		else
		{
			template.set(npcDat);
		}
	}
	
	public void reloadNpc(int id, boolean base, boolean ai, boolean element, boolean skills, boolean drops, boolean minions)
	{
		try
		{
			if (base)
			{
				loadNpcs(id);
			}
			if (ai)
			{
				loadNpcsAI(id);
			}
			if (element)
			{
				loadNpcsElement(id);
			}
			if (skills)
			{
				loadNpcsSkills(id);
				loadNpcsSkillLearn(id);
			}
			if (drops)
			{
				loadNpcsDrop(id);
			}
			if (minions)
			{
				loadMinions(id);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Could not reload data for NPC " + id + ": " + e.getMessage(), e);
		}
	}
	
	public void reloadAllNpc()
	{
		restoreNpcData();
	}
	
	public void saveNpc(StatsSet npc)
	{
		final int npcId = npc.getInteger("npcId");
		
		final StringBuilder npcAttributes = new StringBuilder();
		final ArrayList<Object> npcAttributeValues = new ArrayList<>();
		
		final StringBuilder npcaidataAttributes = new StringBuilder();
		final ArrayList<Object> npcaidataAttributeValues = new ArrayList<>();
		
		final StringBuilder npcElementAttributes = new StringBuilder();
		final ArrayList<Object> npcElementAttributeValues = new ArrayList<>();
		
		for (Entry<String, Object> entry : npc.getSet().entrySet())
		{
			switch (entry.getKey())
			{
				case "npcId":
					break;
				case "serverSideName":
				case "serverSideTitle":
				case "sex":
				case "enchant":
				case "level":
				case "str":
				case "con":
				case "dex":
				case "int":
				case "wit":
				case "men":
				case "critical":
				case "dropHerbGroup":
				case "atkspd":
				case "matkspd":
				case "attackrange":
				case "rhand":
				case "lhand":
				case "idTemplate":
				case "exp":
				case "sp":
				case "collision_radius":
				case "collision_height":
				case "walkspd":
				case "runspd":
				case "patk":
				case "pdef":
				case "matk":
				case "mdef":
				case "hp":
				case "mp":
				case "hpreg":
				case "mpreg":
				case "type":
				case "title":
				case "name":
				{
					appendEntry(npcAttributes, entry.getKey());
					npcAttributeValues.add(entry.getValue());
					break;
				}
				case "canMove":
				case "targetable":
				case "showName":
				case "isChaos":
				case "dodge":
				case "minSkillChance":
				case "maxSkillChance":
				case "minRangeChance":
				case "maxRangeChance":
				case "ssChance":
				case "spsChance":
				case "aggro":
				case "clanRange":
				case "enemyRange":
				case "primarySkillId":
				case "minRangeSkill":
				case "maxRangeSkill":
				case "soulShot":
				case "spiritShot":
				case "clan":
				case "enemyClan":
				case "aiType":
				{
					appendEntry(npcaidataAttributes, entry.getKey());
					npcaidataAttributeValues.add(entry.getValue());
					break;
				}
				case "elemAtkType":
				case "elemAtkValue":
				case "fireDefValue":
				case "waterDefValue":
				case "windDefValue":
				case "earthDefValue":
				case "holyDefValue":
				case "darkDefValue":
				{
					appendEntry(npcElementAttributes, entry.getKey());
					npcElementAttributeValues.add(entry.getValue());
					break;
				}
				default:
				{
					_log.warning("Unknown stat " + entry.getKey() + " can't set.");
					return;
				}
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int updated = 0;
			if (Config.CUSTOM_NPC_TABLE)
			{
				updated += performUpdate(npcAttributes, "custom_npc", "id", npcAttributeValues, npcId, con);
				updated += performUpdate(npcaidataAttributes, "custom_npcaidata", "npcId", npcaidataAttributeValues, npcId, con);
				updated += performUpdate(npcElementAttributes, "custom_npc_elementals", "npc_id", npcElementAttributeValues, npcId, con);
			}
			
			if (updated == 0)
			{
				updated += performUpdate(npcAttributes, "npc", "id", npcAttributeValues, npcId, con);
				updated += performUpdate(npcaidataAttributes, "npcaidata", "npcId", npcaidataAttributeValues, npcId, con);
				updated += performUpdate(npcElementAttributes, "npc_elementals", "npc_id", npcElementAttributeValues, npcId, con);
			}
			
			if (updated > 0)
			{
				reloadNpc(npcId, !npcAttributeValues.isEmpty(), !npcaidataAttributeValues.isEmpty(), !npcElementAttributeValues.isEmpty(), false, false, false);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Could not store new NPC data in database: " + e.getMessage(), e);
		}
	}
	
	private final void appendEntry(StringBuilder sb, String attribute)
	{
		if (sb.length() > 0)
		{
			sb.append(", ");
		}
		sb.append("`");
		sb.append(attribute);
		sb.append("` = ?");
	}
	
	private final int performUpdate(StringBuilder sb, String table, String key, Collection<Object> values, int npcId, Connection con) throws SQLException
	{
		int updated = 0;
		if ((sb != null) && !sb.toString().isEmpty())
		{
			final StringBuilder sbQuery = new StringBuilder(sb.length() + 28);
			sbQuery.append("UPDATE ");
			sbQuery.append(table);
			sbQuery.append(" SET ");
			sbQuery.append(sb.toString());
			sbQuery.append(" WHERE ");
			sbQuery.append(key);
			sbQuery.append(" = ?");
			try (PreparedStatement ps = con.prepareStatement(sbQuery.toString()))
			{
				int i = 1;
				for (Object value : values)
				{
					ps.setObject(i, value);
					i++;
				}
				ps.setInt(i, npcId);
				updated = ps.executeUpdate();
			}
		}
		return updated;
	}
	
	public L2NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}
	
	public L2NpcTemplate getTemplateByName(String name)
	{
		for (L2NpcTemplate npcTemplate : _npcs.values())
		{
			if (npcTemplate.getName().equalsIgnoreCase(name))
			{
				return npcTemplate;
			}
		}
		return null;
	}
	
	public List<L2NpcTemplate> getAllOfLevel(int... lvls)
	{
		final List<L2NpcTemplate> list = new ArrayList<>();
		for (int lvl : lvls)
		{
			for (L2NpcTemplate t : _npcs.values())
			{
				if (t.getLevel() == lvl)
				{
					list.add(t);
				}
			}
		}
		return list;
	}
	
	public List<L2NpcTemplate> getAllMonstersOfLevel(int... lvls)
	{
		final List<L2NpcTemplate> list = new ArrayList<>();
		for (int lvl : lvls)
		{
			for (L2NpcTemplate t : _npcs.values())
			{
				if ((t.getLevel() == lvl) && t.isType("L2Monster"))
				{
					list.add(t);
				}
			}
		}
		return list;
	}
	
	public List<L2NpcTemplate> getAllNpcStartingWith(String... letters)
	{
		final List<L2NpcTemplate> list = new ArrayList<>();
		for (String letter : letters)
		{
			for (L2NpcTemplate t : _npcs.values())
			{
				if (t.getName().startsWith(letter) && t.isType("L2Npc"))
				{
					list.add(t);
				}
			}
		}
		return list;
	}
	
	public List<L2NpcTemplate> getAllNpcOfClassType(String... classTypes)
	{
		final List<L2NpcTemplate> list = new ArrayList<>();
		for (String classType : classTypes)
		{
			for (L2NpcTemplate t : _npcs.values())
			{
				if (t.isType(classType))
				{
					list.add(t);
				}
			}
		}
		return list;
	}
	
	public void loadNpcs(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int count = loadNpcs(con, id, false);
			int ccount = 0;
			if (Config.CUSTOM_NPC_TABLE)
			{
				ccount = loadNpcs(con, id, true);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " (Custom: " + ccount + ") NPC template(s).");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
	}
	
	public int loadNpcs(Connection con, int id, boolean isCustom)
	{
		int count = 0;
		try
		{
			final String query = isCustom ? ((id > 0) ? CUSTOM_SELECT_NPC_BY_ID : CUSTOM_SELECT_NPC_ALL) : ((id > 0) ? SELECT_NPC_BY_ID : SELECT_NPC_ALL);
			try (PreparedStatement ps = con.prepareStatement(query))
			{
				if (id > 0)
				{
					ps.setInt(1, id);
				}
				
				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next())
					{
						fillNpcTable(rs);
						count++;
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error creating NPC table.", e);
		}
		return count;
	}
	
	public void loadNpcsSkills(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int count = loadNpcsSkills(con, id, false);
			int ccount = 0;
			if (Config.CUSTOM_NPC_SKILLS_TABLE)
			{
				ccount = loadNpcsSkills(con, id, true);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " (Custom: " + ccount + ") NPC skills.");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
	}
	
	private int loadNpcsSkills(Connection con, int id, boolean isCustom)
	{
		int count = 0;
		final String query = isCustom ? ((id > 0) ? CUSTOM_SELECT_SKILLS_BY_ID : CUSTOM_SELECT_SKILLS_ALL) : ((id > 0) ? SELECT_SKILLS_BY_ID : SELECT_SKILLS_ALL);
		try (PreparedStatement ps = con.prepareStatement(query))
		{
			if (id > 0)
			{
				ps.setInt(1, id);
			}
			
			try (ResultSet rs = ps.executeQuery())
			{
				L2NpcTemplate npcDat = null;
				L2Skill npcSkill = null;
				
				while (rs.next())
				{
					int mobId = rs.getInt("npcid");
					npcDat = _npcs.get(mobId);
					
					if (npcDat == null)
					{
						_log.warning(getClass().getSimpleName() + ": Skill data for undefined NPC. npcId: " + mobId);
						continue;
					}
					
					int skillId = rs.getInt("skillid");
					int level = rs.getInt("level");
					
					if (skillId == L2Skill.SKILL_NPC_RACE)
					{
						npcDat.setRace(level);
						continue;
					}
					
					npcSkill = SkillHolder.getInstance().getInfo(skillId, level);
					if (npcSkill == null)
					{
						continue;
					}
					count++;
					npcDat.addSkill(npcSkill);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error reading NPC skills table.", e);
		}
		return count;
	}
	
	public void loadNpcsDrop(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int count = loadNpcsDrop(con, id, false);
			int ccount = 0;
			if (Config.CUSTOM_DROPLIST_TABLE)
			{
				ccount = loadNpcsDrop(con, id, true);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " (Custom: " + ccount + ") drops.");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
	}
	
	public void loadNpcsSkillLearn(int id)
	{
		if (id > 0)
		{
			final List<ClassId> teachInfo = SkillLearnParser.getInstance().getSkillLearnData(id);
			final L2NpcTemplate template = _npcs.get(id);
			if ((teachInfo != null) && (template != null))
			{
				template.addTeachInfo(teachInfo);
			}
		}
		else
		{
			for (L2NpcTemplate template : _npcs.values())
			{
				final List<ClassId> teachInfo = SkillLearnParser.getInstance().getSkillLearnData(template.getId());
				if (teachInfo != null)
				{
					template.addTeachInfo(teachInfo);
				}
			}
		}
	}
	
	public int loadNpcsDrop(Connection con, int id, boolean isCustom)
	{
		int count = 0;
		final String query = isCustom ? ((id > 0) ? CUSTOM_SELECT_DROPLIST_BY_ID : CUSTOM_SELECT_DROPLIST_ALL) : ((id > 0) ? SELECT_DROPLIST_BY_ID : SELECT_DROPLIST_ALL);
		try (PreparedStatement ps = con.prepareStatement(query))
		{
			if (id > 0)
			{
				ps.setInt(1, id);
			}
			
			try (ResultSet rs = ps.executeQuery())
			{
				L2DropData dropDat = null;
				L2NpcTemplate npcDat = null;
				while (rs.next())
				{
					int mobId = rs.getInt("mobId");
					npcDat = _npcs.get(mobId);
					if (npcDat == null)
					{
						_log.warning(getClass().getSimpleName() + ": Drop data for undefined NPC. npcId: " + mobId);
						continue;
					}
					dropDat = new L2DropData();
					
					dropDat.setItemId(rs.getInt("itemId"));
					dropDat.setMinDrop(rs.getInt("min"));
					dropDat.setMaxDrop(rs.getInt("max"));
					dropDat.setChance(rs.getInt("chance"));
					
					int category = rs.getInt("category");
					if (ItemHolder.getInstance().getTemplate(dropDat.getItemId()) == null)
					{
						_log.warning(getClass().getSimpleName() + ": Drop data for undefined item template! NpcId: " + mobId + " itemId: " + dropDat.getItemId());
						continue;
					}
					count++;
					npcDat.addDropData(dropDat, category);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error reading NPC dropdata. ", e);
		}
		return count;
	}
	
	public void loadMinions(int id)
	{
		final String query = (id > 0) ? SELECT_MINION_BY_ID : SELECT_MINION_ALL;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			if (id > 0)
			{
				statement.setInt(1, id);
			}
			
			int count = 0;
			try (ResultSet rset = statement.executeQuery())
			{
				L2MinionData minionDat = null;
				L2NpcTemplate npcDat = null;
				
				int raidId;
				while (rset.next())
				{
					raidId = rset.getInt("boss_id");
					npcDat = _npcs.get(raidId);
					if (npcDat == null)
					{
						_log.warning(getClass().getSimpleName() + ": Minion references undefined boss NPC. Boss NpcId: " + raidId);
						continue;
					}
					
					minionDat = new L2MinionData();
					minionDat.setMinionId(rset.getInt("minion_id"));
					minionDat.setAmountMin(rset.getInt("amount_min"));
					minionDat.setAmountMax(rset.getInt("amount_max"));
					npcDat.addRaidData(minionDat);
					count++;
				}
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " Minions.");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error loading minion data.", e);
		}
	}
	
	public void loadNpcsAI(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int count = loadNpcAi(con, id, false);
			int ccount = 0;
			if (Config.CUSTOM_NPC_TABLE)
			{
				ccount = loadNpcAi(con, id, true);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " (Custom: " + ccount + ") AI Data.");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
	}
	
	private int loadNpcAi(Connection con, int id, boolean isCustom)
	{
		int count = 0;
		final String query = isCustom ? ((id > 0) ? CUSTOM_SELECT_NPC_AI_BY_ID : CUSTOM_SELECT_NPC_AI_ALL) : ((id > 0) ? SELECT_NPC_AI_BY_ID : SELECT_NPC_AI_ALL);
		try (PreparedStatement ps = con.prepareStatement(query))
		{
			if (id > 0)
			{
				ps.setInt(1, id);
			}
			
			try (ResultSet rs = ps.executeQuery())
			{
				L2NpcAIData npcAIDat = null;
				L2NpcTemplate npcDat = null;
				
				int npcId;
				while (rs.next())
				{
					npcId = rs.getInt("npcId");
					npcDat = _npcs.get(npcId);
					if (npcDat == null)
					{
						_log.severe(getClass().getSimpleName() + ": AI Data Error with id : " + npcId);
						continue;
					}
					
					npcAIDat = new L2NpcAIData();
					npcAIDat.setPrimarySkillId(rs.getInt("primarySkillId"));
					npcAIDat.setMinSkillChance(rs.getInt("minSkillChance"));
					npcAIDat.setMaxSkillChance(rs.getInt("maxSkillChance"));
					npcAIDat.setAggro(rs.getInt("aggro"));
					npcAIDat.setCanMove(rs.getInt("canMove"));
					npcAIDat.setShowName(rs.getInt("showName") == 1);
					npcAIDat.setTargetable(rs.getInt("targetable") == 1);
					npcAIDat.setSoulShot(rs.getInt("soulshot"));
					npcAIDat.setSpiritShot(rs.getInt("spiritshot"));
					npcAIDat.setSoulShotChance(rs.getInt("ssChance"));
					npcAIDat.setSpiritShotChance(rs.getInt("spsChance"));
					npcAIDat.setIsChaos(rs.getInt("isChaos"));
					npcAIDat.setShortRangeSkill(rs.getInt("minRangeSkill"));
					npcAIDat.setShortRangeChance(rs.getInt("minRangeChance"));
					npcAIDat.setLongRangeSkill(rs.getInt("maxRangeSkill"));
					npcAIDat.setLongRangeChance(rs.getInt("maxRangeChance"));
					npcAIDat.setClan(rs.getString("clan"));
					npcAIDat.setClanRange(rs.getInt("clanRange"));
					npcAIDat.setEnemyClan(rs.getString("enemyClan"));
					npcAIDat.setEnemyRange(rs.getInt("enemyRange"));
					npcAIDat.setDodge(rs.getInt("dodge"));
					npcAIDat.setAi(rs.getString("aiType"));
					
					npcDat.setAIData(npcAIDat);
					count++;
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
		return count;
	}
	
	public void loadNpcsElement(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int count = loadNpcsElement(con, id, false);
			int ccount = 0;
			if (Config.CUSTOM_NPC_TABLE)
			{
				ccount = loadNpcsElement(con, id, true);
			}
			_log.info(getClass().getSimpleName() + ": Loaded " + count + " (Custom: " + ccount + ") Elementals Data.");
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error reading NPC AI Data: " + e.getMessage(), e);
		}
	}
	
	private int loadNpcsElement(Connection con, int id, boolean isCustom)
	{
		int count = 0;
		final String query = isCustom ? ((id > 0) ? CUSTOM_SELECT_NPC_ELEMENTALS_BY_ID : CUSTOM_SELECT_NPC_ELEMENTALS_ALL) : ((id > 0) ? SELECT_NPC_ELEMENTALS_BY_ID : SELECT_NPC_ELEMENTALS_ALL);
		try (PreparedStatement ps = con.prepareStatement(query))
		{
			if (id > 0)
			{
				ps.setInt(1, id);
			}
			
			try (ResultSet rset = ps.executeQuery())
			{
				L2NpcTemplate npcDat = null;
				int npcId;
				while (rset.next())
				{
					npcId = rset.getInt("npc_id");
					npcDat = _npcs.get(npcId);
					if (npcDat == null)
					{
						_log.severe(getClass().getSimpleName() + ": Elementals Error with id : " + npcId);
						continue;
					}
					switch (rset.getByte("elemAtkType"))
					{
						case Elementals.FIRE:
							npcDat.setBaseFire(rset.getInt("elemAtkValue"));
							break;
						case Elementals.WATER:
							npcDat.setBaseWater(rset.getInt("elemAtkValue"));
							break;
						case Elementals.EARTH:
							npcDat.setBaseEarth(rset.getInt("elemAtkValue"));
							break;
						case Elementals.WIND:
							npcDat.setBaseWind(rset.getInt("elemAtkValue"));
							break;
						case Elementals.HOLY:
							npcDat.setBaseHoly(rset.getInt("elemAtkValue"));
							break;
						case Elementals.DARK:
							npcDat.setBaseDark(rset.getInt("elemAtkValue"));
							break;
						default:
							_log.severe(getClass().getSimpleName() + ": Elementals Error with id : " + npcId + "; unknown elementType: " + rset.getByte("elemAtkType"));
							continue;
					}
					npcDat.setBaseFireRes(rset.getInt("fireDefValue"));
					npcDat.setBaseWaterRes(rset.getInt("waterDefValue"));
					npcDat.setBaseEarthRes(rset.getInt("earthDefValue"));
					npcDat.setBaseWindRes(rset.getInt("windDefValue"));
					npcDat.setBaseHolyRes(rset.getInt("holyDefValue"));
					npcDat.setBaseDarkRes(rset.getInt("darkDefValue"));
					count++;
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": Error reading NPC Elementals Data: " + e.getMessage(), e);
		}
		return count;
	}
	
	public static NpcTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcTable _instance = new NpcTable();
	}
}