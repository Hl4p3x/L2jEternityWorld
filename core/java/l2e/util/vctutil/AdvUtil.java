package l2e.util.vctutil;

import java.util.Arrays;
import java.util.Collection;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.L2Character;

public class AdvUtil
{
	public final static boolean findIntersection(Location p1, Location p2, Location p3, Location p4)
	{
		if ((p1.getX() == p2.getX()) && (p1.getY() == p2.getY()))
		{
			return false;
		}
		float xD1, yD1, xD2, yD2, xD3, yD3;
		float dot, deg, len1, len2;
		float segmentLen1, segmentLen2;
		float ua, div;
		
		xD1 = (p2.getX() - p1.getX());
		xD2 = (p4.getX() - p3.getX());
		yD1 = (p2.getY() - p1.getY());
		yD2 = (p4.getY() - p3.getY());
		xD3 = (p1.getX() - p3.getX());
		yD3 = (p1.getY() - p3.getY());
		
		len1 = (float) Math.sqrt((xD1 * xD1) + (yD1 * yD1));
		len2 = (float) Math.sqrt((xD2 * xD2) + (yD2 * yD2));
		
		dot = ((xD1 * xD2) + (yD1 * yD2));
		deg = dot / (len1 * len2);
		
		if (Math.abs(deg) == 1)
		{
			return false;
		}
		
		div = (yD2 * xD1) - (xD2 * yD1);
		ua = ((xD2 * yD3) - (yD2 * xD3)) / div;
		
		float ptX = p1.getX() + (ua * xD1);
		float ptY = p1.getY() + (ua * yD1);
		
		xD1 = ptX - p1.getX();
		xD2 = ptX - p2.getX();
		yD1 = ptY - p1.getY();
		yD2 = ptY - p2.getY();
		segmentLen1 = (float) (Math.sqrt((xD1 * xD1) + (yD1 * yD1)) + Math.sqrt((xD2 * xD2) + (yD2 * yD2)));
		
		xD1 = ptX - p3.getX();
		xD2 = ptX - p4.getX();
		yD1 = ptY - p3.getY();
		yD2 = ptY - p4.getY();
		segmentLen2 = (float) (Math.sqrt((xD1 * xD1) + (yD1 * yD1)) + Math.sqrt((xD2 * xD2) + (yD2 * yD2)));
		
		if ((Math.abs(len1 - segmentLen1) > 0.02) || (Math.abs(len2 - segmentLen2) > 0.02))
		{
			return false;
		}
		
		return true;
	}
	
	public static final L2Character[] cleanCopyTargetList(Collection<L2Character> a)
	{
		if ((a == null) || (a.size() == 0))
		{
			return new L2Character[0];
		}
		
		L2Character[] r = new L2Character[a.size()];
		int i = 0;
		synchronized (a)
		{
			for (L2Character c : a)
			{
				if ((c != null) && (i < r.length))
				{
					r[i] = c;
					i++;
				}
			}
		}
		if (i < (r.length - 1))
		{
			return Arrays.copyOf(r, i);
		}
		return r;
	}
}
