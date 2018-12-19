package org.idelta;

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
import java.util.List;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ImageCompare
{
	private BufferedImage image1;
	private BufferedImage image2;
	private List<CompareResultPixel> compareResultPixelList;
	private List<CompareResultArea> compareResultAreaList;
	private String toStringValue;
	private List<IgnoreArea> ignoreAreaList;
	private double margin;
	private double areaColorDiffCuttoff;
	private boolean reanalyze;
	private File tempFile1;
	private File tempFile2;
	
	public static void main(String args[]) throws Exception {
		if(args.length!=5) throw new Exception("Expected 5 argumens : file1 file2 diffFile margin colorDiffCutoff");
		String file1 = args[0];
		String file2 = args[1];
		String file3 = args[2];
		String marginString = args[3];
		String colorDiffCutoff = args[4];
		
    	Gson gson = new Gson();
		Type objectType = new TypeToken<String[]>() {}.getType();
		String json = gson.toJson(args,objectType);
		System.out.println("args="+json);
		
		ImageCompare imageCompare = new ImageCompare(file1,file2,
				Double.parseDouble(marginString),
				Double.parseDouble(colorDiffCutoff),
				new ArrayList<IgnoreArea>());
		imageCompare.analyze();
		BufferedImage annotatedBufferedImage = imageCompare.getAnnotatedBufferedImage(imageCompare.getImage2(),new BasicStroke(3.0f), Color.RED);
		File annotatedBufferedImageFile = new File(file3);
		System.out.println("writing to : "+file3);

	    ImageIO.write(annotatedBufferedImage, "png", annotatedBufferedImageFile);
	    
		System.out.println("wrote : "+file3);


	}
	
	/**
	 * @param image1FileString file location of first image to compare
	 * @param image2FileString file location of second image to compare
	 * @param margin
	 * @param areaColorDiffCuttoff
	 * @param ingoreAreaList
	 * @throws Exception
	 */
	public ImageCompare(String image1FileString, String image2FileString, double margin, double areaColorDiffCuttoff,List<IgnoreArea> ingoreAreaList) throws Exception
	{
		this.ignoreAreaList = ingoreAreaList;		
		this.setImage1(ImageIO.read( new File(image1FileString)));
		this.setImage2(ImageIO.read( new File(image2FileString)));
		
		
		this.setImage1( getScaledImage(getImage1(),0.25));
		this.setImage2( getScaledImage(getImage2(),0.25));
		
		this.setMargin(margin);
		this.setAreaColorDiffCuttoff(areaColorDiffCuttoff);
		this.setReanalyze(false);
	}
	
	public BufferedImage getScaledImage(BufferedImage toScale,double scaleFactor) {
		int width = new Double(toScale.getWidth()*scaleFactor).intValue();
		int height = new Double(toScale.getHeight()*scaleFactor).intValue();
		Image scaledImage = toScale.getScaledInstance(
				width,
				height,
				Image.SCALE_AREA_AVERAGING);
		
		BufferedImage bufferedImage = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bufferedImage.createGraphics();
	    bGr.drawImage(scaledImage, 0, 0, null);
	    bGr.dispose();
		return(bufferedImage);
	}

	/**
	 * @param img1 1st image to compare
	 * @param img2 2nd image to compare
	 * @param margin
	 * @param areaColorDiffCuttoff
	 * @param ingoreAreaList
	 */
	public ImageCompare(BufferedImage img1, BufferedImage img2, double margin, double areaColorDiffCuttoff, List<IgnoreArea> ingoreAreaList)
	{
		this.ignoreAreaList = ingoreAreaList;		
		this.setImage1(img1);
		this.setImage2(img2);
		this.setMargin(margin);
		this.setAreaColorDiffCuttoff(areaColorDiffCuttoff);
		this.setReanalyze(false);
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
		this.setReanalyze(false);
		setCompareResultPixelList(new ArrayList<CompareResultPixel>());
		setCompareResultAreaList(new ArrayList<CompareResultArea>());

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
					CompareResultPixel compareResultPixel = new CompareResultPixel(new Point2D.Double((double)x, (double)y), getImage1().getRGB(x, y), getImage2().getRGB(x, y));
					if(compareResultPixel.getTotalDiff() != 0)
					{
						if(compareResultPixel.getPercentDiff() > 0.015)
						{
							// it is is inside an ingore area we will "ingore it" ;)
							compareResultPixel.setInisdeOfIgnoreArea(isCompareResultPixelInIngoreAreas(compareResultPixel));
							if(!compareResultPixel.isInisdeOfIgnoreArea()) getCompareResultPixelList().add(compareResultPixel);
						}
					}
				}
				int pixlesDone = x*height1;
				double percentDone = (1.0*pixlesDone)/totalPixles;				
				int percentDoneInt = new Double(percentDone * 100).intValue();
				if(percentDoneInt>lastPercent)
				{
					lastPercent = percentDoneInt;
					System.out.println("Loop1:Percent done:"+percentDoneInt);
				}
			}
		}

		List<CompareResultArea> compareResultAreaList = new ArrayList<CompareResultArea>();
		// find each compareResultPixel that had differences
		int totalCompareResultPixel = getCompareResultPixelList().size();
		lastPercent = 0;
		int compareResultPixelDone = 0;
		
		System.out.println("ImageCompare:------------------------------------------------------------------");
		System.out.println("ImageCompare:getCompareResultPixelList().size()=" + getCompareResultPixelList().size() + ":margin=" + getMargin());
		System.out.printf("ImageCompare:percent diffrent=%.1f"+":pixles diffrent="+getCompareResultPixelList().size()+":total pixles="+totalPixles+"\n",
				( (getCompareResultPixelList().size() )/ (totalPixles*0.1) )
				);
		System.out.println("ImageCompare:------------------------------------------------------------------");
		
		// loop over all of the pixels which are different
		for(CompareResultPixel compareResultPixel:getCompareResultPixelList())
		{

			// System.out.println("ImageCompare:analyze:adding to compare areas:compareResultPixel=" + compareResultPixel);
			// make a new area and find out what (if any) areas it overlaps
			CompareResultArea newCompareResultArea = new CompareResultArea(getMargin(), compareResultPixel);
			boolean added = false;
			for(CompareResultArea compareResultArea:compareResultAreaList)
			{
				// if it is going to overlap an existing area
				if(newCompareResultArea.getBounds2D().intersects(compareResultArea.getBounds2D()))
				{
					if(newCompareResultArea.isOverlaping(compareResultArea))
					{
						added = true;
						compareResultArea.addCompareResultPixel(compareResultPixel);
						break;
					}
				}
			}
			if(!added)
			{
				compareResultAreaList.add(newCompareResultArea);
			}
			
			compareResultPixelDone++;
			double percentDone = (1.0*compareResultPixelDone)/totalCompareResultPixel;				
			int percentDoneInt = new Double(percentDone * 100).intValue();
			if(percentDoneInt>lastPercent)
			{
				lastPercent = percentDoneInt;
				System.out.println("Loop2:Percent done:"+percentDoneInt+
						":totalCompareResultPixel="+totalCompareResultPixel+
						":compareResultAreaList.size"+compareResultAreaList.size());
			}
		}

		// merge any overlapping lists
		boolean checkAgainForMerge = true;
		System.out.println("\nImageCompare:compareResultAreaList.size()=" + compareResultAreaList.size());
		while(checkAgainForMerge)
		{
			List<CompareResultArea> mergedCompareResultAreaList = new ArrayList<CompareResultArea>();
			checkAgainForMerge = false;
			for(CompareResultArea compareResultArea:compareResultAreaList)
			{
				boolean added = false;
				for(CompareResultArea mergedCompareResultArea:mergedCompareResultAreaList)
				{
					if(mergedCompareResultArea.isOverlaping(compareResultArea))
					{
						mergedCompareResultArea.mergeCompareResultArea(compareResultArea);
						added = true;
						checkAgainForMerge = true;
						break;
					}
				}
				if(!added)
				{
					mergedCompareResultAreaList.add(compareResultArea);
				}

			}
			compareResultAreaList.clear();
			compareResultAreaList.addAll(mergedCompareResultAreaList);
			mergedCompareResultAreaList.clear();
		}

		// Get rid of any areas below the cutoff
		List<CompareResultArea> finalCompareResultAreaList = new ArrayList<CompareResultArea>();
		for(CompareResultArea compareResultArea:compareResultAreaList)
		{
			System.out.println("ImageCompare:compareResultArea:.getRatioOfPixelsInAreaReducedByColorDiffrenceOverTotalPixelsInArea=" + compareResultArea.getRatioOfPixelsInAreaReducedByColorDiffrenceOverTotalPixelsInArea());
			if(compareResultArea.getRatioOfPixelsInAreaReducedByColorDiffrenceOverTotalPixelsInArea() > 
			this.getAreaColorDiffCuttoff()
			) finalCompareResultAreaList.add(compareResultArea);
		}
		compareResultAreaList.clear();
		compareResultAreaList.addAll(finalCompareResultAreaList);

		// save only the different pixels

		List<CompareResultPixel> diffrentPixelsOnly = new ArrayList<CompareResultPixel>();
		for(CompareResultArea compareResultArea:getCompareResultAreaList())
		{
			diffrentPixelsOnly.addAll(compareResultArea.getCompareResultAreaPixelList());
		}
		setCompareResultPixelList(diffrentPixelsOnly);

		setToStringValue("CompareResultPage" + ":width1=" + width1 + ":width2=" + width2 + ":height1=" + height1 + ":height2=" + height2 + "");
		System.out.println("ImageCompare:FINAL compareResultAreaList.size()=" + compareResultAreaList.size());
		System.out.println("------------------------------------------------------------------------------------");
		setCompareResultAreaList(compareResultAreaList);
	}

	public int getTotalDiffrentPixles()
	{
		int totalDiffrentPixles = 0;
		for(CompareResultArea compareResultArea:this.getCompareResultAreaList())
			totalDiffrentPixles += compareResultArea.getCompareResultAreaPixelList().size();
		System.out.println("ImageCompare:getTotalDiffrentPixles()=" + totalDiffrentPixles);
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
		for(IgnoreArea ingoreAreaList:getIgnoreAreaList())
		{
			isInIngoreAreas = ingoreAreaList.getMappedIgnoreRectangle(ingoreAreaList.getIgnoreRectangle(),getImage1().getWidth(),getImage1().getHeight()).contains(compareResultPixel.getPoint());
			if(isInIngoreAreas) break;
		}
		return(isInIngoreAreas);
	}

	public String toString()
	{
		return(getToStringValue());
	}

	public boolean areImagesEqual()
	{
		return(getPercentDiff() == 0.0);
	}

	public int getMaxPixelDiff()
	{
		int maxPixelDiff = 0;
		for(CompareResultPixel compareResultPagePixel:getCompareResultPixelList())
		{
			if(compareResultPagePixel.getRedDiff() > maxPixelDiff) maxPixelDiff = compareResultPagePixel.getRedDiff();
			if(compareResultPagePixel.getGreenDiff() > maxPixelDiff) maxPixelDiff = compareResultPagePixel.getGreenDiff();
			if(compareResultPagePixel.getBlueDiff() > maxPixelDiff) maxPixelDiff = compareResultPagePixel.getBlueDiff();
		}
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
		for(CompareResultArea compareResultArea:this.getCompareResultAreaList())
		{
			g2d.setPaint(getColorFromString("ff000077"));
			g2d.fill(compareResultArea);
			
			g2d.setPaint(color);
			g2d.setStroke(stroke);
			g2d.draw(compareResultArea);
		}
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

			for(CompareResultPixel compareResultPixel:compareResultArea.getCompareResultAreaPixelList())
			{
				g2d.fillRect((int)compareResultPixel.getPoint().getX(), (int)compareResultPixel.getPoint().getY(), 1, 1);
			}
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

	public List<CompareResultPixel> getCompareResultPixelList()
	{
		return compareResultPixelList;
	}

	public void setCompareResultPixelList(List<CompareResultPixel> compareResultPagePixelList)
	{
		this.compareResultPixelList = compareResultPagePixelList;
	}

	public List<CompareResultArea> getCompareResultAreaList()
	{
		return compareResultAreaList;
	}

	public void setCompareResultAreaList(List<CompareResultArea> compareResultAreaList)
	{
		this.compareResultAreaList = compareResultAreaList;
	}

	public String getToStringValue()
	{
		return toStringValue;
	}

	public void setToStringValue(String toStringValue)
	{
		this.toStringValue = toStringValue;
	}

	public List<IgnoreArea> getIgnoreAreaList()
	{
		return ignoreAreaList;
	}

	public void setIgnoreAreaList(List<IgnoreArea> ignoreAreaList)
	{
		this.ignoreAreaList = ignoreAreaList;
	}
	
	public double getMargin()
	{
		return margin;
	}

	public void setMargin(double margin)
	{
		this.margin = margin;
	}

	public double getAreaColorDiffCuttoff()
	{
		return areaColorDiffCuttoff;
	}

	public void setAreaColorDiffCuttoff(double areaColorDiffCuttoff)
	{
		this.areaColorDiffCuttoff = areaColorDiffCuttoff;
	}

	public boolean isReanalyze()
	{
		return reanalyze;
	}

	public void setReanalyze(boolean reanalyze)
	{
		this.reanalyze = reanalyze;
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