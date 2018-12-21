package org.imagediff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PixelCompareResult  {
	private List<PixelCompare> pixelCompareList;
	double maxColorDistance;
	double maxScale;
	double totalScale;
	double meadianScale;
	private double meanScale;
	private ArrayList<Double> percentileList;
	private ArrayList<Integer> percentileImageIndexList;

	
	public PixelCompareResult() {
		this(new ArrayList<PixelCompare>());
	}

	public PixelCompareResult(List<PixelCompare> pixelCompareList) {
		this.pixelCompareList = pixelCompareList;
		this.setPercentileList(new ArrayList<Double>());
		this.setPercentileImageIndexList(new ArrayList<Integer>());
		refreshStats();
	}

	public List<PixelCompare> getPixelCompareList(int lowerPercentile, int upperPercentile) {
		List<PixelCompare> pixelCompareList = new ArrayList<PixelCompare>();
		
		int startIndex = this.getPercentileImageIndex(lowerPercentile);
		int endIndex = this.getPercentileImageIndex(upperPercentile);
		
		for(int i=startIndex;i<endIndex;i++) {
			PixelCompare pc = getPixelCompareList().get(i);
			if(pc.getColorDistanceScale()>getPercentile(lowerPercentile) &&
					pc.getColorDistanceScale()<=getPercentile(upperPercentile))
				pixelCompareList.add(pc);
		}
		return(pixelCompareList);
	}
	
	public void refreshStats() {
		if(getPixelCompareList().isEmpty()) return;
		
		getPercentileList().clear();
		getPercentileImageIndexList().clear();
		double maxColorDistance = 0.0;
		double maxScale = 0.0;
		double totalScale = 0.0;		
		double meadian = getPixelCompareList().get(getPixelCompareList().size()/2).getColorDistanceScale();

		
		for(PixelCompare pc:getPixelCompareList())  {	
			double colorDistance = pc.getColorDistance();
			if(maxColorDistance<colorDistance) maxColorDistance = colorDistance;
			double scale = pc.getColorDistanceScale();
			totalScale += scale;
			if(maxScale<scale) maxScale = scale;
		}
		
		setTotalScale(totalScale);
		totalScale = 0.0;
		int percentile = 0;
		double percentileTotalCutoff = 0.0;
		double lastPercentile = 0.0;
		int index = 0;
		Collections.sort(getPixelCompareList());
		
		for(PixelCompare pc:getPixelCompareList())  {
			totalScale += pc.getColorDistanceScale();
			if(totalScale>=percentileTotalCutoff) {
				lastPercentile = pc.getColorDistanceScale();
				getPercentileList().add(lastPercentile);
				getPercentileImageIndexList().add(index);
				percentile++;
				percentileTotalCutoff = getTotalScale()*(percentile/100.0);
			}
			index++;
		}
		//for(;percentile<100;percentile++) getPercentileList().add(lastPercentile);
		
		this.setMaxColorDistance(maxColorDistance);
		this.setMaxScale(maxScale);
		this.setMeadianScale(meadian);
		this.setMeanScale( this.getTotalScale()/getPixelCompareList().size() );
	}
	
	public  void quickGraph(int percentile) {
		refreshStats();
		
		int[] graph = new int[100];
		for(int i=0;i<graph.length;i++) graph[i] = 0;
		for(PixelCompare pc:getPixelCompareList())  {
			graph[Double.valueOf(Math.round(pc.getColorDistanceScale()*100)).intValue()]++;
			}
		
		int max = 0;
		for(int i=0;i<graph.length;i++) if(graph[i]>max) max = graph[i];
		for(int i=0;i<graph.length;i++) 
		{
			System.out.printf("%3d:",i);
			int length = (graph[i]*150)/max;
			for(int m=0;m<length;m++) System.out.print("*");
			System.out.print("\n");
		}
		
		System.out.println("maxColorDistance="+getMaxColorDistance());
		System.out.println("maxScale="+getMaxScale());
		System.out.println("meanScale="+getMeanScale());
		System.out.println("meadianScale="+getMeadianScale());
		System.out.println("mean/meadian="+(getMeanScale()/getMeadianScale()));
		System.out.println("cutoffPercent="+percentile);
		System.out.println("cutoffPercentValue="+getPercentile(percentile));
	}
	
	public double getPercentile(int percentile) {
		if(percentile < 0) percentile = 0;
		else if(percentile >= (getPercentileList().size()) ) percentile = getPercentileList().size()-1;
		return( getPercentileList().get(percentile) );
	}
	
	public int getPercentileImageIndex(int percentile) {
		if(percentile < 0) percentile = 0;
		else if(percentile >= (getPercentileList().size()) ) percentile = getPercentileList().size()-1;
		return( getPercentileImageIndexList().get(percentile) );
	}
	
	public void addPixelCompare(PixelCompare pixelCompare) {
		this.getPixelCompareList().add(pixelCompare);
	}
	public List<PixelCompare> getPixelCompareList() {
		return pixelCompareList;
	}

	public void setPixelCompareList(List<PixelCompare> pixelCompareList) {
		this.pixelCompareList = pixelCompareList;
	}


	public double getMaxColorDistance() {
		return maxColorDistance;
	}

	public void setMaxColorDistance(double maxColorDistance) {
		this.maxColorDistance = maxColorDistance;
	}

	public double getMaxScale() {
		return maxScale;
	}

	public void setMaxScale(double maxScale) {
		this.maxScale = maxScale;
	}

	public double getTotalScale() {
		return totalScale;
	}

	public void setTotalScale(double totalScale) {
		this.totalScale = totalScale;
	}

	public double getMeadianScale() {
		return meadianScale;
	}

	public void setMeadianScale(double meadianScale) {
		this.meadianScale = meadianScale;
	}

	public double getMeanScale() {
		return meanScale;
	}

	public void setMeanScale(double meanScale) {
		this.meanScale = meanScale;
	}

	public ArrayList<Double> getPercentileList() {
		return percentileList;
	}

	public void setPercentileList(ArrayList<Double> percentileList) {
		this.percentileList = percentileList;
	}

	public ArrayList<Integer> getPercentileImageIndexList() {
		return percentileImageIndexList;
	}

	public void setPercentileImageIndexList(ArrayList<Integer> percentileImageIndexList) {
		this.percentileImageIndexList = percentileImageIndexList;
	}
}