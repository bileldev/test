package cdm.pre.imp.json.reader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cdm.pre.imp.Mode;
import cdm.pre.imp.XMLFileData;
import cdm.pre.imp.batch.Arguments;
import cdm.pre.imp.mod.TreeElement;
import cdm.pre.imp.mod.TreeElementFactoryFromJSON;
import cdm.pre.imp.mod.writer.XMLFileWriter;
import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.ReaderSingleton;


public class ExpandJsonParser {
	private final static Logger logger = LogManager.getLogger(ExpandJsonParser.class.getName());

	private static final String DMU_PKW = "DMU_PKW";

	public static final String JSON_FILE="employee.txt";

	//public static Sma4UJsonLogger SMA4U_LOGGER = null;

	public ArrayList<JSONElelment> jsonDataItems = new ArrayList<>();
	public ArrayList<JSONElelment> jsonObjectItems = new ArrayList<>();
	public HashMap<String, JSONElelment> objectsMap = null;
	public HashMap<String, JSONElelment> dataItemsMap = null;
	private String dbConfigFileLoc = null;
	private String propertyfileLoc = null;
	private boolean isDbConfigLocAvailable = false;

	private HashMap<Object, Object> propsMap;
	protected Map<String, String> argumentsMap  = new HashMap<String, String>();
	
	//private  Logger SLF4J_LOGGER;

	private String relClass;
	
	//HashMap<String, HashMap<Object,JSONElelment>> partWithRelCountMap = new HashMap<>();
	HashMap<Object, JSONElelment> partMap = new HashMap<>();


	//private String dbConfigFile;
	public static void main(String[] args) throws IOException 
	{
		String logArgValue = null;
		String[] argumentNameValue = args[6].split("=");
		if (argumentNameValue.length == 2) {
			if (argumentNameValue[0].equals("-log")) {
				logArgValue = argumentNameValue[1];//
				// PropertyConfigurator.configure(fis);
				Configurator.initialize(null, logArgValue);
			}
		}
		if (logArgValue == null) {
			logger.error("-log argument missing. it is Mandtory argument for logging the trace.......");
		}
		
		ExpandJsonParser expParser = new ExpandJsonParser();
		// getting parameters
		System.out.println(String.format("DB config file %s", args[0]) );
		String dbConfigFile= args[0];
		
		System.out.println(String.format("JSON file %s", args[1]) );
		String jsonfile = args[1];
		
		System.out.println(String.format("Outout file directory location %s", args[2]) );
		String outputdir = args[2];
		
		System.out.println(String.format("Output file name %s", args[3]) );
		String outputfileName = args[3];
		
		System.out.println(String.format("Mapping file path %s", args[4]) );
		String mappingFilePath = args[4];
		
		System.out.println(String.format("EndItem %s", args[5]) );
		String endItem = args[5];
		
		System.out.println(String.format("Property file %s", args[7]) );
		String propertyfile = args[7];
		
		//String propertyfileLocation = "D:\\AS-PLM\\Tests\\PreImporter\\conf\\preImporterPropertiesFile\\preimporter.properties";
		expParser.setDbConfigFileLoc(dbConfigFile);
		expParser.setPropertiesFileLoc(propertyfile);
		expParser.parseJsonResponse(jsonfile, outputdir, outputfileName, mappingFilePath, endItem); 

		//expParser.readValueFromEnvVariable("CONFIG_LOCATION");
		////expParser.setDbConfigFileLoc("D:\\ASPLM\\1.9.1_p001\\TCDBConfig2.txt");
		//expParser.parseJsonResponse("D:\\ASPLM\\Sma4U\\development\\issue\\SMA-37\\1677760526363_048c4db6-9b8c-48a2-9f25-7c3891410c83_INT_dosnot_work2.json","D:\\ASPLM\\Sma4U\\output","sampleInt_123.xml","D:\\ASPLM\\1.9.5\\deployment\\conf\\CDMImporter_ASPLM_MapFile.xml"); 
		
		////expParser.parseJsonResponse("D:\\ASPLM\\Sma4U\\development\\issue\\SMA-111\\425e9dc4-f30d-4c68-8ba7-013cb929d2e6_577b20d4-b026-4517-9e14-475af6ddf30e_expand.json","D:\\ASPLM\\Sma4U\\output","425e9dc4-f30d-4c68-8ba7-013cb929d2e6_577b20d4-b026-4517-9e14-475af6ddf30e_expand_4.xml","D:\\ASPLM\\Sma4U\\development\\Config\\CDMImporter_ASPLM_MapFile.xml", ""); 
		
		
		//expParser.parseJsonResponse("D:\\ASPLM\\Sma4U\\development\\issue\\SMA-37\\1677674964234_cfa0511d-a95e-45d1-8450-cd7db111aff0_T3_works.json","D:\\ASPLM\\Sma4U\\output","1677674964234_cfa0511d-a95e-45d1-8450-cd7db111aff0_T3_works.xml","D:\\ASPLM\\1.9.5\\deployment\\conf\\CDMImporter_ASPLM_MapFile.xml"); 
		
		//expParser.parseJsonResponse("D:\\ASPLM\\Sma4U\\development\\issue\\output\\krishna\\32d12926-05cc-4f4d-b701-9c913597b789_53d98952-4fc4-4a86-a611-7fca6687eb40.json","D:\\ASPLM\\Sma4U\\output","expand_20230511.xml","D:\\ASPLM\\1.9.5\\deployment\\conf\\CDMImporter_ASPLM_MapFile.xml"); 
		
		
		//expParser.parseJsonResponse("D:\\ASPLM\\Sma4U\\development\\issue\\coderule\\expand.json","D:\\ASPLM\\Sma4U\\output","expand_20230511.xml","D:\\ASPLM\\1.9.5\\deployment\\conf\\CDMImporter_ASPLM_MapFile.xml"); 
		//expParser.parseJsonResponse("D:\\ASPLM\\Sma4U\\development\\issue\\SMA-37\\1676361482607_4016a79e-923d-4fb8-b491-24c7ee058179.json","D:\\ASPLM\\Sma4U\\output","1676361482607_4016a79e-923d-4fb8-b491-24c7ee058179.xml","D:\\ASPLM\\1.9.5\\deployment\\conf\\CDMImporter_ASPLM_MapFile.xml"); 
		
		
		
		//expParser.parseJsonResponse("D:\\ASPLM\\Sma4U\\Krishna\\expand\\15112022\\1668419625590_9acacc88-43f5-4150-8ee1-bd2a51de1508_expand.json","D:\\ASPLM\\Sma4U\\output","sampleInt.xml","D:\\ASPLM\\1.9.5\\deployment\\conf\\CDMImporter_ASPLM_MapFile.xml","D:\\ASPLM\\1.9.1_p001\\TCDBConfig2.txt"); 
		//expParser.parseJsonResponse("D:\\ASPLM\\Sma4U\\Krishna\\expand\\test_new_28thSep.json"); 
		//expParser.parseJsonResponse("D:\\ASPLM\\Sma4U\\Krishna\\expand\\data1\\json\\a393814d-c3d7-47f4-9e5a-8c2898d31658.json");

		//expParser.parseJsonResponse("D:\\ASPLM\\Sma4U\\Krishna\\expand\\data2\\json2\\json\\0f9dae8f-42c7-4621-b1e2-0e71051dfa1b.json");

	}


	
	public void parseJsonResponse(String filePath, String pkgLoc, String intermediateFileName, String configMapXMLFile, String endItem) {
		parseJsonResponse(filePath, pkgLoc, intermediateFileName, configMapXMLFile, endItem, true, true);
	}

