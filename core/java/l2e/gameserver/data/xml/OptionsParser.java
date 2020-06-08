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
package l2e.gameserver.data.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.gameserver.engines.DocumentParser;
import l2e.gameserver.model.holders.SkillsHolder;
import l2e.gameserver.model.options.Options;
import l2e.gameserver.model.options.OptionsSkillHolder;
import l2e.gameserver.model.options.OptionsSkillType;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.funcs.LambdaConst;
import l2e.gameserver.model.stats.Stats;

public class OptionsParser extends DocumentParser
{
	private final Map<Integer, Options> _data = new HashMap<>();
	
	protected OptionsParser()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		parseDirectory("data/stats/options");
		_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _data.size() + " Options.");
	}
	
	@Override
	protected void parseDocument()
	{
		int id;
		Options op;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("option".equalsIgnoreCase(d.getNodeName()))
					{
						id = parseInt(d.getAttributes(), "id");
						op = new Options(id);
						
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							switch (cd.getNodeName())
							{
								case "for":
								{
									for (Node fd = cd.getFirstChild(); fd != null; fd = fd.getNextSibling())
									{
										switch (fd.getNodeName())
										{
											case "add":
											{
												parseFuncs(fd.getAttributes(), "Add", op);
												break;
											}
											case "mul":
											{
												parseFuncs(fd.getAttributes(), "Mul", op);
												break;
											}
											case "basemul":
											{
												parseFuncs(fd.getAttributes(), "BaseMul", op);
												break;
											}
											case "sub":
											{
												parseFuncs(fd.getAttributes(), "Sub", op);
												break;
											}
											case "div":
											{
												parseFuncs(fd.getAttributes(), "Div", op);
												break;
											}
											case "set":
											{
												parseFuncs(fd.getAttributes(), "Set", op);
												break;
											}
										}
									}
									break;
								}
								case "active_skill":
								{
									op.setActiveSkill(new SkillsHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level")));
									break;
								}
								case "passive_skill":
								{
									op.setPassiveSkill(new SkillsHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level")));
									break;
								}
								case "attack_skill":
								{
									op.addActivationSkill(new OptionsSkillHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level"), parseDouble(cd.getAttributes(), "chance"), OptionsSkillType.ATTACK));
									break;
								}
								case "magic_skill":
								{
									op.addActivationSkill(new OptionsSkillHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level"), parseDouble(cd.getAttributes(), "chance"), OptionsSkillType.MAGIC));
									break;
								}
								case "critical_skill":
								{
									op.addActivationSkill(new OptionsSkillHolder(parseInt(cd.getAttributes(), "id"), parseInt(cd.getAttributes(), "level"), parseDouble(cd.getAttributes(), "chance"), OptionsSkillType.CRITICAL));
									break;
								}
							}
						}
						_data.put(op.getId(), op);
					}
				}
			}
		}
	}
	
	private void parseFuncs(NamedNodeMap attrs, String func, Options op)
	{
		Stats stat = Stats.valueOfXml(parseString(attrs, "stat"));
		int ord = Integer.decode(parseString(attrs, "order"));
		double val = parseDouble(attrs, "val");
		op.addFunc(new FuncTemplate(null, null, func, stat, ord, new LambdaConst(val)));
	}
	
	public Options getOptions(int id)
	{
		return _data.get(id);
	}
	
	public static final OptionsParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final OptionsParser _instance = new OptionsParser();
	}
}