package cdm.pre.imp.writer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.map.JTFileManagerUtils;
import cdm.pre.imp.map.NXPartFileManagerUtils;
import cdm.pre.imp.map.TypeMaps;
import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.reader.Element;
import cdm.pre.imp.reader.IConstants;

public class Dataset extends BaseElement {
   private String endItem;
   private Writer lstWriter;

   public Dataset(final XMLStreamWriter streamWriter, final Writer lstWriter, final Element element, final String endItem) {
      super(streamWriter, element);
      this.endItem = endItem;
      this.lstWriter = lstWriter;
   }

   @Override
   protected String getCDMType() throws CDMException {
      String clazz = element.getClazz();
      String ret = TypeMaps.getFileType(clazz);
      if (ret == null) {
         throw new CDMException("Unsupported Dataset type : " + clazz);
      }
      return ret;
   }

   /**
    * Toggles to Windows or Unix style path..
    * 
    * @param path
    *           The path to be toggled.
    * @return The toggled path.
    */
   private String translatePath(final String path, final char sep) {
      StringBuilder sb = new StringBuilder();
      final int len = path.length();
      for (int i = 0; i < len; i++) {
         final char c = path.charAt(i);
         if (c == '\\' || c == '/') {
            sb.append(sep);
         } else {
            sb.append(c);
         }
      }
      return sb.toString();
   }

   @Override
   public void writeObjectValues(final boolean hasChanges) throws XMLStreamException, IOException {
	  String strJTFileName = "";
      String objectName = userValues.get(IConstants.DataItemDesc);
      if (objectName == null) {
         objectName = attributes.get(IConstants.name);
         if (objectName != null) {
            int pos = objectName.indexOf(',');
            if (pos > -1) {
               objectName = objectName.substring(0, pos);
            }
         }
      }
      writeValue(TDatatype.T_STRING, PropPlace.Undef, ICustom.ObjectName, objectName);
      String dstPath = PreImpConfig.getInstance().getDstPath();
      String srcPath = PreImpConfig.getInstance().getSrcPath();
      String location = attributes.get("location");
      
      
      /** 
       *  Start : Renaming the part file name to its real name (i.e Item Id)
       */
      // Rename .prt file
      if((location!=null)&&(NXPartFileManagerUtils.getIsSendToProcess())){
    	  char sep = getPathSeparator(NXPartFileManagerUtils.getPlmXmlFileLoc(), NXPartFileManagerUtils.getPlmXmlFileLoc());
    	  String fileLoc = NXPartFileManagerUtils.getPlmXmlFileLoc().substring(0,NXPartFileManagerUtils.getPlmXmlFileLoc().lastIndexOf(sep));    	  
    	  File oldName = new File(fileLoc+sep+location);
    	  
    	  Map<String, String> partFileNameMap = NXPartFileManagerUtils.getPartFileNameMap();
    	  if(partFileNameMap!=null && partFileNameMap.containsKey(location)){
    		  location = partFileNameMap.get(location) + ".prt";
    	  }
    	  
          File newName = new File(fileLoc+sep+location);
          
          if(oldName.renameTo(newName)) {
             //System.out.println("renamed : " + newName.getName());
          } else {
             //System.out.println("Error renaming : " + oldName.getName());
          }          
      }
      
      // Rename .jt file      
      if((location!=null) && (JTFileManagerUtils.getIsSendToProcess()) && (JTFileManagerUtils.getIsAstonMartinDS())){
    	  char sep = getPathSeparator(JTFileManagerUtils.getPlmXmlFileLoc(), JTFileManagerUtils.getPlmXmlFileLoc());    	  
    	  String fileLoc = JTFileManagerUtils.getPlmXmlFileLoc().substring(0,JTFileManagerUtils.getPlmXmlFileLoc().lastIndexOf(sep));    	  
    	  File oldName = new File(fileLoc+sep+location);
    	  
    	  Map<String, String> jtFileNameMap = JTFileManagerUtils.getPartFileNameMap();
    	  if(jtFileNameMap!=null && jtFileNameMap.containsKey(location)){
    		  strJTFileName = jtFileNameMap.get(location) + ".jt";
    		  location = fileLoc+sep+jtFileNameMap.get(location) + ".jt";
    	  }
    	  
          File newName = new File(fileLoc+sep+strJTFileName);
          
          if(oldName.renameTo(newName)) {
             //System.out.println("renamed : " + newName.getName());
          } else {
            // System.out.println("Error renaming : " + oldName.getName());
          }          
      }
      /** 
       *  End : Renaming the part file name to its real name (i.e Item Id)
       */
      
      String localSrcPath = null;
      if (dstPath != null && srcPath != null && !"".equals(srcPath) && !"".equals(dstPath)) {
         char sep = getPathSeparator(srcPath, dstPath);
         
         String localBulkPath = PreImpConfig.getInstance().getLocalBulkPath();
         boolean isAddRefNametoPath = PreImpConfig.getInstance().isRefNameIncluded();

         if (isAddRefNametoPath && localBulkPath != null && !"".equals(localBulkPath)) {
            char srcPath_sep = getPathSeparator(srcPath, srcPath);
            // String filename = new File(location).getName();
            String filename = location.substring(location.lastIndexOf(srcPath_sep) + 1);
            StringBuilder sb = new StringBuilder(dstPath);
            sb.append(sep);
            sb.append(endItem);
            sb.append(sep);
            sb.append(filename);

            localSrcPath = translatePath(location.replace(srcPath, localBulkPath), getPathSeparator(srcPath, localBulkPath));
            location = sb.toString();
         } else {
            location = translatePath(location.replace(srcPath, dstPath), sep);
         }
      }
      writeValue(TDatatype.T_STRING, PropPlace.Undef, "file", location);
      //if (hasChanges) {
         if (lstWriter != null && localSrcPath != null) {
            lstWriter.write(localSrcPath);
            lstWriter.write('\n');
         }
      //}
   }

   private char getPathSeparator(final String srcPath, final String dstPath) {
      boolean isUnix = dstPath.indexOf('/') != -1;
      boolean isWindows = dstPath.indexOf('\\') != -1;
      return isUnix || isWindows ? (isUnix ? '/' : '\\') : (srcPath.indexOf('/') != -1 ? '/' : '\\');
   }
}
