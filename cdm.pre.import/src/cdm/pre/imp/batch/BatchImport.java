package cdm.pre.imp.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
//import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.Mode;
import cdm.pre.imp.VersionInfo;
import cdm.pre.imp.XMLFileData;
import cdm.pre.imp.csvreader.CSVFileProcesser;
import cdm.pre.imp.csvreader.RefConfigMappingObject;
import cdm.pre.imp.dbconnector.DBConnSingleton;
import cdm.pre.imp.dbconnector.DBConnUtilities;
import cdm.pre.imp.map.JTFileManagerUtils;
import cdm.pre.imp.map.NXPartFileManagerUtils;
import cdm.pre.imp.mod.TreeElement;
import cdm.pre.imp.mod.TreeElementFactoryFromPLMXML;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.Handler;
import cdm.pre.imp.reader.PLMUtils;
import cdm.pre.imp.reader.PLMUtils.SortedElements;
import cdm.pre.imp.reader.Reader;
import cdm.pre.imp.reader.ReaderSingleton;

/**
 * 
 * @author shagani
 * 
 */
public class BatchImport implements IBatchImport {

	private Map<String, String> argumentsMap = null;
	private int maxElemNum = 0;

	private HashMap<String, SnapshotInfo> plmxml2InfoMap;
	private boolean dbConnectStrat;

	private final static Logger logger	= LogManager.getLogger("cdm.pre.imp.tracelog");

	// new constructor added to invoke the class methods without the need to set the input arguments
	private BatchImport() {
		this.argumentsMap = new HashMap<String, String>();
		// additional initiation of the hash map member
		this.plmxml2InfoMap = new HashMap<String, SnapshotInfo>();
	}

	private BatchImport(Map<String, String> argumentsMap) {
		this.argumentsMap = argumentsMap;
		// additional initiation of the hash map member
		this.plmxml2InfoMap = new HashMap<String, SnapshotInfo>();
	}

	/// used to get the instance of the class by calling the default constructor
	public static BatchImport getDefBatchImporter() {
		return new BatchImport();
	}


	public static IBatchImport getBatchImporter(Map<String, String> argumentsMap) {
		return new BatchImport(argumentsMap);
	}


	public HashMap<String, SnapshotInfo> getPlmxml2InfoMap() {
		return plmxml2InfoMap;
	}

	public void setPlmxml2InfoMap(String plmxmlFileArg, SnapshotInfo snapInfoObj) {
		if(plmxmlFileArg != null && snapInfoObj != null) {
			this.plmxml2InfoMap.put(plmxmlFileArg, snapInfoObj);
		}
	}

	/**
	 * This is wrapper function which will be used in batch mode.
	 * 
	 * @param roots1
	 * @param roots2
	 * @return
	 */
	private List<TreeElement> compareRoots(List<TreeElement> roots1, List<TreeElement> roots2) {
		// logger.info("started compare roots ...");
		List<TreeElement> compRes = TreeElement.compareRoots(roots1, roots2);
		Date lastModifDate = roots1.get(0).getLastModifDate();
		compRes.get(0).setLastModifDate(lastModifDate);
		return compRes;
	}

