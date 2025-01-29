/**
 * 
 */
package cdm.pre.imp.configmap;

import java.util.HashMap;

/**
 * @author amit.rath
 * This class encapsulates the information held in <AttrGrp> elements under a parent <Class> element in the mapping XML file
 */
public class AttrGrpMapInfo {
	
	private String mapFuncName;
	private AttributeMapInfo attrMapInfoObj;
	private HashMap<String, String> mapFuncsParamValues;

	/**
	 * parameter-less constructor. Initializes the class member variables
	 */
	public AttrGrpMapInfo() {
		
		this.mapFuncName			= null;
		this.attrMapInfoObj			= null;
		this.mapFuncsParamValues	= new HashMap<String, String>();	
	}
	
	public AttrGrpMapInfo(String mapFuncName, AttributeMapInfo attrMapInfoObj,
			HashMap<String, String> mapFuncsParamValues) {
		this.mapFuncName = mapFuncName;
		this.attrMapInfoObj = attrMapInfoObj;
		this.mapFuncsParamValues = mapFuncsParamValues;
	}

	/**
	 * copies the object of the same class passed as argument to the method
	 * @param attrGrpInfoObj
	 * @return
	 */
	public static AttrGrpMapInfo copyInstance(AttrGrpMapInfo attrGrpInfoObj) {
		
		return new AttrGrpMapInfo(attrGrpInfoObj.getMapFuncName(), attrGrpInfoObj.getAttrMapInfoObj(), 
				attrGrpInfoObj.getMapFuncsParamValues());
	}
	
	public String getMapFuncName() {
		return mapFuncName;
	}

	public void setMapFuncName(String mapFuncName) {
		this.mapFuncName = mapFuncName;
	}

	public AttributeMapInfo getAttrMapInfoObj() {
		return attrMapInfoObj;
	}

	public void setAttrMapInfoObj(AttributeMapInfo attrMapInfoObj) {
		this.attrMapInfoObj = attrMapInfoObj;
	}

	public HashMap<String, String> getMapFuncsParamValues() {
		return mapFuncsParamValues;
	}

	public void setMapFuncsParamValues(String paramName, String paramValue) {
		this.mapFuncsParamValues.put(paramName, paramValue);
	}
}
