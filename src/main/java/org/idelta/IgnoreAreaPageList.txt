package org.idelta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class IgnoreAreaPageList
{
	private List<List<IgnoreArea>> listOfIgnoreAreaLists;
	private boolean hasChanged;
	private File ingnoreFile;

	public IgnoreAreaPageList()
	{
		this(new ArrayList<List<IgnoreArea>>(), new File("ignore_file.json"));
	}

	public IgnoreAreaPageList(List<List<IgnoreArea>> listOfIgnoreAreaLists, File ingnoreFile)
	{
		this.setListOfIgnoreAreaLists(listOfIgnoreAreaLists);
		this.setIngnoreFile(ingnoreFile);
		this.setHasChanged(false);
	}

	public void recreateIngoreArea(IgnoreAreaPageList ingoreAreaPageList)
	{
		getListOfIgnoreAreaLists().clear();
		getListOfIgnoreAreaLists().addAll(ingoreAreaPageList.getListOfIgnoreAreaLists());
		setIngnoreFile(ingoreAreaPageList.getIngnoreFile());
	}

	public String toJson() throws Exception
	{
    	Gson gson = new Gson();
		Type objectType = new TypeToken<IgnoreAreaPageList>() {}.getType();
		String json = gson.toJson(this,objectType);
		return(json);
	}

	
	public void saveIgnoreAreas(File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(toJson());
		writer.close();
	}

	public List<List<IgnoreArea>> getListOfIgnoreAreaLists()
	{
		return listOfIgnoreAreaLists;
	}

	public void setListOfIgnoreAreaLists(List<List<IgnoreArea>> listOfIgnoreAreaLists)
	{
		this.listOfIgnoreAreaLists = listOfIgnoreAreaLists;
	}

	public boolean isHasChanged()
	{
		return hasChanged;
	}

	public void setHasChanged(boolean hasChanged)
	{
		this.hasChanged = hasChanged;
	}

	public File getIngnoreFile()
	{
		return ingnoreFile;
	}

	public void setIngnoreFile(File ingnoreFile)
	{
		this.ingnoreFile = ingnoreFile;
	}

}
