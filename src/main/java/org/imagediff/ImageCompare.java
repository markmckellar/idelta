package org.imagediff;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.idelta.CompareResultPixel;

public class ImageCompare {
	private BufferedImage image1;
	private BufferedImage image2;
	private List<PixelCompare> pixelCompareList;
	private double scaleFromOriginal;
	private double colorScaleFactor;
	private Color baseDiffColor;
	
	
	public ImageCompare(BufferedImage image1, BufferedImage image2,double scaleFromOriginal) {
		super();
		this.image1 = image1;
		this.image2 = image2;
		this.scaleFromOriginal = scaleFromOriginal;
		this.colorScaleFactor = 1.0;
		resetData();
	}
	
	public String getInfoString() {
		return("width1="+getImage1().getWidth()+
				":height1="+getImage1().getHeight()+
				":width2="+getImage2().getWidth()+
				":height2="+getImage2().getHeight()+
				":scaleFromOriginal="+getScaleFromOriginal()+
				":colorScaleFactor="+getColorScaleFactor()+
				":colorSpace.numComponents="+getImage1().getColorModel().getColorSpace().getNumComponents()+
				":colorSpace.pixelSize="+getImage1().getColorModel().getPixelSize()

				);
	}
	
	public String getResultString() {
		return("getNotPercentMatched="+getNotPercentMatched()+":getPercentMatched="+getPercentMatched()+
				":getTotalPixles="+getTotalPixles()+":getNotMatchedPixles="+getNotMatchedPixles());
	}
	
	
	public void resetData() {
		setPixelCompareList(new ArrayList<PixelCompare>());

	}
	
	public int getTotalPixles() {
		int width1 = getImage1().getWidth(null);
		int height1 = getImage1().getHeight(null);
		int totalPixles = width1*height1;
		return(totalPixles);
	}
	
	public double getPercentMatched() {
		return(1.0-getNotPercentMatched());
	}
	
	public double getNotPercentMatched() {
		return( this.getNotMatchedPixles() / (getTotalPixles()*1.0) );
	}
	
	public int getNotMatchedPixles() {
		return(this.getPixelCompareList().size());
	}
	
	public void analyze()
	{
		resetData();

		
		int width1 = getImage1().getWidth(null);
		int width2 = getImage2().getWidth(null);
		int height1 = getImage1().getHeight(null);
		int height2 = getImage2().getHeight(null);
		int totalPixles = width1*height1;
		int lastPercent = 0;
		
		if((width1 == width2) && (height1 == height2))
		{
			for(int x = 0;x < width1;x++)
			{
				for(int y = 0;y < height1;y++)
				{
					PixelCompare pixelCompare = new PixelCompare(
							new Point2D.Double((double)x, (double)y), 
							getImage1().getRGB(x, y), 
							getImage2().getRGB(x, y)
							);
					if(pixelCompare.getTotalDiff()!=0) {
						System.out.println("----- FINAL x="+x+":y="+y+" ----------------------------");
						System.out.println("pixelCompare:"+pixelCompare.getInfoString());
						getPixelCompareList().add(pixelCompare);
					}
				}
			}
		}
	}
	
	public static Color getColorFromString(String colorString) throws Exception
    {
    	float transparency = 1.0f;
    	if(colorString.length()==6)
    	{
    		// do nothing
    	}
    	else if(colorString.length()==8)
    	{
    		String transparencyString = colorString.substring(6);
    		float transparencyNumerator = Integer.parseInt(transparencyString, 16);
    		transparency = transparencyNumerator/255.0f;
    		colorString = colorString.substring(0,6);
    	}
    	else throw new Exception("color param must be 6 or 8 characters in length:"+colorString+" is "+colorString.length());
    	
    	Color color = Color.decode("0x"+colorString);
    
    	color = new Color(
    			(float)color.getRed()/255f,
    			(float)color.getGreen()/255f,
    			(float)color.getBlue()/255f,transparency);
    	return(color);
    }
		
	public BufferedImage getImage1() {
		return image1;
	}
	public void setImage1(BufferedImage image1) {
		this.image1 = image1;
	}
	public BufferedImage getImage2() {
		return image2;
	}
	public void setImage2(BufferedImage image2) {
		this.image2 = image2;
	}

	public List<PixelCompare> getPixelCompareList() {
		return pixelCompareList;
	}

	public void setPixelCompareList(List<PixelCompare> pixelCompareList) {
		this.pixelCompareList = pixelCompareList;
	}

	public double getScaleFromOriginal() {
		return scaleFromOriginal;
	}

	public void setScaleFromOriginal(double scaleFromOriginal) {
		this.scaleFromOriginal = scaleFromOriginal;
	}

	public double getColorScaleFactor() {
		return colorScaleFactor;
	}

	public void setColorScaleFactor(double colorScaleFactor) {
		this.colorScaleFactor = colorScaleFactor;
	}

	public Color getBaseDiffColor() {
		return baseDiffColor;
	}

	public void setBaseDiffColor(Color baseDiffColor) {
		this.baseDiffColor = baseDiffColor;
	}


	
}
