/**
 * 
 */
package cdm.pre.imp.configmap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author amit.rath
 *
 */
public class ClassMapInfo {

	private String srcClassName;							// mapped from source class name from the source data model
	private String trgtClassName;							// mapped to target class name from the target data model
	private String mapFuncName;								// name of the mapping function at the class mapping level
	
	private ArrayList<AttributeMapInfo> attrInfoObjs; 		// list of attribute mappings from the source to target class
	private ArrayList<AttrGrpMapInfo> attrGrpInfoObjs;		// list of attribute group mappings for the <Class> element	
	private ArrayList<ElemExprMapInfo> elemExprInfoObjs;		// list of Element Expression block mappings for the <Class> element	
	
	private HashMap<String, String> mapFuncsParamValues;	// map that stores the mapping function parameter values for class level mapping
	
	/**
	 * 
	 * @param srcClassName:		input argument holding the name of the source class
	 * @param trgtClassName:	input argument holding the name of the target class 
	 */
	public ClassMapInfo(String srcClassName, String trgtClassName) {
		super();
		this.srcClassName	= srcClassName;
		this.trgtClassName	= trgtClassName;
		this.attrInfoObjs	= new ArrayList<AttributeMapInfo>();
		this.attrGrpInfoObjs	= new ArrayList<AttrGrpMapInfo>();
		this.mapFuncsParamValues = new HashMap<String, String>();
		this.elemExprInfoObjs = new ArrayList<ElemExprMapInfo>();
 	}
	
	/**
	 * parameterized constructor with all class members
	 * @param srcClassName
	 * @param trgtClassName
	 * @param attrInfoObjs
	 * @param attrGrpInfoObjs
	 */
	public ClassMapInfo(String srcClassName, String trgtClassName, String mapFuncName, ArrayList<AttributeMapInfo> attrInfoObjs,
			ArrayList<AttrGrpMapInfo> attrGrpInfoObjs, HashMap<String, String> mapFuncParamValues, ArrayList<ElemExprMapInfo> elemExprInfoObjs) {
		super();
		this.srcClassName = srcClassName;
		this.trgtClassName = trgtClassName;
		this.attrInfoObjs = attrInfoObjs;
		this.attrGrpInfoObjs = attrGrpInfoObjs;
		this.mapFuncName	= mapFuncName;
		this.mapFuncsParamValues = mapFuncParamValues;
		this.elemExprInfoObjs = elemExprInfoObjs;
	}
	
	/**
	 * copies the class object to a new instance
	 * @param clsMapObj
	 * @return
	 */
	public static ClassMapInfo copyInstance(ClassMapInfo clsMapObj) {
		
		return new ClassMapInfo(clsMapObj.getSrcClassName(), clsMapObj.getTrgtClassName(), clsMapObj.getMapFuncName(),clsMapObj.getAttrInfoObjs(), 
				clsMapObj.getAttrGrpInfoObjs(), clsMapObj.getMapFuncsParamValues(),clsMapObj.getElemExprInfoObjs());
	}
	
	public String getSrcClassName() {
		return srcClassName;
	}
	public void setSrcClassName(String srcClassName) {
		this.srcClassName = srcClassName;
	}
	public String getTrgtClassName() {
		return trgtClassName;
	}
	public void setTrgtClassName(String trgtClassName) {
		this.trgtClassName = trgtClassName;
	}
	public ArrayList<AttributeMapInfo> getAttrInfoObjs() {
		return attrInfoObjs;
	}
	public void setAttrInfoObjs(AttributeMapInfo attrMapInfo) {
		this.attrInfoObjs.add(attrMapInfo);
	}
	
	public ArrayList<AttrGrpMapInfo> getAttrGrpInfoObjs() {
		return attrGrpInfoObjs;
	}

	public void setAttrGrpInfoObjs(AttrGrpMapInfo attrGrpMapObj) {
		this.attrGrpInfoObjs.add(attrGrpMapObj);
	}
	
	public String getMapFuncName() {
		return mapFuncName;
	}
	
	public void setMapFuncName(String mapFuncName) {
		this.mapFuncName = mapFuncName;
	}
	
	public HashMap<String, String> getMapFuncsParamValues() {
		return mapFuncsParamValues;
	}

	public void setMapFuncsParamValues(String paramName, String paramVal) {
		if(paramName != null && paramVal != null) {
			this.mapFuncsParamValues.put(paramName, paramVal);
		}
	}

	public ArrayList<ElemExprMapInfo> getElemExprInfoObjs() {
		return elemExprInfoObjs;
	}

	public void setElemExprInfoObjs(ElemExprMapInfo elemExprInfoObj) {
		this.elemExprInfoObjs.add(elemExprInfoObj);
	}
	
}
