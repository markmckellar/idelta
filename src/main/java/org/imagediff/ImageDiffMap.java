package org.imagediff;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;


public class ImageDiffMap {
	transient private BufferedImage image1;
	transient private BufferedImage image2;
	transient private BufferedImage diffImage;
	transient private Color diffColor;
	private PixelCompareResult pixelCompareResult;
	private ImageDiffArea imageDiffArea;
	
	public ImageDiffMap(BufferedImage image1, BufferedImage image2,Color diffColor,int margin) {
		super();
		this.image1 = image1;
		this.image2 = image2;
		this.setDiffImage(null);
		this.setDiffColor(diffColor);
		this.setImageDiffArea(new ImageDiffArea(margin));
		resetData();
	}
	
	public String getInfoString() {
		return("width1="+getImage1().getWidth()+
				":height1="+getImage1().getHeight()+
				":width2="+getImage2().getWidth()+
				":height2="+getImage2().getHeight()+
				":colorSpace.numComponents="+getImage1().getColorModel().getColorSpace().getNumComponents()+
				":colorSpace.pixelSize="+getImage1().getColorModel().getPixelSize() );
	}
		
	public void resetData() {
		this.setPixelCompareResult(new PixelCompareResult());
		this.getImageDiffArea().clearImageDiffArea();
	}
	
	public BufferedImage getScaledImage(BufferedImage toScale,double scaleFactor) {
		int width = Double.valueOf(toScale.getWidth()*scaleFactor).intValue();
		int height = Double.valueOf(toScale.getHeight()*scaleFactor).intValue();
		Image scaledImage = toScale.getScaledInstance(
				width,
				height,
				Image.SCALE_FAST);
		
		BufferedImage bufferedImage = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bufferedImage.createGraphics();
	    bGr.drawImage(scaledImage, 0, 0, null);
	    bGr.dispose();
		return(bufferedImage);
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

	public BufferedImage getAnnotatedBufferedImage(BufferedImage bufferedImage, Stroke stroke, Color color) throws Exception
	{
		BufferedImage annotatedBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
		Graphics g = annotatedBufferedImage.getGraphics();
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.drawImage(bufferedImage, 0, 0, null);

		//System.out.println("ImageDiffMap:getAnnotatedBufferedImage:getImageDiffArea().getPixelCompareAreaList().size()="+
		//		getImageDiffArea().getPixelCompareAreaList().size());
		for(PixelCompareArea pca:getImageDiffArea().getPixelCompareAreaList()) {
			Area area = new Area();
			//System.out.println("ImageDiffMap:getAnnotatedBufferedImage:pca.getPixelAreas().size()="+
			//		pca.getPixelAreas().size());
			for(Shape shape:pca.getPixelAreas()) area.add(new Area(shape));
			
			//Stroke stroke = new BasicStroke(5);
		
			Shape bounds = area.getBounds();
			//bounds = area;
			{
				g2d.setPaint(getColorFromString("ff00000f"));
				g2d.fill(bounds);	
				g2d.setPaint(color);
				g2d.setStroke(stroke);
				g2d.draw(bounds);
			}	
		}
		g2d.dispose();
		return(annotatedBufferedImage);
	}
	
	public void analyze() throws Exception
	{
		resetData();
		int width1 = getImage1().getWidth(null);
		int width2 = getImage2().getWidth(null);
		int height1 = getImage1().getHeight(null);
		int height2 = getImage2().getHeight(null);
		BufferedImage diffImage = new BufferedImage(width1,height1, BufferedImage.TYPE_INT_ARGB);
		
		if((width1 == width2) && (height1 == height2))
		{
			for(int x = 0;x < width1;x++)
			{
				for(int y = 0;y < height1;y++)
				{
					PixelCompare pixelCompare = new PixelCompare(
							new Point2D.Double((double)x, (double)y), 
							getImage1().getRGB(x, y), 
							getImage2().getRGB(x, y) );	
					
					getPixelCompareResult().addPixelCompare(pixelCompare);
				}
			}
			getPixelCompareResult().refreshStats();
		
			PixelCompareResult pcr = getPixelCompareResult();
			//getPixelCompareResult().printQuickGraph(95);
			pcr.refreshStats();
			ImageDiffArea ida = this.getImageDiffArea();
			ida.addPixeCompareList(pcr.getPixelCompareList(95,100), true);
			ida.addPixeCompareList(pcr.getPixelCompareList(90,95), true);
			ida.addPixeCompareList(pcr.getPixelCompareList(85,90), true);
			ida.addPixeCompareList(pcr.getPixelCompareList(80,85), true);
			ida.removeSmallPixelCompareAreas(100);
		}
		
		for(PixelCompareArea pca:getImageDiffArea().getPixelCompareAreaList()) 
			pca.fillBoundingBox();
			
		this.setDiffImage(diffImage);
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

	public BufferedImage getDiffImage() {
		return diffImage;
	}

	public void setDiffImage(BufferedImage diffImage) {
		this.diffImage = diffImage;
	}

	public Color getDiffColor() {
		return diffColor;
	}

	public void setDiffColor(Color diffColor) {
		this.diffColor = diffColor;
	}

	public PixelCompareResult getPixelCompareResult() {
		return pixelCompareResult;
	}

	public void setPixelCompareResult(PixelCompareResult pixelCompareResult) {
		this.pixelCompareResult = pixelCompareResult;
	}

	public ImageDiffArea getImageDiffArea() {
		return imageDiffArea;
	}

	public void setImageDiffArea(ImageDiffArea imageDiffArea) {
		this.imageDiffArea = imageDiffArea;
	}



}
