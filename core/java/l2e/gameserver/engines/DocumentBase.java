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
package l2e.gameserver.engines;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.data.sql.ItemHolder;
import l2e.gameserver.model.L2Object.InstanceType;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.base.PlayerState;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.conditions.Condition;
import l2e.gameserver.model.conditions.ConditionChangeWeapon;
import l2e.gameserver.model.conditions.ConditionGameChance;
import l2e.gameserver.model.conditions.ConditionGameTime;
import l2e.gameserver.model.conditions.ConditionGameTime.CheckGameTime;
import l2e.gameserver.model.conditions.ConditionLogicAnd;
import l2e.gameserver.model.conditions.ConditionLogicNot;
import l2e.gameserver.model.conditions.ConditionLogicOr;
import l2e.gameserver.model.conditions.ConditionMinDistance;
import l2e.gameserver.model.conditions.ConditionPlayerActiveEffectId;
import l2e.gameserver.model.conditions.ConditionPlayerActiveSkillId;
import l2e.gameserver.model.conditions.ConditionPlayerAgathionId;
import l2e.gameserver.model.conditions.ConditionPlayerCallPc;
import l2e.gameserver.model.conditions.ConditionPlayerCanEscape;
import l2e.gameserver.model.conditions.ConditionPlayerCanPossessHolything;
import l2e.gameserver.model.conditions.ConditionPlayerCanRefuelAirship;
import l2e.gameserver.model.conditions.ConditionPlayerCanSweep;
import l2e.gameserver.model.conditions.ConditionPlayerCanTransform;
import l2e.gameserver.model.conditions.ConditionPlayerCanUntransform;
import l2e.gameserver.model.conditions.ConditionPlayerCharges;
import l2e.gameserver.model.conditions.ConditionPlayerClassIdRestriction;
import l2e.gameserver.model.conditions.ConditionPlayerCloakStatus;
import l2e.gameserver.model.conditions.ConditionPlayerCp;
import l2e.gameserver.model.conditions.ConditionPlayerFlyMounted;
import l2e.gameserver.model.conditions.ConditionPlayerGrade;
import l2e.gameserver.model.conditions.ConditionPlayerHasCastle;
import l2e.gameserver.model.conditions.ConditionPlayerHasClanHall;
import l2e.gameserver.model.conditions.ConditionPlayerHasFort;
import l2e.gameserver.model.conditions.ConditionPlayerHasPet;
import l2e.gameserver.model.conditions.ConditionPlayerHp;
import l2e.gameserver.model.conditions.ConditionPlayerInsideZoneId;
import l2e.gameserver.model.conditions.ConditionPlayerInstanceId;
import l2e.gameserver.model.conditions.ConditionPlayerInvSize;
import l2e.gameserver.model.conditions.ConditionPlayerIsClanLeader;
import l2e.gameserver.model.conditions.ConditionPlayerIsHero;
import l2e.gameserver.model.conditions.ConditionPlayerLandingZone;
import l2e.gameserver.model.conditions.ConditionPlayerLevel;
import l2e.gameserver.model.conditions.ConditionPlayerLevelRange;
import l2e.gameserver.model.conditions.ConditionPlayerMp;
import l2e.gameserver.model.conditions.ConditionPlayerPkCount;
import l2e.gameserver.model.conditions.ConditionPlayerPledgeClass;
import l2e.gameserver.model.conditions.ConditionPlayerRace;
import l2e.gameserver.model.conditions.ConditionPlayerRangeFromNpc;
import l2e.gameserver.model.conditions.ConditionPlayerServitorNpcId;
import l2e.gameserver.model.conditions.ConditionPlayerSex;
import l2e.gameserver.model.conditions.ConditionPlayerSiegeSide;
import l2e.gameserver.model.conditions.ConditionPlayerSouls;
import l2e.gameserver.model.conditions.ConditionPlayerState;
import l2e.gameserver.model.conditions.ConditionPlayerSubclass;
import l2e.gameserver.model.conditions.ConditionPlayerTransformationId;
import l2e.gameserver.model.conditions.ConditionPlayerTvTEvent;
import l2e.gameserver.model.conditions.ConditionPlayerTvTRoundEvent;
import l2e.gameserver.model.conditions.ConditionPlayerVehicleMounted;
import l2e.gameserver.model.conditions.ConditionPlayerWeight;
import l2e.gameserver.model.conditions.ConditionSiegeZone;
import l2e.gameserver.model.conditions.ConditionSlotItemId;
import l2e.gameserver.model.conditions.ConditionTargetAbnormal;
import l2e.gameserver.model.conditions.ConditionTargetActiveEffectId;
import l2e.gameserver.model.conditions.ConditionTargetActiveSkillId;
import l2e.gameserver.model.conditions.ConditionTargetAggro;
import l2e.gameserver.model.conditions.ConditionTargetClassIdRestriction;
import l2e.gameserver.model.conditions.ConditionTargetInvSize;
import l2e.gameserver.model.conditions.ConditionTargetLevel;
import l2e.gameserver.model.conditions.ConditionTargetLevelRange;
import l2e.gameserver.model.conditions.ConditionTargetMyPartyExceptMe;
import l2e.gameserver.model.conditions.ConditionTargetNpcId;
import l2e.gameserver.model.conditions.ConditionTargetNpcType;
import l2e.gameserver.model.conditions.ConditionTargetPlayable;
import l2e.gameserver.model.conditions.ConditionTargetRace;
import l2e.gameserver.model.conditions.ConditionTargetRaceId;
import l2e.gameserver.model.conditions.ConditionTargetUsesWeaponKind;
import l2e.gameserver.model.conditions.ConditionTargetWeight;
import l2e.gameserver.model.conditions.ConditionUsingItemType;
import l2e.gameserver.model.conditions.ConditionUsingSkill;
import l2e.gameserver.model.conditions.ConditionWithSkill;
import l2e.gameserver.model.effects.EffectTemplate;
import l2e.gameserver.model.interfaces.IIdentifiable;
import l2e.gameserver.model.items.L2Item;
import l2e.gameserver.model.items.type.L2ArmorType;
import l2e.gameserver.model.items.type.L2WeaponType;
import l2e.gameserver.model.skills.L2Skill;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.funcs.Lambda;
import l2e.gameserver.model.skills.funcs.LambdaCalc;
import l2e.gameserver.model.skills.funcs.LambdaConst;
import l2e.gameserver.model.skills.funcs.LambdaStats;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public abstract class DocumentBase
{
	protected final Logger _log = Logger.getLogger(getClass().getName());
	
	private final File _file;
	protected Map<String, String[]> _tables;
	
	protected DocumentBase(File pFile)
	{
		_file = pFile;
		_tables = new FastMap<>();
	}
	
	public Document parse()
	{
		Document doc = null;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(_file);
			parseDocument(doc);
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error loading file " + _file, e);
		}
		return doc;
	}
	
	protected abstract void parseDocument(Document doc);
	
	protected abstract StatsSet getStatsSet();
	
	protected abstract String getTableValue(String name);
	
	protected abstract String getTableValue(String name, int idx);
	
	protected void resetTable()
	{
		_tables = new FastMap<>();
	}
	
	protected void setTable(String name, String[] table)
	{
		_tables.put(name, table);
	}
	
	protected void parseTemplate(Node n, Object template)
	{
		Condition condition = null;
		n = n.getFirstChild();
		if (n == null)
		{
			return;
		}
		if ("cond".equalsIgnoreCase(n.getNodeName()))
		{
			condition = parseCondition(n.getFirstChild(), template);
			Node msg = n.getAttributes().getNamedItem("msg");
			Node msgId = n.getAttributes().getNamedItem("msgId");
			if ((condition != null) && (msg != null))
			{
				condition.setMessage(msg.getNodeValue());
			}
			else if ((condition != null) && (msgId != null))
			{
				condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
				Node addName = n.getAttributes().getNamedItem("addName");
				if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0))
				{
					condition.addName();
				}
			}
			n = n.getNextSibling();
		}
		for (; n != null; n = n.getNextSibling())
		{
			if ("add".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Add", condition);
			}
			else if ("sub".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Sub", condition);
			}
			else if ("mul".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Mul", condition);
			}
			else if ("basemul".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "BaseMul", condition);
			}
			else if ("div".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Div", condition);
			}
			else if ("set".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Set", condition);
			}
			else if ("share".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Share", condition);
			}
			else if ("enchant".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "Enchant", condition);
			}
			else if ("enchanthp".equalsIgnoreCase(n.getNodeName()))
			{
				attachFunc(n, template, "EnchantHp", condition);
			}
			else if ("effect".equalsIgnoreCase(n.getNodeName()))
			{
				if (template instanceof EffectTemplate)
				{
					throw new RuntimeException("Nested effects");
				}
				attachEffect(n, template, condition);
			}
		}
	}
	
	protected void attachFunc(Node n, Object template, String name, Condition attachCond)
	{
		Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
		String order = n.getAttributes().getNamedItem("order").getNodeValue();
		Lambda lambda = getLambda(n, template);
		int ord = Integer.decode(getValue(order, template));
		Condition applayCond = parseCondition(n.getFirstChild(), template);
		FuncTemplate ft = new FuncTemplate(attachCond, applayCond, name, stat, ord, lambda);
		if (template instanceof L2Item)
		{
			((L2Item) template).attach(ft);
		}
		else if (template instanceof L2Skill)
		{
			((L2Skill) template).attach(ft);
		}
		else if (template instanceof EffectTemplate)
		{
			((EffectTemplate) template).attach(ft);
		}
	}
	
	protected void attachLambdaFunc(Node n, Object template, LambdaCalc calc)
	{
		String name = n.getNodeName();
		final StringBuilder sb = new StringBuilder(name);
		sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
		name = sb.toString();
		Lambda lambda = getLambda(n, template);
		FuncTemplate ft = new FuncTemplate(null, null, name, null, calc.funcs.length, lambda);
		calc.addFunc(ft.getFunc(new Env(), calc));
	}
	
	protected void attachEffect(Node n, Object template, Condition attachCond)
	{
		final NamedNodeMap attrs = n.getAttributes();
		final StatsSet set = new StatsSet();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node att = attrs.item(i);
			set.set(att.getNodeName(), getValue(att.getNodeValue(), template));
		}
		
		final StatsSet parameters = parseParameters(n.getFirstChild(), template);
		final Lambda lambda = getLambda(n, template);
		final Condition applayCond = parseCondition(n.getFirstChild(), template);
		
		if (template instanceof IIdentifiable)
		{
			set.set("id", ((IIdentifiable) template).getId());
		}
		
		byte abnormalLvl = 0;
		String abnormalType = "none";
		if (attrs.getNamedItem("abnormalType") != null)
		{
			abnormalType = attrs.getNamedItem("abnormalType").getNodeValue();
		}
		if (attrs.getNamedItem("abnormalLvl") != null)
		{
			abnormalLvl = Byte.parseByte(getValue(attrs.getNamedItem("abnormalLvl").getNodeValue(), template));
		}
		
		final EffectTemplate effectTemplate = new EffectTemplate(attachCond, applayCond, lambda, abnormalType, abnormalLvl, set, parameters);
		parseTemplate(n, effectTemplate);
		if (template instanceof L2Item)
		{
			((L2Item) template).attach(effectTemplate);
		}
		else if (template instanceof L2Skill)
		{
			final L2Skill sk = (L2Skill) template;
			if (set.getInteger("self", 0) == 1)
			{
				sk.attachSelf(effectTemplate);
			}
			else if (sk.isPassive())
			{
				sk.attachPassive(effectTemplate);
			}
			else
			{
				sk.attach(effectTemplate);
			}
		}
	}
	
	private StatsSet parseParameters(Node n, Object template)
	{
		StatsSet parameters = null;
		while ((n != null))
		{
			if ((n.getNodeType() == Node.ELEMENT_NODE) && "param".equals(n.getNodeName()))
			{
				if (parameters == null)
				{
					parameters = new StatsSet();
				}
				NamedNodeMap params = n.getAttributes();
				for (int i = 0; i < params.getLength(); i++)
				{
					Node att = params.item(i);
					parameters.set(att.getNodeName(), getValue(att.getNodeValue(), template));
				}
			}
			n = n.getNextSibling();
		}
		return parameters;
	}
	
	protected Condition parseCondition(Node n, Object template)
	{
		while ((n != null) && (n.getNodeType() != Node.ELEMENT_NODE))
		{
			n = n.getNextSibling();
		}
		
		Condition condition = null;
		if (n != null)
		{
			switch (n.getNodeName())
			{
				case "and":
				{
					condition = parseLogicAnd(n, template);
					break;
				}
				case "or":
				{
					condition = parseLogicOr(n, template);
					break;
				}
				case "not":
				{
					condition = parseLogicNot(n, template);
					break;
				}
				case "player":
				{
					condition = parsePlayerCondition(n, template);
					break;
				}
				case "target":
				{
					condition = parseTargetCondition(n, template);
					break;
				}
				case "using":
				{
					condition = parseUsingCondition(n);
					break;
				}
				case "game":
				{
					condition = parseGameCondition(n);
					break;
				}
			}
		}
		return condition;
	}
	
	protected Condition parseLogicAnd(Node n, Object template)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				cond.add(parseCondition(n, template));
			}
		}
		if ((cond.conditions == null) || (cond.conditions.length == 0))
		{
			_log.severe("Empty <and> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseLogicOr(Node n, Object template)
	{
		ConditionLogicOr cond = new ConditionLogicOr();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				cond.add(parseCondition(n, template));
			}
		}
		if ((cond.conditions == null) || (cond.conditions.length == 0))
		{
			_log.severe("Empty <or> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseLogicNot(Node n, Object template)
	{
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				return new ConditionLogicNot(parseCondition(n, template));
			}
		}
		_log.severe("Empty <not> condition in " + _file);
		return null;
	}
	
	protected Condition parsePlayerCondition(Node n, Object template)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("races".equalsIgnoreCase(a.getNodeName()))
			{
				final String[] racesVal = a.getNodeValue().split(",");
				final Race[] races = new Race[racesVal.length];
				for (int r = 0; r < racesVal.length; r++)
				{
					if (racesVal[r] != null)
					{
						races[r] = Race.valueOf(racesVal[r]);
					}
				}
				cond = joinAnd(cond, new ConditionPlayerRace(races));
			}
			else if ("level".equalsIgnoreCase(a.getNodeName()))
			{
				int lvl = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerLevel(lvl));
			}
			else if ("levelRange".equalsIgnoreCase(a.getNodeName()))
			{
				String[] range = getValue(a.getNodeValue(), template).split(";");
				if (range.length == 2)
				{
					int[] lvlRange = new int[2];
					lvlRange[0] = Integer.decode(getValue(a.getNodeValue(), template).split(";")[0]);
					lvlRange[1] = Integer.decode(getValue(a.getNodeValue(), template).split(";")[1]);
					cond = joinAnd(cond, new ConditionPlayerLevelRange(lvlRange));
				}
			}
			else if ("resting".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RESTING, val));
			}
			else if ("flying".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FLYING, val));
			}
			else if ("moving".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.MOVING, val));
			}
			else if ("running".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RUNNING, val));
			}
			else if ("standing".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.STANDING, val));
			}
			else if ("behind".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.BEHIND, val));
			}
			else if ("front".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FRONT, val));
			}
			else if ("chaotic".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.CHAOTIC, val));
			}
			else if ("olympiad".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.OLYMPIAD, val));
			}
			else if ("ishero".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerIsHero(val));
			}
			else if ("transformationId".equalsIgnoreCase(a.getNodeName()))
			{
				int id = Integer.parseInt(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerTransformationId(id));
			}
			else if ("hp".equalsIgnoreCase(a.getNodeName()))
			{
				int hp = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerHp(hp));
			}
			else if ("mp".equalsIgnoreCase(a.getNodeName()))
			{
				int hp = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerMp(hp));
			}
			else if ("cp".equalsIgnoreCase(a.getNodeName()))
			{
				int cp = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerCp(cp));
			}
			else if ("grade".equalsIgnoreCase(a.getNodeName()))
			{
				int expIndex = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerGrade(expIndex));
			}
			else if ("pkCount".equalsIgnoreCase(a.getNodeName()))
			{
				int expIndex = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerPkCount(expIndex));
			}
			else if ("siegezone".equalsIgnoreCase(a.getNodeName()))
			{
				int value = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionSiegeZone(value, true));
			}
			else if ("siegeside".equalsIgnoreCase(a.getNodeName()))
			{
				int value = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerSiegeSide(value));
			}
			else if ("charges".equalsIgnoreCase(a.getNodeName()))
			{
				int value = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerCharges(value));
			}
			else if ("souls".equalsIgnoreCase(a.getNodeName()))
			{
				int value = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerSouls(value));
			}
			else if ("weight".equalsIgnoreCase(a.getNodeName()))
			{
				int weight = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerWeight(weight));
			}
			else if ("invSize".equalsIgnoreCase(a.getNodeName()))
			{
				int size = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerInvSize(size));
			}
			else if ("isClanLeader".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerIsClanLeader(val));
			}
			else if ("onTvTEvent".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerTvTEvent(val));
			}
			else if ("onTvTRoundEvent".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerTvTRoundEvent(val));
			}
			else if ("pledgeClass".equalsIgnoreCase(a.getNodeName()))
			{
				int pledgeClass = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerPledgeClass(pledgeClass));
			}
			else if ("clanHall".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionPlayerHasClanHall(array));
			}
			else if ("fort".equalsIgnoreCase(a.getNodeName()))
			{
				int fort = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerHasFort(fort));
			}
			else if ("castle".equalsIgnoreCase(a.getNodeName()))
			{
				int castle = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerHasCastle(castle));
			}
			else if ("sex".equalsIgnoreCase(a.getNodeName()))
			{
				int sex = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionPlayerSex(sex));
			}
			else if ("flyMounted".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerFlyMounted(val));
			}
			else if ("vehicleMounted".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerVehicleMounted(val));
			}
			else if ("landingZone".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerLandingZone(val));
			}
			else if ("active_effect_id".equalsIgnoreCase(a.getNodeName()))
			{
				int effect_id = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id));
			}
			else if ("active_effect_id_lvl".equalsIgnoreCase(a.getNodeName()))
			{
				String val = getValue(a.getNodeValue(), template);
				int effect_id = Integer.decode(getValue(val.split(",")[0], template));
				int effect_lvl = Integer.decode(getValue(val.split(",")[1], template));
				cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id, effect_lvl));
			}
			else if ("active_skill_id".equalsIgnoreCase(a.getNodeName()))
			{
				int skill_id = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id));
			}
			else if ("active_skill_id_lvl".equalsIgnoreCase(a.getNodeName()))
			{
				String val = getValue(a.getNodeValue(), template);
				int skill_id = Integer.decode(getValue(val.split(",")[0], template));
				int skill_lvl = Integer.decode(getValue(val.split(",")[1], template));
				cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id, skill_lvl));
			}
			else if ("class_id_restriction".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionPlayerClassIdRestriction(array));
			}
			else if ("subclass".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerSubclass(val));
			}
			else if ("instanceId".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionPlayerInstanceId(array));
			}
			else if ("agathionId".equalsIgnoreCase(a.getNodeName()))
			{
				int agathionId = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerAgathionId(agathionId));
			}
			else if ("cloakStatus".equalsIgnoreCase(a.getNodeName()))
			{
				int val = Integer.parseInt(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerCloakStatus(val));
			}
			else if ("hasPet".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionPlayerHasPet(array));
			}
			else if ("servitorNpcId".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionPlayerServitorNpcId(array));
			}
			else if ("npcIdRadius".equalsIgnoreCase(a.getNodeName()))
			{
				final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				if (st.countTokens() == 3)
				{
					final String[] ids = st.nextToken().split(";");
					final int[] npcIds = new int[ids.length];
					for (int index = 0; index < ids.length; index++)
					{
						npcIds[index] = Integer.parseInt(getValue(ids[index], template));
					}
					final int radius = Integer.parseInt(st.nextToken());
					final boolean val = Boolean.parseBoolean(st.nextToken());
					cond = joinAnd(cond, new ConditionPlayerRangeFromNpc(npcIds, radius, val));
				}
			}
			else if ("callPc".equalsIgnoreCase(a.getNodeName()))
			{
				cond = joinAnd(cond, new ConditionPlayerCallPc(Boolean.parseBoolean(a.getNodeValue())));
			}
			else if ("canEscape".equalsIgnoreCase(a.getNodeName()))
			{
				cond = joinAnd(cond, new ConditionPlayerCanEscape(Boolean.parseBoolean(a.getNodeValue())));
			}
			else if ("canPossessHolything".equalsIgnoreCase(a.getNodeName()))
			{
				cond = joinAnd(cond, new ConditionPlayerCanPossessHolything(Boolean.parseBoolean(a.getNodeValue())));
			}
			else if ("canRefuelAirship".equalsIgnoreCase(a.getNodeName()))
			{
				cond = joinAnd(cond, new ConditionPlayerCanRefuelAirship(Integer.parseInt(a.getNodeValue())));
			}
			else if ("canSweep".equalsIgnoreCase(a.getNodeName()))
			{
				cond = joinAnd(cond, new ConditionPlayerCanSweep(Boolean.parseBoolean(a.getNodeValue())));
			}
			else if ("canTransform".equalsIgnoreCase(a.getNodeName()))
			{
				cond = joinAnd(cond, new ConditionPlayerCanTransform(Boolean.parseBoolean(a.getNodeValue())));
			}
			else if ("canUntransform".equalsIgnoreCase(a.getNodeName()))
			{
				cond = joinAnd(cond, new ConditionPlayerCanUntransform(Boolean.parseBoolean(a.getNodeValue())));
			}
			else if ("insideZoneId".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionPlayerInsideZoneId(array));
			}
		}
		
		if (cond == null)
		{
			_log.severe("Unrecognized <player> condition in " + _file);
		}
		
		return cond;
	}
	
	protected Condition parseTargetCondition(Node n, Object template)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("aggro".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetAggro(val));
			}
			else if ("siegezone".equalsIgnoreCase(a.getNodeName()))
			{
				int value = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionSiegeZone(value, false));
			}
			else if ("level".equalsIgnoreCase(a.getNodeName()))
			{
				int lvl = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionTargetLevel(lvl));
			}
			else if ("levelRange".equalsIgnoreCase(a.getNodeName()))
			{
				String[] range = getValue(a.getNodeValue(), template).split(";");
				if (range.length == 2)
				{
					int[] lvlRange = new int[2];
					lvlRange[0] = Integer.decode(getValue(a.getNodeValue(), template).split(";")[0]);
					lvlRange[1] = Integer.decode(getValue(a.getNodeValue(), template).split(";")[1]);
					cond = joinAnd(cond, new ConditionTargetLevelRange(lvlRange));
				}
			}
			else if ("myPartyExceptMe".equalsIgnoreCase(a.getNodeName()))
			{
				cond = joinAnd(cond, new ConditionTargetMyPartyExceptMe(Boolean.parseBoolean(a.getNodeValue())));
			}
			else if ("playable".equalsIgnoreCase(a.getNodeName()))
			{
				cond = joinAnd(cond, new ConditionTargetPlayable());
			}
			else if ("class_id_restriction".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionTargetClassIdRestriction(array));
			}
			else if ("active_effect_id".equalsIgnoreCase(a.getNodeName()))
			{
				int effect_id = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionTargetActiveEffectId(effect_id));
			}
			else if ("active_effect_id_lvl".equalsIgnoreCase(a.getNodeName()))
			{
				String val = getValue(a.getNodeValue(), template);
				int effect_id = Integer.decode(getValue(val.split(",")[0], template));
				int effect_lvl = Integer.decode(getValue(val.split(",")[1], template));
				cond = joinAnd(cond, new ConditionTargetActiveEffectId(effect_id, effect_lvl));
			}
			else if ("active_skill_id".equalsIgnoreCase(a.getNodeName()))
			{
				int skill_id = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
			}
			else if ("active_skill_id_lvl".equalsIgnoreCase(a.getNodeName()))
			{
				String val = getValue(a.getNodeValue(), template);
				int skill_id = Integer.decode(getValue(val.split(",")[0], template));
				int skill_lvl = Integer.decode(getValue(val.split(",")[1], template));
				cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id, skill_lvl));
			}
			else if ("abnormal".equalsIgnoreCase(a.getNodeName()))
			{
				int abnormalId = Integer.decode(getValue(a.getNodeValue(), template));
				cond = joinAnd(cond, new ConditionTargetAbnormal(abnormalId));
			}
			else if ("mindistance".equalsIgnoreCase(a.getNodeName()))
			{
				int distance = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionMinDistance(distance * distance));
			}
			else if ("race_id".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionTargetRaceId(array));
			}
			else if ("races".equalsIgnoreCase(a.getNodeName()))
			{
				final String[] racesVal = a.getNodeValue().split(",");
				final Race[] races = new Race[racesVal.length];
				for (int r = 0; r < racesVal.length; r++)
				{
					if (racesVal[r] != null)
					{
						races[r] = Race.valueOf(racesVal[r]);
					}
				}
				cond = joinAnd(cond, new ConditionTargetRace(races));
			}
			else if ("using".equalsIgnoreCase(a.getNodeName()))
			{
				int mask = 0;
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					for (L2WeaponType wt : L2WeaponType.values())
					{
						if (wt.toString().equals(item))
						{
							mask |= wt.mask();
							break;
						}
					}
					for (L2ArmorType at : L2ArmorType.values())
					{
						if (at.toString().equals(item))
						{
							mask |= at.mask();
							break;
						}
					}
				}
				cond = joinAnd(cond, new ConditionTargetUsesWeaponKind(mask));
			}
			else if ("npcId".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item, null)));
				}
				cond = joinAnd(cond, new ConditionTargetNpcId(array));
			}
			else if ("npcType".equalsIgnoreCase(a.getNodeName()))
			{
				String values = getValue(a.getNodeValue(), template).trim();
				String[] valuesSplit = values.split(",");
				
				InstanceType[] types = new InstanceType[valuesSplit.length];
				InstanceType type;
				
				for (int j = 0; j < valuesSplit.length; j++)
				{
					type = Enum.valueOf(InstanceType.class, valuesSplit[j]);
					if (type == null)
					{
						throw new IllegalArgumentException("Instance type not recognized: " + valuesSplit[j]);
					}
					types[j] = type;
				}
				
				cond = joinAnd(cond, new ConditionTargetNpcType(types));
			}
			else if ("weight".equalsIgnoreCase(a.getNodeName()))
			{
				int weight = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionTargetWeight(weight));
			}
			else if ("invSize".equalsIgnoreCase(a.getNodeName()))
			{
				int size = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionTargetInvSize(size));
			}
		}
		if (cond == null)
		{
			_log.severe("Unrecognized <target> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseUsingCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("kind".equalsIgnoreCase(a.getNodeName()))
			{
				int mask = 0;
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					int old = mask;
					String item = st.nextToken().trim();
					if (ItemHolder._weaponTypes.containsKey(item))
					{
						mask |= ItemHolder._weaponTypes.get(item).mask();
					}
					
					if (ItemHolder._armorTypes.containsKey(item))
					{
						mask |= ItemHolder._armorTypes.get(item).mask();
					}
					
					if (old == mask)
					{
						_log.info("[parseUsingCondition=\"kind\"] Unknown item type name: " + item);
					}
				}
				cond = joinAnd(cond, new ConditionUsingItemType(mask));
			}
			else if ("skill".equalsIgnoreCase(a.getNodeName()))
			{
				int id = Integer.parseInt(a.getNodeValue());
				cond = joinAnd(cond, new ConditionUsingSkill(id));
			}
			else if ("slotitem".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int slot = Integer.parseInt(st.nextToken().trim());
				int enchant = 0;
				if (st.hasMoreTokens())
				{
					enchant = Integer.parseInt(st.nextToken().trim());
				}
				cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
			}
			else if ("weaponChange".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionChangeWeapon(val));
			}
		}
		if (cond == null)
		{
			_log.severe("Unrecognized <using> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseGameCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("skill".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionWithSkill(val));
			}
			if ("night".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionGameTime(CheckGameTime.NIGHT, val));
			}
			if ("chance".equalsIgnoreCase(a.getNodeName()))
			{
				int val = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionGameChance(val));
			}
		}
		if (cond == null)
		{
			_log.severe("Unrecognized <game> condition in " + _file);
		}
		return cond;
	}
	
	protected void parseTable(Node n)
	{
		NamedNodeMap attrs = n.getAttributes();
		String name = attrs.getNamedItem("name").getNodeValue();
		if (name.charAt(0) != '#')
		{
			throw new IllegalArgumentException("Table name must start with #");
		}
		StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
		List<String> array = new ArrayList<>(data.countTokens());
		while (data.hasMoreTokens())
		{
			array.add(data.nextToken());
		}
		setTable(name, array.toArray(new String[array.size()]));
	}
	
	protected void parseBeanSet(Node n, StatsSet set, Integer level)
	{
		String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
		String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
		char ch = value.isEmpty() ? ' ' : value.charAt(0);
		if ((ch == '#') || (ch == '-') || Character.isDigit(ch))
		{
			set.set(name, String.valueOf(getValue(value, level)));
		}
		else
		{
			set.set(name, value);
		}
	}
	
	protected void setExtractableSkillData(StatsSet set, String value)
	{
		set.set("capsuled_items_skill", value);
	}
	
	protected Lambda getLambda(Node n, Object template)
	{
		Node nval = n.getAttributes().getNamedItem("val");
		if (nval != null)
		{
			String val = nval.getNodeValue();
			if (val.charAt(0) == '#')
			{
				return new LambdaConst(Double.parseDouble(getTableValue(val)));
			}
			else if (val.charAt(0) == '$')
			{
				if (val.equalsIgnoreCase("$player_level"))
				{
					return new LambdaStats(LambdaStats.StatsType.PLAYER_LEVEL);
				}
				if (val.equalsIgnoreCase("$target_level"))
				{
					return new LambdaStats(LambdaStats.StatsType.TARGET_LEVEL);
				}
				if (val.equalsIgnoreCase("$player_max_hp"))
				{
					return new LambdaStats(LambdaStats.StatsType.PLAYER_MAX_HP);
				}
				if (val.equalsIgnoreCase("$player_max_mp"))
				{
					return new LambdaStats(LambdaStats.StatsType.PLAYER_MAX_MP);
				}
				StatsSet set = getStatsSet();
				String field = set.getString(val.substring(1));
				if (field != null)
				{
					return new LambdaConst(Double.parseDouble(getValue(field, template)));
				}
				throw new IllegalArgumentException("Unknown value " + val);
			}
			else
			{
				return new LambdaConst(Double.parseDouble(val));
			}
		}
		LambdaCalc calc = new LambdaCalc();
		n = n.getFirstChild();
		while ((n != null) && (n.getNodeType() != Node.ELEMENT_NODE))
		{
			n = n.getNextSibling();
		}
		if ((n == null) || !"val".equals(n.getNodeName()))
		{
			throw new IllegalArgumentException("Value not specified");
		}
		
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}
			attachLambdaFunc(n, template, calc);
		}
		return calc;
	}
	
	protected String getValue(String value, Object template)
	{
		if (value.charAt(0) == '#')
		{
			if (template instanceof L2Skill)
			{
				return getTableValue(value);
			}
			else if (template instanceof Integer)
			{
				return getTableValue(value, ((Integer) template).intValue());
			}
			else
			{
				throw new IllegalStateException();
			}
		}
		return value;
	}
	
	protected Condition joinAnd(Condition cond, Condition c)
	{
		if (cond == null)
		{
			return c;
		}
		if (cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}
}