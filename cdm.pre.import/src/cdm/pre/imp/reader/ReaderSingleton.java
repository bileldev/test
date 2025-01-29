package cdm.pre.imp.reader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.configmap.ClassMapInfo;
import cdm.pre.imp.configmap.ConfigMapUtils;
import cdm.pre.imp.configmap.GlobalConstMapInfo;
import cdm.pre.imp.configmap.ReadMappingFile;
import cdm.pre.imp.csvreader.RefConfigMappingObject;
import cdm.pre.imp.dbconnector.DBConnBroker;
import cdm.pre.imp.dbconnector.DBConnUtilities;
import cdm.pre.imp.json.reader.Sma4UJsonLogger;

/**
 * singleton class whose instance stores information needed across the pre-importer session.
 * @author amit.rath
 *
 */

public class ReaderSingleton {

	private String bcsProjectName;												// name of the Truck main project name as deduced from the id of the reference item

	private String refFzgName;													// identifier for the truck reference configuration

	private String fzgTypeName;													// name of the vehicle type being processed by the pre-importer

	private String rootPartnumber;												// Part Number of the Root Item

	private String currBaumsterID;												// id of the baumuster and it's children being written to the intermediate xml file

	private HashMap<String, String> part2effectivityIDMap;						// baumuster id to end item id map

	private static ReaderSingleton singleton = null;		            		// instance of the current class

	private ArrayList<ObjectInfo> objInfoList;									// list of ObjectInfo objects

	private HashMap<String, Element> id2ElemObjMap;								// map between the element id in the source PLMXML file and the Element instance

	private HashMap<String, ArrayList<ClassMapInfo>> mappingInfoMap;			// mapping information - maps the source class name to the class instance

	private ArrayList<GlobalConstMapInfo> globalConstCollection;				// Global COnstant information - stores all the Global Constants from Mapping File

	private HashMap<Integer, TreeMap<String, BOMInfo>> bomParent2ChildMap;		 // map between the bom parent id and list of child object ids 

	private ArrayList<RefConfigMappingObject> sumRefConfigMapObjs; 		  		 // Reference configuration Mapping object list

	private String refConfigurationType;									  // type of reference configuration being imported. 1) SUM Config 2) 100% Ref. Config. 3) Montage Config.

	private String inputMode;         										  //	Input Mode for Pre-importer Values ( xml_car_var, SEND_1 ( Send toPLMXML without refconfig), SEND_2 etc...

	private DBConnBroker dbConnBroker;

	private String endItem;

	private boolean isDbConnect = false;

	private HashMap<String, Integer> mapQry2ExecuteFreq;					// map that holds the name of the query to the number of invocations
	private HashMap<String, Long> mapQry2ExecTime;						// map that holds the name of the query to the total time spent on the queries

	private HashMap<String, Integer> mapDbObjectSum;

	private String refConfigName;										// Reference Configuration Name

	private ArrayList<String> mLogOBIDActionListObj = new ArrayList<>();

	private static ArrayList<String> cdi3dList = new ArrayList<>();
	private static ArrayList<String> cdi3dListWriter = new ArrayList<>();

	private static PrintWriter mLogOccWriterObj;
	private static PrintWriter mLogOccConnWriterObj;
	private static ArrayList<String> mLogOccConnReaderListObj = new ArrayList<>();
	private static ArrayList<String> mLogOccConnWriterListObj = new ArrayList<>();

	/**
	 *  private constructor for Singleton
	 */
	private ReaderSingleton() {
		this.bcsProjectName				= null;
		this.fzgTypeName				= null;
		this.mappingInfoMap 			= new HashMap<String, ArrayList<ClassMapInfo>>();
		this.part2effectivityIDMap 		= new HashMap<String, String>();
		this.id2ElemObjMap 				= new HashMap<String, Element>();
		this.objInfoList				= new ArrayList<ObjectInfo>(); 
		this.bomParent2ChildMap 		= new HashMap<Integer, TreeMap<String, BOMInfo>>();
		this.mapQry2ExecTime			= new HashMap<String, Long>();
		this.mapQry2ExecuteFreq			= new HashMap<String, Integer>();
		//this.addMapDbObjectSum(new HashMap<>());
		this.mapDbObjectSum				= new HashMap<>();
	} 

