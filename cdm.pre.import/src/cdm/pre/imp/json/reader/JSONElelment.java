package cdm.pre.imp.json.reader;

import java.util.ArrayList;
import java.util.HashMap;

import cdm.pre.imp.reader.MappedAttributes;

public class JSONElelment 
{
	private String name;
	private String id;
	private String objectType;
	private String partId;
	private HashMap<String, Object> attributes = new HashMap<>();
	private HashMap<String, Object> relAttributes = new HashMap<>();
	private ArrayList<JSONElelment> children = new ArrayList<>();
	private boolean isProcessedForStruct = false;
	private HashMap<String, ArrayList<MappedAttributes>> mappedElementsMap = new HashMap<String, ArrayList<MappedAttributes>>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public HashMap<String, Object> getAttributes() {
		return attributes;
	}
	public void setAttributes(HashMap<String, Object> attributes) {
		this.attributes = attributes;
		this.setObjectType((String) attributes.get("Class"));
		//System.out.println("Class Name : "+(String) attributes.get("Class"));
	}
	
	public HashMap<String, Object> getRelAttributes() {
		return relAttributes;
	}
	public void setRelAttributes(HashMap<String, Object> relAttributes) {
		this.relAttributes = relAttributes;
	}
	public ArrayList<JSONElelment> getChildren() {
		return children;
	}
	public void setChildren(ArrayList<JSONElelment> children) {
		this.children = children;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public String getPartId() {
		return partId;
	}
	public void setPartId(String partId) {
		this.partId = partId;
	}
	public boolean isProcessedForStruct() {
		return isProcessedForStruct;
	}
	public void setProcessedForStruct(boolean isProcessedForStruct) {
		this.isProcessedForStruct = isProcessedForStruct;
	}
	
	/**
	 * Setter method for the mapped elements map 
	 * @param trgtClassName 	: Name of the Target Class in TEAMCENTER
	 * @param trgtAttrName		: Name of the Target Attribute in TEAMCENTER
	 * @param trgtAttrValue 	: Value of the target Attribute as read from the PLMXML file
	 * @param trgtAttrScope 	: Scope of the target Attribute as read from the mapping file
	 * @param trgtAttrDataType 	: Value of the target Attribute as read from the PLMXML file
	 */
	public void setMappedElementsMap(String trgtClassName, MappedAttributes mapAttrObj) {
	
		// checks if the member map already has an entry for the target class
		if(this.mappedElementsMap.containsKey(trgtClassName)) {
			// insert the object into the class member map
			this.mappedElementsMap.get(trgtClassName).add(mapAttrObj);
		}
		// executes if there is no entry for the target class in the member map
		else {
			// creates an entry for the target class in the member map and then assigns the MappedAttribute instance to the map 
			this.mappedElementsMap.put(trgtClassName, new ArrayList<MappedAttributes>());
			this.mappedElementsMap.get(trgtClassName).add(mapAttrObj);
		}
	}
	
	
	
	public HashMap<String, ArrayList<MappedAttributes>> getMappedElementsMap() {
		return mappedElementsMap;
	}
}
