/**
 * 
 */
package cdm.pre.imp.configmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author amit.rath
 *
 */
public class ParseHandler extends DefaultHandler {

	private boolean isMapElemInScope;									// boolean variable which tells if the "MappingElement" element is being parsed
	private boolean isAttrElemInScope;									// boolean variable which tells if the "Attr" element is being parsed
	private boolean isAttrGrpElemInScope;								// boolean variable which tells if the "AttrGrp" element is being parsed
	private boolean isParamElemInScope;									// boolean variable which tells if the "Param" element is being parsed
	private boolean isMapFuncElemInScope;								// boolean variable which tells if the "MapFunc" element is being parsed
	private boolean isClassElemInScope;									// boolean variable which tells if the "Class" element is being parsed
	private boolean isElemExprElemInScope;								// boolean variable which tells if the "ElemExpr" element is being parsed
	private boolean isElemAttrElemInScope;								// boolean variable which tells if the "ElemAttr" element is being parsed
	private boolean isGlobalConstElemInScope;							// boolean variable which tells if the "GlobalConst" element is being parsed


	private ClassMapInfo clsMapObj;										// instance of ClassMapInfo representing the current <Class> element
	private AttrGrpMapInfo attrGrpMapObj;								// instance of the AttrGrpMapInfo representing the current <AttrGrp> element
	private ArrayList<ClassMapInfo> clsMapInfoArray;					// holds all the read mapping values for all the class mappings in the mapping file

	private ElemExprMapInfo elemExprMapObj;								// instance of the ElemExprMapInfo representing the current <ElemExpr> element

	private GlobalConstMapInfo globalConstMappInfo;						// instance of the GlobalConstMapInfo representing the current <GlobalConst> element

	private HashMap<String, ArrayList<ClassMapInfo>> clsMapCollection;	// placeholder for all the mapping information read from the mapping.xml

	private ArrayList<GlobalConstMapInfo> globalConstCollection;	// placeholder for all the mapping information read from the mapping.xml

	/**
	 * Class constructor that initializes the class member variables
	 */
	public ParseHandler() {
		this.isAttrElemInScope			= false;
		this.isAttrGrpElemInScope		= false;
		this.isMapElemInScope			= false;
		this.isMapFuncElemInScope		= false;
		this.isParamElemInScope			= false;
		this.isClassElemInScope			= false;
		this.isElemExprElemInScope		= false;
		this.isElemAttrElemInScope		= false;
		this.isGlobalConstElemInScope	= false;
		this.clsMapObj					= null;
		this.clsMapInfoArray 			= new ArrayList<ClassMapInfo>();
		this.clsMapCollection			= new HashMap<String, ArrayList<ClassMapInfo>>();
		this.globalConstCollection 		= new ArrayList<GlobalConstMapInfo>();
	}

	public HashMap<String, ArrayList<ClassMapInfo>> getClsMapCollection() {
		return clsMapCollection;
	}

	/**
	 * 
	 * @param srcClass
	 * @param clsMapObj
	 */
	public void setClsMapCollection(String srcClass, ClassMapInfo clsMapObj) {

		boolean foundInMap = false;							// boolean value to determine if the map has an entry for the input src class

		// checks if there is an existing entry in the collection object for the src class name
		if(!this.clsMapCollection.isEmpty()) {
			// define iterator for the map
			Iterator<Entry<String, ArrayList<ClassMapInfo>>> itr = clsMapCollection.entrySet().iterator();
			// iterates through the map
			while (itr.hasNext()) {
				Map.Entry<String, ArrayList<ClassMapInfo>> pair = (Map.Entry<String, ArrayList<ClassMapInfo>>)itr.next();	
				if(pair.getKey().equals(srcClass)) {
					pair.getValue().add(clsMapObj);
					foundInMap = true;
					break;
				}
			}
			// checks if the src class is present in the map or not
			if(foundInMap == false) {
				ArrayList<ClassMapInfo> tempList = new ArrayList<ClassMapInfo>();
				tempList.add(clsMapObj);
				this.clsMapCollection.put(srcClass, tempList);
			}
		}
		// executes if this is the first call to populate the map
		else {
			ArrayList<ClassMapInfo> tempList = new ArrayList<ClassMapInfo>();
			tempList.add(clsMapObj);
			this.clsMapCollection.put(srcClass, tempList);
		}
	}

