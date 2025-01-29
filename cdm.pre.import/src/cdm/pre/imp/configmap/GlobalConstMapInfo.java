package cdm.pre.imp.configmap;

public class GlobalConstMapInfo 
{
	
	
	//<GlobalConst var="creationDate" varXMLPath="dcx:Header" varXMLAttr="creationDate"/>
	//<GlobalConst var="REFCONFIG" varXMLPath="dcx:Header@UserValue" varXMLAttr="title" value="value"/>
	//<GlobalConst var="WELD_TYPES" value="weld1, weld2,weld3">
	
	private String varName = null;						// Global Atribute Name
	
	private String varXMLPath =  null;					// XML Path to pick the global attribute value
	
	private String varXMLAttr = null;					// XML attribute name for global attribute
	
	private String value = null;						// Global Atribute Value		

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getVarXMLPath() {
		return varXMLPath;
	}

	public void setVarXMLPath(String varXMLPath) {
		this.varXMLPath = varXMLPath;
	}

	public String getVarXMLAttr() {
		return varXMLAttr;
	}

	public void setVarXMLAttr(String varXMLAttr) {
		this.varXMLAttr = varXMLAttr;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
