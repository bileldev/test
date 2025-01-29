/**
 * 
 */
package cdm.pre.imp.configmap;

import java.util.ArrayList;

/**
 * @author amit.rath
 *
 */
public class AttributeMapInfo {
	
	boolean trgtAttrReq;				// attribute which states if it is required attribute or not
	
	String trgtAttrName;				// name of the attribute in the target data model
	String trgtAttrType;				// data type of the target attribute
	String trgtAttrScope;				// target object type to which the attribute belongs (Item, ItemRev, Dataset, Form, etc.)
	
	ArrayList<String> srcAttrNames;		// array of source attribute names for N:1 mapping
	
	/**
	 * non parameterized constructor
	 */
	public AttributeMapInfo() {
		
		this.trgtAttrReq	= false;
		this.trgtAttrName	= null;
		this.trgtAttrScope	= null;
		this.trgtAttrScope 	= null;
		
		this.srcAttrNames	= new ArrayList<String>();
		
	}
	
	public AttributeMapInfo(boolean trgtAttrReq, String trgtAttrName, String trgtAttrType, String trgtAttrScope,
			ArrayList<String> srcAttrNames) {
		this.trgtAttrReq = trgtAttrReq;
		this.trgtAttrName = trgtAttrName;
		this.trgtAttrType = trgtAttrType;
		this.trgtAttrScope = trgtAttrScope;
		this.srcAttrNames = srcAttrNames;
	}
	
	public boolean isTrgtAttrReq() {
		return trgtAttrReq;
	}
	
	public void setTrgtAttrReq(boolean trgtAttrReq) {
		this.trgtAttrReq = trgtAttrReq;
	}
	
	public String getTrgtAttrName() {
		return trgtAttrName;
	}
	
	public void setTrgtAttrName(String trgtAttrName) {
		this.trgtAttrName = trgtAttrName;
	}
	
	public String getTrgtAttrType() {
		return trgtAttrType;
	}
	
	public void setTrgtAttrType(String trgtAttrType) {
		this.trgtAttrType = trgtAttrType;
	}
	
	public String getTrgtAttrScope() {
		return trgtAttrScope;
	}
	
	public void setTrgtAttrScope(String trgtAttrScope) {
		this.trgtAttrScope = trgtAttrScope;
	}
	
	public ArrayList<String> getSrcAttrNames() {
		return srcAttrNames;
	}
	
	public void setSrcAttrNames(String srcAttrName) {
		this.srcAttrNames.add(srcAttrName);
	}
	

}
