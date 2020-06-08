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

import l2e.gameserver.data.xml.CharTemplateParser;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.network.serverpackets.NewCharacterSuccess;

public final class NewCharacter extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final NewCharacterSuccess ct = new NewCharacterSuccess();
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.fighter));
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.mage));
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.elvenFighter));
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.elvenMage));
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.darkFighter));
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.darkMage));
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.orcFighter));
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.orcMage));
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.dwarvenFighter));
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.maleSoldier));
		ct.addChar(CharTemplateParser.getInstance().getTemplate(ClassId.femaleSoldier));
		sendPacket(ct);
	}
}