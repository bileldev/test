package cdm.pre.imp.reader;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.configmap.ElemExprMapInfo;
import cdm.pre.imp.writer.BaseElement;
import cdm.pre.imp.writer.ConfigMapDataset;
import cdm.pre.imp.writer.ConfigMapItem;
import cdm.pre.imp.writer.ConfigMapModel;
import cdm.pre.imp.writer.WriterUtils;
import cdm.pre.imp.reader.MappedAttributes;


/**
 * This class represents a element of the PLMXML file.
 * 
 * @author wikeim
 * 
 */
public class Element {

	private String                           id;
	private String                           obid;
	/**
	 * Id used to uniquely identify the element in a delta export, because it
	 * contains elements of two structures.
	 */
	private Map<String, String>              userValues;
	private Map<String, Map<String, String>> typedUserValues = new HashMap<String, Map<String, String>>();
	private String                           transform;
	private String                           tagName;
	private String                           appLabel;
	private String                           parentProjectName;
	// 30-01-2017 - additional attribute for Truck implementation only
	private String			 				 endItemIDs;			// captures the baumuster specific end item id for the baumuster children at all levels

	private Map<String, String>              attributes;

	@SuppressWarnings("unused")
	private static final Pattern             obidPattern     = Pattern.compile(".+?~.+?~(.+?)~.+");
	
	private final static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");
	
	/**
	 * member that holds the output of the logical combination of the mapping info with the data read from the PLMXML file
	 * The key of the Map is the name of the Target Data Model class name
	 * The value of the Map is an array list of the instances of the MappedAttributes class
	 */
	private HashMap<String, ArrayList<MappedAttributes>> mappedElementsMap;
	private boolean hasPropChanges;
	private boolean isApplyEffectivityNone = false;
	private String action = null;
	private boolean dbExceptionThrown = false;
	
	private ArrayList<ElemExprMapInfo> exprBlocksList;
	
	private String updatedProjectName = null;
	
	/**
	 * Setter method for the mapped elements map 
	 * @param trgtClassName 	: Name of the Target Class in TEAMCENTER
	 * @param trgtAttrName		: Name of the Target Attribute in TEAMCENTER
	 * @param trgtAttrValue 	: Value of the target Attribute as read from the PLMXML file
	 * @param trgtAttrScope 	: Scope of the target Attribute as read from the mapping file
	 * @param trgtAttrDataType 	: Value of the target Attribute as read from the PLMXML file
	 */
	public void setMappedElementsMap(String trgtClassName, MappedAttributes mapAttrObj) {
	
		// checks if the member map already has an entry for the target class
		if(this.mappedElementsMap.containsKey(trgtClassName)) {
			// insert the object into the class member map
			this.mappedElementsMap.get(trgtClassName).add(mapAttrObj);
		}
		// executes if there is no entry for the target class in the member map
		else {
			// creates an entry for the target class in the member map and then assigns the MappedAttribute instance to the map 
			this.mappedElementsMap.put(trgtClassName, new ArrayList<MappedAttributes>());
			this.mappedElementsMap.get(trgtClassName).add(mapAttrObj);
		}
		
		/*this.mappedElementsMap	= new HashMap<String, ArrayList<MappedAttributes>>();
		this.mappedElementsMap.put(trgtClassName, new ArrayList<MappedAttributes>());
		this.mappedElementsMap.get(trgtClassName).add(mapAttrObj);*/
	}
	
	
	
	public HashMap<String, ArrayList<MappedAttributes>> getMappedElementsMap() {
		return mappedElementsMap;
	}

	// test bug
	public Element(String pTagName, Map<String, String> pAttributes) {
		tagName = pTagName;
		attributes = pAttributes;
		if (attributes != null)
			id = attributes.get("id");
		this.mappedElementsMap	= new HashMap<String, ArrayList<MappedAttributes>>();
		this.userValues = new HashMap<String, String>();
		this.endItemIDs = null;
	}

	public Map<String, String> getUserValues() {
		return userValues;
	}

	public String getParentProjectName() {
		return parentProjectName;
	}

