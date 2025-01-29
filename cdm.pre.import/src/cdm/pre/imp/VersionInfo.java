package cdm.pre.imp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionInfo {
   private final static Logger  logger  = Logger.getLogger(VersionInfo.class.getName());
   private final static Pattern pattern = Pattern.compile("(\\d\\.\\d\\.\\d)\\.(.*)");

   public static String getVersionNumber() {
      String ret = "undefined";
      try {
         ret = getBuildVersion();
      } catch (IOException e) {
         logger.throwing(VersionInfo.class.getName(), "getVersionNumber", e);
      }
      return ret;
   }

   /**
    * Returns the version information from the MANIFEST.MF file of the plug-in.
    * 
    * @return A string array of the size 3 if the version information is given
    *         in the following format 0.0.0.qualifier. The second element of the
    *         string is the value of 1.0.0 the third element is the value of the
    *         qualifier and the first element if the version information as it
    *         is. If the format is different it returns a string array of size 1
    *         where the first element is the version information as it is.
    * @throws IOException
    */
   public static String[] getManifestVersionNumber() throws IOException {
      String[] ret = null;
      File file = null;
      FileInputStream fileInput = null;
      InputStream is = null;
      try {
        URL url = Thread.currentThread().getContextClassLoader().getResource("/META-INF/MANIFEST.MF");
    	  if(url!=null){
    		  is = url.openStream();
    		  if (is != null) {
    			  ret = readManifestFile(is);
    		  }
    	  }else{
    		  file = new File("META-INF/MANIFEST.MF");
	       	 	if(file!=null){
	       	 		fileInput = new FileInputStream(file);
	       	 		if(fileInput!=null)
	       	 			ret = readManifestFile(fileInput);
	       	 	}
    	  }
      }catch(Exception e){
    	  logger.severe(e.getMessage());
      }finally {
         if (is != null) {
            is.close();
         }
         if(fileInput!=null){
        	 fileInput.close();
         }
      }
      return ret;
   }
   
   /**
    * Reading the Manifest file
    * @param is - InputStream
    * @return Array of Manifest Details
    */
   public static String[] readManifestFile(InputStream is){
	   String[] ret = null;
	   try{
		   Manifest manifest = new Manifest(is);
	       Attributes attrs = manifest.getMainAttributes();
	       String version = attrs.getValue("Bundle-Version");
	       if (version != null) {
	          Matcher m = pattern.matcher(version);
	          if (m.matches()) {
	             ret = new String[3];
	             ret[1] = m.group(1);
	             ret[2] = m.group(2);
	             ret[0] = version;
	          } else {
	             ret = new String[1];
	             ret[0] = version;
	          }
	       }		   
	   }catch(Exception e){
		   logger.severe(e.getMessage());
	   }
	   return ret;
   }

   public static String getBuildVersion() throws IOException {
      String ret = null;
      URL url = Thread.currentThread().getContextClassLoader().getResource("/build/version.properties");
      if (url == null) {
         // we are in batch mode, rsrc is used and the version.properties is
         // copied to the package directory during the build
    	 url = Thread.currentThread().getContextClassLoader().getResource("version.properties");
         //url = VersionInfo.class.getResource("version.properties");
      }
      InputStream is = null;
      try {
    	  if(url != null){
             is = url.openStream();
	         Properties props = new Properties();
	         props.load(is);
	         ret = (String) props.get("version");
    	  } else{
    		  /** Start : Added to handle the case of SendTo Package **/
    	      if(url == null){
    	    	//  System.out.println("4");
    	    	  try{
    	    	      File file = new File("build/version.properties");    	    	      
    	    	      if(file!=null){
    	    		      FileInputStream fileInput = new FileInputStream(file);
    	    		      Properties properties = new Properties();
    	    		      properties.load(fileInput);
    	    		      ret = (String) properties.get("version");
    	    		      fileInput.close();
    	    	      }
    	          }catch(Exception e){
    	        	  logger.severe(e.getMessage());
    	          }          
    	      }    	      
    	      /** End : Added to handle the case of SendTo Package **/
    	  }
      } finally {
         if (is != null) {
            is.close();
         }
      }
     /* if ( ret == null )
      {
    	  ret = "1.7.0";
      }*/
      return ret;
   }
   
   
   public static String getBuildDate() throws IOException {
	      String ret = null;
	      URL url = Thread.currentThread().getContextClassLoader().getResource("/build/version.properties");
	      if (url == null) {
	         // we are in batch mode, rsrc is used and the version.properties is
	         // copied to the package directory during the build
	    	 url = Thread.currentThread().getContextClassLoader().getResource("version.properties");
	         //url = VersionInfo.class.getResource("version.properties");
	      }
	      InputStream is = null;
	      try {
	    	  if(url != null){
	             is = url.openStream();
		         Properties props = new Properties();
		         props.load(is);
		         ret = (String) props.get("Build");
	    	  } else{
	    		  /** Start : Added to handle the case of SendTo Package **/
	    	      if(url == null){
	    	    	//  System.out.println("4");
	    	    	  try{
	    	    	      File file = new File("build/version.properties");    	    	      
	    	    	      if(file!=null){
	    	    		      FileInputStream fileInput = new FileInputStream(file);
	    	    		      Properties properties = new Properties();
	    	    		      properties.load(fileInput);
	    	    		      ret = (String) properties.get("Build");
	    	    		      fileInput.close();
	    	    	      }
	    	          }catch(Exception e){
	    	        	  logger.severe(e.getMessage());
	    	          }          
	    	      }    	      
	    	      /** End : Added to handle the case of SendTo Package **/
	    	  }
	      } finally {
	         if (is != null) {
	            is.close();
	         }
	      }
	     /* if ( ret == null )
	      {
	    	  ret = "1.7.0";
	      }*/
	      return ret;
	   }
   
   public static String getPatchNumber() throws IOException {
	      String ret = null;
	      URL url = Thread.currentThread().getContextClassLoader().getResource("/build/version.properties");
	      if (url == null) {
	         // we are in batch mode, rsrc is used and the version.properties is
	         // copied to the package directory during the build
	    	 url = Thread.currentThread().getContextClassLoader().getResource("version.properties");
	         //url = VersionInfo.class.getResource("version.properties");
	      }
	      InputStream is = null;
	      try {
	    	  if(url != null){
	             is = url.openStream();
		         Properties props = new Properties();
		         props.load(is);
		         ret = (String) props.get("patch");
	    	  } else{
	    		  /** Start : Added to handle the case of SendTo Package **/
	    	      if(url == null){
	    	    	//  System.out.println("4");
	    	    	  try{
	    	    	      File file = new File("build/version.properties");    	    	      
	    	    	      if(file!=null){
	    	    		      FileInputStream fileInput = new FileInputStream(file);
	    	    		      Properties properties = new Properties();
	    	    		      properties.load(fileInput);
	    	    		      ret = (String) properties.get("patch");
	    	    		      fileInput.close();
	    	    	      }
	    	          }catch(Exception e){
	    	        	  logger.severe(e.getMessage());
	    	          }          
	    	      }    	      
	    	      /** End : Added to handle the case of SendTo Package **/
	    	  }
	      } finally {
	         if (is != null) {
	            is.close();
	         }
	      }
	     /* if ( ret == null )
	      {
	    	  ret = "1.7.0";
	      }*/
	      return ret;
	   }
}
