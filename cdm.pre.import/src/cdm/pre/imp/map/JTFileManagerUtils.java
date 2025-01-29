package cdm.pre.imp.map;

import java.util.Map;

/*******************************************************************************************************************************************************
 * @author kusaura
 * Purpose : Static Java Bean Class to map JT File name and corresponding item_id for renaming 
 * 
 * ====================================================================================================================================================
 * 		Modification Date                        Name                          Reason
 *   ========================        =========================          ===============================================================================
 *   2016/04/04                      Saurabh Kumar                      Initial file creation
 */

public class JTFileManagerUtils {
	/**
	 * Map to have JTFileName (ex. fms_dsafkjdfkweqiriuwe234324dfdsa.jt) and its corresponding original name (i.e item_id) like A2389020000.jt
	 */
	public static Map <String, String> jtFileNameMap = null;
	
	/**
	 * file location
	 */
	public static String plmXmlFileLoc = null;
	
	/**
	 * Is Send To Process or Downlaod Service
	 */
	public static boolean isSendToProcess =  false;
	
	/**
	 * Is Aston Martin or Downlaod Service
	 */
	public static boolean isAstonMartinDS =  false;
	
	/**
	 * Getting the Map <part file name, item_id> 
	 * @return Map
	 */
	public static Map<String, String> getPartFileNameMap(){
		return jtFileNameMap;
	}
	
	/**
	 * Setting the Map <part file name, item_id> 
	 * @param partFileNameMapParam
	 */
	public static void setJTFileNameMap(Map<String, String> jtFileNameMapParam){
		jtFileNameMap = jtFileNameMapParam;
	}
	
	/**
	 * Getting the xml file location
	 */
	public static String getPlmXmlFileLoc(){
		return plmXmlFileLoc;
	}
	
	/**
	 * Setting the plmxml file location
	 */
	public static void setPlmXmlFileLoc(String plmXmlFileLocParam){
		plmXmlFileLoc = plmXmlFileLocParam;
	}
	
	/**
	 * Getting Is Send to Process or download service
	 */
	public static boolean getIsSendToProcess(){
		return isSendToProcess;
	}
	
	/**
	 * Setting Is Send to Process or download service
	 */
	public static void setIsSendToProcess(boolean isSendToProcessArg){
		isSendToProcess = isSendToProcessArg;
	}
	
	/**
	 * Getting Aston Martin Datasupply option
	 */
	public static boolean getIsAstonMartinDS(){
		return isAstonMartinDS;
	}
	
	/**
	 * Setting Is Send to Process or download service
	 */
	public static void setIsAstonMartinDS(boolean isisAstonMartinDSArg){
		isAstonMartinDS = isisAstonMartinDSArg;
	}
}
