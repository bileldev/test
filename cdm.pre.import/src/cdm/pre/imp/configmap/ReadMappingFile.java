/**
 * 
 */
package cdm.pre.imp.configmap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cdm.pre.imp.reader.ReaderSingleton;




/**
 * @author amit.rath
 * The purpose of this class is to instantiate the SAX parser to parse through the Mapping XML file
 */
public class ReadMappingFile {
	
	private String xmlFilePath = "";						// class member that holds the absolute file path to the mapping file
	private ArrayList<GlobalConstMapInfo> globalConstCollectionObj;
	
	/**
	 * Constructor to the class that initializes the class member
	 * @param xmlFilePath: absolute file path to the mapping XML file.
	 */
	public ReadMappingFile(String xmlFilePath) {
		
		this.xmlFilePath = xmlFilePath;
	}
	
	/**
	 * The method opens the mapping XML file for read, instantiates the SAX parser and invokes the custom handler 
	 * son the parse of the file.
	 */
	public HashMap<String, ArrayList<ClassMapInfo>> parseMapppingFile() {
		
	// output variable
	HashMap<String, ArrayList<ClassMapInfo>> clsMapCollectionObj = new HashMap<String, ArrayList<ClassMapInfo>>();		
	try {
		// instantiate the Java file object for the mapping file
		File mapFileObj = new File(this.xmlFilePath);
		
		
		
		// checks if the file actually exists or not
		if(mapFileObj != null) {
			// if the file does not exist, then it throws an exception and ends the pre-importer
			if(!mapFileObj.exists()) {
				throw new MappingException("Mapping file does not exist at the following path: " + this.xmlFilePath
						+ " Exception thrown in Class: " + this.getClass().getName() + " at Line: " +
							Thread.currentThread().getStackTrace()[2].getLineNumber(), true, ConfigMapUtils.LOG_TYPE_ERROR);
			}
			// else block that instantiates the SAX parser and starts the parse of the mapping xml file
			else {
					SAXParserFactory parseFact = SAXParserFactory.newInstance();
					SAXParser parser = parseFact.newSAXParser();
					ParseHandler handler = new ParseHandler();
					//instantiates the custom parse handler for the parse of the mapping file
					parser.parse(mapFileObj, handler);
					// call to print out the mapping information
					// this should be qualified by a if condition
					clsMapCollectionObj = handler.getClsMapCollection();
					globalConstCollectionObj = handler.getGlobalConstCollection();
					if( globalConstCollectionObj != null && globalConstCollectionObj.size() > 0)
					{
						ReaderSingleton.getReaderSingleton().setGlobalConstCollection(globalConstCollectionObj);
					}
					//ConfigMapUtils.printMappingDetails(handler.getClsMapCollection());
				} 
			}					
		}catch (MappingException ex) {
			ex.printStackTrace();
		}catch(Exception e) {
				e.printStackTrace();
		}
		return clsMapCollectionObj;
	}
}
