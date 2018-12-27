package org.imagediff;

import java.util.ArrayList;
import java.util.List;


public class ImageDiffMapGraph {
	private List<String> percentileGraph;
	
	public ImageDiffMapGraph(PixelCompareResult pcr,int barLength)
	{
		this.percentileGraph = new ArrayList<String>();
		int[] graph = new int[101];
		for(int i=0;i<graph.length;i++) graph[i] = 0;
		for(PixelCompare pc:pcr.getPixelCompareList())  {
			int graphValue = Double.valueOf(Math.round(pc.getColorDistanceScale()*100)).intValue();
			graph[graphValue]++;
			}
		
		int max = 0;
		for(int i=0;i<graph.length;i++) if(graph[i]>max) max = graph[i];
		for(int i=0;i<graph.length;i++) 
		{
			String barString = "";
			int length = Double.valueOf( ( Math.log(graph[i])*barLength)/ Math.log(max) ).intValue();
			for(int m=0;m<length;m++) barString += "*";
			getPercentileGraph().add(barString);
		}
	}

	public List<String> getPercentileGraph() {
		return percentileGraph;
	}

	public void setPercentileGraph(List<String> percentileGraph) {
		this.percentileGraph = percentileGraph;
	}
}