	public ArrayList<ClassMapInfo> getClsMapInfoArray() {
		return clsMapInfoArray;
	}

	public void setClsMapInfoArray(ClassMapInfo clsMapObj) {
		this.clsMapInfoArray.add(clsMapObj);
	}

	public ClassMapInfo getClsMapInfoObj() {
		return clsMapObj;
	}

	public void setClsMapInfoObj(ClassMapInfo clsMapObj) {
		this.clsMapObj = clsMapObj;
	}

	public boolean isMapElemInScope() {
		return isMapElemInScope;
	}

	public void setMapElemInScope(boolean isMapElemInScope) {
		this.isMapElemInScope = isMapElemInScope;
	}

	public boolean isAttrElemInScope() {
		return isAttrElemInScope;
	}

	public void setAttrElemInScope(boolean isAttrElemInScope) {
		this.isAttrElemInScope = isAttrElemInScope;
	}

	public boolean isAttrGrpElemInScope() {
		return isAttrGrpElemInScope;
	}

	public void setAttrGrpElemInScope(boolean isAttrGrpElemInScope) {
		this.isAttrGrpElemInScope = isAttrGrpElemInScope;
	}

	public boolean isParamElemInScope() {
		return isParamElemInScope;
	}

	public void setParamElemInScope(boolean isParamElemInScope) {
		this.isParamElemInScope = isParamElemInScope;
	}

	public boolean isMapFuncElemInScope() {
		return isMapFuncElemInScope;
	}

	public void setMapFuncElemInScope(boolean isMapFuncElemInScope) {
		this.isMapFuncElemInScope = isMapFuncElemInScope;
	}

	public boolean isClassElemInScope() {
		return isClassElemInScope;
	}

	public void setClassElemInScope(boolean isClassElemInScope) {
		this.isClassElemInScope = isClassElemInScope;
	}

	public AttrGrpMapInfo getAttrGrpMapObj() {
		return attrGrpMapObj;
	}

	public void setAttrGrpMapObj(AttrGrpMapInfo attrGrpMapObj) {
		this.attrGrpMapObj = attrGrpMapObj;
	}

