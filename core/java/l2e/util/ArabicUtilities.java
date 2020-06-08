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
package l2e.util;

import java.util.ArrayList;
import java.util.List;

public class ArabicUtilities
{
	private static boolean isArabicCharacter(char target)
	{
		for (char[] element : ArabicReshaper.ARABIC_GLPHIES)
		{
			if (element[0] == target)
			{
				return true;
			}
		}
		
		for (char element : ArabicReshaper.HARAKATE)
		{
			if (element == target)
			{
				return true;
			}
		}
		return false;
	}
	
	private static String[] getWords(String sentence)
	{
		if (sentence != null)
		{
			return sentence.split("\\s");
		}
		return new String[0];
	}
	
	public static boolean hasArabicLetters(String word)
	{
		for (int i = 0; i < word.length(); i++)
		{
			
			if (isArabicCharacter(word.charAt(i)))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isArabicWord(String word)
	{
		for (int i = 0; i < word.length(); i++)
		{
			if (!isArabicCharacter(word.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}
	
	private static String[] getWordsFromMixedWord(String word)
	{
		List<String> finalWords = new ArrayList<>();
		
		String tempWord = "";
		
		for (int i = 0; i < word.length(); i++)
		{
			if (isArabicCharacter(word.charAt(i)))
			{
				if (!tempWord.equals("") && !isArabicWord(tempWord))
				{
					finalWords.add(tempWord);
					tempWord = "" + word.charAt(i);	
				}
				else
				{
					tempWord += word.charAt(i);
				}	
			}
			else
			{
				if (!tempWord.equals("") && isArabicWord(tempWord))
				{
					finalWords.add(tempWord);
					tempWord = "" + word.charAt(i);
				}
				else
				{
					tempWord += word.charAt(i);
				}
			}
		}
		
		if (!tempWord.equals(""))
		{
			finalWords.add(tempWord);
		}
		
		String[] theWords = new String[finalWords.size()];
		theWords = finalWords.toArray(theWords);
		
		return theWords;
	}
	
	public static String reshape(String allText)
	{
		if (allText != null)
		{
			StringBuffer result = new StringBuffer();
			String[] sentences = allText.split("\n");
			for (int i = 0; i < sentences.length; i++)
			{
				result.append(reshapeSentence(sentences[i]));

				if (i < (sentences.length - 1))
				{
					result.append("\n");
				}
			}
			return result.toString();
		}
		return null;
		
	}
	
	public static String reshapeSentence(String sentence)
	{
		String[] words = getWords(sentence);
		StringBuffer reshapedText = new StringBuffer("");
		
		for (int i = words.length - 1; i >= 0; i--)
		{
			if (hasArabicLetters(words[i]))
			{
				if (isArabicWord(words[i]))
				{
					ArabicReshaper arabicReshaper = new ArabicReshaper(words[i]);

					String initial = arabicReshaper.getReshapedWord();
					StringBuffer text = new StringBuffer(initial);
					text = text.reverse();
					initial = text.toString();
					reshapedText.append(initial);
				}
				else
				{
					String[] mixedWords = getWordsFromMixedWord(words[i]);

					for (String mixedWord : mixedWords)
					{
						ArabicReshaper arabicReshaper = new ArabicReshaper(mixedWord);
						reshapedText.append(arabicReshaper.getReshapedWord());
					}
				}
			}
			else
			{
				reshapedText.append(words[i]);
			}
			reshapedText.append(" ");
		}
		return reshapedText.toString();
	}	
}