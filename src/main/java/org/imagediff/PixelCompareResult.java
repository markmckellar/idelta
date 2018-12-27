package org.imagediff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PixelCompareResult  {
	transient private List<PixelCompare> pixelCompareList;
	double maxColorDistance;
	double maxScale;
	double totalScale;
	double meadianScale;
	private double meanScale;
	private ArrayList<Double> percentileList;
	private ArrayList<Integer> percentileImageIndexList;
	private ImageDiffMapGraph colorDiffGraph;
	private int totalPixels;
	private int pixlesDifferent;
	private int pixlesEqual;
	private double diffrentPercent;
	private double meanMedianRatio;
	private int meanPosition;
	private double meanPositionPercent;
	private double meanMeadianPositionRatio;
	private double standardDeviation;
	
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
		int diffTotal = 0;
		
		for(PixelCompare pc:getPixelCompareList())  {
			totalScale += pc.getColorDistanceScale();
			if(pc.getColorDistance()!=0.0) diffTotal++;
			if(totalScale>=percentileTotalCutoff) {
				lastPercentile = pc.getColorDistanceScale();
				getPercentileList().add(lastPercentile);
				getPercentileImageIndexList().add(index);
				percentile++;
				percentileTotalCutoff = getTotalScale()*(percentile/100.0);
			}
			index++;
		}
				
		
		setColorDiffGraph(new ImageDiffMapGraph(this,75));

		this.setMaxColorDistance(maxColorDistance);
		this.setMaxScale(maxScale);
		this.setMeadianScale(meadian);
		this.setMeanScale( this.getTotalScale()/getPixelCompareList().size() );
		this.setMeanMedianRatio(getMeanScale()/getMeadianScale());
		this.setTotalPixels(getPixelCompareList().size());
		this.setPixlesDifferent(diffTotal);
		this.setPixlesEqual(getPixelCompareList().size()-diffTotal);
		this.setDiffrentPercent(diffTotal/(getPixelCompareList().size()*1.0));
		this.setMeanPosition(getPositionOfScale(getMeanScale()));
		this.setMeanMeadianPositionRatio(getMeanPosition()/ (getPixelCompareList().size()/2.0) );
		this.setMeanPositionPercent(getMeanPosition()/(getPixelCompareList().size()*1.0));
		
		//for(;percentile<100;percentile++) getPercentileList().add(lastPercentile);
		/*
		 *  To calculate the standard deviation of those numbers:
			Work out the Mean (the simple average of the numbers)
			Then for each number: subtract the Mean and square the result.
			Then work out the mean of those squared differences.
			Take the square root of that and we are done!
		 */
		double totalstandardDeviation = 0.0;
		for(PixelCompare pc:getPixelCompareList())  {
			totalstandardDeviation += Math.pow(pc.getColorDistanceScale()-getMeanScale(),2.0);
		}
		this.setStandardDeviation(Math.sqrt(totalstandardDeviation/getPixelCompareList().size()));
		
	}
	
	public int getPositionOfScale(double scale) {
		int high = getPixelCompareList().size()-1;
		int low = 0;
		int guess = getPixelCompareList().size()/2;
		
		while(high!=low) {
			double guessScale = getPixelCompareList().get(guess).getColorDistanceScale();
			//System.out.println("high="+high+" low="+low+" scale="+scale+" guessScale="+guessScale);
			if(guessScale==scale) break;
			else if(guessScale>scale) high = guess;
			else low = guess;
			if( (high-low) == 1) high--;
			guess = (high+low)/2;
		}
		return(guess);
	}
	
	public  void printQuickGraph(int percentile) {
		refreshStats();
		
		for(int i=0;i<getColorDiffGraph().getPercentileGraph().size();i++) 
			System.out.printf("%3d:%s\n",i,getColorDiffGraph().getPercentileGraph().get(i));
		System.out.println("maxColorDistance="+getMaxColorDistance());
		System.out.println("maxScale="+getMaxScale());
		System.out.println("meanScale="+getMeanScale());
		System.out.println("meadianScale="+getMeadianScale());
		System.out.println("cutoffPercent="+percentile);
		System.out.println("cutoffPercentValue="+getPercentile(percentile));
		System.out.println("meanMedianRatio:"+getMeanMedianRatio());
		System.out.println("totalPixels="+getTotalPixels());
		System.out.println("pixlesDifferent="+getPixlesDifferent());
		System.out.println("pixlesEqual="+getPixlesEqual());
		System.out.println("diffrentPercent="+getDiffrentPercent());
		System.out.println("meanPosition="+getMeanPosition());
		System.out.println("meanMeadianPositionRation="+getMeanMeadianPositionRatio());
		System.out.println("meanPositionPercent="+getMeanPositionPercent());
		System.out.println("standardDeviation="+getStandardDeviation());
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

	public ImageDiffMapGraph getColorDiffGraph() {
		return colorDiffGraph;
	}

	public void setColorDiffGraph(ImageDiffMapGraph colorDiffGraph) {
		this.colorDiffGraph = colorDiffGraph;
	}
	
	public int getTotalPixels() {
		return totalPixels;
	}

	public void setTotalPixels(int totalPixels) {
		this.totalPixels = totalPixels;
	}

	public int getPixlesDifferent() {
		return pixlesDifferent;
	}

	public void setPixlesDifferent(int pixlesDifferent) {
		this.pixlesDifferent = pixlesDifferent;
	}

	public int getPixlesEqual() {
		return pixlesEqual;
	}

	public void setPixlesEqual(int pixlesEqual) {
		this.pixlesEqual = pixlesEqual;
	}

	public double getDiffrentPercent() {
		return diffrentPercent;
	}

	public void setDiffrentPercent(double diffrentPercent) {
		this.diffrentPercent = diffrentPercent;
	}

	public double getMeanMedianRatio() {
		return meanMedianRatio;
	}

	public void setMeanMedianRatio(double meanMedianRatio) {
		this.meanMedianRatio = meanMedianRatio;
	}

	public int getMeanPosition() {
		return meanPosition;
	}

	public void setMeanPosition(int meanPosition) {
		this.meanPosition = meanPosition;
	}

	public double getMeanMeadianPositionRatio() {
		return meanMeadianPositionRatio;
	}

	public void setMeanMeadianPositionRatio(double meanMeadianPositionRatio) {
		this.meanMeadianPositionRatio = meanMeadianPositionRatio;
	}

	public double getMeanPositionPercent() {
		return meanPositionPercent;
	}

	public void setMeanPositionPercent(double meanPositionPercent) {
		this.meanPositionPercent = meanPositionPercent;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}
}