	@Override
	public void endElement(String uri, String localName, String xmlElemName) throws SAXException {
		try {
			if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_MAPELEM)) {
				this.isMapElemInScope = false;
				// copies the value to handler place holder for all class mappings. Done here since this represents the logical end of one
				// mapping element which corresponds to the class mapping
				//this.clsMapInfoArray.add(ClassMapInfo.copyInstance(this.clsMapObj));
				this.setClsMapCollection(this.clsMapObj.getSrcClassName(), ClassMapInfo.copyInstance(this.clsMapObj));
				this.clsMapObj	 		= null;
			}
			else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_ATTR))  {
				this.isAttrElemInScope = false;
			}
			// sets the class member for the instance of the ClassMapInfo to null for the processing of the next <Class> element
			else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_CLASS))  {
				this.isClassElemInScope = false;
			}
			else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_ATTRGRP))  {
				// checks if there was a <MapFunc> sub element under the <AttrGrp> element
				if(this.attrGrpMapObj.getMapFuncName() == null) {
					throw new MappingException("Improperly formatted Mapping XML file. Missing mandatory sub element: "
							+ "<MapFunc> under the <AttrGrp> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
				}
				// assign a copy of the attr group info object 
				this.clsMapObj.setAttrGrpInfoObjs(AttrGrpMapInfo.copyInstance(this.attrGrpMapObj));
				this.isAttrGrpElemInScope	= false;
				this.attrGrpMapObj			= null;
			}
			else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_ELEMEXPR))  {
				// checks if there was a <MapFunc> sub element under the <AttrGrp> element
				if(this.elemExprMapObj.getElementType() == null) {
					throw new MappingException("Improperly formatted Mapping XML file. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
				}
				else
				{
					ElemExprMapInfo test = ElemExprMapInfo.copyInstance(this.elemExprMapObj);
					// assign a copy of the attr group info object 
					this.clsMapObj.setElemExprInfoObjs(ElemExprMapInfo.copyInstance(test));
					//this.clsMapObj.setElemExprInfoObjs(this.elemExprMapObj);
				}

				this.isElemExprElemInScope	= false;
				this.elemExprMapObj			= null;
			}
			else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_GLOBALCONST))  {
				if(this.globalConstMappInfo == null) {
					throw new MappingException("Improperly formatted Mapping XML file. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
				}
				else
				{
					/*ElemExprMapInfo test = ElemExprMapInfo.copyInstance(this.elemExprMapObj);
					// assign a copy of the attr group info object 
					this.clsMapObj.setElemExprInfoObjs(ElemExprMapInfo.copyInstance(test));*/
					//this.clsMapObj.setElemExprInfoObjs(this.elemExprMapObj);
					
					this.globalConstCollection.add(globalConstMappInfo);
				}

				this.isGlobalConstElemInScope		= false;
				this.globalConstMappInfo			= null;
			}
			else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_ELEMATTR))  {
				this.isElemAttrElemInScope = false;
			}
			else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_MAPFUNC))  {
				this.isMapFuncElemInScope = false;
			}
			else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_PARAM))  {
				this.isParamElemInScope = false;
			}
		} catch(MappingException ex) {
			ex.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public void startElement(String uri, String localName, String xmlElemName, Attributes xmlAttributes) throws SAXException {

		// handles processing of the "Mapping Element" element
		if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_MAPELEM) ) {
			this.isMapElemInScope = true;
		}
		// handles processing of the "Attr" element
		else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_ATTR))  {
			this.isAttrElemInScope = true;
			this.processAttrElem(xmlAttributes);
		}
		// handles processing of the "AttrGrp" element
		else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_ATTRGRP))  {
			this.isAttrGrpElemInScope = true;
			this.processAttrGrpElem();
		}
		// handles processing of the "MapFunc" element
		else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_MAPFUNC))  {
			this.isMapFuncElemInScope = true;
			this.processMapFuncElem(xmlAttributes);
		}
		// handles processing of the "Param" element
		else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_PARAM))  {
			this.isParamElemInScope = true;
			this.processParamElem(xmlAttributes);
		}
		// handles processing of the "Class" element
		else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_CLASS))  {
			this.isClassElemInScope = true;
			// method call to process the <Class> element
			this.processClassElem(xmlAttributes);
		}
		else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_ELEMEXPR))  {
			this.setElemExprElemInScope(true);
			// method call to process the <Class> element
			this.processElemExpr(xmlAttributes);
		}
		else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_ELEMATTR))  {
			this.setElemAttrElemInScope(true);
			this.processExprAttrElem(xmlAttributes);
		}
		else if(xmlElemName.equals(ConfigMapUtils.XML_ELEM_GLOBALCONST))  {
			this.setGlobalConstElemInScope(true);
			this.processGlobalConstElem(xmlAttributes);
		}
	}

	/**
	 * method that processes the "Class" element
	 * A separate method for handling the wrapper mapping element is not required.
	 * @param xmlAttributes
	 */
	public void processClassElem(Attributes xmlAttributes) {

		try {
			// checks if there is already a mapping element wrapper for the class element or not
			if(this.isMapElemInScope == false) {
				throw new MappingException("Improperly formatted mapping XML file. No wrapping <MapppingElement> element for "
						+ "a <Class> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
						Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);	
			}
			// block that processes the <Class> element
			else {
				String srcAttrVal	= null;						// temporary variable for XML attribute <src>
				String trgtAttrVal	= null;;					// temporary variable for XML attribute <target>

				// loops through the attributes for the <Class> element
				for(int iInx=0; iInx < xmlAttributes.getLength(); ++iInx) {
					// checks if the attribute is the src attribute
					if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_SRC)) {
						srcAttrVal = xmlAttributes.getValue(iInx);
					}
					// checks if the attribute is the target attribute
					else if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_TRGT)) {
						trgtAttrVal = xmlAttributes.getValue(iInx);
					}
					// throws an exception for an invalid attribute name for the <Class> element
					else {
						throw new MappingException("Invalid Attribute Name: "+ xmlAttributes.getQName(iInx) + " provided "
								+ "for the <Class> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
				}
				// block of code that checks if the values for the src and target attributes are present or not
				if(srcAttrVal.isEmpty()) {
					throw new MappingException("Value for the xml attribute <src> is missing for <Class> xml element", true,
							ConfigMapUtils.LOG_TYPE_ERROR);
				}
				if(trgtAttrVal.isEmpty()) {
					throw new MappingException("Value for the xml attribute <target> is missing for <Class> xml element", true, 
							ConfigMapUtils.LOG_TYPE_ERROR);
				}
				// creates an instance of the ClassMapInfo class and sets it to the class member
				this.clsMapObj = new ClassMapInfo(srcAttrVal, trgtAttrVal);
			}

		} catch (MappingException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * method that processes the <Attr> XML element
	 * @param xmlAttributes
	 */
	@SuppressWarnings({ })
	public void processAttrElem(Attributes xmlAttributes) {

		try {
			// checks if the <Attr> element is a child of a valid <Class> or <AttrGrp> element element
			if((this.isClassElemInScope == false && this.clsMapObj == null) && (this.isAttrGrpElemInScope == false)) {
				throw new MappingException("Improperly formatted mapping XML file. No wrapping <Class> or <AttrGrp>"
						+ " element for an <Attr> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
						Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
			}
			// second check to ensure that there is not more than one child <Attr> element under an <AttrGrp> element
			if(this.isAttrGrpElemInScope == true && this.attrGrpMapObj.getAttrMapInfoObj() != null) {
				throw new MappingException("Imporerly formatted mapping XML file. There is already an <Attr> element"
						+ " as a child under the current <AttrGrp> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
						Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
			}
			// block that processes the <Attr> element
			else {
				boolean reqAttrVal					= false;					// temporary variable to store the "req" attribute
				String strReqAttrVal				= null;						// temporary variable to store the "req" attribute from mapping xml
				String typeAttrVal					= null;						// temporary variable to store the "type" attribute
				String scopeAttrVal					= null;						// temporary variable to store the "scope" attribute
				String trgtAttrVal					= null;						// temporary variable to store the "target" attribute
				ArrayList<String> srcAttrNameVals	= new ArrayList<String> ();	// temporary variable to store the "src" attribute

				// loops through the attributes of the <Attr> XML element to retrieve their values
				for(int iInx=0; iInx < xmlAttributes.getLength(); ++iInx) {
					if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_REQ)) {
						strReqAttrVal = xmlAttributes.getValue(iInx);
					}
					else if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_SCOPE)) {
						scopeAttrVal = xmlAttributes.getValue(iInx);
					}
					else if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_TRGT)) {
						trgtAttrVal = xmlAttributes.getValue(iInx);
					}
					else if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_TYPE)) {
						typeAttrVal = xmlAttributes.getValue(iInx);
					}
					// need to check if the <Attr> element is a sub element of <AttrGrp> in which case there should be more 
					// than one source attribute separated by a separator
					else if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_SRC)) {
						if(this.isAttrGrpElemInScope == true) {
							if(xmlAttributes.getValue(iInx).contains(ConfigMapUtils.STRING_SEPARATOR)) {
								String[] tempStrArray = xmlAttributes.getValue(iInx).split(ConfigMapUtils.STRING_SEPARATOR);
								if(tempStrArray != null && tempStrArray.length >= 2) {
									srcAttrNameVals.add(tempStrArray[0]);
									srcAttrNameVals.add(tempStrArray[1]);
								}
								else 
								{
									if(tempStrArray != null)
									{
										System.out.println("SRC Name ::::::: "+tempStrArray[0]);
									}
									throw new MappingException("Improperly formatted src attr names in the mapping xml"
											+ " file for the <Attr> element under an <AttrGrp> element. Exception thrown in Class: " 
											+ this.getClass().getName() + " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), true,
											ConfigMapUtils.LOG_TYPE_ERROR);							
								}
							}
							// loop to handle singular source attributes that need a custom mapping function
							else {
								srcAttrNameVals.add(xmlAttributes.getValue(iInx));
							}
						}
						else {
							srcAttrNameVals.add(xmlAttributes.getValue(iInx));
						}
					}
					// throws an exception for an invalid attribute name
					else {
						throw new MappingException("An invalid attribute name: " + xmlAttributes.getQName(iInx) + 
								" is being used in conjunction with the <Attr> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
				}
				// checks for the availability of the mandatory attributes 
				if(strReqAttrVal.isEmpty()) {
					throw new MappingException("Mandatory attribute <req> is missing from the <Attr> sub element "
							+ "belonging to the parent <Class> element with <src> attribute value: " + this.clsMapObj.getSrcClassName()
							+ " Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
				}
				else {
					if(strReqAttrVal.equals("true")) {
						reqAttrVal = true;
					}
					else if(strReqAttrVal.equals("false")) {
						reqAttrVal = false;
					}
					// throws an exception in case an invalid value is specified for the boolean attribute
					else {
						throw new MappingException("Invalid value provided in the mapping XML file for boolean attribute"
								+ " <req>. Valid values are true or false. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
				}
				if(scopeAttrVal.isEmpty()) {
					throw new MappingException("Mandatory attribute <scope> is missing from the <Attr> sub element "
							+ "belonging to the parent <Class> element with <src> attribute value: " + this.clsMapObj.getSrcClassName()
							+ " Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
				}
				if(typeAttrVal.isEmpty()) {
					throw new MappingException("Mandatory attribute <type> is missing from the <Attr> sub element "
							+ "belonging to the parent <Class> element with <src> attribute value: " + this.clsMapObj.getSrcClassName()
							+ " Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
				}
				if(trgtAttrVal.isEmpty()) {
					throw new MappingException("Mandatory attribute <target> is missing from the <Attr> sub element "
							+ "belonging to the parent <Class> element with <src> attribute value: " + this.clsMapObj.getSrcClassName()
							+ " Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
				}
				if(srcAttrNameVals.isEmpty()) {
					throw new MappingException("Mandatory attribute <src> is missing from the <Attr> sub element "
							+ "belonging to the parent <Class> element with <src> attribute value: " + this.clsMapObj.getSrcClassName()
							+ " Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
				}
				// instantiates the AttrMapInfo class and assigns the object to the current instance of the ClassMapInfo class
				AttributeMapInfo attrMapInfoObj = new AttributeMapInfo(reqAttrVal, trgtAttrVal, typeAttrVal, scopeAttrVal, 
						srcAttrNameVals);
				if(attrMapInfoObj != null) {
					// sets the attrMap object to the direct XML element parent
					if(this.isAttrGrpElemInScope == true) {
						this.attrGrpMapObj.setAttrMapInfoObj(attrMapInfoObj);
					}
					else if(this.isClassElemInScope == true) {
						this.clsMapObj.setAttrInfoObjs(attrMapInfoObj);
					}
				}
			}

		} catch (MappingException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * method that processes the <AttrGrp> XML element
	 */
	public void processAttrGrpElem() {

		try {
			// checks of the <AttrGrp> element has a parent <Class> element or not in the mapping XML file
			if(this.isClassElemInScope == false && this.clsMapObj == null) {
				throw new MappingException("Improperly formatted mapping XML file. No wrapping <Class> element for "
						+ "an <AttrGrp> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
						Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
			}
			// processes the <AttrGrp> element in the mapping XML file
			else {
				// instantiates the AttrGrpMapInfo class to the class member
				this.attrGrpMapObj = new AttrGrpMapInfo();
			}
		} catch(MappingException ex) {
			ex.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * method that processes the <AttrGrp> XML element
	 * @param xmlAttributes 
	 */
	/*public void processElemExpr(Attributes xmlAttributes) {

		try {
			// checks of the <AttrGrp> element has a parent <Class> element or not in the mapping XML file
			if(this.isClassElemInScope == false && this.clsMapObj == null) {
				throw new MappingException("Improperly formatted mapping XML file. No wrapping <Class> element for "
						+ "an <AttrGrp> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
			}
			// processes the <AttrGrp> element in the mapping XML file
			else {
				// instantiates the AttrGrpMapInfo class to the class member
				this.attrGrpMapObj = new AttrGrpMapInfo();
			}
		} catch(MappingException ex) {
			ex.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}*/

	/**
	 * this method processes the <MapFunc> element in the mapping XML file
	 * @param xmlAttributes
	 */
	public void processMapFuncElem(Attributes xmlAttributes) {

		try {
			// checks if the map function element is for a class mapping
			if(this.isClassElemInScope == true) {
				// checks if the class mapping already has a mapping function definition
				if(this.clsMapObj.getMapFuncName() != null) {
					// throws a custom mapping exception and exits out of the code
					throw new MappingException("Improperly formatted mapping file. There cannot be more than one mapping functions defined for the"
							+ " same <Class> element. Error found in mapping element for source class: " + this.clsMapObj.getSrcClassName()
							+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
				}
				if(this.isAttrGrpElemInScope == false) {

					// handling of the class to class mapping function. 
					for(int iInx=0; iInx < xmlAttributes.getLength(); ++iInx) {
						if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_NAME)) {
							this.clsMapObj.setMapFuncName(xmlAttributes.getValue(iInx));
						}
					}

					/*throw new MappingException("Improperly formatted mapping xml file. <MapFunc> element either does not"
							+ " have a parent <AttrGrp> element or does not have at least one sibling <Attr> element. Exception "
							+ "thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);*/
				}
				// sets the values to the current attribute group info object - handling for the map function inside an <AttrGrp> element
				else {
					// null check for the attribute group map
					if(this.attrGrpMapObj != null) { 
						for(int iInx=0; iInx < xmlAttributes.getLength(); ++iInx) {
							if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_NAME)) {
								this.attrGrpMapObj.setMapFuncName(xmlAttributes.getValue(iInx));
							}
							else {
								throw new MappingException("Invalid attribute name: " + xmlAttributes.getQName(iInx) 
								+" being used in conjunction with <MapFunc> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
							}
						}
					}
					if(this.attrGrpMapObj.getMapFuncName().isEmpty()) {
						throw new MappingException("Value for mandatory attribute: <name> of <MapFunc> missing. Exception "
								+ "thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
				}
			}
		} catch(MappingException ex) {
			ex.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * this method processes the <MapFunc> element in the mapping XML file
	 * @param xmlAttributes
	 */
	public void processElemExpr(Attributes xmlAttributes) {

		try {
			// checks if the map function element is for a class mapping
			if(this.isClassElemInScope == true) {

				if(this.isElemExprElemInScope == false) {

					// handling of the class to class mapping function. 
					/*for(int iInx=0; iInx < xmlAttributes.getLength(); ++iInx) {
						if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_NAME)) {
							this.clsMapObj.setMapFuncName(xmlAttributes.getValue(iInx));
						}
					}*/

					throw new MappingException("Improperly formatted mapping xml file. <ElemExpr> element either does notat least one sibling <ExprAttr> element. Exception "
							+ "thrown in Class: " + this.getClass().getName() + " at Line: " + 
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
				}
				// sets the values to the current attribute group info object - handling for the map function inside an <AttrGrp> element
				else {
					// null check for the attribute group map
					elemExprMapObj = new ElemExprMapInfo();
					if(this.elemExprMapObj != null) { 
						for(int iInx=0; iInx < xmlAttributes.getLength(); ++iInx) {
							if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_ELEMTYPE)) {
								this.elemExprMapObj.setElementType(xmlAttributes.getValue(iInx));
							}
							else if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_ELMEXPR_LOC)) {
								this.elemExprMapObj.setLoc(xmlAttributes.getValue(iInx));
							}
							else {
								throw new MappingException("Invalid attribute name: " + xmlAttributes.getQName(iInx) 
								+" being used in conjunction with <ElemExpr> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
							}
						}
					}
					if(this.elemExprMapObj.getElementType().isEmpty()) {
						throw new MappingException("Value for mandatory attribute: <name> of <ElemExpr> missing. Exception "
								+ "thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
				}
			}
		} catch(MappingException ex) {
			ex.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This method takes care of the processing of the <Param> element in the mapping XML file
	 * @param xmlAttributes
	 */
	public void processParamElem(Attributes xmlAttributes) {

		try {
			String paramName 	= null;								// temporary variable to store the parameter name
			String paramVal		= null;								// temporary variable to store the parameter value

			if(this.isClassElemInScope == true) {
				// checks if the <Param> element is a child of a <MapFunc> element
				// if((this.isMapFuncElemInScope == false) && (this.attrGrpMapObj.getMapFuncName().isEmpty())) {
				if(this.isAttrGrpElemInScope == false) {
					// retrieves the mapping function parameters for the class level mapping function
					// need to check if there is already a mapping function defined for this class mapping object
					for(int iInx=0; iInx < xmlAttributes.getLength(); ++iInx) {
						if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_NAME)) {
							paramName = xmlAttributes.getValue(iInx);
						}
						else if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_VALUE)) {
							paramVal = xmlAttributes.getValue(iInx);
						}
					}
					if(paramName.isEmpty()) {
						throw new MappingException("Value of mandatory attribute <name> is missing for the <Param> element for the <Class> element. "
								+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
					if(paramVal.isEmpty()) {
						throw new MappingException("Value of mandatory attribute <value> is missing for the <Param> element for the <Class> element. "
								+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
					// sets the value to the class to class mapping
					this.clsMapObj.setMapFuncsParamValues(paramName, paramVal);
					/*throw new MappingException("Either the <Param> element does not have a parent <MapFunc> element or"
						+ " the <MapFunc> element does not have the mandatory name attribute. Exception thrown in "
						+ "Class: " + this.getClass().getName() + " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), true,
						ConfigMapUtils.LOG_TYPE_ERROR);*/
				}
				// processes the <Param> element in the mapping XML file for a Map Func inside an AttrGrp element
				else {
					// loops through the attribute list for the <Param> xml element
					for(int iInx=0; iInx < xmlAttributes.getLength(); ++iInx) {
						if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_NAME)) {
							paramName = xmlAttributes.getValue(iInx);
						}
						else if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_VALUE)) {
							paramVal = xmlAttributes.getValue(iInx);
						}
						// throws an exception for an invalid attribute in the <Param> element
						else {
							throw new MappingException("Invalid attribute name: " + xmlAttributes.getQName(iInx) 
							+" being used in conjunction with <Param> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
						}
					}
					// checks for the availability of the attribute values
					if(paramName.isEmpty()) {
						throw new MappingException("Value of mandatory attribute <name> is missing for the <Param> element. "
								+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
					if(paramVal.isEmpty()) {
						throw new MappingException("Value of mandatory attribute <value> is missing for the <Param> element. "
								+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
					//saves the param name and value to the current attr grp object
					this.attrGrpMapObj.setMapFuncsParamValues(paramName, paramVal);
				}
			}

		} catch(MappingException ex) {
			ex.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}




	/**
	 * This method takes care of the processing of the <Param> element in the mapping XML file
	 * @param xmlAttributes
	 */
	public void processGlobalConstElem(Attributes xmlAttributes) {

		try {
			String attrName 	= null;								// temporary variable to store the Attribute name
			String attrVal		= null;								// temporary variable to store the Attribute value

			if(this.isGlobalConstElemInScope ) {
				globalConstMappInfo = new GlobalConstMapInfo();

				// loops through the attribute list for the <GlobalConst> xml element. 
				for(int iInx=0; iInx < xmlAttributes.getLength(); ++iInx) {

					attrName = xmlAttributes.getQName(iInx);
					attrVal = xmlAttributes.getValue(iInx);
					switch (attrName) {
					case ConfigMapUtils.XML_ATTR_VAR:
						globalConstMappInfo.setVarName(attrVal);
						break;
					case ConfigMapUtils.XML_ATTR_VARXMLPATH:
						globalConstMappInfo.setVarXMLPath(attrVal);
						break;
					case ConfigMapUtils.XML_ATTR_VARXMLATTR:
						globalConstMappInfo.setVarXMLAttr(attrVal);
						break;
					case ConfigMapUtils.XML_ATTR_VALUE:
						globalConstMappInfo.setValue(attrVal);
						break;
					default:
						break;
					}
				/*	if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_VAR)) {
						globalConstMappInfo.setVarName(xmlAttributes.getValue(iInx));
					}
					
					// throws an exception for an invalid attribute in the <Param> element
					else {
						throw new MappingException("Invalid attribute name: " + xmlAttributes.getQName(iInx) 
						+" being used in conjunction with <Param> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
						Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}*/
				}
				
			}

		} catch(MappingException ex) {
			ex.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * This method takes care of the processing of the <Param> element in the mapping XML file
	 * @param xmlAttributes
	 */
	public void processExprAttrElem(Attributes xmlAttributes) {

		try {
			String exprAttrName 	= null;								// temporary variable to store the Expression Block attribute Name
			String exprAttrValue		= null;								// temporary variable to store the Expression Block attribute Value

			if(this.isClassElemInScope == true) {

				if(this.isElemExprElemInScope )
				{

					// loops through the attribute list for the <Param> xml element
					for(int iInx=0; iInx < xmlAttributes.getLength(); ++iInx) {
						if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_NAME)) {
							exprAttrName = xmlAttributes.getValue(iInx);
						}
						else if(xmlAttributes.getQName(iInx).equals(ConfigMapUtils.XML_ATTR_VALUE)) {
							exprAttrValue = xmlAttributes.getValue(iInx);
						}
						// throws an exception for an invalid attribute in the <Param> element
						else {
							throw new MappingException("Invalid attribute name: " + xmlAttributes.getQName(iInx) 
							+" being used in conjunction with <Param> element. Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
						}
					}
					// checks for the availability of the attribute values
					if(exprAttrName.isEmpty()) {
						throw new MappingException("Value of mandatory attribute <name> is missing for the <Param> element. "
								+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
					if(exprAttrValue.isEmpty()) {
						throw new MappingException("Value of mandatory attribute <value> is missing for the <Param> element. "
								+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
								Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
					}
					//saves the param name and value to the current attr grp object
					this.elemExprMapObj.setElemExprAttrValues(exprAttrName, exprAttrValue);
				}
				else
				{
					throw new MappingException("Either the <ExprAttr> element does not have a parent <ElemExpr> element  Exception thrown in "
							+ "Class: " + this.getClass().getName() + " at Line: " + Thread.currentThread().getStackTrace()[2].getLineNumber(), true,
							ConfigMapUtils.LOG_TYPE_ERROR);
				}
			}

		} catch(MappingException ex) {
			ex.printStackTrace();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean isElemExprElemInScope() {
		return isElemExprElemInScope;
	}

	public void setElemExprElemInScope(boolean isElemExprElemInScope) {
		this.isElemExprElemInScope = isElemExprElemInScope;
	}

	public boolean isElemAttrElemInScope() {
		return isElemAttrElemInScope;
	}

	public void setElemAttrElemInScope(boolean isElemAttrElemInScope) {
		this.isElemAttrElemInScope = isElemAttrElemInScope;
	}

	public ElemExprMapInfo getElemExprMapObj() {
		return elemExprMapObj;
	}

	public void setElemExprMapObj(ElemExprMapInfo elemExprMapObj) {
		this.elemExprMapObj = elemExprMapObj;
	}

	public ArrayList<GlobalConstMapInfo> getGlobalConstCollection() {
		return globalConstCollection;
	}

	public void setGlobalConstCollection(ArrayList<GlobalConstMapInfo> globalConstCollection) {
		this.globalConstCollection = globalConstCollection;
	}

	public boolean isGlobalConstElemInScope() {
		return isGlobalConstElemInScope;
	}

	public void setGlobalConstElemInScope(boolean isGlobalConstElemInScope) {
		this.isGlobalConstElemInScope = isGlobalConstElemInScope;
	}

}

