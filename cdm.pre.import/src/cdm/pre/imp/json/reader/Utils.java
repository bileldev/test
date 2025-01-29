package cdm.pre.imp.json.reader;

import java.io.File;
import java.sql.SQLException;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.dbconnector.DBConnBroker;
import cdm.pre.imp.dbconnector.DBConnSingleton;
import cdm.pre.imp.dbconnector.DBConnUtilities;
import cdm.pre.imp.reader.ReaderSingleton;

public class Utils {
	final private static Logger LOGGER = LogManager.getLogger(Utils.class); 
	
	public static String listFilesForFolder(File folder) {

		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				// System.out.println("Reading files under the folder "+folder.getAbsolutePath());
				listFilesForFolder(fileEntry);
			} else {
				if (fileEntry.isFile()) {
					String dbConnectionFile = fileEntry.getName();
					if (dbConnectionFile != null && dbConnectionFile.equals("dbconnection.properties"))
					{
						dbConnectionFile = folder.getAbsolutePath()+ File.separator + fileEntry.getName();
						//SMA4U_LOGGER.writeToLogFile(("File= " + folder.getAbsolutePath()+  File.separator + fileEntry.getName()),"INFO");
						LOGGER.info(("File= " + folder.getAbsolutePath()+  File.separator + fileEntry.getName()));
						return dbConnectionFile;
					}
				}
			}
		}
		return null;
	}
	
	public static String listFilesForFolder(File folder, String fileName) {

		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				// System.out.println("Reading files under the folder "+folder.getAbsolutePath());
				listFilesForFolder(fileEntry);
			} else {
				if (fileEntry.isFile()) {
					String dbConnectionFile = fileEntry.getName();
					if (dbConnectionFile != null && dbConnectionFile.equals(fileName))
					{
						dbConnectionFile = folder.getAbsolutePath()+ File.separator + fileEntry.getName();
						////SMA4U_LOGGER.writeToLogFile(("File = " + folder.getAbsolutePath()+  File.separator + fileEntry.getName()),"INFO");
						LOGGER.info(("File = " + folder.getAbsolutePath()+  File.separator + fileEntry.getName()));
						return dbConnectionFile;
					}
				}
			}
		}
		return null;
	}
	
	public static String readValueFromEnvVariable(String envVarName, String fileName)
	{
		if(envVarName != null)
		{
			String dbConnectConfigDir = System.getenv(envVarName);
			//System.out.println("Config Folder : "+dbConnectConfigDir);
			if(dbConnectConfigDir != null)
			{
				File folder = new File(dbConnectConfigDir);
				if(folder !=null)
				{
					 if (folder.isDirectory()) 
					 {
						 String dbConficFile = listFilesForFolder(folder,fileName);
						 if(dbConficFile != null)
						 {
							// System.out.println(dbConficFile);
							 //SMA4U_LOGGER.writeToLogFile((" File Found :  "+dbConficFile),"INFO");
							 LOGGER.info((" File Found :  "+dbConficFile));
							 return dbConficFile;
						 }
						 else
						 {
							 //System.out.println(" No "+fileName+" file found on " + folder.getAbsolutePath());
							 //SMA4U_LOGGER.writeToLogFile((" No "+fileName+" file found on " + folder.getAbsolutePath()),"ERROR");
							 LOGGER.error((" No "+fileName+" file found on " + folder.getAbsolutePath()));
						 }
					 }
					 else
						{
							//SMA4U_LOGGER.writeToLogFile("Directory Not accessible or No Access Rights ","ERROR");
						 LOGGER.error("Directory Not accessible or No Access Rights ");
						}
				}
				
				
			}
		}
		else
		{
			//SMA4U_LOGGER.writeToLogFile(envVarName+" Environment Variable Not Found ","ERROR");
			LOGGER.error(envVarName+" Environment Variable Not Found ");
		}
		return null;
	}
	
	public static boolean startDatabaseConnection(String dbConfigFile)
	{
		if(dbConfigFile != null && !dbConfigFile.isEmpty())
		{			
			//SMA4U_LOGGER.writeToLogFile("READING DB CONFIG FILE : ","INFO");
			LOGGER.info("READING DB CONFIG FILE : "+dbConfigFile);
			
			//SMA4U_LOGGER.writeToLogFile("READING DB CONFIG FILE : ","INFO");
			LOGGER.info("DB CONFIG FILE : "+dbConfigFile);
			
			ReaderSingleton.getReaderSingleton().startDBConnBroker(dbConfigFile, LOGGER);
			
			if(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SID) != null)
			{
				//SMA4U_LOGGER.writeToLogFile("SID : "+DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SID),"INFO");
				LOGGER.info("SID : "+DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SID));
			}
			else if(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SERVICE) != null){
				//SMA4U_LOGGER.writeToLogFile("SERVICE : "+DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SERVICE),"INFO");
				LOGGER.info("SERVICE : "+DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SERVICE));
			}
			try
			{
				ReaderSingleton.getReaderSingleton().getDbConnBroker().manageDBConnect(false);
			} 
			catch (SQLException e) 
			{
				// TODO Auto-generated catch block
				LOGGER.error("EXCEPTION HAS BEEN ENCOUNTERED WHILE CREATING DB CONNECTION ", e);
			}
			
			if(DBConnSingleton.getInstance().getmLogObj() != null) 
			{
				DBConnSingleton.getInstance().getmLogObj().writeLogHeader();
			}

			ReaderSingleton.getReaderSingleton().setDbConnect(true);
			
			
			if(DBConnSingleton.getInstance().getmDBConnInst() == null)
			{
				//SMA4U_LOGGER.writeToLogFile("EXCEPTION HAS BEEN ENCOUNTERED WHILE CREATING DB CONNECTION ","INFO");
				LOGGER.info("EXCEPTION HAS BEEN ENCOUNTERED WHILE CREATING DB CONNECTION ");
			
				ReaderSingleton.getReaderSingleton().setDbConnect(false);
				return false;
			}else {
				return true;
			}
		}
		else
		{
			//SMA4U_LOGGER.writeToLogFile("DB ConfigPath missing.. Please provide 'dbConnectFile' argument ","ERROR");
			LOGGER.error("DB ConfigPath missing.. Please provide 'dbConnectFile' argument ");
			return false;
		}
		
	}
	
}
