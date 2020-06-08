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
package l2e.gameserver.model.actor.templates;

import l2e.gameserver.model.L2Range;
import l2e.gameserver.model.StatsSet;
import l2e.gameserver.model.interfaces.IIdentifiable;

public class L2DoorTemplate extends L2CharTemplate implements IIdentifiable
{
	public final int doorId;
	public final int nodeX[];
	public final int nodeY[];
	public final int nodeZ;
	public final int height;
	public final int posX;
	public final int posY;
	public final int posZ;
	public final int emmiter;
	public final int childDoorId;
	public final String name;
	public final String groupName;
	public final boolean showHp;
	public final boolean isWall;
	public final byte masterDoorClose;
	public final byte masterDoorOpen;
	protected final L2Range range;
	
	public L2DoorTemplate(StatsSet set)
	{
		super(set);
		
		doorId = set.getInteger("id");
		name = set.getString("name");
		
		String[] pos = set.getString("pos").split(";");
		posX = Integer.parseInt(pos[0]);
		posY = Integer.parseInt(pos[1]);
		posZ = Integer.parseInt(pos[2]);
		height = set.getInteger("height");
		
		nodeZ = set.getInteger("nodeZ");
		nodeX = new int[4];
		nodeY = new int[4];
		for (int i = 0; i < 4; i++)
		{
			String split[] = set.getString("node" + (i + 1)).split(",");
			nodeX[i] = Integer.parseInt(split[0]);
			nodeY[i] = Integer.parseInt(split[1]);
		}
		
		emmiter = set.getInteger("emitter_id", 0);
		showHp = set.getBool("hp_showable", true);
		isWall = set.getBool("is_wall", false);
		groupName = set.getString("group", null);
		
		childDoorId = set.getInteger("child_id_event", -1);
		String masterevent = set.getString("master_close_event", "act_nothing");
		if (masterevent.equals("act_open"))
		{
			masterDoorClose = 1;
		}
		else if (masterevent.equals("act_close"))
		{
			masterDoorClose = -1;
		}
		else
		{
			masterDoorClose = 0;
		}
		
		masterevent = set.getString("master_open_event", "act_nothing");
		if (masterevent.equals("act_open"))
		{
			masterDoorOpen = 1;
		}
		else if (masterevent.equals("act_close"))
		{
			masterDoorOpen = -1;
		}
		else
		{
			masterDoorOpen = 0;
		}
		
		String[] pos1 = set.getString("node1").split(",");
		String[] pos2 = set.getString("node2").split(",");
		String[] pos3 = set.getString("node3").split(",");
		String[] pos4 = set.getString("node4").split(",");
		range = new L2Range();
		if (pos1 != null)
		{
			range.add(Integer.parseInt(pos1[0]), Integer.parseInt(pos1[1]), set.getInteger("nodeZ"));
			range.add(Integer.parseInt(pos2[0]), Integer.parseInt(pos2[1]), set.getInteger("nodeZ"));
			range.add(Integer.parseInt(pos3[0]), Integer.parseInt(pos3[1]), set.getInteger("nodeZ") + height);
			range.add(Integer.parseInt(pos4[0]), Integer.parseInt(pos4[1]), set.getInteger("nodeZ") + height);
		}
		else
		{
			range.add(posX + 12, posY - 12, posZ - 6);
			range.add(posX - 12, posY + 12, posZ - 6);
			range.add(posX + 12, posY + 12, posZ + height + 6);
			range.add(posX - 12, posY - 12, posZ + height + 6);
		}
	}
	
	@Override
	public int getId()
	{
		return doorId;
	}
	
	public L2Range getRange()
	{
		return range;
	}
}