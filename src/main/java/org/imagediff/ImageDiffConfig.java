package org.imagediff;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ImageDiffConfig {

	private String inFile1 = "";
	private String inFile2 = "";
	private String outImageFile = "";
	private String outJsonFile = "";
	private int compareMargin = 1;
	
	public  static ImageDiffConfig imageDiffConfigFromFile(String imageDiffConfigFile) throws Exception {
		
		Path path = Paths.get(imageDiffConfigFile);
		return(ImageDiffConfig.imageDiffConfigFromJson(
				new String( Files.readAllBytes( path )
				)));
	}
	
	public  static ImageDiffConfig imageDiffConfigFromJson(String imageDiffConfigJson) throws Exception  {
		Gson gson = new Gson();
		Type objectType = new TypeToken<ImageDiffConfig>() {}.getType();
		ImageDiffConfig imageDiffConfig = gson.fromJson(imageDiffConfigJson,objectType);
		return(imageDiffConfig);
	}
	
	public  String toJson() throws Exception  {
		Gson gson = new Gson();
		Type objectType = new TypeToken<ImageDiffConfig>() {}.getType();
		String  json = gson.toJson(this,objectType);
		return(json);
	}



	public String getOutImageFile() {
		return outImageFile;
	}

	public void setOutImageFile(String outImageFile) {
		this.outImageFile = outImageFile;
	}

	public String getOutJsonFile() {
		return outJsonFile;
	}

	public void setOutJsonFile(String outJsonFile) {
		this.outJsonFile = outJsonFile;
	}

	public int getCompareMargin() {
		return compareMargin;
	}

	public void setCompareMargin(int compareMargin) {
		this.compareMargin = compareMargin;
	}

	public String getInFile1() {
		return inFile1;
	}

	public void setInFile1(String inFile1) {
		this.inFile1 = inFile1;
	}

	public String getInFile2() {
		return inFile2;
	}

	public void setInFile2(String inFile2) {
		this.inFile2 = inFile2;
	}

}
