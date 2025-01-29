package cdm.pre.imp.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import cdm.pre.imp.app.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
    * initializeDefaultPreferences()
    */
   public void initializeDefaultPreferences() {
      IPreferenceStore store = Activator.getDefault().getPreferenceStore();
      store.setDefault(PreferenceConstants.P_DISABLE2D, true);
      store.setDefault(PreferenceConstants.P_DISABLE_SECFOLD, true);
      store.setDefault(PreferenceConstants.P_IGNORE_INVALID, true);
      store.setDefault(PreferenceConstants.P_JT_ONLY, false);
      store.setDefault(PreferenceConstants.P_PATH_SRC, "");
      store.setDefault(PreferenceConstants.P_PATH_DST, "");
   // Krishna : Set the default value as nothing
      // String path = null;
      String path = "";
    /*  URL url = Thread.currentThread().getContextClassLoader().getResource("conf/CDMImporter_DIFA_MapFile.xml");
      if( url != null){
    	  try 
 		 {
 			path = FileLocator.toFileURL(url).getFile();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		 if(path == null )
 		 {
 			 path = "";
 		 }
 		 else if( path.startsWith("/") && path.contains(":"))
 		 {
 			path=  path.substring(1, path.length());
 		 }
      }
      else
      {
    	  path = "";
      } */
		
			 
      store.setDefault(PreferenceConstants.P_CONFIG_MAPPING_FILE_PATH, path);
      store.setDefault(PreferenceConstants.P_IMPORTXML_FILE, "");
      store.setDefault(PreferenceConstants.P_ADD_REF_NAME_TO_PATH, false);
      store.setDefault(PreferenceConstants.P_LOCAL_BULK_DIR_PATH, "");
      store.setDefault(PreferenceConstants.P_ONLYGEOPOS, true);
      store.setDefault(PreferenceConstants.P_AM_DATA_SUPPLY, false);
	  store.setValue(PreferenceConstants.P_PROCESS_SEND2PACKAGE, false); // to add new preference for processing send2package and set its default value as false
	  // Krishna : Set the default value as nothing 
	   store.setDefault(PreferenceConstants.P_VEHICLE_TYP, PreferenceConstants.P_FZG_TYP_PKW);
	  //store.setDefault(PreferenceConstants.P_VEHICLE_TYP, "");
	   store.setDefault(PreferenceConstants.P_PROCESS_SEND2PACKAGE, false);
	   //store.setDefault(PreferenceConstants.P_WITH_END_ITEM, true);
   }

}
