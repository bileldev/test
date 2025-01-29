package cdm.pre.imp.prefs;

import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.jface.preference.IPreferenceStore;

import cdm.pre.imp.app.Activator;

/**
 * To configure the pre importer process.
 * 
 * @author wikeim
 * 
 */
public class PreImpConfig {
	private static PreImpConfig singleton;
	public Map<String, String>  argumentsMap = null;

	public PreImpConfig() {
	}

	private IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public boolean isIgnoreInvalidSmaDia() {
		return getPreferenceStore().getBoolean(PreferenceConstants.P_IGNORE_INVALID);
	}

	public boolean isDisable2D() {
		return isJTOnly() ? true : getPreferenceStore().getBoolean(PreferenceConstants.P_DISABLE2D);
	}

	public boolean isDisableSecFolder() {
		return isJTOnly() ? true : getPreferenceStore().getBoolean(PreferenceConstants.P_DISABLE_SECFOLD);
	}

	public boolean isOnlyGeoPos() {
		return getPreferenceStore().getBoolean(PreferenceConstants.P_ONLYGEOPOS);
	}

	public boolean isAMSupply() {
		return getPreferenceStore().getBoolean(PreferenceConstants.P_AM_DATA_SUPPLY);
	}

	public String getImportXMLFile() {
		return getPreferenceStore().getString(PreferenceConstants.P_IMPORTXML_FILE);
	}

	public String getSrcPath() {
		return getPreferenceStore().getString(PreferenceConstants.P_PATH_SRC);
	}

	public String getDstPath() {
		return getPreferenceStore().getString(PreferenceConstants.P_PATH_DST);
	}

	public String getConfigMappingFilePath() {
		return getPreferenceStore().getString(PreferenceConstants.P_CONFIG_MAPPING_FILE_PATH);
	}

	public boolean isJTOnly() {
		return getPreferenceStore().getBoolean(PreferenceConstants.P_JT_ONLY);
	}

	public boolean isProcessSendToPackage() {
		return new Boolean(argumentsMap.get(PreferenceConstants.P_PROCESS_SEND2PACKAGE));
	}
	/*public boolean isWithEndItem() {
		return new Boolean(argumentsMap.get(PreferenceConstants.P_WITH_END_ITEM));
	}*/
	public String getLocalBulkPath() {
		String localBulkPath = null;
		if (isRefNameIncluded()) {
			localBulkPath = getPreferenceStore().getString(PreferenceConstants.P_LOCAL_BULK_DIR_PATH);
		}
		return localBulkPath;
	}

	public boolean isRefNameIncluded() {
		return getPreferenceStore().getBoolean(PreferenceConstants.P_ADD_REF_NAME_TO_PATH);
	}

	public String getStopClass() {
		return getPreferenceStore().getString(PreferenceConstants.P_STOP_CLASS);
	}
	
	public String getVehicleType() {
		return getPreferenceStore().getString(PreferenceConstants.P_VEHICLE_TYP);
		
	}
	
	

	public void writeSettings(final XMLStreamWriter streamWriter) throws XMLStreamException {
		streamWriter.writeEmptyElement("settings");
		streamWriter.writeAttribute(PreferenceConstants.P_IGNORE_INVALID, Boolean.toString(isIgnoreInvalidSmaDia()));
		streamWriter.writeAttribute(PreferenceConstants.P_DISABLE2D, Boolean.toString(isDisable2D()));
		streamWriter.writeAttribute(PreferenceConstants.P_DISABLE_SECFOLD, Boolean.toString(isDisableSecFolder()));
		streamWriter.writeAttribute(PreferenceConstants.P_JT_ONLY, Boolean.toString(isJTOnly()));
		streamWriter.writeAttribute(PreferenceConstants.P_ADD_REF_NAME_TO_PATH, Boolean.toString(isRefNameIncluded()));
		streamWriter.writeAttribute(PreferenceConstants.P_ONLYGEOPOS, Boolean.toString(isOnlyGeoPos()));
		streamWriter.writeAttribute(PreferenceConstants.P_VEHICLE_TYP, getVehicleType());
	}

	public void setArgumentsMap(Map<String, String> argumentsMap) {
		this.argumentsMap = argumentsMap;
	}

	public synchronized static PreImpConfig getInstance() {
		return getInstance(false, null);
	}

	public synchronized static PreImpConfig getPlainInstance() {
		if (singleton == null) {
			// assume we are not running in batch mode
			singleton = new PreImpConfig();
		}
		return singleton;
	}

	public synchronized static PreImpConfig getInstance(boolean batchMode, Map<String, String> argumentsMap) {
		if (singleton != null) {
			return singleton;
		} else if (batchMode && singleton == null) {
			singleton = new BatchPreImpConfig();
			singleton.setArgumentsMap(argumentsMap);
		} else if (!batchMode && singleton == null) {
			singleton = new PreImpConfig();
		}
		return singleton;
	}
}
