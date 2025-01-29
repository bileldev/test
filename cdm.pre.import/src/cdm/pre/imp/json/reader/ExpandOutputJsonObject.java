package cdm.pre.imp.json.reader;

import java.util.ArrayList;

public class ExpandOutputJsonObject 
{
	private String rootId;
	private ArrayList<JSONElelment> objects = new ArrayList<>();
	private ArrayList<JSONElelment> dataObjects =  new ArrayList<>();

	public String getRootId() {
		return rootId;
	}
	public void setRootId(String rootId) {
		this.rootId = rootId;
	}
	
	public ArrayList<JSONElelment> getObjects() {
		return objects;
	}
	public void setObjects(ArrayList<JSONElelment> objects) {
		this.objects = objects;
	}

	public ArrayList<JSONElelment> getDataObjects() {
		return dataObjects;
	}
	public void setDataObjects(ArrayList<JSONElelment> dataObjects) {
		this.dataObjects = dataObjects;
	}
	
}
