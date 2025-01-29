package cdm.pre.imp.csvreader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CSVFileProcesser
{
	HashMap<String, RefConfigMappingObject> refConfigMapObjMap = null;
	private final static Logger logger	= LogManager.getLogger("cdm.pre.imp.tracelog");

	public CSVFileProcesser()
	{
		refConfigMapObjMap = new HashMap<>();
	}

	public static void main(String[] args) 
	{
		//String csvFile = "D:\\Krishna\\DiFa\\Varint Conditions\\refConfigMapping.csv";
		//CSVFileProcesser csvFileProcessor = new CSVFileProcesser();
		//csvFileProcessor.parseCSV(csvFile);
	}

	public void parseCSV(String csvFileLocation)
	{
		logger.info("Reading .csv file : "+csvFileLocation);
		try 
		{
			logger.info("Processing .csv file.....");
			Reader reader = Files.newBufferedReader(Paths.get(csvFileLocation));
			CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
			String[] nextRecord;
			RefConfigMappingObject refConfigMapObj  = null;
			while ((nextRecord = csvReader.readNext()) != null) 
			{
				refConfigMapObj = new RefConfigMappingObject();
				// use comma as separator
				String[] refConfigMapLine = nextRecord;
				if( refConfigMapLine != null && refConfigMapLine.length == 5 )
				{
					refConfigMapObj.setRefConfig(refConfigMapLine[0]);
					refConfigMapObj.setVariant(refConfigMapLine[1]);
					refConfigMapObj.setParentRefConfig(refConfigMapLine[2]);
					refConfigMapObj.setTopNode(refConfigMapLine[3]);
					refConfigMapObj.setOptionName("car");
				}

				if( refConfigMapObj.getRefConfig() != null && !refConfigMapObj.getRefConfig().equals(""))
				{
					if(refConfigMapObj.getTopNode() == null || refConfigMapObj.getTopNode().equals("") || refConfigMapObj.getTopNode().equals("-"))
					{
						RefConfigMappingObject parentMapObj = refConfigMapObjMap.get(refConfigMapObj.getParentRefConfig());
						if(parentMapObj != null)
						{
							refConfigMapObj.setParentRefConfigMapObj(parentMapObj);
							refConfigMapObj.setOptionName("cfg");
							refConfigMapObj.setTopNode(parentMapObj.getTopNode());
						}

					}
					this.refConfigMapObjMap.put(refConfigMapObj.getRefConfig(),refConfigMapObj);
				}
			}
			
			logger.info("Processing .csv file completed......");
		} 
		catch (IOException e) 
		{
			
			logger.error("Error in processing .csv file.....");
			logger.error(e.getMessage());
			
			//e.printStackTrace();
		}
	}

	public HashMap<String, RefConfigMappingObject> getRefConfigObjectsMap()
	{
		return refConfigMapObjMap;
	}
}