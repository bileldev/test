/**
 * 
 */
package cdm.pre.imp.configmap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.DateTime;

import cdm.pre.imp.reader.IConstants;



/**
 * @author amit.rath
 * This class will hold the mapping functions and the constants needed in the implementation
 */
public final class ConfigMapUtils {

	// defines for mapping XML file element names
	public static final String XML_ELEM_MAPELEM 			= "MappingElement";
	public static final String XML_ELEM_CLASS				= "Class";
	public static final String XML_ELEM_ATTR				= "Attr";
	public static final String XML_ELEM_ATTRGRP				= "AttrGrp";
	public static final String XML_ELEM_MAPFUNC				= "MapFunc";
	public static final String XML_ELEM_PARAM				= "Param"; 
	public static final String XML_ELEM_ELEMEXPR			= "ElemExpr"; 
	public static final String XML_ELEM_ELEMATTR			= "ElemAttr"; 
	public static final String XML_ELEM_GLOBALCONST			= "GlobalConst"; 
	
	// defines for the mapping XML file attribute names
	public static final String XML_ATTR_NAME				= "name";
	public static final String XML_ATTR_SRC					= "src";
	public static final String XML_ATTR_TRGT				= "target";
	public static final String XML_ATTR_SCOPE				= "scope";
	public static final String XML_ATTR_TYPE				= "type"; 
	
	public static final String XML_ATTR_ELEMTYPE			= "elemTyp";
	public static final String XML_ATTR_ELMEXPR_LOC			= "loc";
	public static final String XML_ATTR_REQ					= "req";
	public static final String XML_ATTR_VALUE				= "value";
	
	public static final String XML_ATTR_VAR					= "var";
	public static final String XML_ATTR_VARXMLPATH			= "varXMLPath";
	public static final String XML_ATTR_VARXMLATTR			= "varXMLAttr";
	
	// defines for string separators defined in the mapping XML file
	public static final String STRING_SEPARATOR				= "#";
	public static final String PARAM_VALUE_DESIGNATOR		= "$";
	public static final String GLOBALPARAM_VALUE_DESIGNATOR	= "_$";
	
	// defines stating the vehicle type being considered for the import 
	public static final String IMP_SCOPE_CAR			= "car";
	public static final String IMP_SCOPE_TRUCK			= "truck";
	
	// defines for special attributes
	public static final String ATTR_LABEL				= "label";
	public static final String ATTR_OBID				= "OBID";
	public static final String ATTR_LOCATION			= "location";
	
	// defines for log message classification
	public static final String LOG_TYPE_INFO			= "info";
	public static final String LOG_TYPE_WARNING			= "warning";
	public static final String LOG_TYPE_ERROR			= "error";
	public static final String LOG_TYPE_OBID_ERROR		= "OBIDError";
	
	// defines for the Truck BOM Teamcenter class names
	
	
	
	private final static Logger logger	= LogManager.getLogger("cdm.pre.imp.tracelog");
			
