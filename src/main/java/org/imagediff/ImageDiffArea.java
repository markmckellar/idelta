package org.imagediff;

import java.util.ArrayList;
import java.util.List;

public class ImageDiffArea {
	private List<PixelCompareArea> pixelCompareAreaList;
	private int margin;
	public ImageDiffArea(int margin) {
		this.pixelCompareAreaList = new ArrayList<PixelCompareArea>();
		this.margin = margin;
	}

	public List<PixelCompareArea> getPixelCompareAreaList() {
		return pixelCompareAreaList;
	}

	public void clearImageDiffArea() {
		for(PixelCompareArea pca:getPixelCompareAreaList()) pca.clearPixelCompareArea();
	}
	
	public void setPixelCompareAreaList(List<PixelCompareArea> pixelCompareAreaList) {
		this.pixelCompareAreaList = pixelCompareAreaList;
	}
	
	public String getInfoString() {
		String infoString = "ImageDiffAreaInfo:pixelCompareAreaList.size="+getPixelCompareAreaList().size()+"\n";
		for(PixelCompareArea pca:getPixelCompareAreaList()) infoString +="     "+ pca.getInfoString()+"\n";
		return(infoString);
	}
	
	public void removeSmallPixelCompareAreas(int removeCutoff) {
		List<PixelCompareArea> removePixelCompareAreaList = new ArrayList<PixelCompareArea>();
		for(PixelCompareArea pca:getPixelCompareAreaList()) 
			if(pca.getPixelCompareList().size()<=removeCutoff) removePixelCompareAreaList.add(pca);
		for(PixelCompareArea pca:removePixelCompareAreaList) 
			getPixelCompareAreaList().remove(pca);
	}

	public void addPixeCompareList(List<PixelCompare> pixelCompareList,boolean alwaysAdd) {
		List<PixelCompareArea> matchingPcaList = new ArrayList<PixelCompareArea>();
		//System.out.println("ImageDiffArea:addPixelCompareList:size="+pixelCompareList.size());
		for(PixelCompare pc:pixelCompareList)
		{
			matchingPcaList.clear();
			
			for(PixelCompareArea pca:getPixelCompareAreaList()) 
				if(pca.containsPixelCompare(pc)) matchingPcaList.add(pca);
			
			if(matchingPcaList.isEmpty()) {
				if(alwaysAdd) getPixelCompareAreaList().add(new PixelCompareArea(pc,getMargin()));
			}
			else {
				PixelCompareArea pixelCompareArea = matchingPcaList.get(0);
				for(int i=1;i<matchingPcaList.size();i++)
				{
					PixelCompareArea pca = matchingPcaList.get(i);
					pixelCompareArea.mergePixelCompareArea(pca);
					getPixelCompareAreaList().remove(pca);
					pixelCompareArea.addPixelCompare(pc,getMargin());
				}
			}
		}
	}

	public int getMargin() {
		return margin;
	}

	public void setMargin(int margin) {
		this.margin = margin;
	}
}






