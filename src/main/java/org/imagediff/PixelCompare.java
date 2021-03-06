package org.imagediff;


import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class PixelCompare implements Comparable<PixelCompare>
{
	private Point2D point;
	private int rgb1;
	private int rgb2;
	private double colorDistance;
	private double colorDistanceScale;
	
	public PixelCompare(Point2D point, int rgb1, int rgb2)
	{
		setPoint(point);
		this.setRgb1( rgb1 );
		this.setRgb2( rgb2 );
		this.setColorDistance(this.getColorDistanceCalc());
		this.setColorDistanceScale(this.getColorDistanceScaleCalc());
	}

	public void setBufferedImagePixel(BufferedImage bufferedImage,double cutoff,Color baseColor) throws Exception {
		if(getColorDistanceScale()>=cutoff)
			bufferedImage.setRGB(
					Double.valueOf(getPoint().getX()).intValue(),
					Double.valueOf(getPoint().getY()).intValue(),
					baseColor.getRGB());
	}
	
	public Color getDiffColorScale(double cutoff,Color baseColor) throws Exception
    {
		double scale = 0.0;
		if(getColorDistanceScale()>=cutoff) scale = 1.0;
		float transparency = Double.valueOf(scale).floatValue();   
	
    	Color color = new Color(
    			(float)baseColor.getRed()/255f,
    			(float)baseColor.getGreen()/255f,
    			(float)baseColor.getBlue()/255f,transparency);
    	return(color);
    }
	public int compareTo(PixelCompare pc) {
		return (Double.valueOf(getColorDistance()).compareTo(Double.valueOf(pc.getColorDistance())));
	}


	public String getInfoString()
	{
		return("(" + 
					":x" + getPoint().getX() + 
					":y" + getPoint().getY() + 
					":rgb1=" + getRgb1() + 
					":rgb2=" + getRgb2() + 
					":r1=" + getRed1() +
					":b1=" + getBlue1() +
					":g1=" + getGreen1() +
					":r2=" + getRed2() +
					":b2=" + getBlue2() +
					":g2=" + getGreen2() +
					":rd=" + getRedDiff() + 
					":gd=" + getGreenDiff() + 
					":bd=" + getBlueDiff() + 
					":md=" + getMaxDiff() + 
					":td=" + getTotalDiff() +
					":td=" + getTotalDiff() +
					")");
	}
	
	public boolean arePointPositionsEqual(PixelCompare compareResultPixel)
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
	
	public double getColorDistanceCalc()
	{
		double distance = 
				Math.pow(this.getRedDiff(), 2.0)+
				Math.pow(this.getGreenDiff(), 2.0)+
				Math.pow(this.getBlueDiff(), 2.0)
				;
		
		return(distance);
	}
	
	public double getColorDistanceScaleCalc() {
		double colorDistance = getColorDistance();
		double scale = Math.pow(colorDistance/65536.0,0.5);
		if(scale>1.0) scale = 1.0;
		return(scale);
	}
	
	public int getHue1() {
		return( this.getHue(this.getRed1(),this.getGreen1(),this.getBlue1()));
	}
	
	public int getHue2() {
		return( this.getHue(this.getRed2(),this.getGreen2(),this.getBlue2()));
	}
	
	public int getHue(int red, int green, int blue) {

	    float min = Math.min(Math.min(red, green), blue);
	    float max = Math.max(Math.max(red, green), blue);

	    if (min == max) {
	        return 0;
	    }

	    float hue = 0f;
	    if (max == red) {
	        hue = (green - blue) / (max - min);

	    } else if (max == green) {
	        hue = 2f + (blue - red) / (max - min);

	    } else {
	        hue = 4f + (red - green) / (max - min);
	    }

	    hue = hue * 60;
	    if (hue < 0) hue = hue + 360;

	    return Math.round(hue);
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
		return( r1 );
	}

	public int getRed2()
	{
		int r2 = (getRgb2() >> 16) & 0xff;
		return( r2 );
	}

	public int getGreen1()
	{
		int g1 = (getRgb1() >> 8) & 0xff;
		return( g1 );
	}

	public int getGreen2()
	{
		int g2 = (getRgb2() >> 8) & 0xff;
		return( g2 );
	}

	public int getBlue1()
	{
		int b1 = (getRgb1()) & 0xff;
		return( b1 );
	}

	public int getBlue2()
	{
		int b2 = (getRgb2()) & 0xff;
		return( b2 );
	}

	public double getColorDistance() {
		return colorDistance;
	}

	public void setColorDistance(double colorDistance) {
		this.colorDistance = colorDistance;
	}

	public double getColorDistanceScale() {
		return colorDistanceScale;
	}

	public void setColorDistanceScale(double colorDistanceScale) {
		this.colorDistanceScale = colorDistanceScale;
	}


}