	/**
	 * getter for the current baumuster whose child connections are being written out to the intermediate xml file
	 * @return
	 */
	public String getCurrBaumsterID() {
		return currBaumsterID;
	}

	/**
	 * 
	 * @param currBaumsterID
	 */
	public void setCurrBaumsterID(String currBaumsterID) {
		this.currBaumsterID = currBaumsterID;
	}

	public static void addWriterAppLabel(String appLabel)
	{
		if(cdi3dListWriter == null)
		{
			cdi3dListWriter = new ArrayList<>();
		}
		cdi3dListWriter.add(appLabel);
	}
	public static ArrayList<String> getCdi3dListWriter()
	{
		return cdi3dListWriter;
	}
	public static void addAppLabel(String appLabel)
	{
		if(cdi3dList == null)
		{
			cdi3dList = new ArrayList<>();
		}
		cdi3dList.add(appLabel);
	}

	public static ArrayList<String> getCdi3dList()
	{
		return cdi3dList;
	}
	/**
	 * getter for the member variable
	 * @return : value of the fahrzeug type member variable
	 */
	public String getFzgTypeName() {
		return fzgTypeName;
	}

	/**
	 * setter for the member variable
	 * @param fzgTypeName : input value for the fahrzeug type
	 */ 
	public void setFzgTypeName(String fzgTypeName) {
		this.fzgTypeName = fzgTypeName;
	}

	/**
	 * getter for the member string variable
	 * @return : reference fahrzeug name stored in the Singleton instance
	 */
	public String getRefFzgName() {
		return refFzgName;
	}

	/**
	 * setter for the member variable
	 * @param refFzgName : input reference fahrzeug name as read from the input plmxml file
	 */
	public void setRefFzgName(String refFzgName) {
		// 24-05-2017: additional check in order to ensure that a second root instance from a secondary plmxml file does not overwrite the 
		// reference fahrzeug name read from the primary xml file
		if(this.refFzgName == null) {
			this.refFzgName = refFzgName;
		}
	}

	/**
	 * static method that returns the singleton object by invking the private constructor
	 * @return
	 */
	public static ReaderSingleton getReaderSingleton() {

		if(ReaderSingleton.singleton == null) {
			ReaderSingleton.singleton = new ReaderSingleton();
		}
		return ReaderSingleton.singleton;
	}

	/**
	 * getter for the project names for the BCS elements
	 * @return : project name as deduced from the input DMU XML file
	 */
	public String getBcsProjectName() {
		return bcsProjectName;
	}

