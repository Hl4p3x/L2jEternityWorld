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
package l2e.gameserver.model;

import java.util.ArrayList;

import javolution.text.TextBuilder;
import l2e.util.Rnd;

public class L2Bingo
{
	protected static final String template = "%msg%<br><br>%choices%<br><br>%board%";
	protected static final String template_final = "%msg%<br><br>%board%";
	protected static final String template_board = "For your information, below is your current selection.<br><table border=\"1\" border color=\"white\" width=100><tr><td align=\"center\">%cell1%</td><td align=\"center\">%cell2%</td><td align=\"center\">%cell3%</td></tr><tr><td align=\"center\">%cell4%</td><td align=\"center\">%cell5%</td><td align=\"center\">%cell6%</td></tr><tr><td align=\"center\">%cell7%</td><td align=\"center\">%cell8%</td><td align=\"center\">%cell9%</td></tr></table>";
	protected static final String msg_again = "You have already selected that number. Choose your %choicenum% number again.";
	protected static final String msg_begin = "I've arranged 9 numbers on the panel.<br>Now, select your %choicenum% number.";
	protected static final String msg_next = "Now, choose your %choicenum% number.";
	protected static final String msg_0lines = "You are spectacularly unlucky! The red-colored numbers on the panel below are the ones you chose. As you can see, they didn't create even a single line. Did you know that it is harder not to create a single line than creating all 3 lines?";
	protected static final String msg_3lines = "You've created 3 lines! The red colored numbers on the bingo panel below are the numbers you chose. Congratulations!";
	protected static final String msg_lose = "Hmm... You didn't make 3 lines. Why don't you try again? The red-colored numbers on the panel are the ones you chose.";
	protected static final String[] nums =
	{
		"first",
		"second",
		"third",
		"fourth",
		"fifth",
		"final"
	};
	protected int lines;
	private final String _template_choice;
	private final ArrayList<Integer> board = new ArrayList<>();
	private final ArrayList<Integer> guesses = new ArrayList<>();
	
	public L2Bingo(String template_choice)
	{
		_template_choice = template_choice;
		while (board.size() < 9)
		{
			int num = Rnd.get(1, 9);
			if (!(board.contains(Integer.valueOf(num))))
			{
				board.add(num);
			}
		}
	}
	
	public String Select(String s)
	{
		try
		{
			return Select(Integer.valueOf(s));
		}
		catch (Exception ignored)
		{
		}
		return null;
	}
	
	public String Select(int choise)
	{
		if ((choise < 1) || (choise > 9))
		{
			return null;
		}
		if (guesses.contains(Integer.valueOf(choise)))
		{
			return getDialog("You have already selected that number. Choose your %choicenum% number again.");
		}
		guesses.add(choise);
		if (guesses.size() == 6)
		{
			return getFinal();
		}
		return getDialog("");
	}
	
	protected String getBoard()
	{
		if (guesses.size() == 0)
		{
			return "";
		}
		String result = "For your information, below is your current selection.<br><table border=\"1\" border color=\"white\" width=100><tr><td align=\"center\">%cell1%</td><td align=\"center\">%cell2%</td><td align=\"center\">%cell3%</td></tr><tr><td align=\"center\">%cell4%</td><td align=\"center\">%cell5%</td><td align=\"center\">%cell6%</td></tr><tr><td align=\"center\">%cell7%</td><td align=\"center\">%cell8%</td><td align=\"center\">%cell9%</td></tr></table>";
		for (int i = 1; i <= 9; ++i)
		{
			String cell = new StringBuilder().append("%cell").append(String.valueOf(i)).append("%").toString();
			int num = board.get(i - 1);
			if (guesses.contains(Integer.valueOf(num)))
			{
				result = result.replaceFirst(cell, new StringBuilder().append("<font color=\"").append((guesses.size() == 6) ? "ff0000" : "ffff00").append("\">").append(String.valueOf(num)).append("</font>").toString());
			}
			else
			{
				result = result.replaceFirst(cell, "?");
			}
		}
		return result;
	}
	
	public String getDialog(String _msg)
	{
		String result = "<html><body>%msg%<br><br>%choices%<br><br>%board%</body></html>";
		if (guesses.size() == 0)
		{
			result = result.replaceFirst("%msg%", "I've arranged 9 numbers on the panel.<br>Now, select your %choicenum% number.");
		}
		else
		{
			result = result.replaceFirst("%msg%", (_msg.equalsIgnoreCase("")) ? "Now, choose your %choicenum% number." : _msg);
		}
		result = result.replaceFirst("%choicenum%", nums[guesses.size()]);
		TextBuilder choices = TextBuilder.newInstance();
		for (int i = 1; i <= 9; ++i)
		{
			if (!(guesses.contains(Integer.valueOf(i))))
			{
				choices.append(_template_choice.replaceAll("%n%", String.valueOf(i)));
			}
		}
		result = result.replaceFirst("%choices%", choices.toString());
		result = result.replaceFirst("%board%", getBoard());
		return result;
	}
	
	protected String getFinal()
	{
		String result = "<html><body>%msg%<br><br>%board%</body></html>".replaceFirst("%board%", getBoard());
		calcLines();
		if (lines == 3)
		{
			result = result.replaceFirst("%msg%", "You've created 3 lines! The red colored numbers on the bingo panel below are the numbers you chose. Congratulations!");
		}
		else if (lines == 0)
		{
			result = result.replaceFirst("%msg%", "You are spectacularly unlucky! The red-colored numbers on the panel below are the ones you chose. As you can see, they didn't create even a single line. Did you know that it is harder not to create a single line than creating all 3 lines?");
		}
		else
		{
			result = result.replaceFirst("%msg%", "Hmm... You didn't make 3 lines. Why don't you try again? The red-colored numbers on the panel are the ones you chose.");
		}
		return result;
	}
	
	public int calcLines()
	{
		lines = 0;
		
		lines += ((checkLine(0, 1, 2)) ? 1 : 0);
		lines += ((checkLine(3, 4, 5)) ? 1 : 0);
		lines += ((checkLine(6, 7, 8)) ? 1 : 0);
		
		lines += ((checkLine(0, 3, 6)) ? 1 : 0);
		lines += ((checkLine(1, 4, 7)) ? 1 : 0);
		lines += ((checkLine(2, 5, 8)) ? 1 : 0);
		
		lines += ((checkLine(0, 4, 8)) ? 1 : 0);
		lines += ((checkLine(2, 4, 6)) ? 1 : 0);
		return lines;
	}
	
	public boolean checkLine(int idx1, int idx2, int idx3)
	{
		return ((guesses.contains(board.get(idx1))) && (guesses.contains(board.get(idx2))) && (guesses.contains(board.get(idx3))));
	}
}