	/**
	 * This is wrapper function which will be used in batch mode.
	 * 
	 * @param imports
	 * @param delta
	 * @throws CDMException
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private void writeImportXML(List<TreeElement> imports, Mode mode, String plmxmlPath1, String plmxmlPath2)
			throws IOException, XMLStreamException, CDMException 
	{

		File plmxmlFile1 = null;
		File plmxmlFile2 = null;
		if (plmxmlPath1 != null && plmxmlPath1.length() > 0)
		{
			plmxmlFile1 = new File(plmxmlPath1);
		}
		if (plmxmlPath2 != null && plmxmlPath2.length() > 0)
		{
			plmxmlFile2 = new File(plmxmlPath2);
		}

		XMLFileData xmlFileData =null; 
		String endItem = null;

		if(argumentsMap.get("processSendToPackage").equalsIgnoreCase("true")){
			NXPartFileManagerUtils.setIsSendToProcess(true);
			NXPartFileManagerUtils.setPlmXmlFileLoc(plmxmlPath1);

			JTFileManagerUtils.setIsSendToProcess(true);
			JTFileManagerUtils.setPlmXmlFileLoc(plmxmlPath1);
			
			HashMap<String, String> datFilePropsMap = getEndItemForSendToPackage(plmxmlPath1);

			String inputMode = argumentsMap.get(Arguments.INPUT_MODE);
			if(inputMode == null && datFilePropsMap != null)
			{
				inputMode = datFilePropsMap.get("mode");
			}
			ReaderSingleton.getReaderSingleton().setInputMode(inputMode);
			// 21.01.2019 [Amit]- Changed from P_MODE_SENDTO_WITHOUT_REF_CONFIG to P_MODE_SENDTO_2_WITHOUT_REF_CONFIG
			if(inputMode != null && inputMode.equals(PreferenceConstants.P_MODE_SENDTO_2_WITHOUT_REF_CONFIG))
			{
				logger.info("Processing SendTo PLMXML with no END ITEM NAME ( input Mode : "+PreferenceConstants.P_MODE_SENDTO_2_WITHOUT_REF_CONFIG+": ::. ");
				System.out.println("SendTo PLMXML with no END ITEM NAME ::. ");
				endItem = "";
			}
			else 
			{
				if(datFilePropsMap != null)
				
				endItem = datFilePropsMap.get("ref_config_name");//(plmxmlPath1);
				
				if(inputMode == null && (endItem== null || endItem.equals("")))
				{
					logger.error("END ITEM ( Reference Configuration Name)  Missing. Please check the .dat file.");
					System.out.println("END ITEM ( Reference Configuration Name)  Missing. Please check the .dat file.");
					System.exit(1);
				}
				else if(inputMode != null && (endItem== null || endItem.equals("")))
				{
					logger.error("END ITEM ( Reference Configuration Name)  Missing. Please check the .dat file.");
					System.out.println("END ITEM ( Reference Configuration Name)  Missing. Please check the .dat file.");
					System.exit(1);
				}
			}

			if(endItem!=null)
			{
				logger.info("END Item from .dat File : "+endItem);
				xmlFileData = new XMLFileData(imports.get(0), argumentsMap.get(Arguments.OUTPUT_ARG_1), endItem, plmxmlFile1, plmxmlFile2, mode);
			}
		}
		else
		{
			xmlFileData = new XMLFileData(imports.get(0), argumentsMap.get(Arguments.OUTPUT_ARG_1), imports
					.get(0).getRefConfigName(), plmxmlFile1, plmxmlFile2, mode);    	
			endItem = xmlFileData.getEndItem();
			if(endItem == null)
			{
				logger.error("DMU PLMXML file : "+plmxmlFile1.getAbsolutePath());
				logger.error("END ITEM ( Reference Configuration Name)  Missing. Please check the PLMXML file.");
				System.exit(1);
			}
		}

		ReaderSingleton.getReaderSingleton().setEndItem(xmlFileData.getEndItem());
		//String dbConnectStr = argumentsMap.get(PreferenceConstants.P_DBCONNECT);
	/*	boolean dbConnectStrat = false;
		if(argumentsMap.get(PreferenceConstants.P_DBCONNECT) != null && (argumentsMap.get(PreferenceConstants.P_DBCONNECT).equals("true")))
		{
			String dbConfigFile = argumentsMap.get(PreferenceConstants.P_DBCONNECT_FILE);
			if(dbConfigFile != null)
			{			
				ReaderSingleton.getReaderSingleton().startDBConnBroker(dbConfigFile);
				ReaderSingleton.getReaderSingleton().getDbConnBroker().manageDBConnect(false);
				if(DBConnSingleton.getInstance().getmLogObj() != null) {
					DBConnSingleton.getInstance().getmLogObj().writeLogHeader();
				}

				ReaderSingleton.getReaderSingleton().setDbConnect(true);
				dbConnectStrat = true;	
			}
			else
			{
				logger.error("DB ConfigPath missing.. Please provide 'dbConnectFile' argument ");
			}
		}*/
		//ReaderSingleton.getReaderSingleton().getDbConnBroker().manageDBConnect(false);
		imports.get(0).writeItemXMLFile(xmlFileData, null, true);
		if(dbConnectStrat)
		{
			// writes out the query timings to the SQL Log
			if(DBConnSingleton.getInstance().getmLogObj() != null) {
				int iTotExecFreq = 0;									// total number of queries that have been executed in the Pre-Importer session
				long lTotExecTime = 0L;									// total time taken for all the executed queries in the Pre-Importer session
				// writes out the log output
				if(ReaderSingleton.getReaderSingleton().getMapQry2ExecTime() != null) {
					for (Entry<String, Long> entry : ReaderSingleton.getReaderSingleton().getMapQry2ExecTime().entrySet()) {
						long lTimeTaken = 0L;
						int  iQryFreq	= 0;
						
						// time taken to execute the query
						lTimeTaken = entry.getValue()/1000;
						// add to the total time
						if(!entry.getKey().equals("executeDBAction")) {
							lTotExecTime = lTotExecTime + lTimeTaken;
						}
								
						if(ReaderSingleton.getReaderSingleton().getMapQry2ExecuteFreq().containsKey(entry.getKey())) {
							iQryFreq = ReaderSingleton.getReaderSingleton().getMapQry2ExecuteFreq().get(entry.getKey());
						}
						
						// add to the total number of executions
						if(!entry.getKey().equals("executeDBAction")) {
							iTotExecFreq = iTotExecFreq + iQryFreq;
						}
						
						// writes out the log entry to the SQL Connector log file
						if(DBConnSingleton.getInstance().getmLogObj() != null) {
	 						DBConnSingleton.getInstance().getmLogObj().writeToLogFile("QUERY NAME: <" + entry.getKey() 
									+ "> -------> FREQ: <" + iQryFreq + ">" + " -------> TIME TAKEN: <" + lTimeTaken +  "s.>" , DBConnUtilities.LOG_LEV_INFO);
						}
					}
					// writes out the total execution time and total number of queries executed
					if(DBConnSingleton.getInstance().getmLogObj() != null) {
 						DBConnSingleton.getInstance().getmLogObj().writeToLogFile("TOTAL NUMBER OF QUERIES: <" + iTotExecFreq 
								+ "> EXECUTED IN: <" + lTotExecTime + "s.>" , DBConnUtilities.LOG_LEV_INFO);
					}
				}
				// end call to the log file
				DBConnSingleton.getInstance().getmLogObj().writeLogFooter();;
			}
			
			try {
				ReaderSingleton.getReaderSingleton().getDbConnBroker().manageDBConnect(true);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		maxElemNum = imports.get(0).getMaxElemNmb();
	}

	/**
	 * This is wrapper function which will be used in batch mode.
	 * 
	 * @param fileName		- path to the source Smaragd PLMXML file
	 * @param plmxmlFileArg	- s1 or s2
	 * @param vehicleTyp	- xml_car or xml_truck
	 * @param isSecXML		- boolean variable that states if the PLMXML file being processed is a main PLMXML file or a secondary PLMXML file
	 * @return				- array of instances of the TreeElement class which represents the BOM structure from the source PLMXML file
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws CDMException
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public List<TreeElement> readSnapShot(String fileName, String plmxmlFileArg, String vehicleTyp, boolean isSecXML) throws ParserConfigurationException, SAXException,
	IOException, CDMException, NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		// 02-12-2016 - Truck implementation - this method must be modified to process the secondary PLMXML files before creating the structure elements
		// for the main PLMXML file.

		List<TreeElement> roots 		= null;
		SortedElements sortedElements 	= null;

		String endItem = null;
		String rootItemPartNumber = null;

		Reader reader = new Reader();
		Handler handler = reader.readSnapshot(fileName, vehicleTyp);

		if(handler != null) {
			// call to the createStructure with the boolean value for is secondary xml file or not
			roots = TreeElementFactoryFromPLMXML.createStructure(handler, isSecXML);
			
			if (roots.isEmpty()) {
				return roots;
			}

			Date lastModifDate = handler.getExportDate();
			roots.get(0).setLastModifDate(lastModifDate);
			endItem =roots.get(0).getRefConfigName();
			if(endItem == null && !argumentsMap.get("processSendToPackage").equalsIgnoreCase("true"))
			{
				logger.error("DMU PLMXML file : "+fileName);
				logger.error("END ITEM ( Reference Configuration Name)  Missing. Please check the PLMXML file.");
				System.exit(1);
			}
			else
			{
				ReaderSingleton.getReaderSingleton().setRefConfigName(endItem);
			}

			rootItemPartNumber = roots.get(0).getPartNumber();
			sortedElements  = reader.getSortedElements();	


			// instantiate an object of the SnapshotInfo class to store the information read from the input PLMXML file
			// added the boolean variable in order to prevent the sorted elements from the child XMLs from being populated in the member map
			if(sortedElements != null && endItem != null && rootItemPartNumber != null && isSecXML == false) {
				SnapshotInfo snapInfoObj = new SnapshotInfo(sortedElements, endItem, rootItemPartNumber);
				snapInfoObj.setPlmxmlExportDate(lastModifDate);
				// 13.02.2017 -  add the information to the member map that stores the it against the state of the PLMXML file
				this.plmxml2InfoMap.put(plmxmlFileArg, snapInfoObj);
			}
		} else {
			// throw exit exception to end the pre-importer
		} 

		return roots;
	}

	/**
	 * This function parse the PLMXML files and generates import XML.
	 * 
	 * @throws CDMException
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void generateImportFile() throws Exception {
		List<TreeElement> roots1 = null;
		List<TreeElement> roots2 = null;
		List<TreeElement> result = null;

		if(argumentsMap.get(Arguments.FAHRZEUG_TYPE) != null) {
			if(argumentsMap.get(Arguments.FAHRZEUG_TYPE).equals(PreferenceConstants.P_FZG_TYP_LKW)) {
				// sets the Fahrzeug type to the Reader Singleton instance
				ReaderSingleton.getReaderSingleton().setFzgTypeName(PreferenceConstants.P_FZG_TYP_LKW);
			}
			else if(argumentsMap.get(Arguments.FAHRZEUG_TYPE).equals(PreferenceConstants.P_FZG_TYP_PKW)) {
				// sets the Fahrzeug type to the Reader Singleton instance
				ReaderSingleton.getReaderSingleton().setFzgTypeName(PreferenceConstants.P_FZG_TYP_PKW);
			}
		}


		if(argumentsMap.containsKey(Arguments.CONFIG_MAP_FILE))
		{
			ReaderSingleton.getReaderSingleton().readMappingFile(argumentsMap.get(Arguments.CONFIG_MAP_FILE));
		}

		
		dbConnectStrat = false;
		if(argumentsMap.get(PreferenceConstants.P_DBCONNECT) != null && (argumentsMap.get(PreferenceConstants.P_DBCONNECT).equals("true")))
		{
			String dbConfigFile = argumentsMap.get(PreferenceConstants.P_DBCONNECT_FILE);
			if(dbConfigFile != null)
			{			
				logger.info("READING DB CONFIG FILE : ");
				logger.info("DB CONFIG FILE : "+dbConfigFile);
				
				ReaderSingleton.getReaderSingleton().startDBConnBroker(dbConfigFile);
				
				if(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SID) != null)
				{
					logger.info("SID : "+DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SID));
				}
				else if(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SERVICE) != null){
					logger.info("SERVICE : "+DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SERVICE));
				}
				ReaderSingleton.getReaderSingleton().getDbConnBroker().manageDBConnect(false);
				if(DBConnSingleton.getInstance().getmLogObj() != null) {
					DBConnSingleton.getInstance().getmLogObj().writeLogHeader();
				}

				ReaderSingleton.getReaderSingleton().setDbConnect(true);
				dbConnectStrat = true;	
				
				if(DBConnSingleton.getInstance().getmDBConnInst() == null)
				{
					logger.info("EXCEPTION HAS BEEN ENCOUNTERED WHILE CREATING DB CONNECTION ");
				//	element.setAction("not-checked");
					argumentsMap.put(PreferenceConstants.P_DBCONNECT,"false");
					ReaderSingleton.getReaderSingleton().setDbConnect(false);
					//return;
				}
			}
			else
			{
				logger.error("DB ConfigPath missing.. Please provide 'dbConnectFile' argument ");
			}
		}

		if (argumentsMap.containsKey(Arguments.INPUT_ARG_1)) {
			roots1 = readSnapShot(argumentsMap.get(Arguments.INPUT_ARG_1), Arguments.INPUT_ARG_1, argumentsMap.get(Arguments.FAHRZEUG_TYPE), false);
			if( roots1 != null && !roots1.isEmpty())
			{
				if (roots1.get(0) != null) {
					ReaderSingleton.getReaderSingleton().setRootPartnumber(roots1.get(0).getPartNumber());
				}
			}
		}
		if (argumentsMap.containsKey(Arguments.INPUT_ARG_2)) {
			roots2 = readSnapShot(argumentsMap.get(Arguments.INPUT_ARG_2), Arguments.INPUT_ARG_2, argumentsMap.get(Arguments.FAHRZEUG_TYPE), false);
		}
		if (!roots1.isEmpty() && roots1.get(0) != null) {
			System.out.println("Root Part Number : "+roots1.get(0).getPartNumber());
		} else {
			logger.error("Root Part not found or invalid.");
		}
		if( argumentsMap.get(Arguments.INPUT_MODE) != null)
		{
			String inputMode = argumentsMap.get(Arguments.INPUT_MODE);
			if( inputMode.equals("xml_car_var"))
			{
				//String csvFile = "D:\\Krishna\\DiFa\\Varint Conditions\\refConfigMapping.csv";
				CSVFileProcesser csvProcesser = new CSVFileProcesser();
				String csvFile = argumentsMap.get(Arguments.CSV_MAP_FILE);
				if( csvFile != null)
				{
					csvProcesser.parseCSV(csvFile);
					HashMap<String, RefConfigMappingObject> map = csvProcesser.getRefConfigObjectsMap();
					if(map == null || map.size() == 0)
					{
						logger.error("Error in processing .csv file.......");
						System.exit(1);
					}
					if(roots1 != null)
					{
						if(roots1.get(0) != null) 
						{

							String refConfigName = roots1.get(0).getRefConfigName();
							String rootObjClass = roots1.get(0).getClazz();
							logger.info("Root Object Class Name : "+rootObjClass);
							ArrayList<RefConfigMappingObject> sumRefconfigMapsList = new ArrayList<RefConfigMappingObject>();
							if( refConfigName != null && !refConfigName.equals(""))
							{
								RefConfigMappingObject refConfigMapObject = map.get(refConfigName);
								if(refConfigMapObject != null )
								{
									if( refConfigMapObject.getParentRefConfigMapObj() == null)
									{
										/*if(rootObjClass.equals(IConstants.j0Montge))
										{
											ReaderSingleton.getReaderSingleton().setRefConfigurationType(PreferenceConstants.P_REFCONFIG_TYP_MONTAGE);
										}
										else
										{
											ReaderSingleton.getReaderSingleton().setRefConfigurationType(PreferenceConstants.P_REFCONFIG_TYP_SUM);
										}*/

										ReaderSingleton.getReaderSingleton().setRefConfigurationType(PreferenceConstants.P_REFCONFIG_TYP_SUM);
										sumRefconfigMapsList.add(refConfigMapObject);
										for (Entry<String, RefConfigMappingObject> entry : map.entrySet()) 
										{
											RefConfigMappingObject value = entry.getValue();
											if(value!= null && value.getParentRefConfig() != null && value.getParentRefConfig().equals(refConfigMapObject.getRefConfig()))
											{
												sumRefconfigMapsList.add(value);
											}
										}

									}
									else if( refConfigMapObject.getParentRefConfigMapObj() != null)
									{
										ReaderSingleton.getReaderSingleton().setRefConfigurationType(PreferenceConstants.P_REFCONFIG_TYP_100);
										sumRefconfigMapsList.add(refConfigMapObject.getParentRefConfigMapObj()); 
										sumRefconfigMapsList.add(refConfigMapObject); 
									}

