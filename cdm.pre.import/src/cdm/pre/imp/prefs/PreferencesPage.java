package cdm.pre.imp.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import cdm.pre.imp.app.Activator;
import cdm.pre.imp.map.JTFileManagerUtils;
import cdm.pre.imp.map.NXPartFileManagerUtils;
import cdm.pre.imp.reader.IConstants;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class PreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
   private BooleanFieldEditor jtBFEditor;
   private BooleanFieldEditor dis2DBFEditor;
   private BooleanFieldEditor disSFBFEditor;
   private BooleanFieldEditor rnIPBFEditor;
   private StringFieldEditor  disLBDPSFEditor;
   private ComboFieldEditor   stopClassEditor;
   private ComboFieldEditor   preImpModeEditor;
   
   /**
    * To add new preference for processing send2package
    * and disable all other preference, when it is set to true
    */
   private BooleanFieldEditor processSendToPackage;
   private BooleanFieldEditor withEndItem;
   private StringFieldEditor  sourcePath;
   private StringFieldEditor  destinationPath;
   private BooleanFieldEditor ignoreInvalidsmaDia2;
   private BooleanFieldEditor importOnlygeoPos;
   private BooleanFieldEditor amDataSupply;
   private StringFieldEditor  configMappingFilePath;

   public PreferencesPage() {
      super(GRID);
      setPreferenceStore(Activator.getDefault().getPreferenceStore());
      setDescription("NOTE: Restart the application after changing the preferences!");
   }

   /**
    * Creates the field editors. Field editors are abstractions of the common
    * GUI blocks needed to manipulate various types of preferences. Each field
    * editor knows how to save and restore itself.
    */
   public void createFieldEditors() {
      addField(new FileFieldEditor(PreferenceConstants.P_IMPORTXML_FILE, "&Import XML file name:", getFieldEditorParent()));
      configMappingFilePath = new FileFieldEditor(PreferenceConstants.P_CONFIG_MAPPING_FILE_PATH, "&Configuration Mapping File :", getFieldEditorParent());

      /**
       * Adding new field for send to package preference
       */
      processSendToPackage = new BooleanFieldEditor(PreferenceConstants.P_PROCESS_SEND2PACKAGE, "&Process Send To Package", getFieldEditorParent());
      addField(processSendToPackage);
      
     /* withEndItem = new BooleanFieldEditor(PreferenceConstants.P_WITH_END_ITEM, "&With END ITEM NAME", getFieldEditorParent());
      addField(withEndItem);*/
      jtBFEditor = new BooleanFieldEditor(PreferenceConstants.P_JT_ONLY, "&Import JTs only", getFieldEditorParent());
      addField(jtBFEditor);
      dis2DBFEditor = new BooleanFieldEditor(PreferenceConstants.P_DISABLE2D, "&Disable 2D element processing", getFieldEditorParent());
      addField(dis2DBFEditor);
      disSFBFEditor = new BooleanFieldEditor(PreferenceConstants.P_DISABLE_SECFOLD, "&Disable secondary folder processing",
            getFieldEditorParent());
      addField(disSFBFEditor);
      /**
       * To add new preference for processing send2package 
       */
      ignoreInvalidsmaDia2 = new BooleanFieldEditor(PreferenceConstants.P_IGNORE_INVALID, "&Ignore invalid SmaDia2 elements", getFieldEditorParent()); 
      addField(ignoreInvalidsmaDia2);
      importOnlygeoPos = new BooleanFieldEditor(PreferenceConstants.P_ONLYGEOPOS, "&Import only GeoPos marked Positions", getFieldEditorParent());
      addField(importOnlygeoPos);
      amDataSupply = new BooleanFieldEditor(PreferenceConstants.P_AM_DATA_SUPPLY, "&Aston Martin Data Supply", getFieldEditorParent());
      addField(amDataSupply);
      String[][] clazzes = new String[4][2];
      clazzes[0][0] = "";
      clazzes[0][1] = "";
      clazzes[1][0] = "SmaDia2 Position";
      clazzes[1][1] = IConstants.j0SDPos;
      clazzes[2][0] = "SmaDia2 Position Variant";
      clazzes[2][1] = IConstants.j0SDPosV;
      clazzes[3][0] = "SmaDia2 PosVLage";
      clazzes[3][1] = IConstants.j0SDLage;

      stopClassEditor = new ComboFieldEditor(PreferenceConstants.P_STOP_CLASS, "XML creation should stop expanding till:", clazzes,
            getFieldEditorParent());
      addField(stopClassEditor);
      
      String[][] vehicleTyps = new String[3][2];
      vehicleTyps[0][0] = "";
      vehicleTyps[0][1] = "";
      vehicleTyps[1][0] = "Car";
      vehicleTyps[1][1] = PreferenceConstants.P_FZG_TYP_PKW;
      vehicleTyps[2][0] = "Truck";
      vehicleTyps[2][1] = PreferenceConstants.P_FZG_TYP_LKW;
      preImpModeEditor = new ComboFieldEditor(PreferenceConstants.P_VEHICLE_TYP, "Pre-Importer Vehicle Type", vehicleTyps,
              getFieldEditorParent());
      addField(preImpModeEditor);
      /**
       * To add new preference for processing send2package
       */
      sourcePath = new StringFieldEditor(PreferenceConstants.P_PATH_SRC, "Source path to be replaced:", getFieldEditorParent());
      addField(sourcePath);
      destinationPath = new StringFieldEditor(PreferenceConstants.P_PATH_DST, "Destination path to be use:", getFieldEditorParent());
      addField(destinationPath);
      addField(configMappingFilePath);

      rnIPBFEditor = new BooleanFieldEditor(PreferenceConstants.P_ADD_REF_NAME_TO_PATH,
            "&Add Reference Name to Bulk Data Path (LocalPDM Specific)", getFieldEditorParent());
      addField(rnIPBFEditor);
      disLBDPSFEditor = new StringFieldEditor(PreferenceConstants.P_LOCAL_BULK_DIR_PATH, "&Local Bulk Data Directory Path:",
            getFieldEditorParent());
      addField(disLBDPSFEditor);
   }

   @Override
   public void propertyChange(PropertyChangeEvent event) {
      super.propertyChange(event);
      if (event.getSource() == jtBFEditor) {
         Boolean newValue = (Boolean) event.getNewValue();
         setJTOnlyDependendFiels(newValue);
      }
      if (event.getSource() == rnIPBFEditor) {
         Boolean newValue = (Boolean) event.getNewValue();
         setLocalPDMonlyDependendFields(newValue);
      }
      
      /**
       * To add new preference for processing send2package
       * and disable all other preference, when it is set to true
       */
      if( event.getSource() == processSendToPackage){
    	  Boolean newValue = (Boolean) event.getNewValue();
    	  setProcessSendToPackageFields(newValue);
      }
      
      /**
       * To add new preference for processing Astom Martin DS
       */
      if( event.getSource() == amDataSupply){
    	  Boolean newValue = (Boolean) event.getNewValue();
    	  setAstomMartinDS(newValue);
      }
   }

   private void setJTOnlyDependendFiels(final boolean value) {
      dis2DBFEditor.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
      disSFBFEditor.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
   }
   
   /**
    * To add new preference for processing send2package
    * and disable all other preference, when it is set to true
    */
   private void setProcessSendToPackageFields(final boolean value){
	   jtBFEditor.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
	   //dis2DBFEditor.setEnabled(!value, PreferencesPage.this.getFieldEditorParent()); //to handle NX drawing	
	   disSFBFEditor.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
	   rnIPBFEditor.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
	   disLBDPSFEditor.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
	   stopClassEditor.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
	   ignoreInvalidsmaDia2.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
	   importOnlygeoPos.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
	   sourcePath.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
	 //  configMappingFilePath.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
	   destinationPath.setEnabled(!value, PreferencesPage.this.getFieldEditorParent());
	   NXPartFileManagerUtils.setIsSendToProcess(value);	   
	   JTFileManagerUtils.setIsSendToProcess(value);
	   withEndItem.setEnabled(value, PreferencesPage.this.getFieldEditorParent());
   }
   
   private void setAstomMartinDS(final boolean value){	   
	   JTFileManagerUtils.setIsAstonMartinDS(value);
   }

   private void setLocalPDMonlyDependendFields(final boolean value) {
      disLBDPSFEditor.setEnabled(value, PreferencesPage.this.getFieldEditorParent());
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
    */
   public void init(IWorkbench workbench) {
   }

   @Override
   protected void initialize() {
      super.initialize();
      setJTOnlyDependendFiels(getPreferenceStore().getBoolean(PreferenceConstants.P_JT_ONLY));
      setLocalPDMonlyDependendFields(getPreferenceStore().getBoolean(PreferenceConstants.P_ADD_REF_NAME_TO_PATH));
      
      /**
       * To add new preference for processing send2package
       * and disable all other preference, when it is set to true
       */
      setProcessSendToPackageFields(getPreferenceStore().getBoolean(PreferenceConstants.P_PROCESS_SEND2PACKAGE));
   }
}