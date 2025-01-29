package cdm.pre.imp.json.reader;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.configmap.ConfigMapUtils;
import cdm.pre.imp.dbconnector.DBConnBroker;
import cdm.pre.imp.dbconnector.DBConnDatasetInfo;
import cdm.pre.imp.dbconnector.DBConnResponse;
import cdm.pre.imp.dbconnector.DTO.CDIModelDTO;
import cdm.pre.imp.dbconnector.DTO.JTDTO;
import cdm.pre.imp.map.TypeMaps;
import cdm.pre.imp.reader.ReaderSingleton;

public class DBConnectionUtils 
{
	private static Logger LOGGER = LogManager.getLogger(DBConnectionUtils.class);
	private static DBConnBroker connBrokerObj;
	
	/*
	* Input:

	- j0CTModSnr
	vom parent
	mit Class = j0Cdi3D

	- j0CTModNumber
	vom parent
	mit Class = j0Cdi3D

	- OBID
	vom parent
	mit Class = j0Cdi3D

	- Sequence
	vom
	vom parent
	mit Class = j0Cdi3D

	- OBID
	vom JT

	- j0Nomenclature
	vom
	Jt

	- j0FileSize
	vom JT

	- Sequence
	vom JT

	*/

	public static boolean startDatabaseConnection() {
		LOGGER.info("Searching for  dbconnection.properties in CONFIG_LOCATION .... ");
		String dbConPropertiesFile = Utils.readValueFromEnvVariable("CONFIG_LOCATION","dbconnection.properties");
		if(dbConPropertiesFile == null)
		{
			LOGGER.warn("Failed to locate database configuration");
			return false;
		}
		
		try
		{
			// Open DB connection
			return Utils.startDatabaseConnection(dbConPropertiesFile);
		}
		catch (Exception e) {
			LOGGER.info("Failed to open database connection", e);
			return false;
		}
	}
	
	public static boolean closeDatabaseConnection() {
		// Close DB connection
		try {
			ReaderSingleton.getReaderSingleton().getDbConnBroker().manageDBConnect(true);
			return true;
		} catch (SQLException e) {
			LOGGER.error("Error while closing database conection");
			return false;
		}

	}

	public static void existsJtInASPLM (Collection<JTDTO> jts )
	{
		existsJtInASPLM(jts, true);
	}
	
	public static void existsJtInASPLM (Collection<JTDTO> jts, boolean isManageDBConnection)
	{
		try
		{
			// Open DB connection
			if (isManageDBConnection)
				startDatabaseConnection();
			
			//if (!isManageDBConnection || isManageDBConnection & startDatabaseConnection())
				existsJts(jts);
		}
		catch (Exception e) {
			LOGGER.info("Error checking jt in the database", e);
		}
		finally
		{
			// Close DB connection
			if (isManageDBConnection)
				closeDatabaseConnection();
		}	
	}