	/**
	 * setter for the bcs project name member variable name
	 * @param refFzgName : part number of the reference configuration as read from the input DMU XML file 
	 * @throws TruckException 
	 */
	public void setBcsProjectName(String refFzgName) throws TruckException {
		if(refFzgName != null) {
			// implements the following assignment of project name based on the part number of the reference vehicle
			// if first two letters are S3 - project name is SFTP
			// if first two letters are A6 - project name is NGA
			// if first two letters are F2 - project name is FUSO
			// if first two letters are A3 - project name is ACTROS			
			if(refFzgName.startsWith(IConstants.PROJ_PREFIX_SFTP)) {
				this.bcsProjectName = IConstants.PROJ_SFTP;
			}
			else if(refFzgName.startsWith(IConstants.PROJ_PREFIX_NGA)) {
				this.bcsProjectName = IConstants.PROJ_NGA;
			}
			else if(refFzgName.startsWith(IConstants.PROJ_PREFIX_ACTROS)) {
				this.bcsProjectName = IConstants.PROJ_ACTROS;
			}
			else if(refFzgName.startsWith(IConstants.PROJ_PREFIX_FUSO)) {
				this.bcsProjectName = IConstants.PROJ_FUSO;
			}
			//for Daimler Truck POC
			else if(refFzgName.startsWith("DG")) {
				this.bcsProjectName = "N_GE_Trans@Doku";//IConstants.PROJ_FUSO;
			}
			else if(refFzgName.startsWith("BUS")) {
				this.bcsProjectName = "N_GE_Trans@Doku";//IConstants.PROJ_FUSO;
			}
			else {
				throw new TruckException("No default project definition exists for the reference fahrzeug with name: " + refFzgName + " Exiting the pre-importer"
						+ " as being unable to determine the parent project. "
						+ "Exception thrown in Class: " + this.getClass().getName() + " at Line: " 
						+ Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
			}
		}
	}

	/**
	 * getter for retrieving the list of effectivities for a Truck DMU XML file
	 * @return : array list of effectivity ids
	 */
	public HashMap<String, String> getPart2EffIDMap() {
		return part2effectivityIDMap;
	}

	/**
	 * method that sets value to the member map as a mapping between the c/d-bm id and the end item name generated out of it
	 * @param partIdent : identifier for the the C/D-BM as read from the input Truck PLMXML file 
	 */
	public void setPart2EffIDMap(String partIdent) {
		if(partIdent != null && this.refFzgName != null) {
			String effectivityID = null;						// local variable that holds the derived end item id for effectivity
			// builds the end item id for effectivity
			effectivityID = IConstants.PREFIX_EFF_ID + "_" + this.refFzgName + "_" + partIdent;
			// sets the values to the map
			this.part2effectivityIDMap.put(partIdent ,effectivityID);
		}
	}

	/**
	 * getter for the member map for the class member mappingInfoMap
	 * @return : value of the 
	 */
	public HashMap<String, ArrayList<ClassMapInfo>> getMappingInfoMap() {
		return mappingInfoMap;
	}

	/**
	 * setter for the member map
	 * @param mappingInfoMap : Input mapping info map 
	 */
	public void setMappingInfoMap(HashMap<String, ArrayList<ClassMapInfo>> mappingInfoMap) {
		this.mappingInfoMap = mappingInfoMap;
	}

	/**
	 * method that triggers functionality for reading mapping file
	 * @param mappingFilePath :	input mapping file path as read from the input arguments
	 */
	public void readMappingFile(String mappingFilePath) {
		// checks if the mapping file is null or not
		if(mappingFilePath != null) {
			// checks if the mapping file exists or not - redundant check
			ReadMappingFile mapFileReader = new ReadMappingFile(mappingFilePath);
			if(mapFileReader != null) {
				// sets the mapping information 
				this.mappingInfoMap = mapFileReader.parseMapppingFile();
			}
		}
	}

	/**
	 * getter for the member map
	 * @return
	 */
	public HashMap<String, Element> getId2ElemObjMap() {
		return id2ElemObjMap;
	}

	/**
	 * setter for the member map
	 * @param id 		: id value for the element as read from the input PLMXML file
	 * @param elemObj	: Element instance which corresponds to the id value
	 */
	public void setId2ElemObjMap(String id, Element elemObj) {
		if(id != null && elemObj != null) {
			this.id2ElemObjMap.put(id, elemObj);
		}
	}

	/**
	 * getter for an instance of the ObjectInfo class
	 * @param	: name of the source Smaragd class 
	 * @return 	: instance of the ObjectInfo class
	 */
	public ObjectInfo getObjInfoInst(String smaClassName) {

		ObjectInfo objInst = null;

		if(this.objInfoList != null) {
			for(ObjectInfo objInfoInst : this.objInfoList) {
				if(objInfoInst.getSmaClsName().equals(smaClassName)) {
					objInst = objInfoInst;
					break;
				}
			}
		}
		return objInst;
	}

	/**
	 * getter for class member
	 * @return : objInfoList member 
	 */
	public ArrayList<ObjectInfo> getObjInfoList() {

		return this.objInfoList;
	}

	/**
	 * setter for the class member
	 * @param objClassName 	: name of the Smaragd class name
	 * @param scope			: either Smaragd or Teamcenter object type
	 */
	public void setObjInfoList(ObjectInfo objInfoInst) {

		this.objInfoList.add(objInfoInst);
	}

	/**
	 * getter for the member map
	 * @return : value of the bomParent2ChildMap class member
	 */
	public HashMap<Integer, TreeMap<String, BOMInfo>> getBOMParent2ChildMap() {
		return bomParent2ChildMap;
	}

	/**
	 * 
	 * @param bomParentID
	 * @param bomChildID
	 * @param bomParentName
	 * @param bomParentPartNo
	 * @param bomLevel
	 * @param parBOMRevID
	 * @param childBOMRevID
	 */
	public void setBOMParent2ChildMap(String bomParentID, String bomChildID, String bomParentName, String bomParentPartNo, int bomLevel, 
			String parBOMRevID, String childBOMRevID) {

		boolean isMapPopulated = false;
		if(this.bomParent2ChildMap != null && bomParentID != null && childBOMRevID != null && bomParentName != null && bomParentPartNo != null
				&& parBOMRevID != null && childBOMRevID != null) {
			// iterate to get the correct BOM level in the map
			if(this.bomParent2ChildMap.containsKey(bomLevel)) {
				// checks if the OBID of the parent BOM line is already present in the map or not
				if(this.bomParent2ChildMap.get(bomLevel).containsKey(bomParentID)) {
					// sets the bom child id to the BOMInfo object
					this.bomParent2ChildMap.get(bomLevel).get(bomParentID).setBomChildOBID(bomChildID, childBOMRevID);
					isMapPopulated = true;
				} 
				// loop when the bom level exists but the bom parent does not exist
				else {
					// dummy BOMInfo instance
					BOMInfo bomInfoObj = new BOMInfo(bomParentID, bomParentName, bomParentPartNo, bomLevel, parBOMRevID);
					// set the bom child ID to the above created instance
					if(bomInfoObj != null) {
						bomInfoObj.setBomChildOBID(bomChildID, childBOMRevID);
					}
					// set the dmyMap instance to the Singleton class member
					this.bomParent2ChildMap.get(bomLevel).put(bomParentID, bomInfoObj);
					isMapPopulated = true;
				}
			}
		}
		// loop that executes if the bom level information is not already in the Singleton member
		if(isMapPopulated == false) {
			// instantiate the BOMInfo class
			BOMInfo bomInfoObj = new BOMInfo(bomParentID, bomParentName, bomParentPartNo, bomLevel, parBOMRevID);
			// sets the bom children to the BOMInfo object
			if(bomInfoObj != null) {
				bomInfoObj.setBomChildOBID(bomChildID, childBOMRevID);
			}
			// instantiation of the content map
			if(bomParentID != null ){

				TreeMap <String, BOMInfo> dummyMap = new TreeMap<String, BOMInfo>();
				dummyMap.put(bomParentID, bomInfoObj);
				// populate the member map
				//this.bomParent2ChildMap.put((this.bomParent2ChildMap.size()+1), dummyMap);
				this.bomParent2ChildMap.put(bomLevel, dummyMap);
			}
		}
	}

	/**
	 * 
	 * @param bomParentID
	 * @param bomChildID
	 * @param bomParentName
	 * @param bomParentPartNo
	 * @param bomLevel
	 * @param parBOMRevID
	 * @param childBOMRevID
	 * @param endItemID
	 */
	public void setBOMParent2ChildMap(String bomParentID, String bomChildID, String bomParentName, String bomParentPartNo, int bomLevel, 
			String parBOMRevID, String childBOMRevID, String endItemID) {

		boolean isMapPopulated = false;

		if(this.bomParent2ChildMap != null) {
			// iterate to get the correct BOM level in the map
			if(this.bomParent2ChildMap.containsKey(bomLevel)) {
				// checks if the OBID of the parent BOM line is already present in the map or not
				//	if(this.bomParent2ChildMap.get(bomLevel).containsKey(bomParentID)) {
				if(bomParentID != null ) {
					if( this.bomParent2ChildMap.get(bomLevel).containsKey(bomParentID)) {
						// sets the bom child id to the BOMInfo object
						this.bomParent2ChildMap.get(bomLevel).get(bomParentID).setBomChildOBID(bomChildID, childBOMRevID);
						isMapPopulated = true;
					} 
					// loop when the bom level exists but the bom parent does not exist
					else {
						// dummy BOMInfo instance
						BOMInfo bomInfoObj = new BOMInfo(bomParentID, bomParentName, bomParentPartNo, bomLevel, parBOMRevID);
						// set the bom child ID to the above created instance
						if(bomInfoObj != null) {
							bomInfoObj.setBomChildOBID(bomChildID, childBOMRevID);
							bomInfoObj.setTrckBMEndItem(endItemID);
						}

						// set the dmyMap instance to the Singleton class member
						this.bomParent2ChildMap.get(bomLevel).put(bomParentID, bomInfoObj);
						isMapPopulated = true;
					}
				}
			}
		}
		// loop that executes if the bom level information is not already in the Singleton member
		if(isMapPopulated == false && bomParentID != null) {
			// instantiate the BOMInfo class
			BOMInfo bomInfoObj = new BOMInfo(bomParentID, bomParentName, bomParentPartNo, bomLevel, parBOMRevID);
			// sets the bom children to the BOMInfo object
			if(bomInfoObj != null) {
				bomInfoObj.setBomChildOBID(bomChildID, childBOMRevID);
				bomInfoObj.setTrckBMEndItem(endItemID);
			}
			// instantiation of the content map
			TreeMap <String, BOMInfo> dummyMap = new TreeMap<String, BOMInfo>();
			dummyMap.put(bomParentID, bomInfoObj);
			// populate the member map
			//this.bomParent2ChildMap.put((this.bomParent2ChildMap.size()+1), dummyMap);
			this.bomParent2ChildMap.put(bomLevel, dummyMap);
		}
	}

	public ArrayList<RefConfigMappingObject> getSumRefConfigMapObjs() {
		return sumRefConfigMapObjs;
	}

	public void setSumRefConfigMapObjs(ArrayList<RefConfigMappingObject> sumRefConfigMapObjs) {
		this.sumRefConfigMapObjs = sumRefConfigMapObjs;
	}

	public String getRefConfigurationType() {
		return refConfigurationType;
	}

	public void setRefConfigurationType(String refConfigurationType) {
		this.refConfigurationType = refConfigurationType;
	}

	public String getInputMode() {
		return inputMode;
	}

	public void setInputMode(String inputMode) {
		this.inputMode = inputMode;
	}

	public ArrayList<GlobalConstMapInfo> getGlobalConstCollection() {
		return globalConstCollection;
	}

	public void setGlobalConstCollection(ArrayList<GlobalConstMapInfo> globalConstCollectionObj) {
		this.globalConstCollection = globalConstCollectionObj;
	}


	public String getGlobalConstValue(String globalConstVarName)
	{
		String value = null;

		if( globalConstCollection!= null && globalConstCollection.size() > 0)
		{
			for (GlobalConstMapInfo globalConstMapInfo : globalConstCollection) 
			{
				if(globalConstMapInfo.getVarName().equals(globalConstVarName))
				{
					value = globalConstMapInfo.getValue();
					break;
				}
			}
		}

		return value;
	}

	public String getRootPartnumber() {
		return rootPartnumber;
	}

	public void setRootPartnumber(String rootPartnumber) {
		this.rootPartnumber = rootPartnumber;
	}

	public HashMap<String, Integer> getMapQry2ExecuteFreq() {
		return mapQry2ExecuteFreq;
	}

	public void setMapQry2ExecuteFreq(String sQryName) {
		// checks if the query name already exists in the map or not
		if(this.mapQry2ExecuteFreq.containsKey(sQryName)) {
			Integer iCurrFreq = this.mapQry2ExecuteFreq.get(sQryName);
			iCurrFreq = iCurrFreq + 1;
			// removes the existing entry
			this.mapQry2ExecuteFreq.remove(sQryName);
			// adds the updated frequency count against the query name
			this.mapQry2ExecuteFreq.put(sQryName, iCurrFreq);
		}
		else {
			// first hit of the query in the execution of the Pre-Importer
			this.mapQry2ExecuteFreq.put(sQryName, 1);
		}
	}

	public HashMap<String, Long> getMapQry2ExecTime() {
		return (HashMap<String, Long>) mapQry2ExecTime;
	}

	public void setMapQry2ExecTime(String sQryName, Long dQryTime) {
		// checks if the query exists in the map or not
		if(this.mapQry2ExecTime.containsKey(sQryName)) {
			Long dTotQryTime = (Long) this.mapQry2ExecTime.get(sQryName);
			// adds the input query execution time to the total query time
			dTotQryTime = dTotQryTime + dQryTime;
			// removes the current entry in the map
			this.mapQry2ExecTime.remove(sQryName);
			// sets the new total time against the query in the map
			this.mapQry2ExecTime.put(sQryName, dTotQryTime);
		}
		else {
			// first entry of the execution time for the Query in the Pre-Importer
			this.mapQry2ExecTime.put(sQryName, dQryTime);
		}
	}

	public void startDBConnBroker(String dbConfigPath)
	{
		// invokes the constructor of the DBConnBroker class
		if(dbConfigPath != null) {
			this.dbConnBroker = new DBConnBroker(dbConfigPath);
		}
	}
	
	public void startDBConnBroker(String dbConfigPath, Logger logger)
	{
		// invokes the constructor of the DBConnBroker class
		if(dbConfigPath != null) {
			this.dbConnBroker = new DBConnBroker(dbConfigPath, logger);
		}
	}
	public DBConnBroker getDbConnBroker() {

		return dbConnBroker;
	}

	public void setDbConnBroker(DBConnBroker dbConnBroker) {
		this.dbConnBroker = dbConnBroker;
	}

	public String getEndItem() {
		return endItem;
	}

	public void setEndItem(String endItem) {
		this.endItem = endItem;
	}

	public boolean isDbConnect() {
		return isDbConnect;
	}

	public void setDbConnect(boolean isDbConnect) {
		this.isDbConnect = isDbConnect;
	}

	public HashMap<String, Integer> getMapDbObjectSum() {
		return mapDbObjectSum;
	}

	public void addMapDbObjectSum(String clsName) {
		//this.mapDbObjectSum = mapDbObjectSum;
		if(clsName != null )
		{
			if(mapDbObjectSum.get(clsName) != null)
			{
				mapDbObjectSum.put(clsName, mapDbObjectSum.get(clsName)+1);
			}
			else
			{
				mapDbObjectSum.put(clsName, 1);
			}
		}
	}

	public String getRefConfigName() {
		return refConfigName;
	}

	public void setRefConfigName(String refConfigName) {
		this.refConfigName = refConfigName;
	}

	public PrintWriter getOccConnLogger() {

		if(System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) != null) {
			String fileName = System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) + "//" + "OccConnReaderLog_" + System.currentTimeMillis() + ".txt";
			try {
				if(mLogOccWriterObj == null)
				{
					mLogOccWriterObj = new PrintWriter(fileName);
				}
				return mLogOccWriterObj;
				//this.mOutStream = new BufferedOutputStream(new FileOutputStream(fileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// provide warning that the logging will not be possible because the environment variable is not defined
			System.out.println("WARNING: UNABLE TO WRITE LOG INFORMATION FOR THE SQL DB CONNECTOR AS THE ENV. VARIABLE - CDM_IMPORTER_LOG IS NOT SET ");
		}
		return mLogOccConnWriterObj;

	}
	public static void stopOccConnLogger() {

		if(mLogOccWriterObj != null)
		{
			mLogOccWriterObj.close();
		}

	}
	public PrintWriter getOccConnWriteLogger() {

		if(System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) != null) {
			String fileName = System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) + "//" + "OccConnWriterLog_" + System.currentTimeMillis() + ".txt";
			try {
				if(mLogOccConnWriterObj == null)
				{
					mLogOccConnWriterObj = new PrintWriter(fileName);
				}
				return mLogOccConnWriterObj;
				//this.mOutStream = new BufferedOutputStream(new FileOutputStream(fileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// provide warning that the logging will not be possible because the environment variable is not defined
			System.out.println("WARNING: UNABLE TO WRITE LOG INFORMATION FOR THE SQL DB CONNECTOR AS THE ENV. VARIABLE - CDM_IMPORTER_LOG IS NOT SET ");
		}
		return mLogOccConnWriterObj;

	}
	public static void stopOccConnWriteLogger() {

		if(mLogOccConnWriterObj != null)
		{
			mLogOccConnWriterObj.close();
		}

	}
	
	public void addOccConnReaderLogList(String occConnReaderEntry)
	{
		if(mLogOccConnReaderListObj == null)
		{
			mLogOccConnReaderListObj = new ArrayList<>();
		}
		mLogOccConnReaderListObj.add(occConnReaderEntry);
	}
	
	public void addOBIDActionLogList(String obidActionEntry)
	{
		if(mLogOBIDActionListObj == null)
		{
			mLogOBIDActionListObj = new ArrayList<>();
		}
		mLogOBIDActionListObj.add(obidActionEntry);
	}
	
	public void writeOBIDActionListToFile()
	{

		if(System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) != null) {
			String fileName = System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) + "//" + "SortedOBIDActionLog_" + System.currentTimeMillis() + ".txt";
			try {
				//if(mLogOccWriterObj == null)
				//{
					PrintWriter printWriter = new PrintWriter(fileName);
					if(mLogOBIDActionListObj != null && mLogOBIDActionListObj.size() > 0)
					{
						//Collections.sort(mLogOccConnReaderListObj);
						for (String string : mLogOBIDActionListObj) {
							printWriter.write(string+"\n");
						}
					}
					printWriter.close();
				//}
				
				//this.mOutStream = new BufferedOutputStream(new FileOutputStream(fileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// provide warning that the logging will not be possible because the environment variable is not defined
			System.out.println("WARNING: UNABLE TO WRITE LOG INFORMATION FOR THE SQL DB CONNECTOR AS THE ENV. VARIABLE - CDM_IMPORTER_LOG IS NOT SET ");
		}
		

	
	}
	
	public void writeOccConnReaderLogListToFile()
	{

		if(System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) != null) {
			String fileName = System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) + "//" + "SortedOccConnReaderLog_" + System.currentTimeMillis() + ".txt";
			try {
				//if(mLogOccWriterObj == null)
				//{
					PrintWriter printWriter = new PrintWriter(fileName);
					if(mLogOccConnReaderListObj != null && mLogOccConnReaderListObj.size() > 0)
					{
						//Collections.sort(mLogOccConnReaderListObj);
						for (String string : mLogOccConnReaderListObj) {
							printWriter.write(string+"\n");
						}
					}
					printWriter.close();
				//}
				
				//this.mOutStream = new BufferedOutputStream(new FileOutputStream(fileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// provide warning that the logging will not be possible because the environment variable is not defined
			System.out.println("WARNING: UNABLE TO WRITE LOG INFORMATION FOR THE SQL DB CONNECTOR AS THE ENV. VARIABLE - CDM_IMPORTER_LOG IS NOT SET ");
		}
		

	
	}
	
	public void addOccConnWriterLogList(String occConnWriterEntry)
	{
		if(mLogOccConnWriterListObj == null)
		{
			mLogOccConnWriterListObj = new ArrayList<>();
		}
		mLogOccConnWriterListObj.add(occConnWriterEntry);
	}
	public void writeOccConnWriterLogListToFile()
	{

		if(System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) != null) {
			String fileName = System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) + "//" + "SortedOccConnWriterLog_" + System.currentTimeMillis() + ".txt";
			try {
				//if(mLogOccWriterObj == null)
				//{
					PrintWriter printWriter = new PrintWriter(fileName);
					if(mLogOccConnWriterListObj != null && mLogOccConnWriterListObj.size() > 0)
					{
						// Collections.sort(mLogOccConnWriterListObj);
						for (String string : mLogOccConnWriterListObj) {
							printWriter.write(string+"\n");
						}
					}
					printWriter.close();
				//}
				
				//this.mOutStream = new BufferedOutputStream(new FileOutputStream(fileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// provide warning that the logging will not be possible because the environment variable is not defined
			System.out.println("WARNING: UNABLE TO WRITE LOG INFORMATION FOR THE SQL DB CONNECTOR AS THE ENV. VARIABLE - CDM_IMPORTER_LOG IS NOT SET ");
		}
		

	
	}

}