									/*if(rootObjClass.equals(IConstants.j0Montge))
									{
										ReaderSingleton.getReaderSingleton().setRefConfigurationType(PreferenceConstants.P_REFCONFIG_TYP_MONTAGE);
									}*/

									if( sumRefconfigMapsList.size() > 0)
									{
										for(int i = 0; i< sumRefconfigMapsList.size();i++)
										{
											sumRefconfigMapsList.get(i).print();
										}
										ReaderSingleton.getReaderSingleton().setSumRefConfigMapObjs(sumRefconfigMapsList);
									}
								}
								else
								{
									logger.error(" Reference Configuration Name : "+refConfigName+" is not defined in .csv file....");
									System.exit(1);

								}
							}
						}
					}
				}
				else
				{
					logger.error(" .csv file is missing.. Please check "+Arguments.CSV_MAP_FILE+" Argument......");
					System.exit(1);
				}
				
				

			}
		}

		// Truck implementation to introduce configuration items into the structure hierarchy.
		if(ReaderSingleton.getReaderSingleton().getFzgTypeName() != null) {
			if(ReaderSingleton.getReaderSingleton().getFzgTypeName().equals(PreferenceConstants.P_FZG_TYP_LKW)) {

				// loop to get the project name for the BCS elements
				if(roots1 != null) {
					if(!roots1.isEmpty() && roots1.get(0) != null) {
						if(roots1.get(0).getPartNumber() != null) {
							// sets the Truck project name in the singleton for the BCS elements in the DMU XML file
							if(ReaderSingleton.getReaderSingleton().getBcsProjectName() == null) {
								ReaderSingleton.getReaderSingleton().setBcsProjectName(roots1.get(0).getPartNumber());
							}
						}
					}
				}

				if(roots1 != null) {
					roots1  = PLMUtils.generateTruckHierarchy(roots1);
				}

				if(roots2 != null) {
					roots2  = PLMUtils.generateTruckHierarchy(roots2);
				}
			}
		}

		if (roots1 != null && !roots1.isEmpty() && roots2 != null && !roots2.isEmpty()) {
			TreeElement r1 = roots1.get(0);
			TreeElement r2 = roots2.get(0);
			if (r1.getLastModifDate().getTime() - r2.getLastModifDate().getTime() < 0) {
				throw new BatchException("Snapshot-1 modified date should be greater than Snapshot-2 modified date.");
			} else if ((r1.getRefConfigName() != null || r2.getRefConfigName() != null) && r1.getRefConfigName() != null
					&& !r1.getRefConfigName().equals(r2.getRefConfigName())) {
				throw new BatchException("RefConfigs don't match (r1:" + r1.getRefConfigName() + ", r2:"
						+ r2.getRefConfigName() + ".");
			} else {
				result = compareRoots(roots1, roots2);
				if(validateInputPLMXML(result)){
					writeImportXML(result, Mode.Delta, argumentsMap.get(Arguments.INPUT_ARG_1),
							argumentsMap.get(Arguments.INPUT_ARG_2));
				}
				else
				{
					//logger.error(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
					System.exit(1);
				}

			}

		} else if (roots1 != null && !roots1.isEmpty()) {
			writeImportXML(roots1, Mode.PLMXML1, argumentsMap.get(Arguments.INPUT_ARG_1), null);
		} else if (roots2 != null && !roots2.isEmpty()) {
			writeImportXML(roots2, Mode.PLMXML2, null, argumentsMap.get(Arguments.INPUT_ARG_2));
		}

		// prints out the object count information
		PLMUtils.printObjCountReport();
		
		ReaderSingleton.getReaderSingleton().writeOccConnReaderLogListToFile();
		ReaderSingleton.getReaderSingleton().writeOccConnWriterLogListToFile();
		ReaderSingleton.getReaderSingleton().writeOBIDActionListToFile();
		
		PLMUtils.printMissingOBID();
		
		//PLMUtils.printDBObjCountReport();
		// prints out the bom report
		// PLMUtils.printBOMReport();
	}

	

	/**
	 * Getting End item for processing send to package
	 * @throws IOException 
	 */
	/*public String getEndItemForSendToPackage(String plmxmlPath1) throws IOException{
		
		logger.info("******************* Reading .dat file.....********************************");
		System.out.println("******************* Reading .dat file.....********************************");
		logger.info("Reading END_ITEM_NAME from .dat file.. ");
		System.out.println("Reading END_ITEM_NAME from .dat file.. ");
		String refConfigName = null;
		String packageDirectory = null;
		String plmxmlFileName = null;

		if(plmxmlPath1.contains("/"))
		{
			packageDirectory =    plmxmlPath1.substring(0,plmxmlPath1.lastIndexOf("/"));
			plmxmlFileName = 	plmxmlPath1.substring(plmxmlPath1.lastIndexOf("/")+1);
		}
		else if(plmxmlPath1.contains("\\"))
		{
			packageDirectory =    plmxmlPath1.substring(0,plmxmlPath1.lastIndexOf("\\"));
			plmxmlFileName = 	plmxmlPath1.substring(plmxmlPath1.lastIndexOf("\\")+1);
		}

		if(plmxmlPath1 != null)
		{
			System.out.println("PLMXML File  : "+plmxmlPath1);
			logger.info("PLMXML File  : "+plmxmlPath1);
		}
		File dir = new File(packageDirectory);

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".dat");
			}
		};

		File[] datFile = dir.listFiles(filter);
		FileInputStream fileInput = null;
		try{
			if(datFile.length>0){
				//System.out.println("No.of .dat files : "+datFile.length);
				File datFileInstance = null;
				for (File file : datFile) {
					//System.out.println(".dat file : "+file.getName());
					if(file.getName().startsWith(plmxmlFileName.substring(0,plmxmlFileName.lastIndexOf("."))))
					{
						datFileInstance = file;
						break;
					}
				}
				if(datFileInstance == null)
				{
					System.out.println("Processing SendTo PLMXML : "+plmxmlPath1);
					System.out.println(".dat file missing. Reading Reference Configuration Name failed.. ");
					logger.error("Processing SendTo PLMXML : "+plmxmlPath1);
					logger.error(".dat file missing. Reading Reference Configuration Name failed.. ");
					return refConfigName;
				}
				System.out.println(".dat file Location : "+datFileInstance.getAbsolutePath());
				logger.info(".dat file Location : "+datFileInstance.getAbsolutePath());
				printDatFileContents(datFileInstance.getAbsolutePath());
				fileInput = new FileInputStream(datFileInstance);
				Properties properties = new Properties();
				properties.load(fileInput);
				refConfigName = (String) properties.get("ref_config_name");   
				logger.info("ref_config_name : "+refConfigName);  //mode
				
				String mode = (String) properties.get("mode");   
				logger.info("mode : "+mode);
				String user = (String) properties.get("user");   
				logger.info("user : "+user);
				
			}
			else
			{
				logger.error("Processing SendTo PLMXML : "+plmxmlPath1);
				logger.error(".dat file missing. Reading Reference Configuration Name failed.. ");
				System.exit(1);
			}
		}catch(Exception e){
			logger.error(e.getMessage());
		}finally{
			if(fileInput!=null){
				fileInput.close();
			}
		}
		logger.info("****************************** End of Reading .dat file.....***********************************");
		System.out.println("****************************** End of Reading .dat file.....***********************************");
		return refConfigName;

	}   */

	
	
	
	/**
	 * Getting End item for processing send to package
	 * @throws IOException 
	 */
	public HashMap<String, String> getEndItemForSendToPackage(String plmxmlPath1) throws IOException{
		
		logger.info("******************* Reading .dat file.....********************************");
		System.out.println("******************* Reading .dat file.....********************************");
		logger.info("Reading END_ITEM_NAME from .dat file.. ");
		System.out.println("Reading END_ITEM_NAME from .dat file.. ");
		String refConfigName = null;
		String packageDirectory = null;
		String plmxmlFileName = null;
		HashMap<String, String> dataFileProps = null;

		if(plmxmlPath1.contains("/"))
		{
			packageDirectory =    plmxmlPath1.substring(0,plmxmlPath1.lastIndexOf("/"));
			plmxmlFileName = 	plmxmlPath1.substring(plmxmlPath1.lastIndexOf("/")+1);
		}
		else if(plmxmlPath1.contains("\\"))
		{
			packageDirectory =    plmxmlPath1.substring(0,plmxmlPath1.lastIndexOf("\\"));
			plmxmlFileName = 	plmxmlPath1.substring(plmxmlPath1.lastIndexOf("\\")+1);
		}

		if(plmxmlPath1 != null)
		{
			System.out.println("PLMXML File  : "+plmxmlPath1);
			logger.info("PLMXML File  : "+plmxmlPath1);
		}
		File dir = new File(packageDirectory);

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".dat");
			}
		};

		File[] datFile = dir.listFiles(filter);
		FileInputStream fileInput = null;
		try{
			if(datFile.length>0){
				//System.out.println("No.of .dat files : "+datFile.length);
				File datFileInstance = null;
				for (File file : datFile) {
					//System.out.println(".dat file : "+file.getName());
					if(file.getName().startsWith(plmxmlFileName.substring(0,plmxmlFileName.lastIndexOf("."))))
					{
						datFileInstance = file;
						break;
					}
				}
				if(datFileInstance == null)
				{
					System.out.println("Processing SendTo PLMXML : "+plmxmlPath1);
					System.out.println(".dat file missing. Reading Reference Configuration Name failed.. ");
					logger.error("Processing SendTo PLMXML : "+plmxmlPath1);
					logger.error(".dat file missing. Reading Reference Configuration Name failed.. ");
					return dataFileProps;
				}
				dataFileProps = new HashMap<>();
				System.out.println(".dat file Location : "+datFileInstance.getAbsolutePath());
				logger.info(".dat file Location : "+datFileInstance.getAbsolutePath());
				printDatFileContents(datFileInstance.getAbsolutePath());
				fileInput = new FileInputStream(datFileInstance);
				Properties properties = new Properties();
				properties.load(fileInput);
				refConfigName = (String) properties.get("ref_config_name");  
				if(refConfigName != null)
				{
				dataFileProps.put("ref_config_name", refConfigName);
				}
				logger.info("ref_config_name : "+refConfigName);  //mode
				
				String mode = (String) properties.get("mode");   
				logger.info("mode : "+mode);
				if(mode != null)
				{
				dataFileProps.put("mode", mode);
				}
				String user = (String) properties.get("user");   
				logger.info("user : "+user);
				if(user != null)
				{
				dataFileProps.put("user", user);
				}
				
			}
			else
			{
				logger.error("Processing SendTo PLMXML : "+plmxmlPath1);
				logger.error(".dat file missing. Reading Reference Configuration Name failed.. ");
				System.exit(1);
			}
		}catch(Exception e){
			logger.error(e.getMessage());
		}finally{
			if(fileInput!=null){
				fileInput.close();
			}
		}
		logger.info("****************************** End of Reading .dat file.....***********************************");
		System.out.println("****************************** End of Reading .dat file.....***********************************");
		return dataFileProps;

	}   
	public void printDatFileContents(String fileName )
	{
		//String fileName = "/Users/pankaj/source.txt";
		 try {
	            // FileReader reads text files in the default encoding.
	            FileReader fileReader = 
	                new FileReader(fileName);

	            // Always wrap FileReader in BufferedReader.
	            BufferedReader bufferedReader = 
	                new BufferedReader(fileReader);

	            String line;
	            
	            
	            System.out.println("**********.dat file Contents ....********");
	            logger.info("**********.dat file Contents ....********");
				
				while((line = bufferedReader.readLine()) != null) {
	                System.out.println(line);
	                logger.info(line);
	            }   
				 System.out.println("******** End of file Contents .... ******");
				 logger.info("******** End of file Contents .... ******");
	            // Always close files.
	            bufferedReader.close();         
	        }
	        catch(FileNotFoundException ex) {
	            System.out.println(
	                "Unable to open file '" + 
	                fileName + "'");                
	        }
	        catch(IOException ex) {
	            System.out.println(
	                "Error reading file '" 
	                + fileName + "'");                  
	            // Or we could just do this: 
	            // ex.printStackTrace();
	        }
	}
	
	public int getMaxElemNum()
	{
		return maxElemNum;
	}

	public boolean validateInputPLMXML( List<TreeElement> roots )
	{
		boolean isValidInput = true;
		// Validating Version Number
		if( roots != null)
		{
			// Validating Pre Importer Version Number
			if(VersionInfo.getVersionNumber() == null)
			{
				isValidInput = false;
				logger.error("Pre Importer Error...............\n Pre Importer Version Number details missing. Please contact Administrator........ \n");
			}

			// Validating Reference Configuration or ENDITEM Name

			/*else if(roots.get(0).getRefConfigName() == null || getRefConfigName() == null)
			{
				isValidInput = false;
				 logger.error("Pre Importer Error...............\n Reference Configuration information is missing. Please validate the input PLMXML file ........ \n");
			}*/

			// Validating Root or Main Item PARTNUMBER

			else if(roots.get(0).getPartNumber() == null)
			{
				isValidInput = false;
				logger.error("Pre Importer Error...............\n Root Item PARTNUMBER missing.  Please validate the input PLMXML file ........ \n");
			}

		}

		else
		{
			isValidInput = false;
			logger.error("Pre Importer Error...............\n Root or Main Item missing. . Please validate the input PLMXML file ........ \n");
		}
		return isValidInput;
	}
}
