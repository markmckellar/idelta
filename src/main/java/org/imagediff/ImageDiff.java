package org.imagediff;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.idelta.CompareResultArea;
import org.idelta.CompareResultPixel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ImageDiff
{
	private BufferedImage image1;
	private BufferedImage image2;
	private File tempFile1;
	private File tempFile2;
	
	public static void main(String args[]) throws Exception {
		if(args.length!=3) throw new Exception("Expected 3 argumens, got "+
				args.length+
				": file1 file2 diffFile margin colorDiffCutoff");
		String file1 = args[0];
		String file2 = args[1];
		String file3 = args[2];
		
    	Gson gson = new Gson();
		Type objectType = new TypeToken<String[]>() {}.getType();
		String json = gson.toJson(args,objectType);
		System.out.println("args="+json);
		
		ImageDiff imageCompare = new ImageDiff(file1,file2);
		ImageDiffMap imageDiffMap = new ImageDiffMap(imageCompare.getImage1(),imageCompare.getImage2(),Color.BLACK);
		imageDiffMap.analyze();
		
		if(imageDiffMap.getDiffImage()!=null)
		{
			File annotatedBufferedImageFile = new File(file3);	
			System.out.println("writing to : "+file3);
		    ImageIO.write(imageCompare.overlayImage(
		    		imageCompare.getImage2(),
		    		imageDiffMap.getDiffImage()),
		    		"png", annotatedBufferedImageFile);
		    System.out.println("wrote : "+file3);
		}
		else
		{
			System.out.println("getDiffImage was null!!");

		}
		
		imageCompare.analyze();
		
		//imageCompare.analyze();
		//////BufferedImage annotatedBufferedImage = imageCompare.getAnnotatedBufferedImage(imageCompare.getImage2(),new BasicStroke(3.0f), Color.RED);
		////////////File annotatedBufferedImageFile = new File(file3);
		/////////System.out.println("writing to : "+file3);

	    ////////////ImageIO.write(annotatedBufferedImage, "png", annotatedBufferedImageFile);
	    
		//////////////System.out.println("wrote : "+file3);


	}
	
	/**
	 * @param image1FileString file location of first image to compare
	 * @param image2FileString file location of second image to compare
	 * @param margin
	 * @param areaColorDiffCuttoff
	 * @param ingoreAreaList
	 * @throws Exception
	 */
	public ImageDiff(String image1FileString, String image2FileString) throws Exception
	{
		this.setImage1(ImageIO.read( new File(image1FileString)));
		this.setImage2(ImageIO.read( new File(image2FileString)));
		
		
		//this.setImage1( getScaledImage(getImage1(),0.25));
		//this.setImage2( getScaledImage(getImage2(),0.25));
		
	}

	/**
	 * @param img1 1st image to compare
	 * @param img2 2nd image to compare
	 * @param margin
	 * @param areaColorDiffCuttoff
	 * @param ingoreAreaList
	 */
	public ImageDiff(BufferedImage img1, BufferedImage img2)
	{
		this.setImage1(img1);
		this.setImage2(img2);
	}
	
	
	public BufferedImage getScaledImage(BufferedImage toScale,double scaleFactor) {
		int width = new Double(toScale.getWidth()*scaleFactor).intValue();
		int height = new Double(toScale.getHeight()*scaleFactor).intValue();
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
	
	public BufferedImage overlayImage(BufferedImage bottom,BufferedImage top) {
		
		BufferedImage bufferedImage = new BufferedImage(bottom.getWidth(),bottom.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bufferedImage.createGraphics();
	    bGr.drawImage(bottom, 0, 0, null);
	    bGr.drawImage(top, 0, 0, null);
	    bGr.dispose();
		return(bufferedImage);
	}


	public void saveImagesToTempFiles() throws Exception
	{
		File image1TempFile = File.createTempFile(hashCode()+"_1","png");
	    ImageIO.write(getImage1(), "png", image1TempFile);
	    image1TempFile.deleteOnExit();
	    setImage1(null);
	    setTempFile1(image1TempFile);
	    
		File image2TempFile = File.createTempFile(hashCode()+"_2","png");
	    ImageIO.write(getImage2(), "png", image2TempFile);
	    image2TempFile.deleteOnExit();	    
	    setImage2(null);
	    setTempFile2(image2TempFile);
	}
	
	public void loadImagesFromTempFiles() throws Exception
	{
		if(getTempFile1()!=null)
		{
			setImage1(ImageIO.read(getTempFile1()));
		}
		
		if(getTempFile2()!=null)
		{
			setImage2(ImageIO.read(getTempFile2()));
		}
	}
	
	public void deleteTempFiles() throws Exception
	{
		if(getTempFile1()!=null)
		{
			getTempFile1().delete();
		    setTempFile1(null);
		}
		
		if(getTempFile2()!=null)
		{
			getTempFile2().delete();
		    setTempFile2(null);
		}
	}


	public void analyze()
	{
		int width1 = getImage1().getWidth();
		int width2 = getImage2().getWidth();
		int height1 = getImage1().getHeight();
		int height2 = getImage2().getHeight();
		int totalPixles = width1*height1;
		int lastPercent = 0;
		
		List<ImageCompare> imageCompareList = new ArrayList<ImageCompare>(); 
		imageCompareList.add(new ImageCompare(getImage1(),getImage2(),1.0));
		
		if((width1 == width2) && (height1 == height2))
		{
			int w = width1;
			int h = width1;
			double scaleFactor = 0.5;
			
			ImageCompare imageCompare = imageCompareList.get(imageCompareList.size()-1);

			
			while(
					(imageCompare.getImage1().getWidth()*scaleFactor) >=1 && 
					(imageCompare.getImage1().getHeight()*scaleFactor) >=1)  {
				
				imageCompare = imageCompareList.get(imageCompareList.size()-1);
				//System.out.println("ImageDiff:------------------------------------------------------------------");
				//System.out.println("ImageDiff:BEFORE:"+imageCompare.getInfoString());
								
				imageCompare = new ImageCompare(
						getScaledImage(imageCompare.getImage1(),scaleFactor),
						getScaledImage(imageCompare.getImage2(),scaleFactor),
						(imageCompare.getImage1().getWidth()  /  (width1*1.0))*scaleFactor
						);
				
				imageCompareList.add(imageCompare);
				
				imageCompare = imageCompareList.get(imageCompareList.size()-1);			
				//System.out.println("ImageDiff:AFTER:"+imageCompare.getInfoString());
			}
		}
		
		Collections.reverse(imageCompareList);
		
		ImageCompare bestImageCompare;
		
		for(ImageCompare imageCompare:imageCompareList) {
			imageCompare.analyze();
			
			System.out.println("ImageDiff:------------------------------------------------------------------");
			System.out.println("ImageDiff:INFO:"+imageCompare.getInfoString());
			System.out.println("ImageDiff:RESULT:"+imageCompare.getResultString());
			if(imageCompare.getNotMatchedPixles()!=0) break;
		}
	}

	public int getTotalDiffrentPixles()
	{
		
		int totalDiffrentPixles = 0;
		/*
		for(CompareResultArea compareResultArea:this.getCompareResultAreaList())
			totalDiffrentPixles += compareResultArea.getCompareResultAreaPixelList().size();
		System.out.println("ImageCompare:getTotalDiffrentPixles()=" + totalDiffrentPixles);
		*/
		return(totalDiffrentPixles);
	}

	public int getTotalPixles()
	{
		System.out.println("getTotalPixles()=totalDiffrentPixles" + (this.getImage1().getWidth() * this.getImage1().getHeight()));
		return(this.getImage1().getWidth() * this.getImage1().getHeight());
	}

	
	public boolean isCompareResultPixelInIngoreAreas(CompareResultPixel compareResultPixel)
	{
		boolean isInIngoreAreas = false;
		/*
		for(IgnoreArea ingoreAreaList:getIgnoreAreaList())
		{
			isInIngoreAreas = ingoreAreaList.getMappedIgnoreRectangle(ingoreAreaList.getIgnoreRectangle(),getImage1().getWidth(),getImage1().getHeight()).contains(compareResultPixel.getPoint());
			if(isInIngoreAreas) break;
		}
		
		*/
		return(isInIngoreAreas);
	}

	/*
	public String toString()
	{
		return(getToStringValue());
	}
*/
	public boolean areImagesEqual()
	{
		return(getPercentDiff() == 0.0);
	}

	public int getMaxPixelDiff()
	{
		int maxPixelDiff = 0;
		/*
		for(CompareResultPixel compareResultPagePixel:getCompareResultPixelList())
		{
			if(compareResultPagePixel.getRedDiff() > maxPixelDiff) maxPixelDiff = compareResultPagePixel.getRedDiff();
			if(compareResultPagePixel.getGreenDiff() > maxPixelDiff) maxPixelDiff = compareResultPagePixel.getGreenDiff();
			if(compareResultPagePixel.getBlueDiff() > maxPixelDiff) maxPixelDiff = compareResultPagePixel.getBlueDiff();
		}
		*/
		return(maxPixelDiff);
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

	/**
	 * @param bufferedImage the image to use as a background to draw the image on
	 * @param stroke the stoke used to draw the anotation lines
	 * @param color what color to draw the anotations in
	 * @return
	 */
	public BufferedImage getAnnotatedBufferedImage(BufferedImage bufferedImage, Stroke stroke, Color color) throws Exception
	{
		BufferedImage annotatedBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
		Graphics g = annotatedBufferedImage.getGraphics();
		Graphics2D g2d = (Graphics2D)g;
		;
		g.drawImage(bufferedImage, 0, 0, null);

		// new BasicStroke(5) Color.BLUE
		/*
		for(CompareResultArea compareResultArea:this.getCompareResultAreaList())
		{
			g2d.setPaint(getColorFromString("ff000077"));
			g2d.fill(compareResultArea);
			
			g2d.setPaint(color);
			g2d.setStroke(stroke);
			g2d.draw(compareResultArea);
		}
		*/
		// BufferedImage(bufferedImage);
		return(annotatedBufferedImage);
	}

	public BufferedImage getCompareAreaAnnotatedBufferedImage(BufferedImage bufferedImage, CompareResultArea compareResultArea, Stroke stroke, Color color, double scaleFactor, double transparency)
	{
		Rectangle2D compareAreaRectangle = compareResultArea.getBounds2D();
		BufferedImage annotatedBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
		Graphics g = annotatedBufferedImage.getGraphics();
		Graphics2D g2d = (Graphics2D)g;
		g.drawImage(bufferedImage, 0, 0, null);

		// g2d.setPaint(color);
		// g2d.setStroke(stroke);
		// g2d.draw(compareResultArea);

		if(transparency > 0.0)
		{
			System.out.println("OVERLAY COLOR:transparency=" + transparency);
			Color overlayColor = new Color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)transparency);
			g2d.setPaint(overlayColor);
			System.out.println("OVERLAY COLOR:" + "red=" + overlayColor.getRed() + "green=" + overlayColor.getGreen() + "blue=" + overlayColor.getBlue() + "");

			/*
			for(CompareResultPixel compareResultPixel:compareResultArea.getCompareResultAreaPixelList())
			{
				g2d.fillRect((int)compareResultPixel.getPoint().getX(), (int)compareResultPixel.getPoint().getY(), 1, 1);
			}
			*/
		}
		BufferedImage subImage = annotatedBufferedImage.getSubimage((int)compareAreaRectangle.getX(), (int)compareAreaRectangle.getY(), (int)compareAreaRectangle.getWidth(),
				(int)compareAreaRectangle.getHeight());

		BufferedImage scaledImage = getResizedBufferedImage(subImage, scaleFactor);
		return(scaledImage);
	}

	public BufferedImage getResizedBufferedImage(BufferedImage bufferedImage, double scaleFactor)
	{
		int w = bufferedImage.getWidth();
		int h = bufferedImage.getHeight();
		BufferedImage resizedBufferedImage = new BufferedImage((int)(w * scaleFactor), (int)(h * scaleFactor), BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(scaleFactor, scaleFactor);
		AffineTransformOp scaleOp = new AffineTransformOp(at, null);
		resizedBufferedImage = scaleOp.filter(bufferedImage, resizedBufferedImage);
		return(resizedBufferedImage);
	}

	public double getPercentDiff()
	{

		double percentDiff = 0.0;
		percentDiff = (double)getTotalDiffrentPixles() / (double)getTotalPixles();
		return(percentDiff);

		/*
		 * double percentDiff = 100.0; int width1 = getImage1().getWidth(null);
		 * int width2 = getImage2().getWidth(null); int height1 =
		 * getImage1().getHeight(null); int height2 =
		 * getImage2().getHeight(null); if((width1 == width2) && (height1 ==
		 * height2)) { long diff = 0; for(CompareResultPixel
		 * compareResultPagePixel:getCompareResultPixelList()) {
		 * if(!compareResultPagePixel.isInisdeOfIgnoreArea()) {
		 * if(compareResultPagePixel.getRedDiff() > maxDiffCuttOff) diff +=
		 * compareResultPagePixel.getRedDiff();
		 * if(compareResultPagePixel.getGreenDiff() > maxDiffCuttOff) diff +=
		 * compareResultPagePixel.getGreenDiff();
		 * if(compareResultPagePixel.getBlueDiff() > maxDiffCuttOff) diff +=
		 * compareResultPagePixel.getBlueDiff(); // if(diffWas != diff)
		 * System.out.println(this + // ":maxDiffCuttOff=" + maxDiffCuttOff +
		 * " " + // compareResultPagePixel); } } double n = width1 * height1 *
		 * 3; percentDiff = diff / n / 255.0; } return(percentDiff);
		 */
	}

	public BufferedImage getImage1()
	{
		return image1;
	}

	public void setImage1(BufferedImage image1)
	{
		this.image1 = image1;
	}

	public BufferedImage getImage2()
	{
		return image2;
	}

	public void setImage2(BufferedImage image2)
	{
		this.image2 = image2;
	}



	public File getTempFile1()
	{
		return tempFile1;
	}

	public void setTempFile1(File tempFile1)
	{
		this.tempFile1 = tempFile1;
	}

	public File getTempFile2()
	{
		return tempFile2;
	}

	public void setTempFile2(File tempFile2)
	{
		this.tempFile2 = tempFile2;
	}

}