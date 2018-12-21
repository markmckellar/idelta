package org.imagediff;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class PixelCompareArea {
	private List<PixelCompare> pixelCompareList;
	private List<Shape> pixelAreas;
	
	public PixelCompareArea() {
		this.pixelCompareList = new ArrayList<PixelCompare>();
		this.setPixelAreas(new ArrayList<Shape>());
	}

	public PixelCompareArea(PixelCompare pixelCompare,int margin) {
		this();
		this.addPixelCompare(pixelCompare,margin);
	}


	public void clearPixelCompareArea() {
		this.getPixelCompareList().clear();
	}
	
	public boolean containsPixelCompare(PixelCompare pc) {
		boolean contains = false;
		for(Shape shape:getPixelAreas())
			if(shape.contains(pc.getPoint())) {
				contains = true;
				break;
			}			
		return contains;
	}

	public void mergePixelCompareArea(PixelCompareArea pixelCompareArea) {
		getPixelAreas().addAll(pixelCompareArea.getPixelAreas());
		getPixelCompareList().addAll(pixelCompareArea.getPixelCompareList());
	}

	public void  addPixelCompare(PixelCompare pc,int margin) {
		this.getPixelCompareList().add(pc);
		this.getPixelAreas().add(	
			new Rectangle2D.Double(
				pc.getPoint().getX()-(margin/2),
				pc.getPoint().getY()-(margin/2),
				margin,
				margin) );
	}

	public List<PixelCompare> getPixelCompareList() {
		return pixelCompareList;
	}

	public void setPixelCompareList(List<PixelCompare> pixelCompareList) {
		this.pixelCompareList = pixelCompareList;
	}

	public List<Shape> getPixelAreas() {
		return pixelAreas;
	}

	public void setPixelAreas(List<Shape> pixelAreas) {
		this.pixelAreas = pixelAreas;
	}

	public String getInfoString() {
		// TODO Auto-generated method stub
		return("PixelCompareArea:pixelAreas.size="+this.getPixelAreas().size()+
				":pixelCompareList.size="+this.getPixelCompareList().size());
	}
}











