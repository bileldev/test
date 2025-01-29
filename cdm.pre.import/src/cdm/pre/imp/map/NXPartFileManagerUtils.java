/**
 * Static Java Bean Class to map Part File name and corresponding item_id for renaming 
 */
package cdm.pre.imp.map;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to provide bean for part file name mapping
 * @author THBALAJ
 *
 */
public class NXPartFileManagerUtils {
	/**
	 * Map to have partFileName (fms_dsafkjdfkweqiriuwe234324dfdsa.prt) and its corresponding original name (i.e item_id) like A2389020000.prt
	 */
	public static Map <String, String> partFileNameMap = null;
	
	/**
	 * Map to have NX drawing Item Id <element_id, item_id>
	 */
	public static Map <String, String> nxDrwgItemIdMap = new HashMap<String, String>();
	
	/**
	 * file location
	 */
	public static String plmXmlFileLoc = null;
	
	/**
	 * Is Send To Process or Downlaod Service
	 */
	public static boolean isSendToProcess =  false;
	
	/**
	 * Getting the Map <part file name, item_id> 
	 * @return Map
	 */
	public static Map<String, String> getPartFileNameMap(){
		return partFileNameMap;
	}
	
	/**
	 * Setting the Map <part file name, item_id> 
	 * @param partFileNameMapParam
	 */
	public static void setPartFileNameMap(Map<String, String> partFileNameMapParam){
		partFileNameMap = partFileNameMapParam;
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
	

}
