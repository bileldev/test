package cdm.pre.imp.variants;

import java.util.ArrayList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cdm.pre.imp.csvreader.RefConfigMappingObject;
import cdm.pre.imp.map.TypeMaps;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.ReaderSingleton;

public class VariantsUtil 
{
	// [Amit] - logger instance is not being used, hence commented.. To be cross-checked and removed
	//private final static Logger logger = LogManager.getLogger("cdm.pre.imp.tracelog");
	public static final String GLOBAL_VARIABLE_VARIANT_REMOVE_TYPE 				= "VARIANT_REMOVE_TYPE"; 
	
	public static final String GLOBAL_VARIABLE_VARIANT_REMOVE_CAR_TYPE 			= "VARIANT_REMOVE_CAR_TYPE"; 
	
	public static final String GLOBAL_VARIABLE_VARIANT_REMOVE_CFG_TYPE 			= "VARIANT_REMOVE_CFG_TYPE"; 
	
	public static String buildVariantCondition(ArrayList<RefConfigMappingObject> refConfigMappingObjList)
	{
		String variantCondition = null;
		if(refConfigMappingObjList != null && refConfigMappingObjList.size() > 0)
		{
			int index =0;
			for (RefConfigMappingObject refConfigMappingObject : refConfigMappingObjList) 
			{
				String condition = refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant();
				if(index==0)
				{
					variantCondition = condition;
					if(ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM))
					{
						return variantCondition;
					}
				}
				else
				{
					variantCondition = variantCondition+","+condition;
				}
				index++;
			}
		}
		
