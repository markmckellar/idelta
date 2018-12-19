package org.idelta;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompareResultArea extends Area
{
	private List<CompareResultPixel>  compareResultAreaPixelList;
	private Double margin;
	
	/**
	 * @param margin how much space to put around the comparison
	 * @param compareResultPixel the pixel to compare
	 */
	public CompareResultArea(Double margin,CompareResultPixel compareResultPixel)
	{
		super();
		this.setMargin(margin);
		this.setCompareResultAreaPixelList(new ArrayList<CompareResultPixel>());
		
		this.getCompareResultAreaPixelList().add(compareResultPixel);	
		add(getNewDifferenceBoundingBox(getMargin(),compareResultPixel));
	}
	
	public void mergeCompareResultArea(CompareResultArea compareResultArea)
	{
		setMargin(Math.max(getMargin(),compareResultArea.getMargin()));
		add(compareResultArea);
		Map<String,CompareResultPixel> compareResultPixelMap = new HashMap<String,CompareResultPixel>();
		for(CompareResultPixel compareResultPixel:getCompareResultAreaPixelList()) compareResultPixelMap.put(compareResultPixel.getPointHashKey(),compareResultPixel);
		for(CompareResultPixel compareResultPixel:compareResultArea.getCompareResultAreaPixelList()) compareResultPixelMap.put(compareResultPixel.getPointHashKey(),compareResultPixel);
		setCompareResultAreaPixelList(new ArrayList<CompareResultPixel>());
		for(String key:compareResultPixelMap.keySet())
		{
			getCompareResultAreaPixelList().add(compareResultPixelMap.get(key));
		}

	}
	
	public int getTotalPixelColorDiffrence()
	{
		int totalPixelColorDiffrence = 0;
		for(CompareResultPixel  compareResultPixel:getCompareResultAreaPixelList())
		{
			totalPixelColorDiffrence += compareResultPixel.getTotalDiff();
		}
		return(totalPixelColorDiffrence);
	}
	
	public double getColorVrsPixelDiffRatio()
	{
		return(getPercentPixelColorDiffrence()/getPercentPixelDiffrence());
	}
	
	public double gePixelrVrsColorDiffRatio()
	{
		return(getPercentPixelDiffrence()/getPercentPixelColorDiffrence());
	}
	
	
	public double getPercentPixelDiffrence()
	{
		double percentPixelDiffrence = (double)getCompareResultAreaPixelList().size()/(double)getTotalPixlesInArea();
		return(percentPixelDiffrence);
	}

	public double getPercentPixelColorDiffrence()
	{
		double percentPixelDiffrence = (double)getTotalPixelColorDiffrence()/(getTotalPixlesInArea()*256.0*3.0);
		return(percentPixelDiffrence);
	}
	
	public double getPixelsInAreaReducedByColorDiffrence()
	{
		return(this.getCompareResultAreaPixelList().size()*this.getPercentPixelColorDiffrence());
	}
	
	public double getRatioOfPixelsInAreaReducedByColorDiffrenceOverTotalPixelsInArea()
	{
		return(this.getPixelsInAreaReducedByColorDiffrence()/this.getTotalPixlesInArea());
	}
	
	public int getTotalPixlesInArea()
	{
		int totalPixels = 0;
		Rectangle bounds = getBounds();
		int xStart = (int)bounds.getX();
		int xRange = xStart+(int)bounds.getWidth();
		int yStart = (int)bounds.getY();
		int yRange = yStart+(int)bounds.getHeight();
		for(int x=xStart;x<xRange;x++)
		{
			for(int y=yStart;y<yRange;y++)
			{
				if(this.contains(x, y)) totalPixels++;
			}
		}
		return(totalPixels);
	}
	
	public boolean isCompareResultPixelInList(CompareResultPixel compareResultPixel)
	{
		for(CompareResultPixel oneCompareResultPixel:this.getCompareResultAreaPixelList())
		{
			if(oneCompareResultPixel.arePointPositionsEqual(compareResultPixel)) return true;
		}
		return false;
	}
	
	public boolean isOverlaping(CompareResultArea compareResultArea)
	{
		boolean overlaps = false;
		if(getBounds2D().intersects(compareResultArea.getBounds2D())) overlaps = true;
		return(overlaps);
	}
	
	public void addCompareResultPixel(CompareResultPixel compareResultPixel)
	{
			add(getNewDifferenceBoundingBox(getMargin(),compareResultPixel));
			getCompareResultAreaPixelList().add(compareResultPixel);
	}

	public Area getNewDifferenceBoundingBox(Double margin,CompareResultPixel compareResultPixel)
	{
		Rectangle2D boundingShape = new Rectangle2D.Double(
				compareResultPixel.getPoint().getX()-(margin/2),
				compareResultPixel.getPoint().getY()-(margin/2),
				margin,
				margin);
		Area area = new Area(boundingShape);
		return(area);
	}
	


	public List<CompareResultPixel> getCompareResultAreaPixelList()
	{
		return compareResultAreaPixelList;
	}


	public void setCompareResultAreaPixelList(List<CompareResultPixel> compareResultAreaPixelList)
	{
		this.compareResultAreaPixelList = compareResultAreaPixelList;
	}

	public Double getMargin()
	{
		return margin;
	}

	public void setMargin(Double margin)
	{
		this.margin = margin;
	}
}
