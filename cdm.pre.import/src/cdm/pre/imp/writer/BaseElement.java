package cdm.pre.imp.writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.configmap.ElemExprMapInfo;
import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.Element;
import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.PLMUtils;
import cdm.pre.imp.reader.ReaderSingleton;

/**
 * This class is used the create the objects which should be written to the
 * Import XML files.
 * 
 * @author WIKEIM
 * 
 */
abstract public class BaseElement {
	public static final String    APPLY_EFFECTIVITY = "ae";
	public final static String    ID                = "id";
	public final static String    ELEM_NMB          = "elemNmb";
	public final static String    DS_REL            = "dsRel";
	public final static String	 END_ITEM_ID	  	= "endItem"; //tagBV
	public final static String	 TAG_BV	   			= "tagBV"; 
	public final static String	 TAG_BVR	   		= "tagBVR"; 
	public final static String	 ACTION	   		= "action"; 
	public final static String	 TAG_OCC	   			= "tagOCC"; 
	public final static String	 TAG_DATASET	   			= "tagDataset"; 
	public final static String 	 TAG_REL_COUNT 		= "relCount";

	private final static Pattern  smaDatePattern    = Pattern.compile("(\\d+)/(\\d+)/(\\d+)-(\\d+):(\\d+):(\\d+):(\\d+)");
	protected XMLStreamWriter     streamWriter;
	protected Map<String, String> attributes;
	protected Map<String, String> userValues;
	protected Element             element;
	

	//private final static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");
	final private static Logger logger = LogManager.getLogger(BaseElement.class);
	public BaseElement(final XMLStreamWriter streamWriter, final Element element) {
		this.streamWriter = streamWriter;
		this.element = element;
		this.userValues = element.getUserValues();
		this.attributes = element.getAttributes();
	}

	protected String getCDMType() throws CDMException {
		String ret = ICustom.TYPE_MAP.get(element.getClazz());
		if (ret == null) {
			logger.error(element.getClazz() + " doesn't have a corresponding CDM type");
			throw new CDMException(element.getClazz() + " doesn't have a corresponding CDM type");
			
		}
		return ret;
	}

	protected String getConfigMappedCDMType() throws CDMException {
		String ret = null;
		if( element.getMappedElementsMap() != null)
		{
			Object[] keys = element.getMappedElementsMap().keySet().toArray();
			if( keys != null && keys.length > 0)
			{
				ret = (String) keys[0];
				logger.info((element.getClazz() + " corresponding CDM type :: "+ret));
			}
		}
		if (ret == null) {
			logger.error(element.getClazz() + " doesn't have a corresponding CDM type");
			throw new CDMException(element.getClazz() + " doesn't have a corresponding CDM type");
		}
		return ret;
	}

	abstract public void writeObjectValues(final boolean hasChanges) throws XMLStreamException, IOException;