	public void setParentProjectName(String parentProjectName) {
		this.parentProjectName = parentProjectName;
	}

	public void setUserValues(Map<String, String> userValues) {
		this.userValues = userValues;
		// 28.10.2016 - commenting out this code as it is being handled in a custom mapping function
		/*if (userValues != null) {
			obid = userValues.get("OBID");
			if (obid == null && appLabel != null) {
				// try to extract the OBID from the application label
				// label: <ApplicationRef application="Smaragd"
				// label="MTIObjectHandle-0002-1~R~gARFYmoQusr_wgub15885385~j0Cdi3D~usr_wgub~~"/>
				// * Pattern.compile(".+?~.+?~(.+?)~.+");
				// OBID: <UserValue title="OBID" value="gARFYmoQusr_wgub15885385"/>
				Matcher match = obidPattern.matcher(appLabel);
				if (match.find()) {
					obid = match.group(1);
				}
			}
		} */
	}
	
	public void setUserValues(String userValKey, String userVal) {
	
		if(userValKey != null && userVal != null) {
			this.userValues.put(userValKey, userVal);
		}
		
	}

	public String getOBID() {
		return obid;
	}
	
	public void setOBID(String obid) {
		
		this.obid = obid;
	}

	public String getTransform() {
		return transform;
	}

	public void setTransform(String transform) {
		this.transform = transform;
	}

	public String getAppLabel() {
		return appLabel;
	}

	public void setAppLabel(String appLabel) {
		this.appLabel = appLabel;
	}

	public String getId() {
		return id;
	}

