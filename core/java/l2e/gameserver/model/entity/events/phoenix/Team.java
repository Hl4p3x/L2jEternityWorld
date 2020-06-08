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

public class Team implements Comparable<Team>
{
	private int score;
	private final String name;
	private final int[] nameColor;
	private final int[] startPos;
	private final int id;
	
	public Team(int id, String name, int[] color, int[] startPos)
	{
		this.id = id;
		score = 0;
		this.name = name;
		nameColor = color;
		this.startPos = startPos;
	}
	
	@Override
	public int compareTo(Team second)
	{
		if (getScore() > second.getScore())
		{
			return 1;
		}
		if (getScore() < second.getScore())
		{
			return -1;
		}
		if (getScore() == second.getScore())
		{
			return 0;
		}
		return 0;
	}
	
	public String getHexaColor()
	{
		String hexa;
		Integer i1 = nameColor[0];
		Integer i2 = nameColor[1];
		Integer i3 = nameColor[2];
		hexa = "" + (i1 > 15 ? Integer.toHexString(i1) : "0" + Integer.toHexString(i1)) + (i2 > 15 ? Integer.toHexString(i2) : "0" + Integer.toHexString(i2)) + (i3 > 15 ? Integer.toHexString(i3) : "0" + Integer.toHexString(i3));
		return hexa;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getScore()
	{
		return score;
	}
	
	public int[] getTeamColor()
	{
		return nameColor;
	}
	
	public int[] getTeamPos()
	{
		return startPos;
	}
	
	public void increaseScore()
	{
		score++;
	}
	
	public void increaseScore(int ammount)
	{
		score += ammount;
	}
	
	public void setScore(int ammount)
	{
		score = ammount;
	}
}