	/**
	 * method that prints out the details of the mapping information as read from the mapping file
	 */
	public static void printMappingDetails(HashMap<String, ArrayList<ClassMapInfo>> clsMapCollection) {
		
		// block iterates through each of the ClassMapInfo instances and prints them out to the console
		// this can be later modified to print to the log file may be on demand with help of a parameter
		if(clsMapCollection.size() > 0) {
			System.out.println("****************MAPPING INFORMATION****************");
			// define the iterator on the input hash map
			Iterator<Entry<String, ArrayList<ClassMapInfo>>> itr  = clsMapCollection.entrySet().iterator();
			while(itr.hasNext()) {
				Map.Entry<String, ArrayList<ClassMapInfo>> pair = (Map.Entry<String, ArrayList<ClassMapInfo>>)itr.next();
				System.out.println("**************** MAPPING ELEMENT ****************");
				// iterate through each of the clsMapInfo objects in the ArrayList - the value of the input Map
				for(ClassMapInfo clsMapInfObj : pair.getValue()) {
					System.out.println("Class Mapping :" + pair.getKey() + "--->" + clsMapInfObj.getTrgtClassName());
					// prints out the 1:1 direct attribute mappings 
					if(clsMapInfObj.getAttrInfoObjs().size() > 0) {
						System.out.println("****************ATTRIBUTE MAPPING DETAILS****************");
						for(AttributeMapInfo attrMapInfObj : clsMapInfObj.getAttrInfoObjs())  {
							// iterates through the list of source attribute names
							if(attrMapInfObj.getSrcAttrNames().size() > 0) { 
								for(String srcAttrName: attrMapInfObj.getSrcAttrNames()) {
									System.out.println("Attribute Mapping: " + srcAttrName + "--->" + attrMapInfObj.getTrgtAttrName() + 
											" <Scope> ---> " + attrMapInfObj.getTrgtAttrScope() + " <DataType> ---> " + attrMapInfObj.getTrgtAttrType() 
											+ " <IsRequired> ---> " + attrMapInfObj.isTrgtAttrReq()) ;
								}
							}
							System.out.println("*********************************************************");
						}
					}
					if(clsMapInfObj.getAttrGrpInfoObjs().size() > 0) {
						System.out.println("************ATTRIBUTE GROUP MAPPING DETAILS**************");
						// iterates through the instances of the Attr Grp class
						for (AttrGrpMapInfo attrGrpMapObj : clsMapInfObj.getAttrGrpInfoObjs()) {
							// iterates through the list of source attribute names
							if(attrGrpMapObj.getAttrMapInfoObj().getSrcAttrNames().size() > 0) { 
								for(String srcAttrName: attrGrpMapObj.getAttrMapInfoObj().getSrcAttrNames()) {
									System.out.println("Attribute Mapping: " + srcAttrName + "--->"
									+ attrGrpMapObj.getAttrMapInfoObj().getTrgtAttrName() + 
									" :<Scope> ---> " + attrGrpMapObj.getAttrMapInfoObj().getTrgtAttrScope() + " <DataType> ---> " 
									+ attrGrpMapObj.getAttrMapInfoObj().getTrgtAttrType() 
									+ " :<IsRequired> ---> " + attrGrpMapObj.getAttrMapInfoObj().isTrgtAttrReq()) ;
								}
							}
							System.out.println("Mapping Function Name: "
									+ attrGrpMapObj.getMapFuncName());
							// iterates and prints the names and values of the parameters of the mapping function
							if(attrGrpMapObj.getMapFuncsParamValues().size() > 0) {
								for(Map.Entry<String, String> mapEntry : attrGrpMapObj.getMapFuncsParamValues().entrySet()) {
									System.out.println("Mapping Function Parameter is: " + mapEntry.getKey() + " with Paramenter Value: " 
											+ mapEntry.getValue());
								}
							}
						}
					}
				}
			}
			System.out.println("****************MAPPING INFORMATION****************");
		}	
	}
	
	/**
	 * Custom mapping function that generates the revision id for the TEAMCENTER item_revision_id attribute
	 * @param revisionID	: input revision id value read from the SMARAGD PLMXML file
	 * @param sequenceID	: input sequence id value read from the SMARAGD PLMXML file
	 * @return
	 */
	public static String revIDGenerator(String revision, String sequence) {
		
		String genRevID = null;				// return value for the method that holds the generated revision id
		
		if(sequence != null)
		{
			if(sequence.length() == 1)
			{
				sequence = "00"+sequence;
			}
			else if(sequence.length() == 2)
			{
				sequence = "0"+sequence;
			}
		}
		if(revision != null && sequence != null) {
			genRevID = revision + "." + sequence;
		}
		// method return value
		return genRevID;
	}
	
	/**
	 * Custom mapping function that generates the item id for the C9Model object
	 * @param j0CTModSnr	: Part Number of the Parent SNR for the Model object 
	 * @param j0CTModNumber	: Model number
	 * @return
	 */
	public static String modelItemIDGenerator(String j0CTModSnr, String j0CTModNumber) {
		
		String modelItemID = null;
		
		if(j0CTModSnr != null && j0CTModNumber != null) {
			// generates the item id string for the C9Model
			modelItemID = j0CTModSnr + "_" + j0CTModNumber;
		}
		// method return value
		return modelItemID;
	}
	
