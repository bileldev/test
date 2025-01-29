/**
 * Class that encapsulates the queried Dataset Information from Teamcenter
 * @author amit.rath
 */

package cdm.pre.imp.dbconnector;

import java.util.HashMap;

public class DBConnDatasetInfo {

	private String mStrModelRevPUID;								// class member that holds the puid of the parent C9ModelRevision 
	private HashMap <String, String> mMapDatasetAttrs;				// class member map that holds the dataset attribute name & it's corresponding value

	
	public String getmStrModelRevPUID() {
		return mStrModelRevPUID;
	}

	public void setmStrModelRevPUID(String mStrModelRevPUID) {
		this.mStrModelRevPUID = mStrModelRevPUID;
	}
	
	public HashMap<String, String> getmMapDatasetAttrs() {
		return mMapDatasetAttrs;
	}

	public void setmMapDatasetAttrs(String attrName, String attrValue) {
		if(attrName != null && attrValue != null) {
			this.mMapDatasetAttrs.put(attrName, attrValue);
		}
	}
	
	public void setmMapDatasetAttrs(HashMap <String, String> mapDSAttrName2Val) {
		if(mapDSAttrName2Val != null && !mapDSAttrName2Val.isEmpty()) {
			this.mMapDatasetAttrs = mapDSAttrName2Val;
		}
	}
}
