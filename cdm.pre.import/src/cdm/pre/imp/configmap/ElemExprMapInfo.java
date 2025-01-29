package cdm.pre.imp.configmap;

import java.util.HashMap;

public class ElemExprMapInfo 
{

	/*	
 	
 		<ElemExpr elemTyp="rel">
			<ElemAttr name ="source" value="ITEM"/>
			<ElemAttr name ="type" value="IMAN_Manifestation"/>
			<ElemAttr name ="trgtType" value="RevisionRule"/>
			<ElemAttr name ="trgtID" value="_$REFCONFIG"/>
		</ElemExpr>

	 */
	private String elementType = null;
	private String loc = null;
	
	private ElemExprMapInfo elemExprMapInfoObj;
	private HashMap<String, String> elemExprAttrValues;
	
	
	/**
	 * parameter-less constructor. Initializes the class member variables
	 */
	public ElemExprMapInfo() 
	{
		
		this.elementType			= null;
		this.elemExprMapInfoObj			= null;
		this.elemExprAttrValues	= new HashMap<String, String>();	
	}
	
	public ElemExprMapInfo(String elementType, String loc,ElemExprMapInfo elemExprMapInfoObj, HashMap<String, String> elemExprAttrValues) 
	{
		this.elementType = elementType;
		this.loc = loc;
		this.elemExprMapInfoObj = elemExprMapInfoObj;
		this.elemExprAttrValues = elemExprAttrValues;
	}

	/**
	 * copies the object of the same class passed as argument to the method
	 * @param elemExprMapInfoObj
	 * @return
	 */
	public static ElemExprMapInfo copyInstance(ElemExprMapInfo elemExprMapInfoObj)
	{
		return new ElemExprMapInfo(elemExprMapInfoObj.getElementType(), elemExprMapInfoObj.getLoc(),elemExprMapInfoObj.getElemExprMapInfoObj(), elemExprMapInfoObj.getElemExprAttrValues());
	}
	
	public String getElementType() {
		return elementType;
	}

	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

		public ElemExprMapInfo getElemExprMapInfoObj() {
		return elemExprMapInfoObj;
	}

	public void setElemExprMapInfoObj(ElemExprMapInfo elemExprMapInfoObj) {
		this.elemExprMapInfoObj = elemExprMapInfoObj;
	}

	public HashMap<String, String> getElemExprAttrValues() {
		return elemExprAttrValues;
	}

	public void setElemExprAttrValues(String elemExprAttrName, String elemExprAttrValue) {
		this.elemExprAttrValues.put(elemExprAttrName, elemExprAttrValue);
	}

	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}
	
}