	public void parseJsonResponse(String filePath, String pkgLoc, String intermediateFileName, String configMapXMLFile, String endItem, boolean isManageDBConnection) {
		parseJsonResponse(filePath, pkgLoc, intermediateFileName, configMapXMLFile, endItem, true, isManageDBConnection);
	}
	
	public void parseJsonResponse(String filePath, String pkgLoc, String intermediateFileName, String configMapXMLFile,
			String endItem, boolean convertJSONModel, boolean isManageDBConnection) {

		
		logger.info("SMA4U Pre-Importer Version : 0.2.10");
		logger.info("SMA4U Pre-Importer Date : 22.11.2023 ");
		logger.info("Expand JSON Response : " + filePath);
		logger.info("Package Location :  " + pkgLoc);
		
		logger.info("Debug level activated ");
		
		logger.debug("Intermediate XML file Name " + intermediateFileName);

		if (filePath == null || filePath.isEmpty() || !new File(filePath).exists()) {
			logger.error("Failure to open json file {}", filePath);
			return;
		}

		// SMA4U_LOGGER.writeToLogFile("Configuration Mapping XMLFile Location :
		// "+configMapXMLFile, "INFO");

		// Reading dbconnection.properties
		// SMA4U_LOGGER.writeToLogFile("Searching for dbconnection.properties in
		// CONFIG_LOCATION .... ", "INFO");
		logger.info("Searching for  dbconnection.properties in CONFIG_LOCATION .... ");
		String dbConPropertiesFile = Utils.readValueFromEnvVariable("CONFIG_LOCATION", "dbconnection.properties");
		if (dbConPropertiesFile != null) {
			setDbConfigFileLoc(dbConPropertiesFile);
		}

		// Reading preimporter.properties
		// SMA4U_LOGGER.writeToLogFile("Searching for preimporter.properties in
		// CONFIG_LOCATION .... ", "INFO");
		
		
		logger.info("Searching for  preimporter.properties in CONFIG_LOCATION .... ");
		String preImporterPropertiesFile = Utils.readValueFromEnvVariable("CONFIG_LOCATION", "preimporter.properties");
		if ( preImporterPropertiesFile != null) {
			setPropertiesFileLoc(preImporterPropertiesFile);
		}
		if (propertyfileLoc != null) {
			// setDbConfigFileLoc(preImporterPropertiesFile);
			// SMA4U_LOGGER.writeToLogFile("Searching for preimporter.properties in
			// CONFIG_LOCATION .... ", "INFO");
			propsMap = readPreImportProps(propertyfileLoc);

			
			if (propsMap != null && propsMap.size() > 0) {
				parsePreImportProps(propsMap);
				// SMA4U_LOGGER.writeToLogFile("Reading Pre - Importer preferences .... ",
				// "INFO");
				logger.info("Reading Pre - Importer preferences .... ");

				propsMap.entrySet().forEach(entry -> {
					// System.out.println(entry.getKey() + " = " + entry.getValue());
					// SMA4U_LOGGER.writeToLogFile(entry.getKey() + " = " + entry.getValue() ,
					// "INFO");
					logger.info(entry.getKey() + " = " + entry.getValue());

				});
			}

		}
		//else { // just for local debugging purpose
		//	BatchImportUtil importUtil = new BatchImportUtil();
		//	importUtil.initializePreferences();
		//}
		
		JSONParser parser = new JSONParser();

		// try {
		// try {
		// SMA4U_LOGGER.writeToLogFile("Processing Expand JSON Response : ", "INFO");
		logger.info("Processing Expand JSON Response : ");
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filePath));
		} catch (IOException | ParseException e) {
			logger.error("Error while parsing json file {}", filePath, e);
			e.printStackTrace();
		}
		if (obj == null) {
			logger.debug("JSON parser return null object ");
			logger.error("Error while parsing json file {}", filePath);
			return;
		}

		JSONObject jsonObject = (JSONObject) obj;
		String rootId = (String) jsonObject.get("rootId");
		if (rootId == null) {
			logger.error("Root id is null ");
			return;
		}

		JSONObject objects = (JSONObject) jsonObject.get("objects");
		JSONObject dataitems = (JSONObject) jsonObject.get("dataItems");

		// process objects from json output
		if (objects != null) {
			// SMA4U_LOGGER.writeToLogFile("Processing Objects .... ", "INFO");
			logger.info("Processing Objects .... ");
			processDataItems(objects, jsonObjectItems);
			objectsMap = new HashMap<>();
			objectsMap = processDataItems(objects, objectsMap);
		}

		// process data Objects from json output
		if (dataitems != null) {
			// SMA4U_LOGGER.writeToLogFile("Processing Data Objects .... ", "INFO");
			logger.info("Processing Data Objects .... ");
			// System.out.println("*********************************************Processing
			// Data Objects ....");
			processDataItems(dataitems, jsonDataItems);
			dataItemsMap = new HashMap<>();
			dataItemsMap = processDataItems(dataitems, dataItemsMap);
		}

		ExpandOutputJsonObject expOutJsonObj = new ExpandOutputJsonObject();
		expOutJsonObj.setRootId(rootId);
		expOutJsonObj.setObjects(jsonObjectItems);
		expOutJsonObj.setDataObjects(jsonDataItems);
		
		isDbConfigLocAvailable = dbConfigFileLoc != null && !dbConfigFileLoc.equals("");
		
		if (isManageDBConnection)
			Utils.startDatabaseConnection(dbConfigFileLoc);
		
		try {

		System.out.println("*******************************************************************");
		JSONTreeElement treeStruct = createStructure(expOutJsonObj);
		// printTreeStruct(treeStruct);
		treeStruct = addDataItemsToTreeStruct(treeStruct, expOutJsonObj);
		// processDataItemsForConnections(treeStruct,expOutJsonObj);
		// System.out.println("*******************************************************************");
		// printTreeStruct(treeStruct);
		// addDataItemsToTreeStruct(treeStruct,expOutJsonObj);
		// printTreeStruct(treeStruct);

		if (convertJSONModel) {
			// Preliminar seettingd needed for the later processing of the common data model
			// generation
			ReaderSingleton.getReaderSingleton().setFzgTypeName(PreferenceConstants.P_FZG_TYP_PKW);

			// calling Mapping XML....
			ReaderSingleton.getReaderSingleton().readMappingFile(configMapXMLFile);

			System.out.println("** Convert JSON model to PLMXML model **");
			logger.debug("** Convert JSON model to PLMXML model **");
			List<TreeElement> roots;
			try {
				roots = TreeElementFactoryFromJSON.createStructure(false, false, treeStruct, endItem);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Failure when converting JSON model", e);
				e.printStackTrace();
				return;
			}
			if (roots == null || roots.isEmpty() || roots.get(0) == null) {
				logger.error("Root Part not found or invalid.");
				return;
			}

			// Further handle the converted model...
			
			// Needed for the expession logic in the writer
			ReaderSingleton.getReaderSingleton().setRootPartnumber(roots.get(0).getPartNumber());
			logger.info("Root Part Number : {} ", roots.get(0).getPartNumber());

			// Write the intermediate XML
			Path destinationPath = Paths.get(pkgLoc + File.separator + intermediateFileName);
			XMLFileData xmlFileData = new XMLFileData(roots.get(0), destinationPath.toString(), endItem,
					new File(filePath), null, Mode.PLMXML1);

			// set endItem
			ReaderSingleton.getReaderSingleton().setEndItem(xmlFileData.getEndItem());

			try {
				new XMLFileWriter().writeItem(roots.get(0), xmlFileData, null, true);
				
				roots.get(0).writeItemXMLFile(xmlFileData, null, true);
			} catch (Exception e) {
				logger.error("Failure when writing intermedaite XML file", e);
				return;
			}

			
			// Setting the input mode
			ReaderSingleton.getReaderSingleton().setInputMode(rootId);

			// writeImportXML(roots, Mode.JSON, argumentsMap.get(Arguments.INPUT_ARG_1),
			// null);

		} else {
			JsonResponseIntXMLWriter instance = new JsonResponseIntXMLWriter();
			// instance.setSma4ULogger(//SMA4U_LOGGER);
			// instance.setSLF4J_LOGGER(SLF4J_LOGGER);
			instance.setDbConfigLocAvailable(isDbConfigLocAvailable);
			// instance.write( "D:\\ASPLM","sampleInt.xml",expOutJsonObj,treeStruct);
			if (endItem == null || endItem.isEmpty()) {
				endItem = DMU_PKW;
			}

			instance.write(pkgLoc, intermediateFileName, configMapXMLFile, endItem, expOutJsonObj, treeStruct);
			instance.renameIntermediateXml(pkgLoc, intermediateFileName);
			// moveJsonFiletoPkgLoc(pkgLoc,filePath);
		}
		
	} finally {
		if (isManageDBConnection) {
			// Close DB Connection
			try {
				if (ReaderSingleton.getReaderSingleton().getDbConnBroker() != null) {
					ReaderSingleton.getReaderSingleton().getDbConnBroker().manageDBConnect(true);
				}
			} catch (Exception e) {
				logger.error("Failure when closing DB connection", e);
			}
		}
	}

	}

	public void parsePreImportProps(HashMap<Object, Object> argsMap)
	{
		if(argsMap != null && argsMap.size() > 0)
		{
			// Vehicle Type is needed for the XML writer.
			String vehicleType = "";
			if(argsMap.get(PreferenceConstants.P_VEHICLE_TYP) == null)
			{
				vehicleType = PreferenceConstants.P_FZG_TYP_PKW;
			}
			else{
				vehicleType= (String) argsMap.get(PreferenceConstants.P_VEHICLE_TYP);
			}
			argumentsMap.put(Arguments.FAHRZEUG_TYPE, vehicleType);
			
			
			String disable2d = "";
			if(argsMap.get(PreferenceConstants.P_DISABLE2D) == null)
			{
				disable2d = "true";
			}
			else{
				disable2d= (String) argsMap.get(PreferenceConstants.P_DISABLE2D);
			}
			argumentsMap.put(PreferenceConstants.P_DISABLE2D, disable2d);
			
			String DISABLE_SECFOLD = "";
			if(argsMap.get(PreferenceConstants.P_DISABLE_SECFOLD) == null)
			{
				DISABLE_SECFOLD = "true";
			}
			else{
				DISABLE_SECFOLD= (String) argsMap.get(PreferenceConstants.P_DISABLE_SECFOLD);
			}
			
			argumentsMap.put(PreferenceConstants.P_DISABLE_SECFOLD, DISABLE_SECFOLD);
			String IGNORE_INVALID = "";
			if(argsMap.get(PreferenceConstants.P_IGNORE_INVALID) == null)
			{
				IGNORE_INVALID = "true";
			}
			else{
				IGNORE_INVALID= (String) argsMap.get(PreferenceConstants.P_IGNORE_INVALID);
			}
			argumentsMap.put(PreferenceConstants.P_IGNORE_INVALID, IGNORE_INVALID);
			
			String JT_ONLY = "";
			if(argsMap.get(PreferenceConstants.P_JT_ONLY) == null)
			{
				JT_ONLY = "false";
			}
			else{
				JT_ONLY= (String) argsMap.get(PreferenceConstants.P_JT_ONLY);
			}
			argumentsMap.put(PreferenceConstants.P_JT_ONLY, JT_ONLY);
			argumentsMap.put(PreferenceConstants.P_PATH_SRC, "");
			argumentsMap.put(PreferenceConstants.P_PATH_DST, "");
			argumentsMap.put(PreferenceConstants.P_IMPORTXML_FILE, "");
			
			String ADD_REF_NAME_TO_PATH = "";
			if(argsMap.get(PreferenceConstants.P_ADD_REF_NAME_TO_PATH) == null)
			{
				ADD_REF_NAME_TO_PATH = "false";
			}
			else{
				ADD_REF_NAME_TO_PATH= (String) argsMap.get(PreferenceConstants.P_ADD_REF_NAME_TO_PATH);
			}
			argumentsMap.put(PreferenceConstants.P_ADD_REF_NAME_TO_PATH, ADD_REF_NAME_TO_PATH);
			argumentsMap.put(PreferenceConstants.P_LOCAL_BULK_DIR_PATH, "");
			argumentsMap.put(PreferenceConstants.P_STOP_CLASS, "");
			
			String ONLYGEOPOS = "";
			if(argsMap.get(PreferenceConstants.P_ONLYGEOPOS) == null)
			{
				ONLYGEOPOS = "true";
			}
			else{
				ONLYGEOPOS= (String) argsMap.get(PreferenceConstants.P_ONLYGEOPOS);
			}
			argumentsMap.put(PreferenceConstants.P_ONLYGEOPOS, ONLYGEOPOS);
			
			String AM_DATA_SUPPLY = "";
			if(argsMap.get(PreferenceConstants.P_AM_DATA_SUPPLY) == null)
			{
				AM_DATA_SUPPLY = "false";
			}
			else{
				AM_DATA_SUPPLY= (String) argsMap.get(PreferenceConstants.P_AM_DATA_SUPPLY);
			}
			argumentsMap.put(PreferenceConstants.P_AM_DATA_SUPPLY, AM_DATA_SUPPLY);
			
			String PROCESS_SEND2PACKAGE = "";
			if(argsMap.get(PreferenceConstants.P_PROCESS_SEND2PACKAGE) == null)
			{
				PROCESS_SEND2PACKAGE = "false";
			}
			else{
				PROCESS_SEND2PACKAGE= (String) argsMap.get(PreferenceConstants.P_PROCESS_SEND2PACKAGE);
			}
			argumentsMap.put(PreferenceConstants.P_PROCESS_SEND2PACKAGE, PROCESS_SEND2PACKAGE);
			argumentsMap.put(PreferenceConstants.P_CONFIG_MAPPING_FILE_PATH, "");

			PreImpConfig.getInstance(true, argumentsMap);
		}

	}
	

	
	public void printTreeStruct(JSONTreeElement treeStruct)
	{
		if(treeStruct != null)
		{
			System.out.println("Name : "+treeStruct.getDataElement().getName()+ "         ::::::::::  Object Type : "+treeStruct.getDataElement().getObjectType());
			if(treeStruct.getDataElement() != null && treeStruct.getDataElement().getRelAttributes() != null)
			{
				System.out.println("Rel Count : "+treeStruct.getDataElement().getRelAttributes().get("j0RelCount"));
			}
			if(treeStruct.getChildren() != null && treeStruct.getChildren().size() > 0)
			{
				for(int i = 0; i < treeStruct.getChildren().size(); i++)
				{
					printTreeStruct(treeStruct.getChildren().get(i));
				}
			}
			System.out.println(":::::::::::End of tree Struct : "+treeStruct.getDataElement().getName());
		}
	}

	public void printJsonObject(ExpandOutputJsonObject jsonOutputObj)
	{
		if(jsonOutputObj != null)
		{
			
			System.out.println("***********************************JSON OUTPUT ***************************************************");
			System.out.println("Root ID : "+jsonOutputObj.getRootId());
			
			ArrayList<JSONElelment> objects = jsonOutputObj.getObjects();
			if(objects != null && objects.size() > 0)
			{
				System.out.println("------------------------ Objects-------------------");
				for (JSONElelment jsonElelment : objects) 
				{
					System.out.println("ID ::: "+jsonElelment.getId());
					System.out.println("Name ::: "+jsonElelment.getName());
					System.out.println("Class 1:: "+jsonElelment.getAttributes().get("Class"));
					System.out.println("Class 2:: "+jsonElelment.getRelAttributes().get("Class"));
					if(jsonElelment.getChildren().size() > 0)
					{
						System.out.println("------------------------ Children-------------------");
						ArrayList<JSONElelment> children = jsonElelment.getChildren();
						for (JSONElelment child : children) {
							System.out.println("ID ::: "+child.getId());
							System.out.println("Part ID ::: "+child.getPartId());
							System.out.println("Class 1:: "+child.getAttributes().get("Class"));
							System.out.println("Class 2:: "+child.getRelAttributes().get("Class"));
						}
						System.out.println("------------------------ End of Children-------------------");
					}
				}
			}
			
			objects = jsonOutputObj.getDataObjects();
			if(objects != null && objects.size() > 0)
			{
				System.out.println("------------------------ DATA Objects-------------------");
				for (JSONElelment jsonElelment : objects) 
				{
					System.out.println("ID ::: "+jsonElelment.getId());
					System.out.println("Name ::: "+jsonElelment.getName());
					System.out.println("Class 1:: "+jsonElelment.getAttributes().get("Class"));
					System.out.println("Class 2:: "+jsonElelment.getRelAttributes().get("Class"));
					if(jsonElelment.getChildren().size() > 0)
					{
						System.out.println("------------------------ Children-------------------");
						ArrayList<JSONElelment> children = jsonElelment.getChildren();
						for (JSONElelment child : children) {
							System.out.println("ID ::: "+child.getId());
							System.out.println("Part ID ::: "+child.getPartId());
							System.out.println("Class 1:: "+child.getAttributes().get("Class"));
							System.out.println("Class 2:: "+child.getRelAttributes().get("Class"));
						}
						System.out.println("------------------------ End of Children-------------------");
					}
				}
			}
			
			System.out.println("------------------------ End of JSON output-------------------");
		}
	}
	
	public void moveJsonFiletoPkgLoc(String pkgLoc, String filepath)
	{
		// renaming file name to .xml_part to .xml
		Path sourcePath      =  Paths.get(filepath);
		////SMA4U_LOGGER.writeToLogFile(" Name :: "+sourcePath.getFileName());
		String name = sourcePath.getFileName().toString();
		String fName = null;
		if(name != null )
		{
			String[] nameSplit = name.split("\\.");

			if(nameSplit != null && nameSplit.length == 2)
			{
				fName = nameSplit[0];
				//SMA4U_LOGGER.writeToLogFile("File Name : "+fName);

				if(fName != null)
				{
					fName = fName+"_processed";
				}
				if(nameSplit[1] != null)
				{
					fName = fName+"."+nameSplit[1];
				}
			}
		}
		//Path destinationPath = Paths.get(pkgLoc+File.separator+sourcePath.getFileName());
		Path destinationPath = Paths.get(pkgLoc+File.separator+fName);

		try {
			Files.copy(sourcePath, destinationPath,
					StandardCopyOption.REPLACE_EXISTING);
			//SMA4U_LOGGER.writeToLogFile(" JSON response file copied  : "+pkgLoc+File.separator+fName,"INFO"); 
			logger.info(" JSON response file copied  : "+pkgLoc+File.separator+fName); 
			//  SMA4U_LOGGER.writeToLogFile(" JSON response file copied  : "+pkgLoc+File.separator+fName);
			/* Files.copy(sourcePath, destinationPath,
					            StandardCopyOption.REPLACE_EXISTING);*/
		} catch (IOException e) {
			//moving file failed.
			e.printStackTrace();
		}
	}
	public HashMap<String, JSONElelment> processDataItems(JSONObject dataitems, HashMap<String, JSONElelment> items )
	{
		//JSONObject dataitems = (JSONObject) jsonObject.get("dataItems");
		Set set = dataitems.keySet();//.entrySet();

		if(set != null && set.size() > 0)
		{
			//int i=1;
			for (Object object : set) 
			{
				//	SMA4U_LOGGER.writeToLogFile(object.toString());
				JSONObject jsonObj = (JSONObject) dataitems.get(object);

				if(jsonObj != null )
				{
					JSONElelment jElement = new JSONElelment();
					String name =  (String) jsonObj.get("name");
					if(name != null )
					{
						jElement.setName(name);
						//System.out.println("Parent Name : "+name);
					}
					String id =  (String) jsonObj.get("id");
					if(id != null )
					{
						jElement.setId(id);
					}
					String partid =  (String) jsonObj.get("partid");
					if(partid != null )
					{
						jElement.setPartId(partid);
					}
					Object children = jsonObj.get("children");
					
					JSONObject attributes = (JSONObject) jsonObj.get("attributes");
					if(attributes != null)
					{
						HashMap<String, Object> attributeMap = toMap(attributes);
						jElement.setAttributes(attributeMap);
						//SMA4U_LOGGER.writeToLogFile("Class Name : "+(String) attributes.get("Class"),"INFO");
					}
					JSONObject relAttributes = (JSONObject) jsonObj.get("relAttributes");
					if(relAttributes != null)
					{
						HashMap<String, Object> relAttributeMap = toMap(relAttributes);
						jElement.setRelAttributes(relAttributeMap);
						String relClass = (String) relAttributeMap.get("Class");
						/*if(relClass != null)
						{
							System.out.println("Relation Class in processDataItems : "+relClass);
						}*/
					}
					if(children != null )
					{
						// parseChildren
						processChildren(jElement, children);
					}
					items.put(id, jElement);
				}
			}
		}
		return items;
	}

	public ArrayList<JSONElelment> processDataItems(JSONObject dataitems, ArrayList<JSONElelment> items )
	{
		//JSONObject dataitems = (JSONObject) jsonObject.get("dataItems");
		Set set = dataitems.keySet();//.entrySet();

		if(set != null && set.size() > 0)
		{
			//int i=1;
			//System.out.println("Objects Size : "+set.size());
			for (Object object : set) 
			{
				//SMA4U_LOGGER.writeToLogFile(object.toString());
				JSONObject jsonObj = (JSONObject) dataitems.get(object);
				//SMA4U_LOGGER.writeToLogFile("*********** Start of JSON Object *************","INFO");
				if(jsonObj != null )
				{
					JSONElelment jElement = new JSONElelment();
					String name =  (String) jsonObj.get("name");
					if(name != null )
					{
						//SMA4U_LOGGER.writeToLogFile("Name : "+name,"INFO");
						jElement.setName(name);
						//System.out.println("Parent Name : "+name);
					}
					String id =  (String) jsonObj.get("id");
					if(id != null )
					{
						//SMA4U_LOGGER.writeToLogFile("ID : "+id,"INFO");
						jElement.setId(id);
					}
					
					String partid =  (String) jsonObj.get("partid");
					if(partid != null )
					{
						//SMA4U_LOGGER.writeToLogFile("Part ID   : "+partid,"INFO");
						jElement.setPartId(partid);
					}
				
					JSONObject attributes = (JSONObject) jsonObj.get("attributes");
					if(attributes != null)
					{
						HashMap<String, Object> attributeMap = toMap(attributes);
						jElement.setAttributes(attributeMap);
						//SMA4U_LOGGER.writeToLogFile("Class Name : "+(String) attributes.get("Class"),"INFO");
						
					}
					JSONObject relAttributes = (JSONObject) jsonObj.get("relAttributes");
					if(relAttributes != null)
					{
						HashMap<String, Object> relAttributeMap = toMap(relAttributes);
						jElement.setRelAttributes(relAttributeMap);
						//System.out.println("Rel Count : "+(String) relAttributeMap.get("j0RelCount"));

					}
					Object children = jsonObj.get("children");
					if(children != null )
					{
						// parseChildren
						processChildren(jElement, children);
					}
					items.add(jElement);
					//	SMA4U_LOGGER.writeToLogFile("***********************************End of Json Object :: *****************************","INFO");
				}
			}
		}
		return items;
	}

	public void processChildren(JSONElelment parentJElement, Object children)
	{
		ArrayList<JSONElelment> childrenList = new ArrayList<>();
		if(children instanceof JSONArray)
		{
			JSONArray childJsonArray =  (JSONArray) children;
			//SMA4U_LOGGER.writeToLogFile("Processing Children :: "+childJsonArray.size(),"INFO");
			//System.out.println("Processing Children");
			for(int i = 0; i < childJsonArray.size();i++)
			{
				JSONObject childJsonObj = (JSONObject) childJsonArray.get(i);
				if(childJsonObj != null && !((String) childJsonObj.get("id")).equals(parentJElement.getId()))
				{

					//SMA4U_LOGGER.writeToLogFile("++++++++++++++++++++++Start of Child Processing  :: ++++++++++++++++++++++","INFO");
					JSONElelment jElement = new JSONElelment();
					String name =  (String) childJsonObj.get("name");
					if(name != null )
					{
						//	SMA4U_LOGGER.writeToLogFile("Name : "+name,"INFO");
					//	System.out.println("Name : "+name);
						jElement.setName(name);
					}
					String id =  (String) childJsonObj.get("id");
					if(id != null )
					{
						//	SMA4U_LOGGER.writeToLogFile("ID : "+id,"INFO");
						jElement.setId(id);
					}

					String partid =  (String) childJsonObj.get("partid");
					if(partid != null )
					{
						//	SMA4U_LOGGER.writeToLogFile("Part ID : "+partid,"INFO");
						jElement.setPartId(partid);
					}
				
					JSONObject attributes = (JSONObject) childJsonObj.get("attributes");
					if(attributes != null)
					{
						HashMap<String, Object> attributeMap = toMap(attributes);
						jElement.setAttributes(attributeMap);
						//SMA4U_LOGGER.writeToLogFile("Class Name : "+(String) attributes.get("Class"),"INFO");
					}

					JSONObject relAttributes = (JSONObject) childJsonObj.get("relAttributes");
					if(relAttributes != null)
					{
						HashMap<String, Object> relAttributeMap = toMap(relAttributes);
						jElement.setRelAttributes(relAttributeMap);
					//	System.out.println("Rel Count : "+relAttributeMap.get("j0RelCount"));
						relClass = (String) relAttributeMap.get("Class");
						/*if(relClass != null)
						{
							System.out.println("Relation Class in processChildren : "+relClass);
						}*/

					}
					if(relClass != null && relClass.equals("j2PbiDtS"))
					{
						continue;
					}
					Object subchildren = childJsonObj.get("children");
					if(subchildren != null)
					{
						
						processChildren(jElement,subchildren);
					}

					childrenList.add(jElement);
					//SMA4U_LOGGER.writeToLogFile("++++++++++++++++++++++++++++++++++End of Child Processing  :: ++++++++++++++++++++++++++++++","INFO");
				}
			}
			//System.out.println("End of Processing Children");
			parentJElement.setChildren(childrenList);
		}

	}

	public static HashMap<String, Object> toMap(JSONObject attributesObj)  {
		HashMap<String, Object> attrMap = new HashMap<String, Object>();
		Set keys = attributesObj.keySet();//.entrySet();

		if(keys != null && keys.size() > 0)
		{
			for (Object key : keys) 
			{
				attrMap.put((String)key, attributesObj.get(key));
			}	
		}
		return attrMap;
	}


	public JSONElelment getObjectData(String partId, ArrayList<JSONElelment> jsonObjectsList)
	{
		if(partId != null && jsonObjectsList != null && jsonObjectsList.size() > 0)
		{
			for(int i = 0; i < jsonObjectsList.size(); i++)
			{
				if(jsonObjectsList.get(i) != null && jsonObjectsList.get(i).getId() != null && jsonObjectsList.get(i).getId().equals(partId))
			//	if(jsonObjectsList.get(i) != null && jsonObjectsList.get(i).getPartId() != null && jsonObjectsList.get(i).getPartId().startsWith(partId))
				{
					//SMA4U_LOGGER.writeToLogFile("Class Name : "+(String) jsonObjectsList.get(i).getAttributes().get("Class"),"INFO");
					if(!jsonObjectsList.get(i).isProcessedForStruct())
					{
						//System.out.println("getObjectData : ID : "+jsonObjectsList.get(i).getName());
						return jsonObjectsList.get(i);
					}
				}
			} 
		}
		return null;
	}
	

	public JSONElelment getChildObjectData(String id, ArrayList<JSONElelment> jsonObjectsList, String parentPartID)
	{
		if(id != null && jsonObjectsList != null && jsonObjectsList.size() > 0)
		{
			for(int i = 0; i < jsonObjectsList.size(); i++)
			{
				if(jsonObjectsList.get(i) != null && jsonObjectsList.get(i).getId() != null && jsonObjectsList.get(i).getId().equals(id))
				{
					return jsonObjectsList.get(i);
				}
			}
		}
		return null;
	}



	public JSONTreeElement createStructure(ExpandOutputJsonObject expOutJsonObject)
	{
		//SMA4U_LOGGER.writeToLogFile("Creating Assembly Structure..... ", "INFO");
		logger.info("Creating Assembly Structure..... ");
		JSONElelment rootObj = null;
		JSONTreeElement treeStruct = null ;
		if(expOutJsonObject != null)
		{
			//printJsonObject(expOutJsonObject);
			String rootId = expOutJsonObject.getRootId();

			if(rootId != null)
			{
				for(int i=0; i < expOutJsonObject.getObjects().size();i++)
				{
					rootObj = expOutJsonObject.getObjects().get(i);
					//SMA4U_LOGGER.writeToLogFile("Root ID : "+rootId);
					//SMA4U_LOGGER.writeToLogFile("Obj ID : "+rootObj.getId());
					if( rootObj != null && rootId.equals(rootObj.getId()))
					{
						//SMA4U_LOGGER.writeToLogFile("Class Name : "+(String) rootObj.getAttributes().get("Class"),"INFO");
						logger.info("Class Name : "+(String) rootObj.getAttributes().get("Class"));

						break;
					}
				}
				if(rootObj != null)
				{
					/*SMA4U_LOGGER.writeToLogFile("Root ID : "+rootObj.getId());
					SMA4U_LOGGER.writeToLogFile("Root Object Name : "+rootObj.getName());
					SMA4U_LOGGER.writeToLogFile("No of Children for the Root : "+rootObj.getChildren().size());*/

					
					
					treeStruct = new JSONTreeElement();
					treeStruct.setDataElement(rootObj);
					if(rootObj.getChildren().size() > 0)
					{
						for(int i=0; i<rootObj.getChildren().size();i++)
						{
							JSONElelment childObj = rootObj.getChildren().get(i);
							if(childObj != null && childObj.getId() != null)
							{
								JSONElelment childJsonElmObj = getObjectData(childObj.getPartId(), expOutJsonObject.getObjects());
								if(childJsonElmObj != null)
								{
									HashMap<String, Object> relAttributes = childObj.getRelAttributes();
									if(childObj.getRelAttributes().get("Class") != null && childObj.getRelAttributes().get("Class").equals("j2PbiDtS"))
										continue;
									childJsonElmObj.setRelAttributes(relAttributes);
									////SMA4U_LOGGER.writeToLogFile("child part id :: "+childObj.getPartId()+ "\n Part ID : "+childJsonElmObj.getId());
									JSONTreeElement childTreeElem = new JSONTreeElement();
									childTreeElem.setDataElement(childJsonElmObj);
									treeStruct.getChildren().add(childTreeElem);
									childTreeElem.setParent(treeStruct);
									childJsonElmObj.setProcessedForStruct(true);
									processChildrenForStructure(childJsonElmObj, expOutJsonObject, childTreeElem);


								}
							}
							/*else if(childObj != null && childObj.getPartId() == null )
							{
								String id = childObj.getId();
								if( id != null)
								{
									JSONElelment childJsonElmObj = getObjectData(childObj.getId(), expOutJsonObject.getDataObjects());
									if(childJsonElmObj != null)
									{
										HashMap<String, Object> relAttributes = childObj.getRelAttributes();
										childJsonElmObj.setRelAttributes(relAttributes);
										//SMA4U_LOGGER.writeToLogFile("child part id :: "+childObj.getPartId()+ "\n Part ID : "+childJsonElmObj.getId());
										JSONTreeElement childTreeElem = new JSONTreeElement();
										childTreeElem.setDataElement(childJsonElmObj);
										treeStruct.getChildren().add(childTreeElem);
										processChildrenForStructure(childJsonElmObj, expOutJsonObject, childTreeElem);


									}
								}
							}*/
							//if(rootObj.getId() != null)
							//{
								//treeStruct.setParentID(rootObj.getId());
								//treeStruct.setParent(rootObj);
							//}
//							if(rootObj.getPartId() != null)
//							{
//								treeStruct.setParentPartId(rootObj.getPartId());
//							}
						}

					}
					//	SMA4U_LOGGER.writeToLogFile("test");

				}
			}
		}
		return treeStruct;
	}


	public void processDataItemsForConnections(JSONTreeElement treeStruct, ExpandOutputJsonObject expOutJsonObj)
	{
		if(treeStruct != null && expOutJsonObj != null)
		{
			ArrayList<JSONElelment> dataObjects = expOutJsonObj.getDataObjects();
			if(dataObjects != null && dataObjects.size() > 0)
			{
				JSONElelment dataObject = null;
				JSONTreeElement dataObjectElement = null;
				for(int i =0;i < dataObjects.size();i++)
				{
					dataObject = dataObjects.get(i);
					if( dataObject != null)
					{
						dataObjectElement = getdataObjectElemFromTreeStruct(dataObject,treeStruct);
						if(dataObjectElement != null)
						{
							updateTreeWithDataItemChildren(dataObjectElement,dataObject);
						}
					}
				}
			}
		}
	}

	public JSONTreeElement getdataObjectElemFromTreeStruct(JSONElelment dataObject, JSONTreeElement treeStruct)
	{
		String dataObjID = null;
		JSONTreeElement dataObjectElement = null;
		if(dataObject.getId() != null)
		{
			dataObjID = dataObject.getId();
			if(treeStruct.getDataElement().getId().equals(dataObjID))
			{
				dataObjectElement =  treeStruct;
			}
			else 
			{
				for(int i=0; i<treeStruct.getChildren().size();i++)
				{
					dataObjectElement =	getdataObjectElemFromTreeStruct(dataObject, treeStruct.getChildren().get(i));
					if(dataObjectElement != null)
					{
						break;
					}
				}
			}
		}
		return dataObjectElement;
	}
	
	/*public void addDataItemsToTreeStruct(JSONTreeElement treeStruct, ExpandOutputJsonObject expOutJsonObj)
	{
		if(treeStruct != null && expOutJsonObj != null)
		{

			SMA4U_LOGGER.writeToLogFile("Writing Elements Section....","INFO"); 
			JSONElelment rJsonTreeElm = treeStruct.getDataElement();

			if(treeStruct.getChildren() != null && treeStruct.getChildren().size() > 0)
			{
				for(int i=0; i < treeStruct.getChildren().size();i++)
				{
					JSONTreeElement childTreeElm = treeStruct.getChildren().get(i);
					if(childTreeElm != null)
					{


						processDataItemChildren(childTreeElm,expOutJsonObj.getDataObjects());
					}
				}
			}

		}
	}*/

	
	public JSONTreeElement addDataItemsToTreeStruct(JSONTreeElement treeStruct, ExpandOutputJsonObject expOutJsonObj)
	{
		if(treeStruct != null && expOutJsonObj != null)
		{

			//SMA4U_LOGGER.writeToLogFile("Writing Elements Section....","INFO"); 
			logger.info("Writing Elements Section...."); 
			//JSONElelment rJsonTreeElm = treeStruct.getDataElement();
			ArrayList<JSONElelment> dataObjs = expOutJsonObj.getDataObjects();

			if(dataObjs != null && dataObjs.size() > 0)
			{
				for (JSONElelment jsonElelment : dataObjs) {
					if(jsonElelment != null && jsonElelment.getChildren().size() > 0)
					{
						processDataItemChildren(jsonElelment, treeStruct);
					}
				}
			}
		}
		return treeStruct;
	}
	
	public void processDataItemChildren(JSONElelment dataObj, JSONTreeElement treeStruct)
	{
		if(treeStruct !=null && dataObj != null)
		{
			//

			if(dataObj.getId().equals(treeStruct.getDataElement().getId()))
			{
				adddataItemChildrenToStruct(dataObj, treeStruct);
				return;
			}
			if (treeStruct.getChildren().size() > 0 )	
			{
				for(int i=0; i<treeStruct.getChildren().size();i++)
				{
					JSONTreeElement dItem = treeStruct.getChildren().get(i);
					processDataItemChildren(dataObj, dItem);
					
				}
			}
		}
	}
	
	public void adddataItemChildrenToStruct(JSONElelment dataObj, JSONTreeElement treeStruct) {
		if (treeStruct != null && dataObj != null) {
			
			//if (dataObj.getId() != null && dataObj.getId().equals("BddjTRewgut1-usr_wgubztC") ) {
			logger.trace("add data items to Object id: {}", dataObj.getId() );
			//}
			
			if (dataObj.getChildren().size() > 0) {
				for (int i = 0; i < dataObj.getChildren().size(); i++) {
					JSONElelment child = dataObj.getChildren().get(i);
					if (child != null) {
						JSONTreeElement childTreeElem = new JSONTreeElement();
						childTreeElem.setDataElement(child);
						treeStruct.getChildren().add(childTreeElem);
						childTreeElem.setParent(treeStruct);
						child.setProcessedForStruct(true);
						if (child.getChildren().size() > 0) {
							for (JSONElelment subChild : child.getChildren()) {
								adddataItemChildrenToStruct(subChild, childTreeElem);
							}
						}
					}
				}
			} else {
				JSONTreeElement childTreeElem = new JSONTreeElement();
				childTreeElem.setDataElement(dataObj);
				treeStruct.getChildren().add(childTreeElem);
				childTreeElem.setParent(treeStruct);
				dataObj.setProcessedForStruct(true);
			}
		}
	}
	
	
	public void processDataItemChildren(JSONTreeElement childTreeElm, ArrayList<JSONElelment> dataObjs)
	{
		if(childTreeElm !=null && dataObjs != null)
		{
			if (dataObjs.size() > 0 )	
			{
				for(int i=0; i<dataObjs.size();i++)
				{
					JSONElelment dItem = dataObjs.get(i);

					if(dItem !=null && dItem.getChildren().size()>0)
					{
						for(int k=0; k < dItem.getChildren().size();k++)
						{
							updateTreeWithDataItemChildren(childTreeElm,dItem.getChildren().get(k));
						}
					}
				}
			}
		}
	}

	public boolean isdItemChildAddedToStruct(JSONTreeElement childTreeElm, JSONElelment jsonElelment)
	{
		if(childTreeElm != null && childTreeElm.getChildren() != null)
		{
			for(int i=0; i<childTreeElm.getChildren().size();i++)
			{
				if(childTreeElm.getChildren().get(i).getDataElement().getId().equals(jsonElelment.getId()))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public JSONTreeElement getDItemChildAddedToStruct(JSONTreeElement childTreeElm, JSONElelment jsonElelment)
	{
		if(childTreeElm != null && childTreeElm.getChildren() != null)
		{
			for(int i=0; i<childTreeElm.getChildren().size();i++)
			{
				if(childTreeElm.getChildren().get(i).getDataElement().getId().equals(jsonElelment.getId()))
				{
					return childTreeElm.getChildren().get(i);
				}
			}
		}
		return null;
	}

	public void updateTreeWithDataItemChildren(JSONTreeElement childTreeElm, JSONElelment jsonElelment)
	{
		if(childTreeElm != null && jsonElelment != null)
		{
			/*if(jsonElelment.getAttributes().get("OBID") != null && jsonElelment.getAttributes().get("OBID").equals("Logzhyzeusr_wgub43357395"))
			{
				//System.out.println("Welcome:::: OBID :: ");
			}*/

			JSONTreeElement subChildTreeElem = null;
			if(!isdItemChildAddedToStruct(childTreeElm, jsonElelment) )
			{
				
					subChildTreeElem = new JSONTreeElement();
					subChildTreeElem.setDataElement(jsonElelment);
					childTreeElm.getChildren().add(subChildTreeElem);


					childTreeElm.getDataElement().getChildren().add(jsonElelment);
					if(jsonElelment.getChildren().size()> 0)
					{
						for(int i=0; i<jsonElelment.getChildren().size();i++)
						{
							subChildTreeElem = new JSONTreeElement();
							subChildTreeElem.setDataElement(jsonElelment.getChildren().get(i));
							childTreeElm.getChildren().add(subChildTreeElem);


							childTreeElm.getDataElement().getChildren().add(jsonElelment.getChildren().get(i));
							updateTreeWithDataItemChildren(childTreeElm,jsonElelment.getChildren().get(i));
						}
					}
			}
			else if(isdItemChildAddedToStruct(childTreeElm, jsonElelment))
			{
				subChildTreeElem = new JSONTreeElement();
				subChildTreeElem.setDataElement(jsonElelment);
				if(jsonElelment.getObjectType().equals("j2PbiDtP"))
				{
					//childTreeElm.getChildren().add(subChildTreeElem);

					if(jsonElelment.getChildren().size()> 0)
					{
						for(int i=0; i<jsonElelment.getChildren().size();i++)
						{
							updateTreeWithDataItemChildren(childTreeElm,jsonElelment.getChildren().get(i));
						}
					}
				}
				else
				{
					//childTreeElm.getChildren().add(subChildTreeElem);
					subChildTreeElem = getDItemChildAddedToStruct(childTreeElm, jsonElelment);
					if(jsonElelment.getChildren().size()> 0)
					{
						for(int i=0; i<jsonElelment.getChildren().size();i++)
						{
							updateTreeWithDataItemChildren(subChildTreeElem,jsonElelment.getChildren().get(i));
						}
					}
				}

			}

		}
	}
	
	public void processChildrenForStructure(JSONElelment childJsonElm, ExpandOutputJsonObject expOutJsonObject,JSONTreeElement childrenTreeElement)
	{
		//HashMap<String, HashMap<Object,JSONElelment>> partWithRelCountMap = new HashMap<>();
		
		//debug
		logger.debug("Start parsing json Object id: {} ...", childJsonElm.getId() );
		//String OBID = (String) childJsonElm.getAttributes().get("OBID");
		//if (OBID != null && (OBID.equals("usr_wgub0000000352954D92") || OBID.equals("usr_wgub00000003528C8BCD"))) {
		//if (rootObj.getId().equals("VngwMUBpbnN0YW5jZV4weDAwMDJfMDAwMT1aNlhXMTlGaHVjM0FKWU9VSDVVTHhHMVYzVVREaHlxQnZDWEZkekV6ZTdrY3lDWmMwdDBmR0ZGdFRMZlRpWHdfZTNMYkhiTW4xTEt6Zk5kNmxnay1MMHlnZkNuQVU4cVg2TFZyc3FGTUdjbi1fbWZLVm0ydV9KTWNLQmdNU0xodUdhY3VzOEZHV0pYNEVHcG9Mal9qczZHbkt5OTgwSjBxNTRoQzZoSmx3Yl9ENmN5d3hwV0J3NmRPUXhBNEJJVUZIRUhtN3NTallXdzY")) {
			//logger.trace("parse json Object id: {}", childJsonElm.getId() );
		//}
		
		if(childJsonElm.getChildren().size() > 0)
		{
			for(int i=0; i<childJsonElm.getChildren().size(); i++)
			{
				JSONElelment childObj = childJsonElm.getChildren().get(i);
				if(childObj != null && childObj.getPartId() != null)
				{
					//if (childObj.getPartId() != null && childObj.getPartId().equals("BddjTRewgut1-usr_wgubztC") ) {
						logger.trace("parse json child  Object id: {}", childObj.getPartId() );
					//}
					
					JSONElelment subChildJsonElmObj = getObjectData(childObj.getPartId(), expOutJsonObject.getObjects());
					if(subChildJsonElmObj != null)
					{
						HashMap<String, Object> relAttributes = childObj.getRelAttributes();
						if(relAttributes != null )
						{
							if(relAttributes.get("Class") != null && relAttributes.get("Class").equals("j2PbiDtS"))
								continue;
							
							//String partNumber = (String) childObj.getAttributes().get("PartNumber");
							//if(partNumber != null) {
							//	partMap.put(partNumber, subChildJsonElmObj);
							//}
							
							partMap.put(childObj.getPartId(), subChildJsonElmObj);
						}
						
						subChildJsonElmObj.setRelAttributes(relAttributes);
						////SMA4U_LOGGER.writeToLogFile("child part id :: "+childObj.getPartId()+ "\n Part ID : "+childJsonElmObj.getId());
						JSONTreeElement subChildTreeElem = new JSONTreeElement();
						subChildTreeElem.setDataElement(subChildJsonElmObj);
						childrenTreeElement.getChildren().add(subChildTreeElem);
						subChildTreeElem.setParent(childrenTreeElement);
						subChildJsonElmObj.setProcessedForStruct(true);
						processChildrenForStructure(subChildJsonElmObj, expOutJsonObject, subChildTreeElem);

					}
					else if(subChildJsonElmObj == null && childObj != null)
					{
						HashMap<String, Object> relAttributes = childObj.getRelAttributes();
						if(relAttributes != null )
						{
							//System.out.println("Rel Count : "+relAttributes.get("j0RelCount"));
							//Object relcnt = relAttributes.get("j0RelCount");
							//String partNumber = (String) childObj.getAttributes().get("PartNumber");
							//if(partNumber != null)
							//{
								if(partMap.get(childObj.getPartId()) != null)
								{
									JSONElelment childElem = partMap.get(childObj.getPartId());
									childObj.setAttributes(childElem.getAttributes());
									childObj.setChildren(childElem.getChildren());
									childObj.setId(childObj.getPartId());
									
									Object obid = childObj.getRelAttributes().get("Right");
									if(obid != null)
									{
										childObj.getAttributes().put("OBID", obid);
										childObj.getAttributes().put("Nomenclature", "clone");
										childObj.getAttributes().put("j0Nomenclature", "clone");
									}
									
									//subChildJsonElmObj = partMap.get(partNumber);
									
									//subChildJsonElmObj.setRelAttributes(relAttributes);
									
									JSONTreeElement subChildTreeElem = new JSONTreeElement();
									//subChildTreeElem.setDataElement(subChildJsonElmObj);
									subChildTreeElem.setDataElement(childObj);
									childrenTreeElement.getChildren().add(subChildTreeElem);
									subChildTreeElem.setParent(childrenTreeElement);
									
									processChildrenForStructure(childObj, expOutJsonObject, subChildTreeElem);
								}	
							//}
						}
					}
				}
			}

		}
	
		childrenTreeElement.getDataElement().setProcessedForStruct(true);
	}

	public JSONElelment getJsonElement(ArrayList<JSONElelment> items, String name)
	{
		JSONElelment jsonElem = null;
		if(items != null && items.size() > 0)
		{
			for(int i=0; i < items.size();i++)
			{
				jsonElem = items.get(i);
				if( name != null && name.equals(jsonElem.getName()))
				{
					break;
				}
			}
		}
		return jsonElem;

	}
	/*public static ArrayList<Object> toList(JSONArray array) {
	    List<Object> list = new ArrayList<Object>();
	    for(int i = 0; i < array.length(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        list.add(value);
	    }
	    return list;
	}*/


	public String getDbConfigFileLoc() {
		return dbConfigFileLoc;
	}


	public void setDbConfigFileLoc(String dbConfigFileLoc) {
		this.dbConfigFileLoc = dbConfigFileLoc;
	}
	
	public void setPropertiesFileLoc(String propertyfileLoc) {
		this.propertyfileLoc = propertyfileLoc;
	}
	
	public String getPropertyfileLoc() {
		return propertyfileLoc;
	}

	public HashMap<Object, Object> readPreImportProps(String preImporterPropFileName)
	{

		HashMap<Object, Object> propsMap = new HashMap<Object, Object>();

		FileInputStream fileInputStream = null;
		try 
		{
			if(preImporterPropFileName != null)
			{
				File propFileInstance = new File(preImporterPropFileName);
				try 
				{
					fileInputStream =  new FileInputStream(propFileInstance);
					Properties props = new Properties();

					props.load(fileInputStream);

					if(props != null)
					{
						props.forEach((key, value) -> propsMap.put(key , value));
					}
				} 
				catch (FileNotFoundException  e) 
				{
					//SMA4U_LOGGER.writeToLogFile("File Not Found..." , "ERROR");
					//SMA4U_LOGGER.writeToLogFile(e.getMessage() , "ERROR");
					
					logger.error("File Not Found..." );
					logger.error(e.getMessage() );
					e.printStackTrace();
				}
				catch (IOException e) 
				{
					logger.error("IO Exception..." );
					logger.error(e.getMessage() );
					
					//SMA4U_LOGGER.writeToLogFile("IO Exception..." , "ERROR");
					//SMA4U_LOGGER.writeToLogFile(e.getMessage() , "ERROR");
					e.printStackTrace();
				}
			} 
		} 
		finally
		{
			if (fileInputStream != null) 
			{
				try 
				{
					fileInputStream.close();
				} 
				catch (IOException e) 
				{
					//SMA4U_LOGGER.writeToLogFile("IO Exception..." , "ERROR");
					//SMA4U_LOGGER.writeToLogFile(e.getMessage() , "ERROR");
					
					logger.error("IO Exception..." );
					logger.error(e.getMessage() );
					e.printStackTrace();
				}
			}
		}

		return propsMap;

	}

}