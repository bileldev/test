package cdm.pre.imp.writer;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.reader.Element;
import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.MappedAttributes;
import cdm.pre.imp.reader.ReaderSingleton;

public class WriterUtils {

	private final static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");
	
	public static String getAttributeValue(Element element, String attrName)
	{
		String attrValue = null;
		// null check on the element object is necessary as not always the calling method does it
		
		//logger.info("Attr Name : "+attrName+" Elment OBID : "+element.getId());
		if(element != null) {
			HashMap<String, ArrayList<MappedAttributes>> mappedElementMap = element.getMappedElementsMap();
			if( mappedElementMap != null)
			{
				Object[] keys = mappedElementMap.keySet().toArray();
				if(keys != null && keys.length > 0)
				{
					for(int i = 0; i < keys.length;i++)
					{
						ArrayList<MappedAttributes> mappedAttrs = mappedElementMap.get(keys[i]);
						if( mappedAttrs != null && mappedAttrs.size() > 0)
						{
							MappedAttributes attribute = null;
							for ( int k = 0; k < mappedAttrs.size(); k++)
							{
								attribute = mappedAttrs.get(k);
								if( attribute != null && attribute.getAttrName() != null && attribute.getAttrName().equals(attrName))
								{
									attrValue =  attribute.getAttrValue();
									break;
								}
							}
						}
					}
				}
			}
			if( attrValue == null && attrName.equals("id"))
			{
				
			}
		}
		return attrValue;
	}
	
	public static MappedAttributes getAttribute(Element element, String attrName)
	{
		String attrValue = null;
		// null check on the element object is necessary as not always the calling method does it
		if(element != null) {
			HashMap<String, ArrayList<MappedAttributes>> mappedElementMap = element.getMappedElementsMap();
			if( mappedElementMap != null)
			{
				Object[] keys = mappedElementMap.keySet().toArray();
				if(keys != null && keys.length > 0)
				{
					for(int i = 0; i < keys.length;i++)
					{
						ArrayList<MappedAttributes> mappedAttrs = mappedElementMap.get(keys[i]);
						if( mappedAttrs != null && mappedAttrs.size() > 0)
						{
							MappedAttributes attribute = null;
							for ( int k = 0; k < mappedAttrs.size(); k++)
							{
								attribute = mappedAttrs.get(k);
								if( attribute != null && attribute.getAttrName() != null && attribute.getAttrName().equals(attrName))
								{
									return attribute;
									//break;
								}
							}
						}
					}
				}
			}
			if( attrValue == null && attrName.equals("id"))
			{
				
			}
		}
		return null;
	}	
	
	/**
	 * utility method that generates the end item id for each C/D-BM in the Truck structure
	 * @param baumusterID : input baumuster id for whom the end item id needs to be determined
	 * @return	: value of the end item id for the input C/D-Baumuster
	 */
	public static String generateEndItemID(String baumusterID) {
		
		String endItemID = null;
		
		if(baumusterID != null && ReaderSingleton.getReaderSingleton().getRefFzgName() != null) {
			
			endItemID = IConstants.PREFIX_EFF_ID + "_" + ReaderSingleton.getReaderSingleton().getRefFzgName() + "_" + baumusterID;
		}
		
		return endItemID;
	}

}