	public String getTagName() {
		return tagName;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setTypedUserValues(String type, Map<String, String> userValues) {
		typedUserValues.put(type, userValues);
	}

	public Map<String, String> getTypedUserValues(final String type) {
		return typedUserValues.get(type);
	}
	
	/**
	 * getter method for the end item id attribute 
	 * @return : string value for the end item id attribute
	 */
	public String getEndItemIDs() {
		return this.endItemIDs;
	}

	/**
	 * setter method for the end item id attribute
	 * @param endItemID : input value to be set to the class member
	 * @param isCFGItem	: boolean input that states if the end item is to be generated for a CFG item or not
	 */
	public void setEndItemIDs(String endItemID, boolean isCFGItem) {
		
		if(!isCFGItem) {
			if(endItemID != null) {
				// checks if the member is already populated or not
				if(this.endItemIDs == null) {
					this.endItemIDs = WriterUtils.generateEndItemID(endItemID); 
				}
				// appends the new end item id to an existing set of end item ids separated by the semi-colon
				else {
					// 16-02-2017 - checks if the end item already exists in the element object member
					if(this.endItemIDs.contains(IConstants.INTXML_ENDITEM_SEP)) {
						
						boolean isPresent = false;							// boolean variable to check if the end item is present or not
						
						// retrieves the individual end items from the class member
						String[] endItemArr = this.endItemIDs.split(IConstants.INTXML_ENDITEM_SEP);
						if(endItemArr.length > 0) {
							for(String endItem : endItemArr) {
								if(endItem.equals(endItemID)) {
									isPresent = true;
									break;
								}
							}
							// checks if the input end item id is not present in the class member
							if(isPresent == false) {
								// sets the hitherto not present end item to the class member
								this.endItemIDs = this.endItemIDs + IConstants.INTXML_ENDITEM_SEP + WriterUtils.generateEndItemID(endItemID);
							}
						}
					}
					// case when there is only one end item populated in the class member
					else {
						if(!this.endItemIDs.equals(WriterUtils.generateEndItemID(endItemID))) {
							this.endItemIDs = this.endItemIDs + IConstants.INTXML_ENDITEM_SEP + WriterUtils.generateEndItemID(endItemID);
						}
					}
				}
			}
		}
		// generates the end item id for a Configuration item
		else {
			this.endItemIDs = endItemID;
		}
	}
	
	public String getClazz() {
		if (userValues == null) {
			return null;
		}
		return userValues.get("Class");
	}

	public boolean isValidForXML() {
		boolean ret = false;
		// all Part elements of the PLMXML are valid for the Import XML
		// in addition to this, all the CDI3D and CDI2D are valid for the Import
		// XML
		if (IConstants.Part.equals(tagName) || IConstants.j0Cdi3D.equals(getClazz()) || IConstants.j0Cdi2D.equals(getClazz())) {
		ret = true;
		} else if (PLMUtils.isFileRef(getClazz())) {
			// selectively check the files that should be exported
			ret = getAttributes().get("location") != null;
		}
		return ret;
	}

	// BauKas & VisInt with location tag
	public boolean isValidWiringHarness() {
		boolean ret = false;
		
		String location = getAttributes().get("location");
		//if(location != null)
		//String location = getAttributes().get("location").toString();	    
		if(location != null)
		{
			if((getClazz().equals(IConstants.j0VisInt)) 
					&& (location.contains(".tif"))){
				ret = true;
			}else if((getClazz().equals(IConstants.j0BauKas))
					&&( location.contains(".csv")||location.contains(".txt"))){
				ret = true;
			}
		}
		else
		{
			logger.warn("Validating Wiring Harness: file location missing for :"+getClazz());
		}
		
		
		return ret;
	}

	public void writeElement(final XMLStreamWriter streamWriter, final Writer lstWriter, final boolean applyEffectivity, final int elemNmb,
			final String mainProject, final String endItem) throws XMLStreamException, CDMException, IOException {
		BaseElement elem = null;
		if (IConstants.Part.equals(tagName)) {
			//elem = new Item(streamWriter, this);
			elem = new ConfigMapItem(streamWriter, this);
		} else if (IConstants.CompoundRep.equals(tagName)) {
			String clazz = getClazz();
			if (PLMUtils.isFileRef(clazz)) {
				if (getAttributes().get("location") != null) {
					//elem = new Dataset(streamWriter, lstWriter, this, endItem);
					elem = new ConfigMapDataset(streamWriter, lstWriter, this, endItem);
				}
			} else if (IConstants.j0Cdi3D.equals(clazz) || IConstants.j0Cdi2D.equals(clazz)) {
				//elem = new Model(streamWriter, this);
				elem = new ConfigMapModel(streamWriter, this);
			}
		}
		if (elem != null) {

			elem.writeObject(applyEffectivity, elemNmb, mainProject, applyEffectivity);
		}
	}

	public void setMappedElementsMap(HashMap<String, ArrayList<MappedAttributes>> mappedElementsMap) {
		this.mappedElementsMap = mappedElementsMap;
	}



	public ArrayList<ElemExprMapInfo> getExprBlocksList() {
		return exprBlocksList;
	}

	
	public void setExprBlocksList(ArrayList<ElemExprMapInfo> elemExprMapInfoList) {
		this.exprBlocksList = elemExprMapInfoList;
	}



	public boolean isHasPropChanges() {
		return hasPropChanges;
	}



	public void setHasPropChanges(boolean hasPropChanges) {
		this.hasPropChanges = hasPropChanges;
	}



	public boolean isApplyEffectivityNone() {
		return isApplyEffectivityNone;
	}



	public void setApplyEffectivityNone(boolean isApplyEffectivityNone) {
		this.isApplyEffectivityNone = isApplyEffectivityNone;
	}



	public String getAction() {
		return action;
	}



	public void setAction(String action) {
		this.action = action;
	}



	public String getUpdatedProjectName() {
		return updatedProjectName;
	}



	public void setUpdatedProjectName(String updatedProjectName) {
		// AMIT DEBUG
		String strItemID = WriterUtils.getAttributeValue(this, "item_id");
		/*if(strItemID != null && strItemID.equals("A1778305401")) {
			System.out.println("Here");
		}*/
		// AMIT DEBUG
		this.updatedProjectName = updatedProjectName;
	}

	public void setDBExceptionThrown(boolean dbExceptionThrown) {
		this.dbExceptionThrown = dbExceptionThrown;
	}
	
	public boolean isDbExceptionThrown() {
		return dbExceptionThrown;
	}
}
