package org.idelta;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageCompareResult
{

	private List<ImageCompare> imageCompareList;
	private IgnoreAreaPageList ingoreAreaPageList;
	
	public ImageCompareResult(List<ImageCompare> imageCompareList,IgnoreAreaPageList ingoreAreaPageList)
	{
		setImageCompareList(imageCompareList);
		this.setIngoreAreaPageList(ingoreAreaPageList);
	}
	
	
	public void saveImagesToTempFiles() throws Exception
	{
		for(ImageCompare imageCompare:getImageCompareList()) imageCompare.saveImagesToTempFiles();
	}
	
	public void loadImagesFromTempFiles() throws Exception
	{
		for(ImageCompare imageCompare:getImageCompareList()) imageCompare.loadImagesFromTempFiles();
	}
	
	public void deleteTempFiles() throws Exception
	{
		for(ImageCompare imageCompare:getImageCompareList()) imageCompare.deleteTempFiles();
	}
	
	public void refreshIgnoreAreas()
	{
		List<List<IgnoreArea>> listOfIgnoreAreaLists = this.getIgnoreAreaPageList().getListOfIgnoreAreaLists();
		for(int c=0;c<getImageCompareList().size();c++)
		{
			List<IgnoreArea> ingoreAreaList = new ArrayList<IgnoreArea>();
			if(c<listOfIgnoreAreaLists.size())
			{
				ingoreAreaList = listOfIgnoreAreaLists.get(c);
			}
			else
			{
				listOfIgnoreAreaLists.add(ingoreAreaList);
			}
			getImageCompareList().get(c).setIgnoreAreaList(ingoreAreaList);
		}
	}
	
	public void analyze()
	{
		this.refreshIgnoreAreas();
		for(ImageCompare imageCompare:getImageCompareList()) imageCompare.analyze();
	}
	
	public boolean areImagesEqual()
	{
		boolean equal = true;
		for(ImageCompare imageCompare:getImageCompareList())
		{
			System.out.println(this+" "+imageCompare);
			equal = equal&imageCompare.areImagesEqual();
		}
		return(equal);		
	}
	
	public double getPercentDiff()
	{
		double percentDiff = 0.0;
		int diffrentPixles = 0;
		int totalPixels = 0;
		for(ImageCompare imageCompare:getImageCompareList())
		{
			diffrentPixles += imageCompare.getTotalDiffrentPixles();
			totalPixels += imageCompare.getTotalPixles();
		}
		percentDiff = (double)diffrentPixles/(double)totalPixels;
		return(percentDiff);
	}


	
	public String toString()
	{
		return("ImageCompare"+":images="+getImageCompareList().size());
	}
	
	public List<BufferedImage> getBufferedImage1List()
	{
		List<BufferedImage> bufferedImageList = new ArrayList<BufferedImage>();
		for(ImageCompare imageCompare:getImageCompareList()) bufferedImageList.add(imageCompare.getImage1());
		return(bufferedImageList);
	}
	
	public List<BufferedImage> getBufferedImage2List()
	{
		List<BufferedImage> bufferedImageList = new ArrayList<BufferedImage>();
		for(ImageCompare imageCompare:getImageCompareList()) bufferedImageList.add(imageCompare.getImage2());
		return(bufferedImageList);
	}
	
	public int getMaxPixelDiff()
	{
		int maxPixelDiff = 0;
		for(ImageCompare imageCompare:getImageCompareList())
    	{
			System.out.println(this+" "+imageCompare);
			if(imageCompare.getMaxPixelDiff()>maxPixelDiff) maxPixelDiff = imageCompare.getMaxPixelDiff();
    	}
		return(maxPixelDiff);
	}
	
	public List<ImageCompare> getImageCompareList() {
		return imageCompareList;
	}

	public void setImageCompareList(List<ImageCompare> imageCompareList) {
		this.imageCompareList = imageCompareList;
	}

	public IgnoreAreaPageList getIgnoreAreaPageList()
	{
		return ingoreAreaPageList;
	}

	public void setIngoreAreaPageList(IgnoreAreaPageList ingoreAreaPageList)
	{
		this.ingoreAreaPageList = ingoreAreaPageList;
		refreshIgnoreAreas();
	}
}