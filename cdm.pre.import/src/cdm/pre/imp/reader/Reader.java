package cdm.pre.imp.reader;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import cdm.pre.imp.CDMException;
import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.reader.PLMUtils.SortedElements;

/**
 * Reads a Smaragd snapshot file.
 * 
 * @author dump1
 * 
 */
public class Reader {
   //private static final Logger logger = Logger.getLogger(Reader.class.getName());
	private Handler handler = null;

	// 05-12-2016 - Amit - this method should be re-visited while implementing the GUI for Truck data.
	// currently this method is redundant, introduced only to avoid a conflict with the existing GUI implementation
	public Handler readSnapshot(final String filename) throws ParserConfigurationException, SAXException, IOException {
	      SAXParserFactory factory = SAXParserFactory.newInstance();
	      factory.setValidating(false);
	      factory.setNamespaceAware(true);
	      SAXParser parser = factory.newSAXParser();
	      handler = new Handler();
	      handler.setGlobalConstCollection(ReaderSingleton.getReaderSingleton().getGlobalConstCollection());
	      //handler.setVehicleType("xml_car");
	      
	      // reads the value of the pre-importer preference for the vehicle type
	      // 17/05/2017 - Amit - the default setting chosen in the code in case the mode is not prescribed is "xml_car".
	      // Hence there is no exception handling in the case the vehicle type is null
	      if(PreImpConfig.getInstance() != null && PreImpConfig.getInstance().getVehicleType() != null) {
	    	  ReaderSingleton.getReaderSingleton().setFzgTypeName(PreImpConfig.getInstance().getVehicleType());
	    	  handler.setVehicleType(PreImpConfig.getInstance().getVehicleType());
	      }
	      
	      //logger.info("Parsing snapshot ...");
	      parser.parse(new File(filename), handler);
	      /*
	       * InputStream is = null; try { is = new FileInputStream(filename);
	       * InputStreamReader ir = new InputStreamReader(is, "UTF-8"); InputSource
	       * inSource = new InputSource(ir); inSource.setEncoding("UTF-8");
	       * parser.parse(inSource, handler); } finally { if (is != null) { try {
	       * is.close(); } catch (IOException ignore) { } } }
	       */

	      //logger.info("... Finished parsing snapshot");
	      
	      return handler;
	}
	
	/**
	 * New Method implemented to handle processing of truck data. This method is extendable to support other formats as well.
	 * Method sets the vehicle type to the handler objects
	 * @param filename			: name of the input file
	 * @param vehicleTyp		: value of the vehicle type being processed. Current values are car (defaul) and truck.
	 * @return					: SAX Handler object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Handler readSnapshot(final String filename, String vehicleTyp) throws ParserConfigurationException, SAXException, IOException {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(false);
      factory.setNamespaceAware(true);
      SAXParser parser = factory.newSAXParser();
      handler = new Handler();
      // sets the vehicle type of the input xml file
      if(vehicleTyp != null) {
    	  handler.setVehicleType(vehicleTyp);
      }
      handler.setGlobalConstCollection(ReaderSingleton.getReaderSingleton().getGlobalConstCollection());
      //logger.info("Parsing snapshot ...");
      parser.parse(new File(filename), handler);
      /*
       * InputStream is = null; try { is = new FileInputStream(filename);
       * InputStreamReader ir = new InputStreamReader(is, "UTF-8"); InputSource
       * inSource = new InputSource(ir); inSource.setEncoding("UTF-8");
       * parser.parse(inSource, handler); } finally { if (is != null) { try {
       * is.close(); } catch (IOException ignore) { } } }
       */

      //logger.info("... Finished parsing snapshot");
      // [Amit] - commented out the below line. To be cross-checked and removed
      //ArrayList<GlobalConstMapInfo> test = ReaderSingleton.getReaderSingleton().getGlobalConstCollection();
      return handler;
   }
   
   public SortedElements getSortedElements()
   {
	   SortedElements sortedElements = null;
	   try
	   {
		   sortedElements = PLMUtils.getElements(handler.getElements());
	   } 
	   catch (CDMException e) 
	   {
		   e.printStackTrace();
	   }
	  
	   return sortedElements;
   }
}
