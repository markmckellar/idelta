package org.idelta;

import java.awt.geom.Point2D;

public class CompareResultPixel
{
	private Point2D point;
	private int rgb1;
	private int rgb2;

	private boolean inisdeOfIgnoreArea;

	public CompareResultPixel(Point2D point, int rgb1, int rgb2)
	{
		setPoint(point);
		this.setRgb1(rgb1);
		this.setRgb2(rgb2);
		this.setInisdeOfIgnoreArea(false);
	}

	public String toString()
	{
		return("(" + getPoint().getX() + "," + this.getPoint().getY() + ",rd" + this.getRedDiff() + ",gd=" + this.getGreenDiff() + ",bd=" + this.getBlueDiff() + ",md=" + this.getMaxDiff() + ",td="
				+ this.getTotalDiff() + ")");
	}

	public boolean arePointPositionsEqual(CompareResultPixel compareResultPixel)
	{
		boolean areEqual = false;
		if(getPoint().getX() == compareResultPixel.getPoint().getX() && getPoint().getY() == compareResultPixel.getPoint().getY())
		{
			areEqual = true;
		}
		return(areEqual);
	}

	/**
	 * @return (the sums of the differences of red green and blue) /  (256.0 * 3.0)
	 */
	public double getPercentDiff()
	{
		double percentDiff = getTotalDiff() / (256.0 * 3.0);
		return(percentDiff);
	}

	/**
	 * @return the maximum differences for red green and blue
	 */
	public int getMaxDiff()
	{
		return(Math.max(Math.max(getRedDiff(), getGreenDiff()), getBlueDiff()));
	}

	/**
	 * @return the sums of the differences of red green and blue
	 */
	public int getTotalDiff()
	{
		return(getRedDiff() + getGreenDiff() + getBlueDiff());
	}

	public int getRedDiff()
	{
		int redDiff = Math.abs(getRed1() - getRed2());
		return redDiff;
	}

	public int getGreenDiff()
	{
		int greenDiff = Math.abs(getGreen1() - getGreen2());
		return greenDiff;
	}

	public int getBlueDiff()
	{

		int blueDiff = Math.abs(getBlue1() - getBlue2());
		return blueDiff;
	}

	public String getPointHashKey()
	{
		return(getPoint().getX() + ":" + getPoint().getY());
	}

	public Point2D getPoint()
	{
		return point;
	}

	public void setPoint(Point2D point)
	{
		this.point = point;
	}

	public boolean isInisdeOfIgnoreArea()
	{
		return inisdeOfIgnoreArea;
	}

	public void setInisdeOfIgnoreArea(boolean inisdeOfIgnoreArea)
	{
		this.inisdeOfIgnoreArea = inisdeOfIgnoreArea;
	}

	public int getRgb1()
	{
		return rgb1;
	}

	public void setRgb1(int rgb1)
	{
		this.rgb1 = rgb1;
	}

	public int getRgb2()
	{
		return rgb2;
	}

	public void setRgb2(int rgb2)
	{
		this.rgb2 = rgb2;
	}

	public int getRed1()
	{
		int r1 = (getRgb1() >> 16) & 0xff;
		return(r1);
	}

	public int getRed2()
	{
		int r2 = (getRgb2() >> 16) & 0xff;
		return(r2);
	}

	public int getGreen1()
	{
		int g1 = (getRgb1() >> 8) & 0xff;
		return(g1);
	}

	public int getGreen2()
	{
		int g2 = (getRgb2() >> 8) & 0xff;
		return(g2);
	}

	public int getBlue1()
	{
		int b1 = (getRgb1()) & 0xff;
		return(b1);
	}

	public int getBlue2()
	{
		int b2 = (getRgb2()) & 0xff;
		return(b2);
	}

}