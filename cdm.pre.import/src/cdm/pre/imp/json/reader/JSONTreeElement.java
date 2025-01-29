package cdm.pre.imp.json.reader;

import java.util.ArrayList;

public class JSONTreeElement 
{
	private JSONTreeElement parent;
	private JSONElelment dataElement ;
	private ArrayList<JSONTreeElement> children = new ArrayList<>();

	public JSONElelment getDataElement() {
		return dataElement;
	}
	
	public void setDataElement(JSONElelment dataElement) {
		this.dataElement = dataElement;
	}
	
	public ArrayList<JSONTreeElement> getChildren() {
		return children;
	}
	
	public void setChildren(ArrayList<JSONTreeElement> children) {
		this.children = children;
	}

	public void setParent(JSONTreeElement parent) {
		this.parent = parent;
		
	}
	
	public JSONTreeElement getParent() {
		return parent;
	}

	
}
