package cdm.pre.imp.reader;


/**
 * 
 * @author amit.rath
 *
 */
public class MappedAttributes {
	
	/**
	 * class member variable that holds information of the mandatory nature of the target attribute
	 */
	private boolean isAttrReq;
	
	/**
	 * class member variable that holds the name of the target attribute
	 */
	private String attrName;
	/**
	 * class member variable that holds the value of the target attribute
	 */
	private String attrValue;
	/**
	 * class member variable that holds the object scope of the target attribute
	 */
	private String attrScope;
	/**
	 * class member variable that holds the data type  of the target attribute
	 */
	private String attrDataType;
	
	
	private boolean isModified = false;
	
	
	
	/**
	 * Parameterless constructor for the class that initializes all the class member variables
	 */
	
	public MappedAttributes() {
		this.attrName		= null;
		this.attrValue		= null;
		this.attrScope		= null;
		this.attrDataType	= null;
		this.isAttrReq		= false;
	}
	
	/**
	 * Parameterized constructor for the class
	 * @param attrName		: Name of the attribute in the target data model
	 * @param attrValue		: Value of the attribute in the target data model
	 * @param attrScope		: Object scope of the attribute in the target data model
	 * @param attrDataType	: Data Type of the attribute in the target data model
	 * @param isAttrReq		: Is attribute mandatory in the target data model ?
	 */
	public MappedAttributes(String attrName, String attrValue, String attrScope, String attrDataType, boolean isAttrReq) {
		this.attrName 		= attrName;
		this.attrValue 		= attrValue;
		this.attrScope		= attrScope;
		this.attrDataType 	= attrDataType;
		this.isAttrReq		= isAttrReq;
	}

	/**
	 * getter for the attribute name class member
	 * @return : name of the attribute 
	 */
	public String getAttrName() {
		return attrName;
	}

	/**
	 * setter for the attribute name class member
	 * @param attrName	: input name of the attribute
	 */
	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	/**
	 * getter for the attribute value class member
	 * @return	: value of the attribute 
	 */
	public String getAttrValue() {
		return attrValue;
	}

	/**
	 * setter for the attribute value class member
	 * @param attrValue : input value for the attribute
	 */
	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}

	/**
	 * getter for the attribute scope class variable
	 * @return	: scope value for the attribute 
	 */
	public String getAttrScope() {
		return attrScope;
	}

	/**
	 * setter for the attribute scope class member
	 * @param attrScope : input value for the attribute scope
	 */
	public void setAttrScope(String attrScope) {
		this.attrScope = attrScope;
	}

	/**
	 * getter for the attribute data type class member
	 * @return : data type of the attribute
	 */
	public String getAttrDataType() {
		return attrDataType;
	}

	/**
	 * setter for the attribute date type class member
	 * @param attrDataType : input value of the attribute data type
	 */
	public void setAttrDataType(String attrDataType) {
		this.attrDataType = attrDataType;
	}

	/**
	 * getter for the "is attribute required" class member
	 * @return	: boolean value for the class member
	 */
	public boolean isAttrReq() {
		return isAttrReq;
	}

	/**
	 * setter for the "is attribute required: class member
	 * @param isAttrReq	: input value for the class member 
	 */
	public void setAttrReq(boolean isAttrReq) {
		this.isAttrReq = isAttrReq;
	}

	public boolean isModified() {
		return isModified;
	}

	public void setModified(boolean isModified) {
		this.isModified = isModified;
	}
	
}
