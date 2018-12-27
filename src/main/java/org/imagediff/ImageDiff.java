package org.imagediff;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ImageDiff
{
	private ImageDiffMap imageDiffMap;
	private ImageDiffConfig imageDiffConfig;
	private int width;
	private int height;
	
	public static void main(String args[]) throws Exception {
		
		/*if(args.length!=3) throw new Exception("Expected 3 aruments, got "+
				args.length+
				": file1 file2 diffFile margin colorDiffCutoff");
		*/
		System.out.println("ImageDiff:passed "+args.length+" config files");
		for(int i=0;i<args.length;i++)
		{
		
			String configFile = args[i];
			System.out.println("* opening config file : "+configFile);
			
			ImageDiff imageDiff = new ImageDiff(ImageDiffConfig.imageDiffConfigFromFile(configFile));
			
			imageDiff.analyze();
			imageDiff.output();
		}
	}
	
	/**
	 * @param image1FileString file location of first image to compare
	 * @param image2FileString file location of second image to compare
	 * @param margin
	 * @param areaColorDiffCuttoff
	 * @param ingoreAreaList
	 * @throws Exception
	 */
	public ImageDiff(ImageDiffConfig imageDiffConfig) throws Exception
	{
		this.setImageDiffConfig(imageDiffConfig);
		this.setImageDiffMap(imageDiffMap);
		BufferedImage img1 = ImageIO.read( new File(imageDiffConfig.getInFile1()));
		BufferedImage img2 = ImageIO.read( new File(imageDiffConfig.getInFile2()));
		if(getImageDiffConfig().getScaleFactor()!=0.0 &&
				getImageDiffConfig().getScaleFactor()!=10.0)
		{
			img1 = this.getScaledImage(img1, getImageDiffConfig().getScaleFactor());
			img2 = this.getScaledImage(img2, getImageDiffConfig().getScaleFactor());

		}
		setImageDiffMap(new ImageDiffMap(
				img1,img2,
				Color.BLACK,
				imageDiffConfig.getCompareMargin()) );
		this.setWidth(img1.getWidth());
		this.setHeight(img2.getHeight());
	
	}
	
	public String toJson() {
		Gson gson = new Gson();
		Type objectType = new TypeToken<ImageDiff>() {}.getType();
		String json = gson.toJson(this,objectType);
		return(json);
	}
	
	public void analyze() throws Exception {
		System.out.println("* analyzing");
		getImageDiffMap().analyze();
	}
	
	public void output() throws Exception {
		System.out.println("* outputting");
		writeOutImageFile();
		writeOutJsonFile();
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
	
	public BufferedImage overlayImage(BufferedImage bottom,BufferedImage top) {
		
		BufferedImage bufferedImage = new BufferedImage(bottom.getWidth(),bottom.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bufferedImage.createGraphics();
	    bGr.drawImage(bottom, 0, 0, null);
	    bGr.drawImage(top, 0, 0, null);
	    bGr.dispose();
		return(bufferedImage);
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
	
	public void writeOutJsonFile() throws Exception {
		if(!imageDiffConfig.getOutJsonFile().isEmpty()) {
			Path path = Paths.get(getImageDiffConfig().getOutJsonFile());
			Files.write(path, toJson().getBytes());
			Files.readAllBytes( path );
		}
	}
	
	
	public void writeOutImageFile() throws Exception {
		if(getImageDiffMap().getDiffImage()!=null && !getImageDiffConfig().getOutImageFile().isEmpty())
		{
			BufferedImage anotatedImage = getImageDiffMap().getAnnotatedBufferedImage(
					getImageDiffMap().getImage1(),
					new BasicStroke(2.0f),
					Color.white);
		
			File annotatedBufferedImageFile = new File(getImageDiffConfig().getOutImageFile());	
			ImageIO.write(overlayImage(
		    		getImageDiffMap().getImage2(),
		    		//imageDiffMap.getDiffImage()
		    		anotatedImage
		    		),
		    		"png", annotatedBufferedImageFile);
		}
	}
	
	public ImageDiffMap getImageDiffMap() {
		return imageDiffMap;
	}

	public void setImageDiffMap(ImageDiffMap imageDiffMap) {
		this.imageDiffMap = imageDiffMap;
	}

	public ImageDiffConfig getImageDiffConfig() {
		return imageDiffConfig;
	}

	public void setImageDiffConfig(ImageDiffConfig imageDiffConfig) {
		this.imageDiffConfig = imageDiffConfig;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}