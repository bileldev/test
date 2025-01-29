package cdm.pre.imp.batch;

import java.util.ArrayList;
import java.util.List;

import cdm.pre.imp.prefs.PreferenceConstants;

public class Arguments {

   // private static String HELP_ARG_1 = "-help";
   public static final String        INPUT_ARG_1      	= "-s1";
   public static final String        INPUT_ARG_2      	= "-s2";
   public static final String        OUTPUT_ARG_1     	= "-o";
   public static final String 		 lOG_ARG_1		  	= "-log";
   public static final String 		 CONFIG_MAP_FILE	= "-configMapFile";
   public static final String 		 FAHRZEUG_TYPE		= "-fzgTyp";
   public static final String 		 INPUT_MODE			= "-mode";
   public static final String 		 CSV_MAP_FILE		= "-csvMapFile";
   //public statis final String 		 dbConnect

   private static final List<String> validArgs        = new ArrayList<String>();
   private static final List<String> validPreferences = new ArrayList<String>();
   private static final List<String> boolPreferences  = new ArrayList<String>();

   static 
   {
      validArgs.add(INPUT_ARG_1);
      validArgs.add(INPUT_ARG_2);
      validArgs.add(OUTPUT_ARG_1);
      validArgs.add(lOG_ARG_1);
      validArgs.add(CONFIG_MAP_FILE);
      validArgs.add(FAHRZEUG_TYPE);
      validArgs.add(INPUT_MODE);
      validArgs.add(CSV_MAP_FILE);
   };

   public static List<String> getValidpreferences() {
	return validPreferences;
   }
static {
      validPreferences.add(PreferenceConstants.P_IGNORE_INVALID);
      validPreferences.add(PreferenceConstants.P_DISABLE2D);
      validPreferences.add(PreferenceConstants.P_DISABLE_SECFOLD);
      validPreferences.add(PreferenceConstants.P_PATH_SRC);
      validPreferences.add(PreferenceConstants.P_PATH_DST);
      validPreferences.add(PreferenceConstants.P_IMPORTXML_FILE);
      validPreferences.add(PreferenceConstants.P_JT_ONLY);
      validPreferences.add(PreferenceConstants.P_STOP_CLASS);
      validPreferences.add(PreferenceConstants.P_ADD_REF_NAME_TO_PATH);
      validPreferences.add(PreferenceConstants.P_LOCAL_BULK_DIR_PATH);
      validPreferences.add(PreferenceConstants.P_ONLYGEOPOS);
      validPreferences.add(PreferenceConstants.P_AM_DATA_SUPPLY);
      validPreferences.add(PreferenceConstants.P_PROCESS_SEND2PACKAGE);
      validPreferences.add(PreferenceConstants.P_FZG_TYP_LKW);
      validPreferences.add(PreferenceConstants.P_FZG_TYP_PKW);
      validPreferences.add(PreferenceConstants.P_VEHICLE_TYP);
      validPreferences.add(PreferenceConstants.P_DBCONNECT_FILE);
      validPreferences.add(PreferenceConstants.P_DBCONNECT);
   }

   static {
      boolPreferences.add(PreferenceConstants.P_IGNORE_INVALID);
      boolPreferences.add(PreferenceConstants.P_DISABLE2D);
      boolPreferences.add(PreferenceConstants.P_DISABLE_SECFOLD);
      boolPreferences.add(PreferenceConstants.P_JT_ONLY);
      boolPreferences.add(PreferenceConstants.P_ADD_REF_NAME_TO_PATH);
      boolPreferences.add(PreferenceConstants.P_ONLYGEOPOS);
      boolPreferences.add(PreferenceConstants.P_AM_DATA_SUPPLY);
      boolPreferences.add(PreferenceConstants.P_PROCESS_SEND2PACKAGE);
      boolPreferences.add(PreferenceConstants.P_DBCONNECT);
   }

   public static boolean isValidArgument(String argumentName) {
      return validArgs.contains(argumentName);
   }

   public static boolean isValidPreference(String preference) {
      return validPreferences.contains(preference);
   }

   public static boolean isBooleanPreference(String preference) {
      return boolPreferences.contains(preference);
   }
   public static List<String> getValidArgs()
   {
	   return validArgs;
   }

}
