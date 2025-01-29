/**
 * 
 */

package cdm.pre.imp.reader;

import java.util.TreeMap;

public class BOMInfo {
	
	int bomParentLevel;								// level of the bom parent in the complete structure 

	private String bomParentOBID;					// unique obid identifier for the bom parent 
	private String bomParentName;					// name of the bom parent line
	private String bomParentID;						// part number of the parent bom line
	private String bomParRevID;						// revision id of the BOM parent item
	private String trckBMEndItem;					// end item corresponding to an end item in case of a Truck vehicle

	private TreeMap<String, String> bomChildID;			// array of bom children part numbers

	/**
	 * parameterzied constructor
	 * @param bomParentOBID
	 * @param bomParentName
	 */
	public BOMInfo(String bomParentOBID, String bomParentName, String bomParentPartNo, int bomLevel, String parBOMRevID) {
		super();
		this.bomParentLevel = bomLevel;
		this.bomParentOBID	= bomParentOBID;
		this.bomParentName	= bomParentName;
		this.bomParentID	= bomParentPartNo; 	
		this.bomParRevID 	= parBOMRevID;
		this.bomChildID		= new TreeMap<String, String>();
	}


	/**
	 * getter for the class member
	 * @return : obid of the parent BOM line
	 */
	public String getBomParentOBID() {
		return bomParentOBID;
	}

	/**
	 * setter for the class member 
	 * @param bomParentOBID : obid of the parent BOM line
	 */
	public void setBomParentOBID(String bomParentOBID) {
		this.bomParentOBID = bomParentOBID;
	}

	/**
	 * getter for the class member
	 * @return : name of the parent BOM line
	 */
	public String getBomParentName() {
		return bomParentName;
	}

	/**
	 * setter for the class member
	 * @param bomParentName : name of the parent BOM line
	 */
	public void setBomParentName(String bomParentName) {
		this.bomParentName = bomParentName;
	}

	/**
	 * getter for the class member
	 * @return : part number of the child BOM line
	 */
	public TreeMap<String, String> getBomChildOBID() {
		return this.bomChildID;
	}

	/**
	 * setter for the class member
	 * @param bomChildId : part number of the child BOM line
	 */
	public void setBomChildOBID(String bomChildId, String bomChildRevID) {
		if(bomChildId != null && bomChildRevID != null) {
			this.bomChildID.put(bomChildId, bomChildRevID);
		}
	}
	
	/**
	 * getter for the class member
	 * @return : part number of the parent BOM line
	 */
	public String getBomParentID() {
		return bomParentID;
	}

	/**
	 * setter for the class member
	 * @param bomParentID : part number of the parent BOM line
	 */
	public void setBomParentID(String bomParentID) {
		this.bomParentID = bomParentID;
	}

	/**
	 * getter for the class member
	 * @return : structure level of the parent BOM line in the entire BOM structure
	 */
	public int getBomParentLevel() {
		return bomParentLevel;
	}

	/**
	 * setter for the class member
	 * @param bomParentLevel : structure level for the parent BOM line
	 */
	public void setBomParentLevel(int bomParentLevel) {
		this.bomParentLevel = bomParentLevel;
	}
	
	public String getBomParRevID() {
		return bomParRevID;
	}

	public void setBomParRevID(String bomParRevID) {
		this.bomParRevID = bomParRevID;
	}
	
	public String getTrckBMEndItem() {
		return trckBMEndItem;
	}

	public void setTrckBMEndItem(String trckBMEndItem) {
		this.trckBMEndItem = trckBMEndItem;
	}
}
