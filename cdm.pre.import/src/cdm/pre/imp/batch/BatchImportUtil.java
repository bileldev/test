package cdm.pre.imp.batch;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;*/
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import cdm.pre.imp.VersionInfo;
import cdm.pre.imp.configmap.MappingException;
import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.ReaderSingleton;

public class BatchImportUtil {


	public boolean              isBatchImport = false;
	protected Map<String, String> argumentsMap  = new HashMap<String, String>();
	protected final static Logger logger        = LogManager.getLogger("cdm.pre.imp.tracelog");
	protected static String configMapArgValue = null;

	public static void main(String args[]) throws MappingException, ParseException {
	
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime()));
		String logArgValue = null;
		
		for (int i = 0; i < args.length; i++) 
		{
			String[] argumentNameValue = args[i].split("=");
			if (argumentNameValue.length == 2) {
				if (argumentNameValue[0].equals("-log"))
				{
					logArgValue = argumentNameValue[1];//
					//PropertyConfigurator.configure(fis);
					Configurator.initialize(null, logArgValue);
				}
				else if (argumentNameValue[0].equals("-configMapFile"))
				{
					configMapArgValue = argumentNameValue[1];//
				}
			} 
		}

		if(logArgValue == null)
		{
			logger.error("-log argument missing. it is Mandtory argument for logging the trace.......");
		}
		if(configMapArgValue == null)
		{
			logger.error("-configMapFile argument missing. it is Mandtory for processing SmaPLMXML");
		}

		BatchImportUtil importUtil = new BatchImportUtil();
		importUtil.initializePreferences();
		try {
			importUtil.parseInputArguments(args);
		} catch (BatchException e) {
			logger.error(e.toString());
			importUtil.printUsage();
			System.exit(1);
		}
		IBatchImport batchImporter = BatchImport.getBatchImporter(importUtil.argumentsMap);
		try {
			logger.info("Generating Import XML file.....");
			// Amit - added a call to instantiate the ReaderSingleton class and populate the mapping information at session start
			// this will make the exit faster from the pre-importer in case there is an exception
			// this call sets the source file path to the reader singleton instance
			batchImporter.generateImportFile();
			logger.info("Import XML generated.....");
			
			if(ReaderSingleton.getReaderSingleton().getMappingInfoMap() != null) {
				System.out.println("Mapping info Map Size"+ReaderSingleton.getReaderSingleton().getMappingInfoMap().size());
			}
			
		} catch (Exception e) {
			logger.error("Error in wrting Intermediate XML.. Exiting");
			logger.error(e.toString());
			int rc = -1;
			if (e instanceof BatchException) {
				rc = -2;
			}
			System.exit(rc);
		}
		
		cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime()));
	}

	// private changed to protected by Krishna

	public void initializePreferences() {
		
		
		  String version = VersionInfo.getVersionNumber();
	         if(version == null)
	         {
	        	 version = "";
	        	 logger.warn("version number is not available. Please define Version.properties");
	         }
	         else
	         {
	        	 logger.info("Version Number : "+version);
	         }
	         
	         
	         String patch;
			try {
				patch = VersionInfo.getPatchNumber();
				  if(patch == null)
			         {
			        	 patch = "";
			        	// logger.warn("version number is not available. Please define Version.properties");
			         }
			         else
			         {
			        	 logger.info("Patch : "+patch);
			         }
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	       
	         try {
				String buildDate = VersionInfo.getBuildDate();
				String startDateString = buildDate;
				DateFormat df = new SimpleDateFormat("dd.MM.yyyy"); 
				Date startDate;
				try {
				    startDate = df.parse(startDateString);
				    String newDateString = df.format(startDate);
				    
				    final SimpleDateFormat df1         = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
				    newDateString =  df1.format(startDate);
				   // System.out.println(newDateString);
				    logger.info(" Build Date :: "+newDateString);
				} catch (ParseException e) {
				    e.printStackTrace();
				}

				
				/*System.out.println(" Build Date :: "+buildDate);
				 logger.info(" Build Date :: "+buildDate);*/
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		logger.info("Initializing Preferences..");
		argumentsMap.put(PreferenceConstants.P_DISABLE2D, "true");
		argumentsMap.put(PreferenceConstants.P_DISABLE_SECFOLD, "true");
		argumentsMap.put(PreferenceConstants.P_IGNORE_INVALID, "true");
		argumentsMap.put(PreferenceConstants.P_JT_ONLY, "false");
		argumentsMap.put(PreferenceConstants.P_PATH_SRC, "");
		argumentsMap.put(PreferenceConstants.P_PATH_DST, "");
		argumentsMap.put(PreferenceConstants.P_IMPORTXML_FILE, "");
		argumentsMap.put(PreferenceConstants.P_ADD_REF_NAME_TO_PATH, "false");
		argumentsMap.put(PreferenceConstants.P_LOCAL_BULK_DIR_PATH, "");
		argumentsMap.put(PreferenceConstants.P_STOP_CLASS, "");
		argumentsMap.put(PreferenceConstants.P_ONLYGEOPOS, "true");
		argumentsMap.put(PreferenceConstants.P_AM_DATA_SUPPLY, "false");
		argumentsMap.put(PreferenceConstants.P_PROCESS_SEND2PACKAGE, "false");
		argumentsMap.put(PreferenceConstants.P_CONFIG_MAPPING_FILE_PATH, configMapArgValue);

		PreImpConfig.getInstance(true, argumentsMap);
		logger.info("Initializing Preferences Done..");
	}

	// private changed to protected by Krishna

	protected void parseInputArguments(String args[]) throws BatchException {
		logger.info("Parsing Input Arguments..");
		String argumentNameValue[] = null;
		if (args.length <= 0)
			throw new BatchException("No argument given");
		for (int i = 0; i < args.length; i++) {
			argumentNameValue = args[i].split("=");
			//System.out.println(argumentNameValue[0] + "=" + argumentNameValue[1]);
			if (argumentNameValue.length == 2) {
				if (!Arguments.isValidArgument(argumentNameValue[0]) && !Arguments.isValidPreference(argumentNameValue[0])) {
					
					logger.error("[ERROR]: " + argumentNameValue[0] + " - is invalied arguments.");
					throw new BatchException("[ERROR]: " + argumentNameValue[0] + " - is invalied arguments.");
				} else if (argumentNameValue[1] == null || argumentNameValue[1].length() <= 0) {
					
					logger.error("[ERROR]: Please provide value for argument - " + argumentNameValue[0]);
					throw new BatchException("[ERROR]: Please provide value for argument - " + argumentNameValue[0]);
				}
			} else {
				
				if( argumentNameValue[0].equals(Arguments.INPUT_MODE)  || argumentNameValue[0].equals(Arguments.CSV_MAP_FILE))
				{
					continue;
				}
				logger.error("[ERROR]: " + argumentNameValue[0] + " - is blank. Please provide valid value.");

				throw new BatchException("[ERROR]: " + argumentNameValue[0] + " - is blank. Please provide valid value.");
			}
			if (Arguments.isBooleanPreference(argumentNameValue[0])) {
				if (!argumentNameValue[1].equalsIgnoreCase("true") && !argumentNameValue[1].equalsIgnoreCase("false")) {
					logger.error("[ERROR]: " + argumentNameValue[0] + " - please provide valid value (true/false).");

					throw new BatchException("[ERROR]: " + argumentNameValue[0] + " - please provide valid value (true/false).");
				}
			} else if (PreferenceConstants.P_STOP_CLASS.equals(argumentNameValue[0])) {
				String[] validStopClasses = { IConstants.j0SDPos, IConstants.j0SDPosV, IConstants.j0SDLage };
				boolean hasMatch = false;
				for (String stopClass : validStopClasses) {
					if (stopClass.equals(argumentNameValue[1])) {
						hasMatch = true;
						break;
					}
				}

				if (!hasMatch) {
					logger.error("[ERROR]: " + argumentNameValue[1] + " is not a valid stop class.");

					throw new BatchException("[ERROR]: " + argumentNameValue[1] + " is not a valid stop class.");
				}
			}
			argumentsMap.put(argumentNameValue[0], argumentNameValue[1]);
		}
		validateMandatoryArguments(); 
		if(argumentsMap != null && argumentsMap.size() > 0)
		{
			for (Entry<String, String> entry : argumentsMap.entrySet()) {
				if( entry.getValue() != null &&  !entry.getValue().equals(""))
				{
					logger.info(entry.getKey() + "----------" + entry.getValue());
				}
			}
		}
		logger.info("Parsing Input Arguments Done..");
	}

	public void validateMandatoryArguments() throws BatchException
	{
		List<String> validArgs = Arguments.getValidArgs();
		if(!validArgs.isEmpty())
		{
			for(int i = 0; i < validArgs.size(); i++)
			{
				if(!argumentsMap.containsKey(validArgs.get(i))&& !validArgs.get(i).equals(Arguments.INPUT_ARG_2))
				{
					if( validArgs.get(i).equals(Arguments.INPUT_MODE)  || validArgs.get(i).equals(Arguments.CSV_MAP_FILE))
					{
						continue;
					}
					logger.error("[ERROR]: Please provide value for argument - " + validArgs.get(i));
					//printUsage();
					throw new BatchException("[ERROR]: Please provide value for argument - " + validArgs.get(i));
				}
			}
		}
	}
	/**
	 * Prints the usage of the class.
	 */

	// private changed to protected by Krishna

	protected void printUsage() {
		System.out.println("Invalid Arguments to the Batch Utility.......\n");
		logger.warn("Invalid Arguments to the Batch Utility.......");
		System.out.println("Usage: java -jar -Dlog4j.configurationFile=<log4j2 configuration path> BatchImportUtil " + Arguments.INPUT_ARG_1 + "=<plmxml file1> " + Arguments.INPUT_ARG_2
				+ "=<plmxml file2> " + Arguments.OUTPUT_ARG_1 + "=<output xml file> "+ Arguments.lOG_ARG_1 + "=<log4j2 configuration path> "+ Arguments.CONFIG_MAP_FILE + "=<Coniguration Map xml file> "
				+ Arguments.FAHRZEUG_TYPE + "=<Vehicle Type>");
		
		logger.warn("Usage: java -jar -Dlog4j.configurationFile=<log4j2 configuration path> BatchImportUtil " + Arguments.INPUT_ARG_1 + "=<plmxml file1> " + Arguments.INPUT_ARG_2
				+ "=<plmxml file2> " + Arguments.OUTPUT_ARG_1 + "=<output xml file> "+ Arguments.lOG_ARG_1 + "=<log4j2 configuration path> "+ Arguments.CONFIG_MAP_FILE + "=<Coniguration Map xml file> "
				+ Arguments.FAHRZEUG_TYPE + "=<Vehicle Type>");
		
		System.out.println("\n\nwhere option include:\n");
		logger.warn("\n\nwhere option include:\n");
		
		System.out.println("\t" + Arguments.INPUT_ARG_1 + "=<snapshot plmxml file 1>");
		logger.warn("\t" + Arguments.INPUT_ARG_1 + "=<snapshot plmxml file 1>");
		
		System.out.println("\t" + Arguments.INPUT_ARG_2 + "=<snapshot plmxml file 2>");
		logger.warn("\t" + Arguments.INPUT_ARG_2 + "=<snapshot plmxml file 2>");
		
		System.out.println("\t" + Arguments.OUTPUT_ARG_1 + "=<output xml file>");
		logger.warn("\t" + Arguments.OUTPUT_ARG_1 + "=<output xml file>");
		
		//  discussed to be removed in next release of DiFa/AS-PLM
		System.out.println("\t" + Arguments.lOG_ARG_1 + "=<log4j2 configuration path>");
		logger.warn("\t" + Arguments.lOG_ARG_1 + "=<log4j2 configuration path>");
		
		System.out.println("\t" + Arguments.CONFIG_MAP_FILE + "=<Configuration Map xml file>");
		logger.warn("\t" + Arguments.CONFIG_MAP_FILE + "=<Configuration Map xml file>");
		
		System.out.println("\t" + Arguments.FAHRZEUG_TYPE + "=<Vehicle Type>  - valid values are xml_truck and xml_car");
		logger.warn("\t" + Arguments.FAHRZEUG_TYPE + "=<Vehicle Type>  - valid values are xml_truck and xml_car");
		
		/*System.out.println("\t" + Arguments.INPUT_MODE + "=<Mode>  - valid values are Override");
		logger.warn("\t" + Arguments.INPUT_MODE + "=<Mode>  - valid values are Override");*/
		
		System.out.println("Optional Preferences :: \n");
		logger.warn("Optional Preferences :: \n");
		
		System.out.println("\t" + PreferenceConstants.P_PATH_SRC + "=<source path>");
		logger.warn("\t" + PreferenceConstants.P_PATH_SRC + "=<source path>");
		
		System.out.println("\t" + PreferenceConstants.P_PATH_DST + "=<dest path>");
		logger.warn("\t" + PreferenceConstants.P_PATH_DST + "=<dest path>");
		
		System.out.println("\t" + PreferenceConstants.P_IGNORE_INVALID + "=<true/false>" + "\tIgnore invalid SmaDia2 eleents");
		logger.warn("\t" + PreferenceConstants.P_IGNORE_INVALID + "=<true/false>" + "\tIgnore invalid SmaDia2 eleents");
		
		System.out.println("\t" + PreferenceConstants.P_JT_ONLY + "=<true/false>" + "\tImport JTs Only");
		logger.warn("\t" + PreferenceConstants.P_JT_ONLY + "=<true/false>" + "\tImport JTs Only");
		
		System.out.println("\t" + PreferenceConstants.P_DISABLE2D + "=<true/false>" + "\tDisable 2D element processing");
		logger.warn("\t" + PreferenceConstants.P_DISABLE2D + "=<true/false>" + "\tDisable 2D element processing");
		
		System.out.println("\t" + PreferenceConstants.P_DISABLE_SECFOLD + "=<true/false>" + "\tDisable secondary folder processing");
		logger.warn("\t" + PreferenceConstants.P_DISABLE_SECFOLD + "=<true/false>" + "\tDisable secondary folder processing");
		
		System.out.println("\t" + PreferenceConstants.P_ADD_REF_NAME_TO_PATH + "=<true/false>"
				+ "\tAdd Reference Name to Bulk Data Path (LocalPDM Specific)");
		logger.warn("\t" + PreferenceConstants.P_ADD_REF_NAME_TO_PATH + "=<true/false>"
				+ "\tAdd Reference Name to Bulk Data Path (LocalPDM Specific)");
		
		System.out.println("\t" + PreferenceConstants.P_LOCAL_BULK_DIR_PATH + "=<directory path>" + "\tLocal Bulk Data Directory Path");
		logger.warn("\t" + PreferenceConstants.P_LOCAL_BULK_DIR_PATH + "=<directory path>" + "\tLocal Bulk Data Directory Path");
	}

	
}
