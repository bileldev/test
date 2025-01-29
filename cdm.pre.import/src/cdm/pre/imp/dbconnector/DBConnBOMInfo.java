/**
 * Class that encapsulates the queried BOM Information from Teamcenter
 * @author amit.rath
 */


package cdm.pre.imp.dbconnector;

import java.util.HashMap;

public class DBConnBOMInfo {
	
	private HashMap<String, String> mMapPSOccAttrs;							// map of the PS Occurrence attribute names and their corresponding values
	private HashMap<String, HashMap<String, String> > mMapEndItem2Eff;	    // map of the PS Occurrence effectivity end item to effectivity information 
	
	
	/** 
	 * Default Constructor for the class
	 */
	public DBConnBOMInfo() {
		// initiates the member maps
		this.mMapPSOccAttrs = new HashMap<String, String>();
		this.mMapEndItem2Eff = new HashMap<String, HashMap<String, String> >();
	}
	
	public HashMap<String, String> getmMapPSOccAttrs() {
		return mMapPSOccAttrs;
	}
	public void setmMapPSOccAttrs(String attrName, String attrVal) {
		if(attrName != null && attrVal != null) {
			if(!this.mMapPSOccAttrs.containsKey(attrName)) {
				this.mMapPSOccAttrs.put(attrName, attrVal);
			}
		}
	}
	public void setmMapPSOccAttrs(HashMap<String, String> mapAttrName2Val) {
		if(!mapAttrName2Val.isEmpty()) {
			this.mMapPSOccAttrs.putAll(mapAttrName2Val);
		}
	}
	
	public HashMap<String, HashMap<String, String>> getmMapEndItem2Eff() {
		return mMapEndItem2Eff;
	}
	
	/**
	 * Setter for the member map
	 * @param endItem		: end item id retrieved from the database
	 * @param effSequence	: date sequence for the effectivity dates for the end item in the first argument
	 * @param effDate		: effectivity date corresponding to the end item and the sequence
	 */
	public void setmMapEndItem2Eff(String endItem, String effSequence, String effDate) {
		if(endItem != null && effSequence != null && effDate != null) {
			// checks if the member map is empty or not
			if(!this.mMapEndItem2Eff.isEmpty()) {
				// checks if there is already an entry for the end item in the member map
				if(this.mMapEndItem2Eff.containsKey(endItem) == true) {
					// now the code logic should check for the presence of sequence in the value of the member map
					if(this.mMapEndItem2Eff.get(endItem).containsKey(effSequence) == true) {
						// if this sequence is present in the map, then compare dates and push the later date to the map
						String laterDate = DBConnUtilities.getLaterDate(this.mMapEndItem2Eff.get(endItem).get(effSequence), effDate);
						if(laterDate != null && !laterDate.equals(this.mMapEndItem2Eff.get(endItem).get(effSequence))) {
							// removes the older entry in the map and puts the new entry against the end item
							this.mMapEndItem2Eff.get(endItem).remove(effSequence);
							this.mMapEndItem2Eff.get(endItem).put(effSequence, laterDate);
						}
					}
					else {
						// no entry in the secondary map for the sequence for the end item
						HashMap<String, String> tmpMapSeq2Date = new HashMap<String, String>();
						tmpMapSeq2Date.put(effSequence, effDate);
						// sets the map of the sequence and the corresponding date to the end item
						this.mMapEndItem2Eff.get(endItem).put(effSequence, effDate);
					}
				}
				else {
					// no entry in the current map for the end item
					HashMap<String, String> tmpMapSeq2Date = new HashMap<String, String>();
					tmpMapSeq2Date.put(effSequence, effDate);
					// adds the entry to the member map
					this.mMapEndItem2Eff.put(endItem, tmpMapSeq2Date);
				}
			}
			else {
				// the member map is completely empty
				// instantiate temporary map to hold the effective sequence and dates
				HashMap<String, String> tmpMapSeq2Date = new HashMap<String, String>();
				tmpMapSeq2Date.put(effSequence, effDate);
				// adds the entry to the member map
				this.mMapEndItem2Eff.put(endItem, tmpMapSeq2Date);
			}
		}
	}
}