	private static void existsJts(Collection<JTDTO> jts) {
		if(jts != null && jts.size() > 0)
		{
			connBrokerObj = ReaderSingleton.getReaderSingleton().getDbConnBroker();
			for (JTDTO jtdto : jts) {
				if(jtdto != null)
				{
					CDIModelDTO parentModel = jtdto.getParent();
					if( parentModel != null)
					{
						String modSnr = parentModel.getModSnr();
						String modNumber = parentModel.getModNumber();
						String sequence = parentModel.getSequence();
						String itemId = ConfigMapUtils.modelItemIDGenerator(modSnr,modNumber);
						String itemRevId = ConfigMapUtils.modelRevIDGenerator(sequence);
						String clsName = "C9Model";
						if(itemId != null && itemRevId !=null)
						{
							getModelDataFromDB(clsName,itemId,itemRevId, false, jtdto);
						}
					}
				}
			}
		}
	}
	
	
	public static void getModelDataFromDB(String clsName, String itemId, String itemRevID, boolean isDataset,JTDTO jt)
	{
		boolean isBVPresent = true;
		boolean isDatasetPresent = false;
		String action = "new";
		
		connBrokerObj = ReaderSingleton.getReaderSingleton().getDbConnBroker();
		connBrokerObj.setmItemID(itemId);
		connBrokerObj.setmItemRevID(itemRevID);
		connBrokerObj.setmObjType(clsName+"Revision");
		connBrokerObj.setmEndItemID(ReaderSingleton.getReaderSingleton().getEndItem());

		DBConnResponse resp = connBrokerObj.executeDBAction(isTypeForOccEff(clsName),isBVPresent,isDatasetPresent,null);
		
		ReaderSingleton.getReaderSingleton().setMapQry2ExecuteFreq("executeDBAction");
		
		//connBrokerObj.manageDBConnect(true);

		if( resp != null && resp.isExceptionThrown())
		{
			LOGGER.info("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for : "+itemId+" Rev ID : "+itemRevID+" of Type : "+clsName+"Revision");
			action="unset";
			//return action;
		}
		if( resp != null && resp.geteCode()!= null && !resp.geteCode().equals("") && resp.geteCode().equals("001"))
		{
			LOGGER.info("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for : "+itemId+" Rev ID : "+itemRevID+" of Type : "+clsName+"Revision");
			action="not-checked";
			//return action;
		}
		else if(resp != null && !resp.isExceptionThrown() && resp.getmMapAttrs() != null && resp.getmMapAttrs().size() <= 1)
		{
			//logger.info("No Data Found for Item ID:  No Mapping Attributes...... : "+itemID+" Rev ID : "+itemRevID+" of Type : "+entry.getKey()+"Revision");
			LOGGER.info("Object Not Found in Teamcenter DB. Action will be new");
			//return action;
		}
		else  if(resp != null && !resp.isExceptionThrown() && resp.getmMapAttrs() != null && resp.getmMapAttrs().size() > 1)
		{
			LOGGER.info("Object Found in Teamcenter DB ");
			DBConnDatasetInfo datasetInfo = getLatestDatasetInfo(resp, "DirectModel");
			if(datasetInfo != null)
			{

				String jtDBObjObid = datasetInfo.getmMapDatasetAttrs().get("object_desc");
				String jtObidPlmxml = jt.getOBID();//WriterUtils.getAttributeValue(element, "id");
				if( jtDBObjObid == null && jtObidPlmxml != null )
				{
					jt.setInASPLM(false);
					LOGGER.info("JT not Found in Teamcenter DB ");
				}
				else if( jtDBObjObid != null && jtObidPlmxml != null )
				{
					if(!jtObidPlmxml.equals(jtDBObjObid))
					{
						jt.setInASPLM(false);
						LOGGER.info("JT Found in Teamcenter DB but JT has a Different OBID");
					}
					else 
					{
						jt.setInASPLM(true);
						LOGGER.info("JT Found in Teamcenter DB");
					}
				}
			}
			else
			{
				jt.setInASPLM(false);
				LOGGER.info("JT not Found in Teamcenter DB ");
			}
		}
	}
	
	
	public static DBConnDatasetInfo getLatestDatasetInfo(DBConnResponse resp,String datasetType)
	{

		Vector<DBConnDatasetInfo> datasetInfoVec = resp.getmMapDatasets().get(datasetType);
		DBConnDatasetInfo latestDBConnDatasetInfo = null;
		String lastModDate =  null;
		SimpleDateFormat dbDateSimpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(datasetInfoVec != null && datasetInfoVec.size() > 0)
		{
			
			for (DBConnDatasetInfo dbConnDatasetInfo : datasetInfoVec) 
			{
				if(dbConnDatasetInfo != null && dbConnDatasetInfo.getmMapDatasetAttrs() != null && dbConnDatasetInfo.getmMapDatasetAttrs().size() > 0)
				{
					String dblastModDate = dbConnDatasetInfo.getmMapDatasetAttrs().get("mod_date");
					if(dblastModDate != null && !dblastModDate.equals(""))
					{
						//System.out.println("Last Modified Date :: "+dblastModDate);

						if( lastModDate == null)
						{
							lastModDate = dblastModDate;	
							latestDBConnDatasetInfo = dbConnDatasetInfo;
						}
						else
						{
							Date dbDateValObj = null;
							try {
								dbDateValObj = dbDateSimpleFormat.parse(dblastModDate);
							} catch (ParseException e) {
								e.printStackTrace();
							}
							Date preDateValObj = null;
							try {
								preDateValObj = dbDateSimpleFormat.parse(lastModDate);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (dbDateValObj != null && preDateValObj != null)
							{
								if(dbDateValObj.after(preDateValObj))
								{
									lastModDate = dblastModDate;	
									latestDBConnDatasetInfo = dbConnDatasetInfo;
								}
							}
						}

					}
				}
			}
		}
		return latestDBConnDatasetInfo;
	
	}
	public static boolean isTypeForOccEff(final String typeName) {
		return TypeMaps.OCCEFF_TYPES.contains(typeName);
	}
}
