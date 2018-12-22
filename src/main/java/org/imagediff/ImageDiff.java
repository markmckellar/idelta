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
import javax.imageio.ImageIO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ImageDiff
{
	private String cmpFile1;
	private String cmpFile2;
	private ImageDiffMap imageDiffMap;
	
	public static void main(String args[]) throws Exception {
		if(args.length!=3) throw new Exception("Expected 3 aruments, got "+
				args.length+
				": file1 file2 diffFile margin colorDiffCutoff");
		String file1 = args[0];
		String file2 = args[1];
		String file3 = args[2];
		
		/*
    	Gson gson = new Gson();
		Type objectType = new TypeToken<String[]>() {}.getType();
		String json = gson.toJson(args,objectType);
		System.out.println("args="+json);
		*/
		ImageDiff imageDiff = new ImageDiff(file1,file2);
		
		imageDiff.getImageDiffMap().analyze();
		
		if(imageDiff.getImageDiffMap().getDiffImage()!=null)
		{
			BufferedImage anotatedImage = imageDiff.getImageDiffMap().getAnnotatedBufferedImage(
					imageDiff.getImageDiffMap().getImage1(),
					new BasicStroke(2.0f),
					Color.white);
			File annotatedBufferedImageFile = new File(file3);	
			//System.out.println("writing to : "+file3);
		   ImageIO.write(imageDiff.overlayImage(
		    		imageDiff.getImageDiffMap().getImage2(),
		    		//imageDiffMap.getDiffImage()
		    		anotatedImage
		    		),
		    		"png", annotatedBufferedImageFile);
		   // System.out.println("wrote : "+file3);
		}
		else
		{
			//System.out.println("getDiffImage was null!!");

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
	public ImageDiff(String cmpFile1, String cmpFile2) throws Exception
	{
		setCmpFile1(cmpFile1);
		setCmpFile2(cmpFile2);
		BufferedImage img1 = ImageIO.read( new File(cmpFile1));
		BufferedImage img2 = ImageIO.read( new File(cmpFile2));
		setImageDiffMap(new ImageDiffMap(
				img1,img2,
				Color.BLACK,
				14) );
	
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
	
	public ImageDiffMap getImageDiffMap() {
		return imageDiffMap;
	}

	public void setImageDiffMap(ImageDiffMap imageDiffMap) {
		this.imageDiffMap = imageDiffMap;
	}

	public String getCmpFile1() {
		return cmpFile1;
	}

	public void setCmpFile1(String cmpFile1) {
		this.cmpFile1 = cmpFile1;
	}

	public String getCmpFile2() {
		return cmpFile2;
	}

	public void setCmpFile2(String cmpFile2) {
		this.cmpFile2 = cmpFile2;
	}

}