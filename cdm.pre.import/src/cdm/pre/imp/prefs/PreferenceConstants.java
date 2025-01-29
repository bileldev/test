package cdm.pre.imp.prefs;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {
   public static final String P_IGNORE_INVALID       				= "ignoreInvalidPref";
   public static final String P_DISABLE2D            				= "disable2DPref";
   public static final String P_DISABLE_SECFOLD      				= "disableSecFoldPref";
   public static final String P_PATH_SRC             				= "pathSrcPref";
   public static final String P_PATH_DST             				= "pathDstPref";
   public static final String P_IMPORTXML_FILE       				= "importXMLFilePref";
   public static final String P_JT_ONLY              				= "jtOnlyPref";
   public static final String P_ADD_REF_NAME_TO_PATH 				= "addRefNametoPath";
   public static final String P_LOCAL_BULK_DIR_PATH  				= "localBulkDirPath";
   public static final String P_STOP_CLASS           				= "stopClassPref";
   public static final String P_ONLYGEOPOS			 				= "onlyGeoPos";
   public static final String P_AM_DATA_SUPPLY       				= "amgDataSuply";
   public static final String P_PROCESS_SEND2PACKAGE 				= "processSendToPackage"; 		// to add new preference for processing send2package
   public static final String P_CONFIG_MAPPING_FILE_PATH 			= "configMappingFilePath";
   public static final String P_FZG_TYP_LKW			 				= "xml_truck";					// preference for vehicle type Truck
   public static final String P_FZG_TYP_PKW			 				= "xml_car";					// preference for vehicle type Car
   public static final String P_VEHICLE_TYP           				= "vehicleTyp";
   public static final String P_REFCONFIG_TYP_SUM      				= "sum";
   public static final String P_REFCONFIG_TYP_100      				= "100";
   public static final String P_REFCONFIG_TYP_MONTAGE  				= "montage";
  //static final String P_WITH_END_ITEM			  					= "withEndItem";  // Associated preference for processSendToPackage. if true, reads End Item Name from .dat file, if false, skip reading END Irem Name from .dat file :: Allowed values are true or false
   public static final String P_MODE_SENDTO_1_WITH_REF_CONFIG		= "SENDTO_1";
   public static final String P_MODE_SENDTO_2_WITHOUT_REF_CONFIG	= "SENDTO_2";
   public static final String P_MODE_VARIANTS_CONFIG				= "xml_car_var";
   public static final String P_DBCONNECT	 						= "dbConnect";
   public static final String P_DBCONNECT_FILE 						= "dbConnectFile";
 
}
