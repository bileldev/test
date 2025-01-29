
package cdm.pre.imp.prefs;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cdm.pre.imp.batch.Arguments;

public class BatchPreImpConfig extends PreImpConfig {

	public boolean isIgnoreInvalidSmaDia() {
		return new Boolean(argumentsMap.get(PreferenceConstants.P_IGNORE_INVALID));
	}


	public boolean isDisable2D() {
		return new Boolean(argumentsMap.get(PreferenceConstants.P_DISABLE2D));
	}


	public boolean isDisableSecFolder() {
		return new Boolean(argumentsMap.get(PreferenceConstants.P_DISABLE_SECFOLD));
	}


	public String getImportXMLFile() {
		return argumentsMap.get(PreferenceConstants.P_IMPORTXML_FILE);
	}


	public String getSrcPath() {
		return argumentsMap.get(PreferenceConstants.P_PATH_SRC);
	}


	public String getDstPath() {
		return argumentsMap.get(PreferenceConstants.P_PATH_DST);
	}

	public String getConfigMappingFilePath() {
		return argumentsMap.get(Arguments.CONFIG_MAP_FILE);
	}
	
	public boolean isJTOnly() {
		return new Boolean(argumentsMap.get(PreferenceConstants.P_JT_ONLY));
	}


	public String getLocalBulkPath() {
		String localBulkPath = null;
		if (isRefNameIncluded()) {
			localBulkPath = argumentsMap.get(PreferenceConstants.P_LOCAL_BULK_DIR_PATH);
		}
		return localBulkPath;
	}


	public boolean isRefNameIncluded() {
		return new Boolean(argumentsMap.get(PreferenceConstants.P_ADD_REF_NAME_TO_PATH));
	}
	
	public boolean isProcessSendToPackage() {
		return new Boolean(argumentsMap.get(PreferenceConstants.P_PROCESS_SEND2PACKAGE));
	}

	/*public boolean isWithEndItem() {
		return new Boolean(argumentsMap.get(PreferenceConstants.P_WITH_END_ITEM));
	}*/

	public String getStopClass() {
		return argumentsMap.get(PreferenceConstants.P_STOP_CLASS);
	}


	@Override
	public boolean isOnlyGeoPos() {
		return Boolean.parseBoolean(argumentsMap.get(PreferenceConstants.P_ONLYGEOPOS));
	}
	
	@Override
	
	public boolean isAMSupply() {
		return Boolean.parseBoolean(argumentsMap.get(PreferenceConstants.P_AM_DATA_SUPPLY));
		
	 }
	
	// 6/6/2017 - added an override method for the batch mode
	public String getVehicleType() {
		return argumentsMap.get(Arguments.FAHRZEUG_TYPE);
		
	}
	
	public void writeSettings(final XMLStreamWriter streamWriter) throws XMLStreamException {
		streamWriter.writeEmptyElement("settings");
		streamWriter.writeAttribute(PreferenceConstants.P_IGNORE_INVALID, Boolean.toString(this.isIgnoreInvalidSmaDia()));
		streamWriter.writeAttribute(PreferenceConstants.P_DISABLE2D, Boolean.toString(this.isDisable2D()));
		streamWriter.writeAttribute(PreferenceConstants.P_DISABLE_SECFOLD, Boolean.toString(this.isDisableSecFolder()));
		streamWriter.writeAttribute(PreferenceConstants.P_JT_ONLY, Boolean.toString(this.isJTOnly()));
		streamWriter.writeAttribute(PreferenceConstants.P_ADD_REF_NAME_TO_PATH, Boolean.toString(this.isRefNameIncluded()));
		streamWriter.writeAttribute(PreferenceConstants.P_ONLYGEOPOS, Boolean.toString(this.isOnlyGeoPos()));
		streamWriter.writeAttribute(PreferenceConstants.P_VEHICLE_TYP, this.getVehicleType());
	}

}