	/**
	 * Custom mapping function that generates the sequence value for the C9ModelRevision
	 * @param sequence	: Input value of the sequence for the j0Cdi3D as read from the SMARAGD PLMXML file
	 * @return			: generated item revision id value for the C9ModelRevision
	 */
	public static String modelRevIDGenerator(String sequence) {
		
		String modelRevID = null;
		
		// checks the size of the sequence value and based on that pads the required number of zeroes before the sequence value
		if(sequence != null) {
			if(sequence.length() == 1) {
				modelRevID = "00" + sequence;
			}
			else if(sequence.length() == 2) {
				modelRevID = "0" + sequence;
			}
			else if(sequence.length() == 3) {
				modelRevID = sequence;
			}
		}
		// method return value
		return modelRevID;
	}
	
	/**
	 * Custom mapping function that generates the value of "id" for the intermediate xml file from label attribute of SMARAGD PLMXML.
	 * Separate implementations are needed for car and truck
	 * @param label			: value label as read from the input PLMXML file
	 * @param impScope		: scope of import - either car or truck
	 * @return				: generated value for id attribute in intermediate XML file
	 */
	public static String labelMapper(String label, String impScope) {
		
		String mappedLabel = null;
		
		if(impScope != null) {
			/// 09/12/2016 - as of now the logic for OBID is same for truck and car
			if(impScope.equals(ConfigMapUtils.IMP_SCOPE_CAR) || impScope.equals(ConfigMapUtils.IMP_SCOPE_TRUCK)) {
				Pattern obidPattern     = Pattern.compile(".+?~.+?~(.+?)~.+");
				
				Matcher match = obidPattern.matcher(label);
				if (match.find()) {
					mappedLabel = match.group(1);
				}
				else
				{
					mappedLabel = label;
				}
			}
		}
		// return value for the method
		return mappedLabel;
	}
	
	/**
	 * Custom mapping function that maps the geo pos value to corresponding Teamcenter attribute value
	 * @param geoPos	: Input value as read from the SMARAGD PLMXML file
	 * @return			: processed value for geo pos for Teamcenter UA attribute
	 */
	public static String geoPosMapper(String geoPos) {
		
		String geoPosVal = null;
		
		if(geoPos != null) {
			// assigns a boolean type value to the method return value
			if(geoPos.equals("+")) {
				geoPosVal = "Y";
			}
			else {
				geoPosVal = "N";
			}
		}
		return geoPosVal;
	}
	
