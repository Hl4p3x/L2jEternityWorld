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
package l2e.gameserver.network.serverpackets;

public class ExStartScenePlayer extends L2GameServerPacket
{
	private final int _movieId;

    	public static final int LINDVIOR 		= 1;	// Lindvior spawns and dissapear at Keuceus base	44500ms
    	public static final int EKIMUS_OPENING 		= 2; 	// Ekimus intro						62000ms
    	public static final int EKIMUS_SUCCESS 		= 3; 	// Ekimus death						18000ms
    	public static final int EKIMUS_FAIL 		= 4; 	// Ekimus wins						17000ms
    	public static final int TIAT_OPENING 		= 5; 	// Tiat intro						54200ms
    	public static final int TIAT_SUCCESS 		= 6; 	// Tiat death						26100ms
    	public static final int TIAT_FAIL 		= 7; 	// Tiat win						24800ms
    	public static final int SSQ_SUSPECIOUS_DEATHS 	= 8; 	// Death in mines					26000ms
    	public static final int SSQ_DYING_MASSAGE 	= 9;  	// Priest praying?					27000ms
    	public static final int SSQ_CONTRACT_OF_MAMMON 	= 10; 	// Story of Shunaiman?					98000ms
    	public static final int SSQ_RITUAL_OF_PRIEST 	= 11;	// 4 Priests performing a ritual			30000ms
    	public static final int SSQ_SEALING_EMPEROR_1ST = 12;	// Request of Anakim					18000ms
    	public static final int SSQ_SEALING_EMPEROR_2ND = 13;	// Sealing seal?					26000ms
    	public static final int SSQ_EMBRYO 		= 14;	//							28000ms
    	public static final int LAND_KSERTH_A 		= 1000; // Ship landing on Keuceus base port A			10000ms
    	public static final int LAND_KSERTH_B 		= 1001; // Ship landing on Keuceus base port B			10000ms
    	public static final int LAND_UNDEAD_A 		= 1002; // Ship landing on seed of infinity			10000ms
    	public static final int LAND_DISTRUCTION_A 	= 1004; // Ship landing on seed of destruction	
	
	public ExStartScenePlayer(int id)
	{
		_movieId = id;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x99);
		writeD(_movieId);
	}
}