	public void writeObject(final boolean applyEffectivity, final int elemNmb, final String mainProject, final boolean hasChanges)
			throws XMLStreamException, IOException, CDMException {

		String item_puid = WriterUtils.getAttributeValue(element, "tagItem");
		String rev_puid = WriterUtils.getAttributeValue(element, "tagItemRev");
		if( element.getClazz().equals(IConstants.j0Cdi3D))
		{
		//	logger.info("Writer : OBID : "+element.getAppLabel());
			
			/*if("VwgpyFKuusr_wgub19238059".equals(WriterUtils.getAttributeValue(element,"id")))
			{
				System.out.println(" In Writing 3DModel :: ");
			}*/
			ReaderSingleton.getReaderSingleton().addWriterAppLabel(element.getAppLabel());
		}

		/*	if(element.getUserValues().get("PartNumber").equals("A3026180076"))
		{
			System.out.println("Welcome");
		}*/

		logger.info("Class Type :: "+element.getClazz() + "  ---  OBID :  "+WriterUtils.getAttributeValue(element,"id"));
	
		streamWriter.writeStartElement("e");
		
		// streamWriter.writeAttribute(ID, element.getOBID()); getAttributeValue
		// 2017-02-23 Amit: adding code to check the attribute value for null before calling the write attribute method
		if(WriterUtils.getAttributeValue(element, "id") != null) {
			streamWriter.writeAttribute(ID, WriterUtils.getAttributeValue(element,"id"));
		}
		streamWriter.writeAttribute(ELEM_NMB, Integer.toString(elemNmb));
		String smaProject = userValues.get(IConstants.ProjectName);
		if (smaProject == null) {
			// check if the element has source class BCS in which case, it will not have a smaragd project
			if(element.getClazz().equals(IConstants.TRUCK_CLASS_BCS)) {
				smaProject = ReaderSingleton.getReaderSingleton().getBcsProjectName();
			}
			else {
				smaProject = element.getParentProjectName();
			}
		}

		if (PreImpConfig.getInstance().isAMSupply())
		{
			streamWriter.writeAttribute("smaProject", "P_Daimler");
			streamWriter.writeAttribute("cdmProject", "P_Daimler");
		}
		else
		{
			String parentProject = element.getParentProjectName();
			if(smaProject != null ) {
				/*String projName = "";
				if(smaProject.equals(parentProject))
				{
					projName = smaProject;
				}
				else if(smaProject.equals(parentProject))
				{
					projName = parentProject+"_COP";
				}*/
				streamWriter.writeAttribute("smaProject", PLMUtils.convertProjectName(smaProject));

				//String cdmProject = getCDMProjectName(smaProject,parentProject,mainProject);
				String cdmProject = element.getUpdatedProjectName();
				if(cdmProject == null)
				{
					if(PLMUtils.isFileRef(element.getClazz()) )
					{
						cdmProject = smaProject;//element.getUpdatedProjectName();
					}
					else if( element.getClazz().equals(IConstants.j0Cdi3D))
					{
						cdmProject = element.getUpdatedProjectName();
						
						
					}
					else
					{
						//System.out.println("CDMProject NULL");
						if(mainProject == null  || elemNmb == 1)
						{
							cdmProject = smaProject;
						}
						else if(parentProject == null)
						{
							cdmProject = smaProject;
						}
						else {
							cdmProject = "";
						}
					}
				}
				if(cdmProject == null)
				{
					//System.out.println("In CDMProject NULL befre writing ");
					cdmProject = element.getParentProjectName();//UpdatedProjectName();
					if(cdmProject == null)
					{
						cdmProject = element.getUserValues().get("ProjectName");
					}
				}
				//System.out.println("SMA Project : "+smaProject+" CDM Project : "+ cdmProject);
				streamWriter.writeAttribute(
						"cdmProject",PLMUtils.convertProjectName(cdmProject));
				//PLMUtils.convertProjectName(smaProject.equals(mainProject) || PLMUtils.isSecuredProject(smaProject) || PLMUtils.isCommonProject(smaProject)? smaProject : mainProject+ "_COP")); // Make it more readable
				//PLMUtils.convertProjectName(smaProject.equals(mainProject) || PLMUtils.isSecuredProject(smaProject) ? smaProject : mainProject+ "_COP")); // Make it more readable
			}
			// To be done: exception thrown in case the project name cannot be resolved
			else {
			}
		}

		String type = null;
		if (this instanceof ConfigMapDataset) {
			type = "Dataset";
		} else {
			type = "ItemRevision";

		}
		//logger.info("Writing Type : "+getConfigMappedCDMType()+"  OBID : "+WriterUtils.getAttributeValue(element,"id"));
		streamWriter.writeAttribute("tcType", type);
		//streamWriter.writeAttribute("cdmType", getCDMType());
		streamWriter.writeAttribute("cdmType", getConfigMappedCDMType());
		streamWriter.writeAttribute("smaType", element.getClazz());

		
		if((item_puid != null && rev_puid != null) && element.isApplyEffectivityNone())
		{
			streamWriter.writeAttribute(APPLY_EFFECTIVITY, "none");
		}
		else
		{
			streamWriter.writeAttribute(APPLY_EFFECTIVITY, Boolean.toString(this instanceof ConfigMapDataset ? false : applyEffectivity));
		}

		// 31-01-2017 - truck implementation only. Sets the end item value/s to the output element in the xml file  
		// prints out the end item information based on the following 3 conditions:
		// 1. the vehicle type is Truck
		// 2. the tc type is an Item Revision
		// 3. the ae flag is set to true
		if(ReaderSingleton.getReaderSingleton().getFzgTypeName() != null) {
			if(ReaderSingleton.getReaderSingleton().getFzgTypeName().equals(PreferenceConstants.P_FZG_TYP_LKW)) {
				if(type.equals("ItemRevision") && (Boolean.toString(this instanceof ConfigMapDataset ? false : applyEffectivity).equals("true"))) {

					// loops through the list of end items and populates the same to the "value" as a string of semi-colon separated values
					if(element.getEndItemIDs() != null && !element.getEndItemIDs().isEmpty()) {		  
						// writes out the end item information from the element object
						//-- Krishna -- Temorary fix for removing endItem Attribute  --------------------------------------------- commenting the below line
						streamWriter.writeAttribute(IConstants.INTXML_ENDITEM_ATTR, element.getEndItemIDs());
					}
				}
			}
		}

		
		// Ein Workaround: ein Exception is getriggert beim Ausführen von DB query. Der Grund dafür ist unklar, daher der Status "not-checked":
		// Das Workaround: || element.isDbExceptionThrown()
		
		streamWriter.writeAttribute("hc", Boolean.toString(hasChanges));
		if( type.equals("Dataset"))
		{
			String dataset_puid = WriterUtils.getAttributeValue(element, "tagDataset");

			if(!ReaderSingleton.getReaderSingleton().isDbConnect() || element.isDbExceptionThrown())  
			{
				streamWriter.writeAttribute("action", "not-checked");
				ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+"not-checked");
				element.setAction("not-checked");
			}
			else if(element.getAction() != null && element.getAction().equals("unset")&& element.getAction().equals("unset"))
			{
				logger.info(element.getClazz() + "  ---  NO Action Marked for the Element : ");
				streamWriter.writeAttribute("action", "");
				ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+"");
				element.setAction("");
			}
			else if(element.getAction() != null && element.getAction().equals("not-checked"))
			{
				streamWriter.writeAttribute("action", element.getAction());
				ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+element.getAction());
				element.setAction(element.getAction());
			}
			else
			{
				if( dataset_puid != null && element.isHasPropChanges())
				{
					streamWriter.writeAttribute("action", "change");
					ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+"change");
					element.setAction("change");
				}
				else if( dataset_puid != null && !element.isHasPropChanges())
				{
					streamWriter.writeAttribute("action", "none");
					ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+"none");
					element.setAction("none");
				}
				else if(dataset_puid == null)
				{
					streamWriter.writeAttribute("action", "new");
					ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+"new");
					element.setAction("new");
				}
			}

		}
		else 
		{
			if(!ReaderSingleton.getReaderSingleton().isDbConnect() || element.isDbExceptionThrown())
			{
				streamWriter.writeAttribute("action", "not-checked");
				ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+"not-checked");
				element.setAction("not-checked");
			}

			else if(element.getAction() != null && element.getAction().equals("unset"))
			{
				streamWriter.writeAttribute("action", "");
				ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+"");
				element.setAction("");
			}
			else if(element.getAction() != null && element.getAction().equals("not-checked"))
			{
				streamWriter.writeAttribute("action", element.getAction());
				ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+element.getAction());
				element.setAction(element.getAction());
			}
			else
			{

				if((item_puid == null && rev_puid == null) || (item_puid != null && rev_puid == null))
				{
					streamWriter.writeAttribute("action", "new");
					ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+"new");
					element.setAction("new");
				}
				else if((item_puid != null && rev_puid != null) && element.isHasPropChanges())
				{
					streamWriter.writeAttribute("action", "change");
					ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+"change");
					element.setAction("change");
				}
				else
				{
					streamWriter.writeAttribute("action", "none");
					ReaderSingleton.getReaderSingleton().addOBIDActionLogList(WriterUtils.getAttributeValue(element, "id")+" : "+"none");
					element.setAction("none");
				}
			}
			logger.info("< "+WriterUtils.getAttributeValue(element, "id")+" > < "+WriterUtils.getAttributeValue(element, "item_id")+" > < "+WriterUtils.getAttributeValue(element, "item_revision_id")+" > < Action : "+element.getAction());
			 //<element OBID> <Item-ID><Rev-ID><action value><db property value><plmxml property value>
		}
		writeObjectValues(hasChanges);
		// adding expression Blocks 


		//(element.)
		String rPartNumber = WriterUtils.getAttributeValue(element, "item_id");
		/*if( rPartNumber != null && ReaderSingleton.getReaderSingleton().getRootPartnumber() != null && rPartNumber.equals(ReaderSingleton.getReaderSingleton().getRootPartnumber()))
		{
			System.out.println("Part Number : "+rPartNumber);
			if(element.getExprBlocksList() != null && element.getExprBlocksList().size() > 0)
			{
				ArrayList<ElemExprMapInfo> elemExprList = element.getExprBlocksList();
				for (ElemExprMapInfo elemExprMapInfo : elemExprList) 
				{
					if(elemExprMapInfo!= null && elemExprMapInfo.getElemExprAttrValues() != null)
					{
						streamWriter.writeStartElement(elemExprMapInfo.getElementType());
						HashMap<String, String> elemExprAttrMap = elemExprMapInfo.getElemExprAttrValues() ;
						Object[] keys = elemExprAttrMap.keySet().toArray();
						if( keys != null && keys.length > 0)
						{
							for (Object key : keys) 
							{
								streamWriter.writeAttribute((String) key, elemExprAttrMap.get(key));
							}
						}
						streamWriter.writeEndElement();
					}
				}
			}

		}*/
		if(element.getExprBlocksList() != null && element.getExprBlocksList().size() > 0)
		{
			ArrayList<ElemExprMapInfo> elemExprList = element.getExprBlocksList();
			for (ElemExprMapInfo elemExprMapInfo : elemExprList) 
			{
				if(elemExprMapInfo!= null && elemExprMapInfo.getElemExprAttrValues() != null)
				{
					String exprLoc = elemExprMapInfo.getLoc();
					boolean isExprIncluded = true;
					if(exprLoc != null)
					{
						if( exprLoc.equals("ROOT") )
						{

							//	String roPartNumber = WriterUtils.getAttributeValue(element, "item_id");
							if( rPartNumber != null && ReaderSingleton.getReaderSingleton().getRootPartnumber() != null && !rPartNumber.equals(ReaderSingleton.getReaderSingleton().getRootPartnumber()))
							{
								isExprIncluded = false;
							}

						}
					}

					if(isExprIncluded)
					{
						streamWriter.writeStartElement(elemExprMapInfo.getElementType());
						HashMap<String, String> elemExprAttrMap = elemExprMapInfo.getElemExprAttrValues() ;
						Object[] keys = elemExprAttrMap.keySet().toArray();
						if( keys != null && keys.length > 0)
						{
							for (Object key : keys) 
							{
								streamWriter.writeAttribute((String) key, elemExprAttrMap.get(key));
							}
						}
						streamWriter.writeEndElement();
					}
				}
			}
		}
		//if(type.equals("ItemRevision"))
		//{
		logger.info("End of Writing Type : "+getConfigMappedCDMType()+"  OBID : "+WriterUtils.getAttributeValue(element,"id"));

		
		//}
		streamWriter.writeEndElement();
	}


	public String getCDMProjectName(String smaProject, String parentProject, String mainProject)
	{
		String cdmProject = null;

		logger.info(" SMA Project : "+smaProject+" Parent Project : "+parentProject+" Main Project : "+mainProject);
		//PLMUtils.convertProjectName(smaProject.equals(mainProject) || PLMUtils.isSecuredProject(smaProject) || PLMUtils.isCommonProject(smaProject)? smaProject : mainProject+ "_COP");
		if(mainProject == null)
		{
			cdmProject = smaProject;
		}
		else 
		{
			/*if(smaProject.equals(parentProject))
			{
			if( smaProject.equals(parentProject) || PLMUtils.isSecuredProject(smaProject) || PLMUtils.isCommonProject(smaProject))
			{

				if(smaProject.equals(parentProject) && mainProject != null && !mainProject.equals(parentProject))
				{
					cdmProject = mainProject+ "_COP";
				}
				else
				{
					cdmProject = smaProject;
				}	
			}
			else 
			{
				cdmProject = parentProject+ "_COP";
			}*/

			/*if( parentProject.equals("GlobalParts"))
			{
				System.out.println("Welcome : ");
			}*/
			if( smaProject.equals(parentProject) || PLMUtils.isSecuredProject(smaProject) || PLMUtils.isCommonProject(smaProject))
			{
				if( mainProject != null && !mainProject.equals(parentProject) && !PLMUtils.isSecuredProject(smaProject) && !PLMUtils.isCommonProject(smaProject))
				{
					cdmProject = mainProject+ "_COP";
				}
				else
				{
					cdmProject = smaProject;
				}

			}
			else 
			{
				if( parentProject != null &&  !PLMUtils.isSecuredProject(parentProject) && !PLMUtils.isCommonProject(parentProject))
				{
					cdmProject = parentProject+ "_COP";
				}
				else
				{
					cdmProject = smaProject;
				}

			}

		}

		cdmProject = PLMUtils.convertProjectName(cdmProject);
		logger.info("CDM Project : "+cdmProject);
		return cdmProject;

	}

	public String getCDMProjectName(String smaProject, String mainProject)
	{
		String cdmProject = null;


		//PLMUtils.convertProjectName(smaProject.equals(mainProject) || PLMUtils.isSecuredProject(smaProject) || PLMUtils.isCommonProject(smaProject)? smaProject : mainProject+ "_COP");
		if( smaProject.equals(mainProject) || PLMUtils.isSecuredProject(smaProject) || PLMUtils.isCommonProject(smaProject))
		{
			cdmProject = smaProject;
		}
		else 
		{
			cdmProject = mainProject+ "_COP";
		}
		cdmProject = PLMUtils.convertProjectName(cdmProject);
		//	System.out.println("CDM Project : "+cdmProject+" SMA Project : "+smaProject+" Main Project : "+mainProject);
		return cdmProject;

	}


	public void writeObject(final boolean applyEffectivity, final int elemNmb, final String mainProject, final boolean hasChanges, final String endItem)
			throws XMLStreamException, IOException, CDMException {
		streamWriter.writeStartElement("e");
		// streamWriter.writeAttribute(ID, element.getOBID()); getAttributeValue


		/*if(element.getUserValues().get("PartNumber").equals("A3026180076"))
		{
			System.out.println("Welcome");
		}*/
		// 2017-02-23 Amit: adding code to check the attribute value for null before calling the write attribute method
		if(WriterUtils.getAttributeValue(element, "id") != null) {
			streamWriter.writeAttribute(ID, WriterUtils.getAttributeValue(element,"id"));
		}
		streamWriter.writeAttribute(ELEM_NMB, Integer.toString(elemNmb));
		String smaProject = userValues.get(IConstants.ProjectName);
		if (smaProject == null) {
			// check if the element has source class BCS in which case, it will not have a smaragd project
			if(element.getClazz().equals(IConstants.TRUCK_CLASS_BCS)) {
				smaProject = ReaderSingleton.getReaderSingleton().getBcsProjectName();
			}
			else {
				smaProject = element.getParentProjectName();
			}
		}

		if (PreImpConfig.getInstance().isAMSupply())
		{
			streamWriter.writeAttribute("smaProject", "P_Daimler");
			streamWriter.writeAttribute("cdmProject", "P_Daimler");
		}
		else
		{
			streamWriter.writeAttribute("smaProject", PLMUtils.convertProjectName(smaProject));

			streamWriter.writeAttribute(
					"cdmProject",
					PLMUtils.convertProjectName(smaProject.equals(mainProject) || PLMUtils.isSecuredProject(smaProject) ? smaProject : mainProject
							+ "_COP"));
		}
		//-- Krishna -- Temorary fix for removing endItem Attribute  --------------------------------------------- commenting the below line
		streamWriter.writeAttribute("endItem", endItem);

		String type = null;
		if (this instanceof ConfigMapDataset) {
			type = "Dataset";
		} else {
			type = "ItemRevision";

		}
		streamWriter.writeAttribute("tcType", type);
		//streamWriter.writeAttribute("cdmType", getCDMType());
		streamWriter.writeAttribute("cdmType", getConfigMappedCDMType());
		streamWriter.writeAttribute("smaType", element.getClazz());
		streamWriter.writeAttribute(APPLY_EFFECTIVITY, Boolean.toString(this instanceof ConfigMapDataset ? false : applyEffectivity));
		streamWriter.writeAttribute("hc", Boolean.toString(hasChanges));
		writeObjectValues(hasChanges);
		streamWriter.writeEndElement();

	}

	protected void writeValue(final TDatatype type, final PropPlace place, final String name, String value) throws XMLStreamException {
		streamWriter.writeEmptyElement("p");
		if (value == null) {
			value = "";
		}
		streamWriter.writeAttribute("n", name);
		streamWriter.writeAttribute("v", value);
		//logger.info("Attribute Name : "+name+" ------- Value : "+value);
		String sType = null;
		switch (type) {
		case T_BOOL:
			sType = "b";
			break;
		case T_DATE:
			sType = "d";
			break;
		case T_DOUBLE:
			sType = "f";
			break;
		case T_INTEGER:
			sType = "i";
			break;
		case T_STRING:
			sType = "s";
			break;
		default:
			break;
		}
		streamWriter.writeAttribute("t", sType);

		String propPlace = null;
		switch (place) {
		case Form:
			break;
		case Item:
			propPlace = "i";
			break;
		case ItemRevision:
			propPlace = "r";
			break;
		case ItemAndRev:
			propPlace = "ir";
			break;
		case Undef:
		default:
			propPlace = "ud";
			break;
		}
		streamWriter.writeAttribute("s", propPlace);
	}

	// modified by Krishna

	protected void writeValue(String type, String propPlace, final String name, String value) throws XMLStreamException {
		streamWriter.writeEmptyElement("p");
		if (value == null) {
			value = "";
		}
		//logger.info("Attribute Name : "+name+" ------- Value : "+value);
		streamWriter.writeAttribute("n", name);
		streamWriter.writeAttribute("v", value);

		streamWriter.writeAttribute("t", type);

		streamWriter.writeAttribute("s", propPlace);
	}

	protected String smaDateToTcDate(final String smaDate) throws XMLStreamException {
		Matcher matcher = smaDatePattern.matcher(smaDate);
		if (!matcher.matches()) {
			new XMLStreamException("Smaragd date is not in the expected format.");
		}
		StringBuilder sb = new StringBuilder();
		try {
			// Smaragd has some invalid dates
			if (Integer.parseInt(matcher.group(1)) < 1970) {
				return ICustom.DEFAULT_DATE;
			}
		} catch (NumberFormatException e) {
			new XMLStreamException(e);
		}
		sb.append(matcher.group(1)).append('/'); // year
		sb.append(matcher.group(2)).append('/'); // month
		sb.append(matcher.group(3)).append('/'); // day
		sb.append(matcher.group(4)).append('/'); // hour
		sb.append(matcher.group(5)).append('/'); // minute
		sb.append(matcher.group(6)); // second
		return sb.toString();
	}

	protected void writerLifecycleState() throws XMLStreamException {
		// Added for date attributes
		String engDateEffFrom = userValues.get(IConstants.j0EngDateEffectiveFrom);
		String relDateEffFrom = userValues.get(IConstants.j0RelDateEffectiveFrom);
		String lcState = userValues.get(IConstants.LifeCycleState);
		if (engDateEffFrom != null || relDateEffFrom != null) {
			if (engDateEffFrom != null) {
				writeValue(TDatatype.T_DATE, PropPlace.ItemRevision, ICustom.ATTR_ENGDATEEFF, smaDateToTcDate(engDateEffFrom));
			}

			if (relDateEffFrom != null) {
				writeValue(TDatatype.T_DATE, PropPlace.ItemRevision, ICustom.ATTR_RELDATEEFF, smaDateToTcDate(relDateEffFrom));
			}
		} else if (lcState != null) {
			if (IConstants.j0LcsEng.equals(lcState)) {
				writeValue(TDatatype.T_DATE, PropPlace.ItemRevision, ICustom.ATTR_ENGDATEEFF, ICustom.DEFAULT_DATE);
			} else if (IConstants.LcsReleased.equals(lcState)) {
				writeValue(TDatatype.T_DATE, PropPlace.ItemRevision, ICustom.ATTR_RELDATEEFF, ICustom.DEFAULT_DATE);
			}
		}
	}

	protected void writerMappedLifecycleState() throws XMLStreamException {
		// Added for date attributes
		String engDateEffFrom = userValues.get(IConstants.j0EngDateEffectiveFrom);
		String relDateEffFrom = userValues.get(IConstants.j0RelDateEffectiveFrom);
		String lcState = userValues.get(IConstants.LifeCycleState);
		if (engDateEffFrom != null || relDateEffFrom != null) {
			if (engDateEffFrom != null) {
				writeValue("d", "ir", ICustom.ATTR_ENGDATEEFF, smaDateToTcDate(engDateEffFrom));
			}

			if (relDateEffFrom != null) {
				writeValue("d", "ir",ICustom.ATTR_RELDATEEFF, smaDateToTcDate(relDateEffFrom));
			}
		} else if (lcState != null) {
			if (IConstants.j0LcsEng.equals(lcState)) {
				writeValue("d", "ir", ICustom.ATTR_ENGDATEEFF, ICustom.DEFAULT_DATE);
			} else if (IConstants.LcsReleased.equals(lcState)) {
				writeValue("d", "ir", ICustom.ATTR_RELDATEEFF, ICustom.DEFAULT_DATE);
			}
		}
	}

	protected void writeValueLoc(TDatatype tDtaTyp, PropPlace tcType, String propName, String propValue) throws XMLStreamException {

		if (propValue != null) {
			if (tDtaTyp == TDatatype.T_BOOL) {
				if ("+".equals(propValue)) {
					propValue = "TRUE";
				} else if ("-".equals(propValue)) {
					propValue = "FALSE";
				}
			}
			writeValue(tDtaTyp, tcType, propName, propValue);
		}
	}

	protected void writeValueLoc(String tDtaTyp, String tcType, String propName, String propValue) throws XMLStreamException {

		if (propValue != null) {
			if (tDtaTyp.equals("b") ) {
				if ("+".equals(propValue)) {
					propValue = "TRUE";
				} else if ("-".equals(propValue)) {
					propValue = "FALSE";
				}
			}
			writeValue(tDtaTyp, tcType, propName, propValue);
		}
	}

	/* public String getAttributeValue(String attrName)
   {
	   String attrValue = null;
	   HashMap<String, ArrayList<MappedAttributes>> mappedElementMap = element.getMappedElementsMap();
		 if( mappedElementMap != null)
		 {
			 Object[] keys = mappedElementMap.keySet().toArray();
			 if(keys != null && keys.length > 0)
			 {
				 for(int i = 0; i < keys.length;i++)
				 {
					 ArrayList<MappedAttributes> mappedAttrs = mappedElementMap.get(keys[i]);
					 if( mappedAttrs != null && mappedAttrs.size() > 0)
					 {
						 MappedAttributes attribute = null;
						 for ( int k = 0; k < mappedAttrs.size(); k++)
						 {
							 attribute = mappedAttrs.get(k);
							 if( attribute != null && attribute.getAttrName() != null && attribute.getAttrName().equals(attrName))
							 {
								 attrValue =  attribute.getAttrValue();
								 break;
							 }
						 }
					 }

				 }
			 }
			 //mappedElementMap.get("")
		 }
		 return attrValue;
   }*/
}
