package cdm.pre.imp;

import java.io.File;

import cdm.pre.imp.mod.TreeElement;

/**
 * Class that defines the data which is required in order to create the XML
 * Import file.
 * 
 * @author dump1
 * 
 */
public class XMLFileData {
   private final TreeElement root;

   private final String      fileloc;
   private final String      endItem;
   private final Mode        mode;
   private final File        plmxmlFile1;
   private final File        plmxmlFile2;

   public XMLFileData(final TreeElement root, final String fileloc, final String endIem, final File plmxmlFile1, final File plmxmlFile2,
         final Mode mode) {
      this.root = root;
      this.fileloc = fileloc;
      this.endItem = endIem;
      this.mode = mode;
      this.plmxmlFile1 = plmxmlFile1;
      this.plmxmlFile2 = plmxmlFile2;
   }

   public TreeElement getRoot() {
      return root;
   }

   public String getFileloc() {
      return fileloc;
   }

   public String getEndItem() {
      return endItem;
   }
   public TreeElement getroot(){
	   return root;
   }

   public boolean isDelta() {
      return mode == Mode.Delta;
   }

   public File getPlmxmlFile1() {
      return mode == Mode.Delta || mode == Mode.PLMXML1 ? plmxmlFile1 : null;
   }

   public File getPlmxmlFile2() {
      return mode == Mode.Delta || mode == Mode.PLMXML2 ? plmxmlFile2 : null;
   }
}