		return variantCondition;
	}
	
	
	public static void addParameters(XMLStreamWriter streamWriter,RefConfigMappingObject refConfigMapObj)
	{
		try 
		{
			writeValue(streamWriter, "s", "i", "item_id", refConfigMapObj.getTopNode());
			//writeValue(streamWriter, "s", "ir", "object_name", refConfigMapObj.getTopNode()+"_"+refConfigMapObj.getRefConfig());
			writeValue(streamWriter, "s", "ir", "object_name", refConfigMapObj.getTopNode());
			writeValue(streamWriter, "s", "r", "item_revision_id", "001");
		}
		catch (XMLStreamException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static String createVarConditionForConnection(String parentClassname, String childClassName, ArrayList<RefConfigMappingObject> refConfigMappingObjList )
	{
		String condition = "";
		if( childClassName != null && refConfigMappingObjList != null && refConfigMappingObjList.size() > 0)
		{
			/*if((ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM) ) && ((childClassName.equals(IConstants.j0SDHMod) || (childClassName.equals(IConstants.j0Montge) ))))
			{
				RefConfigMappingObject refConfigMappingObject = refConfigMappingObjList.get(0);
				condition = refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant();
				return condition;
			}*/
			
			if(ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM)  )
			{
				RefConfigMappingObject refConfigMappingObject = refConfigMappingObjList.get(0);
				if( childClassName.equals(IConstants.j0SDHMod) )
				{
					condition = refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant();
				}

				else if (TypeMaps.isTypeForCarVariantCondition(childClassName ) && parentClassname.equals(IConstants.j0Montge))
				{

					condition = refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant();
				}
				/*if(condition != null && !condition.equals(""))
				{
					System.out.println("createVarConditionForConnection :: Condition : "+condition);
					logger.info("createVarConditionForConnection :: Condition : "+condition);
				}*/
				return condition;
			}
			//if(!ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM) && (TypeMaps.isTypeForVariantCondition(childClassName) ))
			if(!ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM))
			{
				RefConfigMappingObject refConfigMappingObject = null;
				//	if(ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_MONTAGE) && (childClassName.equals(IConstants.j0SDHMod) ))

				if( childClassName.equals(IConstants.j0SDHMod) )
				{
					refConfigMappingObject = refConfigMappingObjList.get(0);
					condition = refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant();
				}
				else 
				{
					if (TypeMaps.isTypeForCarVariantCondition(childClassName ) && parentClassname.equals(IConstants.j0Montge))
					{
						refConfigMappingObject = refConfigMappingObjList.get(0);
						condition = refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant()+",";
					}
					refConfigMappingObject = refConfigMappingObjList.get(1);
					condition = condition + refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant();
				}
				
				/*if(condition != null && !condition.equals(""))
				{
					System.out.println("createVarConditionForConnection 100% -- Condition : "+condition);
					logger.info("createVarConditionForConnection 100% -- Condition : "+condition);
				}*/
				return condition;
			}
		}
		return condition;
	}
	
	
	
	
	/*public static String createVarConditionForConnectionFromGlobalConstant(String parentClassname, String childClassName, ArrayList<RefConfigMappingObject> refConfigMappingObjList )
	{
		String condition = "";
		if( childClassName != null && refConfigMappingObjList != null && refConfigMappingObjList.size() > 0)
		{
			
			if(ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM)  )
			{
				RefConfigMappingObject refConfigMappingObject = refConfigMappingObjList.get(0);
				String gValue = ReaderSingleton.getReaderSingleton().getGlobalConstValue(VariantsUtil.GLOBAL_VARIABLE_VARIANT_REMOVE_CAR_TYPE);
				if(gValue != null && !gValue.equals(""))
				{
					if(gValue.contains(","))
					{
						String[] carTypes = gValue.split(",");
						if( carTypes != null && carTypes.length > 0)
						{
							for (String carType : carTypes) {
								String[] parentChildTypes = carType.split(":");
								if( parentChildTypes != null && parentChildTypes.length > 0)
								{
									if(((parentChildTypes[0].equals("ALL") || parentChildTypes[0].equals(parentClassname)) && parentChildTypes[1].equals(childClassName)) || ( ( parentChildTypes[0].equals(parentClassname)) && (parentChildTypes[1].equals(childClassName) || parentChildTypes[1].equals("ALL")) ) )
									{
										condition = refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant();
										break;
									}
								}
							}
						}
					}
				}
				
				if(condition != null && !condition.equals(""))
				{
					System.out.println("createVarConditionForConnectionFromGlobalConstant -- Condition : "+condition);
					logger.info("createVarConditionForConnectionFromGlobalConstant -- Condition : "+condition);
				}
				return condition;
			}
			if(!ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM))
			{
				RefConfigMappingObject refConfigMappingObject = null;
				refConfigMappingObject = refConfigMappingObjList.get(0);
	
				
				
				
				String gValue = ReaderSingleton.getReaderSingleton().getGlobalConstValue(VariantsUtil.GLOBAL_VARIABLE_VARIANT_REMOVE_CAR_TYPE);
				if(gValue != null && !gValue.equals(""))
				{
					if(gValue.contains(","))
					{
						String[] carTypes = gValue.split(",");
						if( carTypes != null && carTypes.length > 0)
						{
							for (String carType : carTypes) {
								String[] parentChildTypes = carType.split(":");
								if( parentChildTypes != null && parentChildTypes.length > 0)
								{
									if(((parentChildTypes[0].equals("ALL") || parentChildTypes[0].equals(parentClassname)) && parentChildTypes[1].equals(childClassName)) || ( ( parentChildTypes[0].equals(parentClassname)) && (parentChildTypes[1].equals(childClassName) || parentChildTypes[1].equals("ALL")) ) )
									{
										condition = refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant();
										break;
									}
								}
							}
						}
					}
				}
				
				
				
				gValue = ReaderSingleton.getReaderSingleton().getGlobalConstValue(VariantsUtil.GLOBAL_VARIABLE_VARIANT_REMOVE_CFG_TYPE);
				refConfigMappingObject = refConfigMappingObjList.get(1);
				if(gValue != null && !gValue.equals(""))
				{
					if(gValue.contains(","))
					{
						String[] carTypes = gValue.split(",");
						if( carTypes != null && carTypes.length > 0)
						{
							for (String carType : carTypes) {
								String[] parentChildTypes = carType.split(":");
								if( parentChildTypes != null && parentChildTypes.length > 0)
								{
									if(((parentChildTypes[0].equals("ALL") || parentChildTypes[0].equals(parentClassname)) && parentChildTypes[1].equals(childClassName)) || ( ( parentChildTypes[0].equals(parentClassname)) && (parentChildTypes[1].equals(childClassName) || parentChildTypes[1].equals("ALL")) ) )
									{
										if(condition != null & !condition.equals(""))
										{
											condition=condition+",";
										}
										condition = condition + refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant();
										break;
									}
								}
							}
						}
					}
				}
				
				
					
				if(condition != null && !condition.equals(""))
				{
					System.out.println("createVarConditionForConnectionFromGlobalConstant 100% -- Condition : "+condition);
					logger.info("createVarConditionForConnectionFromGlobalConstant 100% -- Condition : "+condition);
				}
				return condition;
			}
		}
		return condition;
	}*/
	
	public static String createVarConditionForConnectionFromGlobalConstant(String parentClassname, String childClassName, ArrayList<RefConfigMappingObject> refConfigMappingObjList )
	{
		String condition = "";
		if( childClassName != null && refConfigMappingObjList != null && refConfigMappingObjList.size() > 0)
		{

			if(ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM)  )
			{
				condition = getVarConditionString(parentClassname, childClassName, refConfigMappingObjList.get(0), VariantsUtil.GLOBAL_VARIABLE_VARIANT_REMOVE_CAR_TYPE);
				return condition;
			}
			if(!ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM))
			{
				condition = getVarConditionString(parentClassname, childClassName, refConfigMappingObjList.get(0), VariantsUtil.GLOBAL_VARIABLE_VARIANT_REMOVE_CAR_TYPE);

				if(condition != null & !condition.equals(""))
				{
					condition=condition+",";
				}
				condition = condition+getVarConditionString(parentClassname, childClassName, refConfigMappingObjList.get(1), VariantsUtil.GLOBAL_VARIABLE_VARIANT_REMOVE_CFG_TYPE);
				return condition;
			}
		}
		return condition;
	}
	
	public static String getVarConditionString(String parentClassname, String childClassName, RefConfigMappingObject refConfigMappingObject, String globalVarName )
	{
		String condition = "";
		
		String gValue = ReaderSingleton.getReaderSingleton().getGlobalConstValue(globalVarName);
		if(gValue != null && !gValue.equals(""))
		{
			if(gValue.contains(","))
			{
				String[] carTypes = gValue.split(",");
				if( carTypes != null && carTypes.length > 0)
				{
					for (String carType : carTypes) {
						String[] parentChildTypes = carType.split(":");
						if( parentChildTypes != null && parentChildTypes.length > 0)
						{
							if(((parentChildTypes[0].equals("ALL") || parentChildTypes[0].equals(parentClassname)) && parentChildTypes[1].equals(childClassName)) || ( ( parentChildTypes[0].equals(parentClassname)) && (parentChildTypes[1].equals(childClassName) || parentChildTypes[1].equals("ALL")) ) )
							{
								condition = condition + refConfigMappingObject.getTopNode()+"|"+refConfigMappingObject.getOptionName()+"|"+refConfigMappingObject.getVariant();
								break;
							}
						}
					}
				}
			}
		}
		
		return condition;
	}
	
	public static void writeValue(XMLStreamWriter streamWriter,String type, String propPlace, final String name, String value) throws XMLStreamException 
	{
	      streamWriter.writeEmptyElement("p");
	      if (value == null) {
	         value = "";
	      }
	      streamWriter.writeAttribute("n", name);
	      streamWriter.writeAttribute("v", value);
	      
	      streamWriter.writeAttribute("t", type);

	      streamWriter.writeAttribute("s", propPlace);
	   }
	
	public static boolean getRemoveState(String optionValue)
	{
		
		if(!ReaderSingleton.getReaderSingleton().getRefConfigurationType().equals(PreferenceConstants.P_REFCONFIG_TYP_SUM) && optionValue.equals("car"))
		{
			return false;
		}
		return true;
		
	}
	public static void buildTopNodeElement()
	{
		
	}
	
	public static void buildTopNodeConnection()
	{
		
	}
	
	public static String generateElementIDForTopNode(RefConfigMappingObject refConfigMapObj)
	{
		String[] params= new String[2];
		String topNodeID = null;
		if( refConfigMapObj != null)
		{
			params[0] = refConfigMapObj.getRefConfig();
			params[1] = refConfigMapObj.getTopNode();
			topNodeID = VariantsUtil.hash(params);
		}
		return topNodeID;
	}
	public static String hash(String[] values) {
		long hash = 0xCBF29CE484222325L;
		for (String s : values) {
			if (s != null) {
				hash ^= s.hashCode();
				hash *= 0x100000001B3L;
			}
		}
		return String.valueOf((hash >>> 1) + (~hash >>> 31));
	}
}