	/**
	 * Custom mapping function that converts the date from - 2015/02/04-15:01:30:627 ----> 2015/02/04/15/01/30 
	 * @param inputDate	: date as read from the input PLMXML file
	 * @return : date compatible with Teamcenter UA
	 * @throws ParseException 
	 */
	public static String dateConverter(String inputDate, String sysStartDate) throws  MappingException {
		return  DateUtils.dateConverter("", "", inputDate, sysStartDate);
	}
	
	
	/**
	 * Custom mapping function that converts the date from - 2015/02/04-15:01:30:627 ----> 2015/02/04/15/01/30 
	 * @param inputDate	: date as read from the input PLMXML file
	 * @return : date compatible with Teamcenter UA
	 * @throws ParseException 
	 */
	public static String difaDateConverter(String inputDate, String sysStartDate) throws ParseException, MappingException {
		
		
		boolean isAfterStartDate = false;								// boolean variable that states if the input date is after system start date
		
		String processedDate 	= null;									// output variable for the mapping function
		
		Date plmxmlDate 		= null;									// Date representation for the string input from the PLMXML file
		Date sysBeginDate 		= null;									// Date representation for the system begin date
	
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS");
		
		String inDate = null;
		
		if(sysStartDate != null) {
			if(df != null) {
				if( inputDate != null && !inputDate.equals(""))
				{
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date date = null;
					try {
						date = format.parse(inputDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}

					
					plmxmlDate = df.parse(df.format(date));
					inDate =  df.format(date).toString();
					
				}
				//plmxmlDate = df.parse(inputDate);
				sysBeginDate = df.parse(sysStartDate);
			}
		} else {
			logger.error("System start date as provided in the mapping file is null");
		}
		
		
		// compare the date
		if(plmxmlDate != null && sysBeginDate != null) {
			// compares the dates and stores the 
			isAfterStartDate = plmxmlDate.after(sysBeginDate);
		}
		
		// checks value of the boolean before the format transformation
		if(isAfterStartDate == true) {
			if(inDate != null) {
				String[] tmpDateArray = inDate.split("-");
				if(tmpDateArray != null) {
					if(tmpDateArray.length == 2) {
						processedDate = tmpDateArray[0];
						String[] tmpTimeArray = tmpDateArray[1].split(":");
						if(tmpTimeArray != null) {
							if(tmpTimeArray.length == 4) {
								processedDate = processedDate + "/" + tmpTimeArray[0] + "/" + tmpTimeArray[1] + "/" + tmpTimeArray[2];  
							}
						}
					}
				}
			}
		}
		// sets the default start date as stated in the mapping file as the attribute value
		else {
			if(sysStartDate != null) {
				String[] tmpDateArray = sysStartDate.split("-");
				if(tmpDateArray != null) {
					if(tmpDateArray.length == 2) {
						processedDate = tmpDateArray[0];
						String[] tmpTimeArray = tmpDateArray[1].split(":");
						if(tmpTimeArray != null) {
							if(tmpTimeArray.length == 4) {
								processedDate = processedDate + "/" + tmpTimeArray[0] + "/" + tmpTimeArray[1] + "/" + tmpTimeArray[2];  
							}
						}
					}
				}
			}
		}
		
		return processedDate;
	}
	/**
	 * Custom mapping function that is used to determine if the target class mentioned in the mapping file element corresponds to the 
	 * structure element at a particular element. The method pre-supposes a frozen definition for object types encoutered at a definite BOM level
	 * This function will need re-definition in case new BOM structures are encountered in the future
	 * @param bomLevel 		: Level in the BOM structure for the particular structure element whose target class type has to be determined
	 * @param partIdentifier: Part number of the structure element as obtained from the input DMU XML file
	 * @param trgtClassName	: Name of the target class as read from the mapping file
	 * @return				: boolean value that states if the mapping to target class is valid for the given input bom level and the input part number
	 * @throws MappingException
	 */
	public static boolean isTargetTruckClass(String bomLevel, String partIdentifier, String trgtClassName) throws MappingException {
		
		boolean isTrgtClass = false;							// return value for the method
		
		int bomLevelInt = Integer.parseInt(bomLevel);
		
		if(bomLevelInt == 1) {
			if(trgtClassName.equals(IConstants.TC_TYPE_TRUCK)) {
				isTrgtClass = true;
			}
		}
		else if(bomLevelInt == 2) {
			// checks that the target class can be either CB9CBM or CB9SBProd
			if(partIdentifier.startsWith(IConstants.CBM_IDENT_PREFIX) && trgtClassName.equals(IConstants.TC_TYPE_CBM)) {
				isTrgtClass = true;
			}
			else if(partIdentifier.startsWith(IConstants.DBM_IDENT_PREFIX) && trgtClassName.equals(IConstants.TC_TYPE_DBM)) {
				isTrgtClass = true;
			}
		}
		else if(bomLevelInt == 3) {
			// checks if the target class is a Navigational Module
			if(trgtClassName.equals(IConstants.TC_TYPE_NAVMOD)) {
				isTrgtClass = true;
			}
		}
		else if(bomLevelInt == 4) {
			// checks if the target class is a Functional Module
			if(trgtClassName.equals(IConstants.TC_TYPE_FUNCMOD)) {
				isTrgtClass = true;
			}
		}
		else if(bomLevelInt == 5) {
			// checks if the target class is a Position
			if(trgtClassName.equals(IConstants.TC_TYPE_POS)) {
				isTrgtClass = true;
			}
		}
		else if(bomLevelInt == 6) {
			// checks if the target class is a LiPos
			if(trgtClassName.equals(IConstants.TC_TYPE_LIPOS)) {
				isTrgtClass = true;
			}
		}
		// boolean return for the function
		return isTrgtClass;
	}
	
	/**
	 * custom mapping function that generates the item identifier for the C9Truck Object
	 * @param partIdentifier : identifier for the C9 Truck object
	 * @return	: unique identifier for the C9 Truck
	 */
	public static String generateTruckID(String partIdentifier) {
		
		String truckID = null;
		
		if(partIdentifier != null) {
			// removes the last 3 entries from the reference vehicle identifier to provide the truck identifier
			truckID = partIdentifier.substring(0, (partIdentifier.length() - 3));			
		}
		return truckID;
	}
	
	/**
	 * custom mapping function that generates the BCS element specific unique identifier
	 * @param objName 	: Name of the BCS element as read from the input PLMXML file
	 * @param partID	: Part number of the BCS element as read from the input PLMXML file
	 * @return	: unique identifier for the BCS element
	 */
	public static String generateBCSElementID(String objName, String partID) {
		
		String bcsElemID = null;
		
		if(objName != null && partID != null) {
			bcsElemID = partID + "_" + objName;
		}
		
		return bcsElemID;
	}
	
	public static String mfgContainerIDGenerator(String partNumber, String procCode)
	{
		String itemId = partNumber+"_"+procCode;
		return itemId;
	}
	
	public static String mfgRevIDGenerator(String revID)
	{
		String id = "001";
		return id;
	}
	
	public static String genWeldItemID(String partNumber,String partRevision,String procCode,String outType, String id, String connectedPart1, String joinPartner1, String joinPartner2, String connectedPart2, String elementNumber, String joinedPartners)
	{
		//String partNumber = "";
		
		

		String pCode = procCode;
		
		String fType = outType;
		
		String fID = id;
		
		String fConnectedPart1 = connectedPart1;
		if (fConnectedPart1 == null || fConnectedPart1.equals("Connected Part 1")) {
			fConnectedPart1 = joinPartner1;

		}
		String fConnectedPart2 = connectedPart2;
		if (fConnectedPart2 == null || fConnectedPart2.equals("Connected Part 2")) {
			fConnectedPart2 = joinPartner2;
		}

		if(fConnectedPart1.equals("Joined Partner 1") && fConnectedPart2.equals("Joined Partner 2")&& joinedPartners != null &&!joinedPartners.equals(""))
		{
			String jPartners = joinedPartners;
			if( jPartners.contains("|"))
			{
				String[] split = jPartners.split(Pattern.quote("|"));
				if( split != null && split.length > 0 )
				{
					 fConnectedPart1 = split[0];
					 if(split.length > 1)
					 {
						 fConnectedPart2 = split[1];
					 }
				}
			}
		}
		
		
		// // for CATIA V5 file format. When ID parameter is missing,
		// ElementNumber is considered for generating ItemID. - START
		String elementNum = elementNumber;
		if (fID == null || fID.equals("ID")) 
		{
			fID = elementNum;
		}
		// for CATIA V5 file format. When ID parameter is missing, ElementNumber
		// is considered for generating ItemID. - END
		String itemIdString = hash(new String[] { partNumber, pCode, fType, fID, fConnectedPart1, fConnectedPart2 });
		if(itemIdString == null)
			System.out.println("inMethod");
		itemIdString = partNumber+"_"+itemIdString;
		return itemIdString;
	}
	
	public static String getFormObjectName(String partNumber,String partRevision,String procCode,String outType, String id, String connectedPart1, String joinPartner1, String joinPartner2, String connectedPart2, String elementNumber, String joinedPartners)
	{

		//String partNumber = "";
		
		

		String pCode = procCode;
		
		String fType = outType;
		
		String fID = id;
		
		String fConnectedPart1 = connectedPart1;
		if (fConnectedPart1 == null || fConnectedPart1.equals("Connected Part 1")) {
			fConnectedPart1 = joinPartner1;

		}
		String fConnectedPart2 = connectedPart2;
		if (fConnectedPart2 == null || fConnectedPart2.equals("Connected Part 2")) {
			fConnectedPart2 = joinPartner2;
		}
		if(fConnectedPart1.equals("Joined Partner 1") && fConnectedPart2.equals("Joined Partner 2")&& joinedPartners != null &&!joinedPartners.equals(""))
		{
			String jPartners = joinedPartners;
			if( jPartners.contains("|"))
			{
				String[] split = jPartners.split(Pattern.quote("|"));
				if( split != null && split.length > 0 )
				{
					 fConnectedPart1 = split[0];
					 if(split.length > 1)
					 {
						 fConnectedPart2 = split[1];
					 }
				}
			}
		}
		// // for CATIA V5 file format. When ID parameter is missing,
		// ElementNumber is considered for generating ItemID. - START
		String elementNum = elementNumber;
		if (fID == null || fID.equals("ID")) 
		{
			fID = elementNum;
		}
		// for CATIA V5 file format. When ID parameter is missing, ElementNumber
		// is considered for generating ItemID. - END
		String itemIdString = hash(new String[] { partNumber, pCode, fType, fID, fConnectedPart1, fConnectedPart2 });
		//itemId = partnumber+"_"+revision+"_"+node.getItemId()+"_"+type+"/001";
		//itemIdString = partNumber+"_"+itemIdString+"_"+type+"/001";
		itemIdString = partNumber+"_"+itemIdString;
		return itemIdString;
	
	}
	
	public static String hash(String[] values) {
		long hash = 0xCBF29CE484222325L;
		for (String s : values) {
			if (s != null) {
				hash ^= s.hashCode();
				hash *= 0x100000001B3L;
			}
		}
		return String.valueOf((hash >>> 1) + (~hash >>> 31));
	}
	public static String genWeldName( String procCode,String kg,String elemNumber)
	{
		
		if(kg==null || kg.equals("KG"))
		{
			kg="";
		}
			
		if(elemNumber==null || elemNumber.equals("Element Number"))
		{
			elemNumber="";
		}
		String weldName = procCode+"_"+kg+elemNumber;
		return weldName;
	}
	
	public static String assgnJoinPartners()
	{
		String id = "";
		return id;
	} 
	
	public static String assgnJoinPartners(String joinedPartners)
	{
		String id = "";
		return id;
	}
	
	public static String assgnJoinPartners(String joinedPartners, String separator)
	{
		String id = "";
		return id;
	}
	
	public static String assgnTightness()
	{
		String id = "";
		return id;
	}
	
	public static boolean isArcWeld(String Type, String arcWledTypes)
	{
		boolean isArchWeld = false;
		if( Type != null && arcWledTypes != null)
		{
			
				String[] types = arcWledTypes.split(",");
				if(types != null && types.length > 0)
				{
					for (String weldType : types) 
					{
						if( weldType.equals(Type))
						{
							isArchWeld = true;
							break;
						}
					}
				}
			
		}
		return isArchWeld;
	}
	public static boolean isSpotWeld(String Type, String arcWledTypes)
	{

		boolean isArchWeld = false;
		if( Type != null && arcWledTypes != null)
		{
			
				String[] types = arcWledTypes.split(",");
				if(types != null && types.length > 0)
				{
					for (String weldType : types) 
					{
						if( weldType.equals(Type))
						{
							isArchWeld = true;
							break;
						}
					}
				}
			
		}
		//System.out.println(" isArcWeld Method......");
		return isArchWeld;
	
	}
	public static boolean isSpotWeldForm(String Type, String arcWledTypes)
	{

		boolean isArchWeld = false;
		if( Type != null && arcWledTypes != null)
		{
			
				String[] types = arcWledTypes.split(",");
				if(types != null && types.length > 0)
				{
					for (String weldType : types) 
					{
						if( weldType.equals(Type))
						{
							isArchWeld = true;
							break;
						}
					}
				}
			
		}
		//System.out.println(" isArcWeld Method......");
		return isArchWeld;
	
	}
	public static boolean isArcWeldForm(String Type, String arcWledTypes)
	{

		boolean isArchWeld = false;
		if( Type != null && arcWledTypes != null)
		{
			
				String[] types = arcWledTypes.split(",");
				if(types != null && types.length > 0)
				{
					for (String weldType : types) 
					{
						if( weldType.equals(Type))
						{
							isArchWeld = true;
							break;
						}
					}
				}
			
		}
		//System.out.println(" isArcWeld Method......");
		return isArchWeld;
	
	}
	
	public static String getWeldPointType(String type, String spotWeldTypes, String arcWeldTypes)
	{
		String weldPointType = null;
		String[] types = null;
		if( type != null )
		{
			if( arcWeldTypes != null)
			{
				types = arcWeldTypes.split(",");
				if( types != null && types.length > 0)
				{
					ArrayList<String> arcWeldPointsList = new ArrayList<String>(Arrays.asList(types));
					if( arcWeldPointsList.contains(type))
					{
						weldPointType = "ArcWeld";
					}
				}
			}
			if( weldPointType == null && spotWeldTypes != null)
			{
				types = spotWeldTypes.split(",");
				if( types != null && types.length > 0)
				{
					ArrayList<String> spotWeldPointsList = new ArrayList<String>(Arrays.asList(types));
					if( spotWeldPointsList.contains(type))
					{
						weldPointType = "WeldPoint";
					}
				}
			}			
		}
		
		if(weldPointType == null)
		{
			logger.warn("Manufacturing Type : "+type+" is not a valid Type. "+type+" might not included in the Specification. so this type is excluded");
		}
		return weldPointType;
	}
	
	public static String getWeldPointFormType(String type, String spotWeldTypes, String arcWeldTypes)
	{
		String weldPointType = null;
		String[] types = null;
		if( type != null )
		{
			if( arcWeldTypes != null)
			{
				types = arcWeldTypes.split(",");
				if( types != null && types.length > 0)
				{
					ArrayList<String> arcWeldPointsList = new ArrayList<String>(Arrays.asList(types));
					if( arcWeldPointsList.contains(type))
					{
						weldPointType = "ArcWeld";
					}
				}
			}
			if( weldPointType == null && spotWeldTypes != null)
			{
				types = spotWeldTypes.split(",");
				if( types != null && types.length > 0)
				{
					ArrayList<String> spotWeldPointsList = new ArrayList<String>(Arrays.asList(types));
					if( spotWeldPointsList.contains(type))
					{
						weldPointType = "SpotWeld";
					}
				}
			}	
			if(weldPointType == null)
			{
				logger.warn("Manufacturing Type : "+type+" is not a valid Type. "+type+" might not included in the Specification. so this type is excluded");
			}
		}
		return weldPointType;
	}
	
	public static String validateRemarks(String remarks, String maxLength)
	{
		int max = Integer.valueOf(maxLength).intValue();
		if(remarks != null && max > 0)
		{
			if( remarks.length() > max)
			{
				remarks = remarks.substring(0, max);
			}
		}
		return remarks;
	}
	
	public static String assgnJoinPartner(String joinedPartners, String index, String separator)
	{
		String joinedPartner = null;
		
		int ind= Integer.valueOf(index).intValue();
		
		if(joinedPartners != null && joinedPartners.contains(separator))
		{
			String[] split = joinedPartners.split(Pattern.quote("|"));
			if( split != null && split.length > ind)
			{
				joinedPartner = split[ind];
			}
		}
		
		if(joinedPartner == null)
		{
			joinedPartner ="";
		}
		return joinedPartner;
	}
	
	public static String assgnTightness(String tightness, String index, String separator)
	{
		String value = null;
		
		int ind= Integer.valueOf(index).intValue();
		
		if(tightness != null && tightness.contains(separator))
		{
			String[] split = tightness.split(Pattern.quote("|"));
			if( split != null && split.length > ind)
			{
				value = split[ind];
			}
		}
		
		if(value == null)
		{
			value ="";
		}
		return value;
	}
	
	public static String assgnExposedSheet(String exposedSheet, String index, String separator)
	{
		String value = null;
		
		int ind= Integer.valueOf(index).intValue();
		
		if(exposedSheet != null && exposedSheet.contains(separator))
		{
			String[] split = exposedSheet.split(Pattern.quote("|"));
			if( split != null && split.length > ind)
			{
				value = split[ind];
			}
		}
		
		if(value == null)
		{
			value ="";
		}
		return value;
	}
	
	public static String  mapMaterialValue(String materialVal, String allowedChars, String spCharSet)
	{
		String material = materialVal;
		if(materialVal != null && !materialVal.equals(""))
		{
			if(allowedChars!=null && spCharSet!=null)
			{
				Pattern p = Pattern.compile(spCharSet);
				Matcher m = p.matcher(materialVal);
				while (m.find()) 
				{
					material = m.replaceAll(allowedChars);
				}
			}
		}
		return material;
	}
	
}
