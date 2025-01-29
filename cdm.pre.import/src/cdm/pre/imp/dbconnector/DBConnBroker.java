package cdm.pre.imp.dbconnector;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.config.Configurator;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.reader.ReaderSingleton;

/**
 * This class administers subsequent calls to
 * 1. Read database connect details from configuration file
 * 2. Connect & Disconnect to the Teamcenter Database
 * 3. Invoke methods for retrieving data from Teamcenter
 * 
 * @author amit.rath
 *
 */


public class DBConnBroker {
	final private static Logger LOGGER = LogManager.getLogger(DBConnBroker.class);
	
	private long mTimeStart;						// start time for the run of the DB SQL componnent
	private long mTimeEnd;							// end time for the run of the DB SQL component
	
	private String mItemID;							// input item ID
	private String mItemRevID;						// input item revision ID
	private String mObjType;						// input object type
	private String mEndItemID;						// input end item representing the reference configuration
	private String mLogLevel;						// log level for writing out to the log file
	
	private boolean mEffReqFlag;					// boolean flag which tells if effectivity information needs to be queried or not
	private boolean mIsOccEffApplied;				// boolean flag which tells if the particular component being queried has occurrence effectivity information associated with it
	
	private JDBCConnector mObjJDBC;					// member variable that holds the instance of JDBCConnector class
	
	
	
	public long getmTimeStart() {
		return mTimeStart;
	}
	public void setmTimeStart(long mTimeStart) {
		this.mTimeStart = mTimeStart;
	}
	public long getmTimeEnd() {
		return mTimeEnd;
	}
	public void setmTimeEnd(long mTimeEnd) {
		this.mTimeEnd = mTimeEnd;
	}
	
	public String getmLogLevel() {
		return mLogLevel;
	}
	public void setmLogLevel(String mLogLevel) {
		this.mLogLevel = mLogLevel;
	}
	
	public JDBCConnector getObjJDBC() {
		return mObjJDBC;
	}
	public void setObjJDBC(JDBCConnector objJDBC) {
		this.mObjJDBC = objJDBC;
	}

	public boolean ismEffReqFlag() {
		return mEffReqFlag;
	}

	public void setmEffReqFlag(boolean mEffReqFlag) {
		this.mEffReqFlag = mEffReqFlag;
	}

	public String getmItemID() {
		return mItemID;
	}

	public void setmItemID(String mItemID) {
		this.mItemID = mItemID;
	}

	public String getmItemRevID() {
		return mItemRevID;
	}

	public void setmItemRevID(String mItemRevID) {
		this.mItemRevID = mItemRevID;
	}

	public String getmObjType() {
		return mObjType;
	}

	public void setmObjType(String mObjType) {
		this.mObjType = mObjType;
	}

	public String getmEndItemID() {
		return mEndItemID;
	}

	public void setmEndItemID(String mEndItemID) {
		this.mEndItemID = mEndItemID;
	}
	
	public boolean ismIsOccEffApplied() {
		return mIsOccEffApplied;
	}
	public void setmIsOccEffApplied(boolean mIsOccEffApplied) {
		this.mIsOccEffApplied = mIsOccEffApplied;
	}

	/**
	 * Constructor for the class.
	 * It is assumed that null checks on the input variables have been carried out in the calling method.
	 * @param dbCfgFilePath
	 */
	public DBConnBroker(String dbCfgFilePath) {		
		// sets the start time of the running of the component to a class member
		this.mTimeStart = System.currentTimeMillis();
		// initializes the logger for the SQL part of the Pre-Importer
		DBConnSingleton.getInstance().initializeLogger();
		// reads the database configuration information from the DB Config file
		
		this.mIsOccEffApplied = false;
		
		if(dbCfgFilePath != null) {
			DBConnUtilities.readDBCfgFile(dbCfgFilePath);
			if(DBConnSingleton.getInstance().getMapCmdArgs() != null && 
					DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_SQLQRYFILE)) {
				DBConnUtilities.readSQLQueryFile(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SQLQRYFILE));
			}
			// get the value of the optional argument "logLevel"
			if(DBConnSingleton.getInstance().getMapCmdArgs() != null) {
				// this loop should be used to check for the mandatory command line arguments
				// get the value of mode
				this.mLogLevel = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CMD_ARG_LOGLEVEL);
				// defaulting the log level to INFO in case it is not set by the command line arguments
				if(this.mLogLevel == null) {
					this.mLogLevel = DBConnUtilities.ARG_MODE_INFO;
					DBConnSingleton.getInstance().setmMode(DBConnUtilities.ARG_MODE_INFO);
				}
				// sets the default log level to INFO
				else {
					DBConnSingleton.getInstance().setmMode(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CMD_ARG_LOGLEVEL));
				}
				// writes out the log level to the log file
				if(DBConnSingleton.getInstance().getmLogObj() != null) {
					DBConnSingleton.getInstance().getmLogObj().writeToLogFile("LOG LEVEL SET TO: " + this.mLogLevel, DBConnUtilities.LOG_LEV_INFO);
				}
				
				// retrieve the Database decrypted password
				if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_PWDFILE)) {
					// retrieves the de-crypted Oracle database password and stores it in a member of the Singelton class
					DBConnSingleton.getInstance().setmDBPassword(DBConnUtilities.getDBPassword()); 
				}
			}
		}
	}
	
	public DBConnBroker(String dbCfgFilePath, Logger sLF4J_LOGGER) {		
		// sets the start time of the running of the component to a class member
		this.mTimeStart = System.currentTimeMillis();
		//sma4uLogger.info("DB CONFIG FILE : "+mTimeStart);
		sLF4J_LOGGER.info("currentTimeMillis : "+mTimeStart);
		// initializes the logger for the SQL part of the Pre-Importer
		//DBConnSingleton.getInstance().initializeLogger(sLF4J_LOGGER);
		// reads the database configuration information from the DB Config file
		
		this.mIsOccEffApplied = false;
		
		if(dbCfgFilePath != null) {
			DBConnUtilities.readDBCfgFile(dbCfgFilePath, sLF4J_LOGGER);
			if(DBConnSingleton.getInstance().getMapCmdArgs() != null && 
					DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_SQLQRYFILE)) {
				DBConnUtilities.readSQLQueryFile(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SQLQRYFILE));
			}
			// get the value of the optional argument "logLevel"
			if(DBConnSingleton.getInstance().getMapCmdArgs() != null) {
				// this loop should be used to check for the mandatory command line arguments
				// get the value of mode
				this.mLogLevel = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CMD_ARG_LOGLEVEL);
				// defaulting the log level to INFO in case it is not set by the command line arguments
				if(this.mLogLevel == null) {
					this.mLogLevel = DBConnUtilities.ARG_MODE_INFO;
					DBConnSingleton.getInstance().setmMode(DBConnUtilities.ARG_MODE_INFO);
				}
				// sets the default log level to INFO
				else {
					DBConnSingleton.getInstance().setmMode(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CMD_ARG_LOGLEVEL));
				}
				// writes out the log level to the log file
				if(DBConnSingleton.getInstance().getmLogObj() != null) {
					sLF4J_LOGGER.info("LOG LEVEL SET TO: " + this.mLogLevel);
				}
				
				// retrieve the Database decrypted password
				if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_PWDFILE)) {
					// retrieves the de-crypted Oracle database password and stores it in a member of the Singelton class
					DBConnSingleton.getInstance().setmDBPassword(DBConnUtilities.getDBPassword()); 
				}
			}
		}
	}
	
	/**
	 * Constructor for the class.
	 * It is assumed that null checks on the input variables have been carried out in the calling method.
	 * @param itemID
	 * @param itemRevID
	 * @param objType
	 * @param endItemID
	 */
	public DBConnBroker(String itemID, String itemRevID, String objType, String endItemID, String dbCfgFilePath) {		
		// setting of the class members to values passed as arguments in the constructor
		this.mItemID = itemID;
		this.mItemRevID = itemRevID;
		this.mObjType = objType;
		this.mEndItemID = endItemID;
		this.mIsOccEffApplied = false;
		
		// sets the start time of the running of the component to a class member
		this.mTimeStart = System.currentTimeMillis();		
		
		// initializes the logger for the SQL part of the Pre-Importer
		DBConnSingleton.getInstance().initializeLogger();
		// reads the database configuration information from the DB Config file
		if(dbCfgFilePath != null) {
			DBConnUtilities.readDBCfgFile(dbCfgFilePath);
			// get the value of the optional argument "logLevel"
			if(DBConnSingleton.getInstance().getMapCmdArgs() != null) {
				// this loop should be used to check for the mandatory command line arguments
				// get the value of mode
				this.mLogLevel = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CMD_ARG_LOGLEVEL);
				// defaulting the log level to INFO in case it is not set by the command line arguments
				if(this.mLogLevel == null) {
					this.mLogLevel = DBConnUtilities.ARG_MODE_INFO;
					DBConnSingleton.getInstance().setmMode(DBConnUtilities.ARG_MODE_INFO);
				}
				// sets the default log level to INFO
				else {
					DBConnSingleton.getInstance().setmMode(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CMD_ARG_LOGLEVEL));
				}
				// writes out the log level to the log file
				if(DBConnSingleton.getInstance().getmLogObj() != null) {
					DBConnSingleton.getInstance().getmLogObj().writeToLogFile("LOG LEVEL SET TO: " + this.mLogLevel, DBConnUtilities.LOG_LEV_INFO);
				}
				
				// retrieve the Database decrypted password
				if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_PWDFILE)) {
					// retrieves the de-crypted Oracle database password and stores it in a member of the Singelton class
					DBConnSingleton.getInstance().setmDBPassword(DBConnUtilities.getDBPassword()); 
				}
			}
		}
	}
	
	/**
	 * 
	 * @param toDisconnect : boolean flag to decide whether to create a connection with database or terminate an existing connection
	 * @throws SQLException 
	 */
	public void manageDBConnect(boolean toDisconnect) throws SQLException {
		
		long timeBefore = 0L;									// time in milliseconds before the execution of an action
		long timeAfter	= 0L;									// time in millisecondds after the execution of an action
		
		// loop that disconnects from the Teamcenter database
		if(toDisconnect) {
			
			// close all the prepared statements
			if(DBConnSingleton.getInstance().getmMapQryName2StmtObj() != null && !DBConnSingleton.getInstance().getmMapQryName2StmtObj().isEmpty()) {
				// loop and close all the Statements
				for (Map.Entry<String, PreparedStatement> entryMap : DBConnSingleton.getInstance().getmMapQryName2StmtObj().entrySet()) {
					// closes the statements
					if(entryMap.getValue() != null) {
						entryMap.getValue().close();
					}
				}
			}
			
			if(this.mObjJDBC != null) {
				this.mObjJDBC.terminateDBConnect();
			}
			// sets the exit time to the class member
			this.mTimeEnd = System.currentTimeMillis();
			
			if(DBConnSingleton.getInstance().getmLogObj() != null) {
				DBConnSingleton.getInstance().getmLogObj().writeToLogFile("CONNECTION WITH THE TEAMCENTER DATABASE TERMINATED",	DBConnUtilities.LOG_LEV_INFO);
			}
			
			// writes out the time taken for the execution to the log file
			/*if(DBConnSingleton.getInstance().getmLogObj() != null) {
				DBConnSingleton.getInstance().getmLogObj().writeToLogFile("TIME TAKEN FOR THE RUNNING OF THE SQL DB COMPONENT IS: " + (this.mTimeEnd - this.mTimeStart)/1000 + "s", 
											DBConnUtilities.LOG_LEV_INFO);
			}*/
			
			// additionally close the logger instance as it is no longer needed
			if(DBConnSingleton.getInstance().getmLogObj() != null) {
				DBConnSingleton.getInstance().getmLogObj().closeWriter();
			}
		}
		// loop that connects to the Teamcenter database 
		else {
			// example code before integration of parameters from the import automation framework.
			String databsePassword= DBConnSingleton.getInstance().getmDBPassword();
			LOGGER.trace("Database password to be used: {}", databsePassword);
			if(this.mObjJDBC == null) {				
				if(databsePassword != null) {
					LOGGER.trace("Database password found: {}", databsePassword);
					this.mObjJDBC = new JDBCConnector(DBConnSingleton.getInstance().getmDBPassword());
				}
			}
			timeBefore = System.currentTimeMillis();
			
			// call to connect to Teamcenter database
			if(this.mObjJDBC != null) {
				this.mObjJDBC.connectToTCDatabase();

				// logging of the time taken to connect to the Teamcenter database
				timeAfter = System.currentTimeMillis();
				// writes out the execution time information to the output log file
				LOGGER.trace("TIME TAKEN TO CONNECT TO TEAMCENTER DB: " + ":" + (timeAfter-timeBefore) + " ms");
			}
			else 
			{
				LOGGER.error("DB Connection Arguments are missing : Password missing");
			}
		}
	}
	
	/**
	 * This method controls the retrieval of the data specific to the item revision (item_id + item_revision_id) from the Teamcenter database. 
	 * @param isOccEffApplied		: boolean flag which tells if the item revision has end-item based effectivity for it's BOM children. 
	 * @param isBVPresent			: boolean flag which tells if the item revision is a leaf level part in the BOM structure or not.
	 * @param isDatasetPresent		: boolean flag which tells if the item revision has an associated dataset or not. 
	 * @param String datasetType	: Dataset Type to be queried
	 * @return
	 */
	@SuppressWarnings("unused")
	public DBConnResponse executeDBAction (boolean isOccEffApplied, boolean isBVPresent, boolean isDatasetPresent, String datasetType) {
		LOGGER.debug(String.format("Exceute DB Action >> - isOccEffApplied: %b isBVPresent %b, isDatasetPresent : %b, datasetType %s", isOccEffApplied, isBVPresent, isDatasetPresent, datasetType));
		
		boolean bIsPrepStmtUsed = true;						// boolean flag which tells if Prepared Statements are used or not for SQL querying. In this case hard-coded to use Prep Stmt.
		
		long timeBefore			= 0L;						// variable that captures the timestamp before the execution of an action
		long timeAfter			= 0L;						// variable that captures the timestamp after the execution of an action
			
		String qryText			= null;						// text of a SQL query 
		String[] args 			= null;						// array of arguments to the CheckTCObjectExists SQL Query
		String[] objQryArgs		= null;						// array of arguments to the object type based SQL Query
		String[] occQryArgs		= null;						// array of arguments to the PS Occurrence query
		String[] dsQryArgs		= null;						// array of arguments to the Dataset query

		
		// initialize the DBConnResponse object
		DBConnResponse dbConnRespObj = new DBConnResponse();
	
		// setting the utility start Time
		DBConnSingleton.getInstance().setmStartTime(System.currentTimeMillis());
		LOGGER.debug(String.format("Start time: %d ", System.currentTimeMillis()));

		
		// assings the value to the member
		this.mIsOccEffApplied = isOccEffApplied;
		
		// checks the value of the end item ID.
		if(this.mEndItemID == null) {
			LOGGER.error(String.format("ENDITEM ID HAS NOT BEEN PROVIDED. HENCE CANNOT EXECUTE TEAMCENTER DB SQL."));
			// returns a null DBConnResponse object
			return null;
		}
				
		// block of code that executes the series of SQL queries to retrieve the required information from the Teamcenter database for the input 
		// item id and item revision id that are passed to the constructor of the current class.
		if(this.mObjJDBC != null) {			
			// logging of the time taken to connect to the Teamcenter database
		
			// 	block of code that checks if the item and item revision supplied as input to the class exists in Teamcenter database or not
			/*if(DBConnSingleton.getInstance().getmQryFileProp() != null) {
				qryText = DBConnSingleton.getInstance().getmQryFileProp().getProperty(DBConnUtilities.QRY_CHECKOBJ_EXISTS);
			}*/
			
			// retrieves the query text from the DBConnSingleton member map
			if(DBConnSingleton.getInstance().getmMapQryName2Text() != null) {
				LOGGER.trace(String.format("All Queries %s", DBConnSingleton.getInstance().getmMapQryName2Text().toString()));
				
				qryText = DBConnSingleton.getInstance().getmMapQryName2Text().get(DBConnUtilities.QRY_CHECKOBJ_EXISTS);
				//LOGGER.debug(String.format("QRY_CHECKOBJ_EXISTS: ", qryText));
				
			}
	
			if(qryText != null) {	
				
				// instantiates the map to pass the arguments to the executeSQLQuery method
				HashMap<Integer, String> qryMapArgIndx2Val = new HashMap<Integer, String>();
				
				// set the arguments for the sql query execution to check the existence of the item and item revision exist in Teamcenter
				if(bIsPrepStmtUsed == false) {
					args = new String[3];
					args[0] = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER);
					args[1] = "'" + this.mItemID + "'";
					args[2] = "'" + this.mItemRevID + "'";
				}
				else if(bIsPrepStmtUsed == true) {
					args = new String[1];
					args[0] = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER);
					qryMapArgIndx2Val.put(1,  this.mItemID);
					qryMapArgIndx2Val.put(2,  this.mItemRevID);
				}
				
				// execution of the SQL query
				timeBefore = System.currentTimeMillis();
				DBConnUtilities.executeSQLQuery(dbConnRespObj, DBConnUtilities.QRY_CHECKOBJ_EXISTS, qryText, this.mEndItemID, DBConnUtilities.QRY_SCOPE_ELEM, bIsPrepStmtUsed, qryMapArgIndx2Val, (Object[])args);
				timeAfter = System.currentTimeMillis();
				// sets the logging information
				if(ReaderSingleton.getReaderSingleton() != null) {
					ReaderSingleton.getReaderSingleton().setMapQry2ExecTime(DBConnUtilities.QRY_CHECKOBJ_EXISTS, (timeAfter-timeBefore));
					ReaderSingleton.getReaderSingleton().setMapQry2ExecuteFreq(DBConnUtilities.QRY_CHECKOBJ_EXISTS);
				}
				// set the argument array to null
				args = null;
				qryMapArgIndx2Val.clear();
				
				// checks the database response object to see if the item / item revision objects exist in Teamcenter or not
				if(dbConnRespObj != null) {
					// checks the value in the attribute map in the DB Response object
					if(dbConnRespObj.getmMapAttrs() != null && !dbConnRespObj.getmMapAttrs().isEmpty()) {
						if(dbConnRespObj.getmMapAttrs().containsKey(DBConnUtilities.COL_ITEM_PUID)) {
							// in the loop means that the item exists in Teamcenter
							// go on to check if the item revision exists in Teamcenter or not
							if(dbConnRespObj.getmMapAttrs().containsKey(DBConnUtilities.COL_REV_PUID)) {
								// this means that both the item and item revision are present. Go on to trigger the bigger queries to retrieve more information from DB
							 	// block of code that checks if the item and item revision supplied as input to the class exists in Teamcenter database or not
								if(DBConnSingleton.getInstance().getmQryFileProp() != null) {							
									// retrieves the query text from the DBConnSingleton member map
									if(DBConnSingleton.getInstance().getmMapQryName2Text() != null) {
										qryText = DBConnSingleton.getInstance().getmMapQryName2Text().get(this.mObjType);
									}

									// sets the arguments for the execution of the item / item revision specific query	
									if(qryText != null) {
										// population of query input parameters
										if(bIsPrepStmtUsed == false) {
											objQryArgs = new String[3];
											objQryArgs[0] = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER);
											objQryArgs[1] = "'" + dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_ITEM_PUID) + "'";
											objQryArgs[2] = "'" + dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID) + "'";
										}
										else if(bIsPrepStmtUsed == true) {
											objQryArgs = new String[1];
											objQryArgs[0] = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER);
											qryMapArgIndx2Val.put(1,  dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_ITEM_PUID));
											qryMapArgIndx2Val.put(2,  dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID));
										}
										
										// execution of the SQL query
										timeBefore = 0L;
										timeBefore = System.currentTimeMillis();
										DBConnUtilities.executeSQLQuery(dbConnRespObj, this.mObjType, qryText, this.mEndItemID, DBConnUtilities.QRY_SCOPE_ELEM, bIsPrepStmtUsed, qryMapArgIndx2Val, (Object[])objQryArgs);
										timeAfter = System.currentTimeMillis();
										// sets the logging information
										if(ReaderSingleton.getReaderSingleton() != null) {
											ReaderSingleton.getReaderSingleton().setMapQry2ExecTime(this.mObjType, (timeAfter-timeBefore));
											ReaderSingleton.getReaderSingleton().setMapQry2ExecuteFreq(this.mObjType);
										}
										// set the argument array to null
										objQryArgs = null;
										qryMapArgIndx2Val.clear();
										
										if(isBVPresent == true) {
											// block of code that triggers the query to retrieve the BOM Information from the Teamcenter database
											// checks the value of the is Occurrence Effectivity flag applied flag
											//if(isOccEffApplied == true) 
											//else 

											// loop that executes the query for the PS Occurrences without associated Occurrence Effectivity
											if(DBConnSingleton.getInstance().getmMapQryName2Text() != null) {
												qryText = DBConnSingleton.getInstance().getmMapQryName2Text().get(DBConnUtilities.QRY_PSOCC_WOEFF);
											}
											if(qryText != null) {
												// initiates and populates the query arguments
												if(bIsPrepStmtUsed == false) {
													occQryArgs = new String[2];
													occQryArgs[0] = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER);
													//occQryArgs[1] = "'" + dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_ITEM_PUID) + "'";
													occQryArgs[1] = "'" + dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID) + "'";
												}
												else if(bIsPrepStmtUsed == true) {
													occQryArgs = new String[1];
													occQryArgs[0] = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER);
													//qryMapArgIndx2Val.put(1,  dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_ITEM_PUID));
													qryMapArgIndx2Val.put(1,  dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID));
												}

												// execution of the SQL query
												timeBefore = 0L;
												timeBefore = System.currentTimeMillis();
												DBConnUtilities.executeSQLQuery(dbConnRespObj, DBConnUtilities.QRY_PSOCC_WOEFF, qryText, this.mEndItemID, DBConnUtilities.QRY_SCOPE_CONN, bIsPrepStmtUsed, qryMapArgIndx2Val, (Object[])occQryArgs);
												timeAfter = System.currentTimeMillis();
												// sets the logging information
												if(ReaderSingleton.getReaderSingleton() != null) {
													ReaderSingleton.getReaderSingleton().setMapQry2ExecTime(DBConnUtilities.QRY_PSOCC_WOEFF, (timeAfter-timeBefore));
													ReaderSingleton.getReaderSingleton().setMapQry2ExecuteFreq(DBConnUtilities.QRY_PSOCC_WOEFF);
												}

												// reset the array to null
												occQryArgs = null;
												qryMapArgIndx2Val.clear();
											}
											else {
												// error message: ps occurrence query clause not found in the query.properties file 
												LOGGER.error(String.format("QUERY CLAUSE MISSING FOR THE QUERY: %s . HENCE CANNOT RETRIEVE BOM SPECIFIC INFORMATION FOR OBJECT: < %s / %s >", DBConnUtilities.QRY_PSOCC_WOEFF, this.mItemID, this.mItemRevID));
											}


											// loop that executes the query for the PS Occurrences with associated Occurrence Effectivity
											// retrieves the query text from the DBConnSingleton member map
											if(DBConnSingleton.getInstance().getmMapQryName2Text() != null) {
												qryText = DBConnSingleton.getInstance().getmMapQryName2Text().get(DBConnUtilities.QRY_PSOCC_WITHEFF);
											}
											if(qryText != null) {
												// initiates and populates the query arguments
												if(bIsPrepStmtUsed == false) {
													occQryArgs = new String[2];
													occQryArgs[0] = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER);
													//occQryArgs[1] = "'" + dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_ITEM_PUID) + "'";
													occQryArgs[1] = "'" + dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID) + "'";
												}
												else if(bIsPrepStmtUsed == true) {
													occQryArgs = new String[1];
													occQryArgs[0] = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER);
													//qryMapArgIndx2Val.put(1,  dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_ITEM_PUID));
													qryMapArgIndx2Val.put(1,  dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID));
												}

												// execution of the SQL query
												timeBefore = 0L;
												timeBefore = System.currentTimeMillis();
												DBConnUtilities.executeSQLQuery(dbConnRespObj, DBConnUtilities.QRY_PSOCC_WITHEFF, qryText, this.mEndItemID, DBConnUtilities.QRY_SCOPE_CONN, bIsPrepStmtUsed, qryMapArgIndx2Val, (Object[])occQryArgs);
												timeAfter = System.currentTimeMillis();
												// sets the logging information
												if(ReaderSingleton.getReaderSingleton() != null) {
													ReaderSingleton.getReaderSingleton().setMapQry2ExecTime(DBConnUtilities.QRY_PSOCC_WITHEFF, (timeAfter-timeBefore));
													ReaderSingleton.getReaderSingleton().setMapQry2ExecuteFreq(DBConnUtilities.QRY_PSOCC_WITHEFF);
												}

												// reset the array to null
												//	occQryArgs = null;
												//qryMapArgIndx2Val.clear();
											}
											else {
												// error message: ps occurrence query clause not found in the query.properties file 
												LOGGER.error("QUERY CLAUSE MISSING FOR THE QUERY: " 
															+ DBConnUtilities.QRY_PSOCC_WITHEFF + ". HENCE CANNOT RETRIEVE BOM SPECIFIC INFORMATION FOR OBJECT: <"
															+ this.mItemID + "/" + this.mItemRevID + ">");
											}

										}
										// executes the database query corresponding to datasets in case the input flag and the dataset type are provided by the calling method
										// currently the program flows allows for just one dataset type (AS-PLM <> JT). In the future this should be enhanced to handle querying
										// for more than one dataset type for the same item revision
										if(isDatasetPresent == true && datasetType != null) {
											// sets the dataset type 
											dbConnRespObj.setmDatasetType(datasetType);
											if(!dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID).isEmpty()) {
												// retrieves the query text from the DBConnSingleton member map
												if(DBConnSingleton.getInstance().getmMapQryName2Text() != null) {
													qryText = DBConnSingleton.getInstance().getmMapQryName2Text().get(DBConnUtilities.QRY_DATASET);
												}
												if(qryText != null) {
													// population of query input parameters
													if(bIsPrepStmtUsed == false) {
														dsQryArgs = new String[3];
														dsQryArgs[0] = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER);
														dsQryArgs[1] = "'" + datasetType + "'";
														dsQryArgs[2] = "'" + dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID) + "'";
													}
													else if(bIsPrepStmtUsed == true) {
														dsQryArgs = new String[1];
														dsQryArgs[0] = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER);
														qryMapArgIndx2Val.put(1,  datasetType);
														qryMapArgIndx2Val.put(2,  dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID));
													}
													// execution of the SQL query
													timeBefore = 0L;
													timeBefore = System.currentTimeMillis();
													LOGGER.info("EXECUTING DATASET QUERY FOR THE ITEM-REVISION WITH ITEM-ID: " + this.mItemID
															+ " AND WITH REV-ID: " + this.mItemRevID + " AND WITH ITEM-REV PUID: " + dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID));
													
													DBConnUtilities.executeSQLQuery(dbConnRespObj, DBConnUtilities.QRY_DATASET, qryText, this.mEndItemID, DBConnUtilities.QRY_SCOPE_DATASET, bIsPrepStmtUsed, 
															qryMapArgIndx2Val, (Object[])dsQryArgs);
													timeAfter = System.currentTimeMillis();
													// sets the logging information
													if(ReaderSingleton.getReaderSingleton() != null) {
														ReaderSingleton.getReaderSingleton().setMapQry2ExecTime(DBConnUtilities.QRY_DATASET, (timeAfter-timeBefore));
														ReaderSingleton.getReaderSingleton().setMapQry2ExecuteFreq(DBConnUtilities.QRY_DATASET);
													}		
													// reset the array to null
													dsQryArgs = null;
													qryMapArgIndx2Val.clear();
												}
												else {
													if(DBConnSingleton.getInstance().getmLogObj() != null) {
														dbConnRespObj.seteCode("001");
														DBConnSingleton.getInstance().getmLogObj().writeToLogFile("UNABLE TO FIND DATASET QUERY TEXT FROM THE QUERY.PROPERTIES FILE." + dbConnRespObj.getmMapAttrs().get(DBConnUtilities.COL_REV_PUID)
																, DBConnUtilities.LOG_LEV_ERROR);
													}
												}
											}
										}
									}
									else {
										// error message: object query clause not found in the query.properties file
										LOGGER.error("QUERY CLAUSE MISSING FOR THE QUERY: " 
													+ this.mObjType + ". HENCE CANNOT RETRIEVE ITEM/ITEM-REVISION ATTRIBUTE INFORMATION FOR OBJECT: <"
															 + this.mItemID + "/" + this.mItemRevID + ">");
									}
								}
							}
						}
					}
					else {
						// this means that the response from the query is null.. hence no more processing
						LOGGER.info("NO OBJECT EXISTS IN TEAMCENTER WITH ITEM ID: " + this.mItemID 
									+ ". HENCE NO DATABASE RESULT HAS BEEN OBTAINED");
					}
				}
				else {
					// this means that the DBConnResponse object returned from the check object query is null.. so a null object will be returned...
					LOGGER.info("NO OBJECT EXISTS IN TEAMCENTER WITH ITEM ID: " + this.mItemID
							+ ". HENCE NO DATABASE RESULT HAS BEEN OBTAINED");
				}
			}
			else {
				LOGGER.error("QUERY CLAUSE MISSING FOR THE QUERY: " + DBConnUtilities.QRY_CHECKOBJ_EXISTS
						+ ". HENCE CANNOT PERFORM FURTHER QUERIES TO RETRIEVE OBJECT INFORMATION.");
					return null;
			}
		}
		
		return dbConnRespObj;
	}
	
	// main method to test the functionality
	public static void main(String args[]) {
		LOGGER.info("Test DB connection.. \n");
		String itemID = "C223    05048 001";
		String itemRevID = "0001.004";
		String endItemId = "AS_C223_FV_L";
		String objectType = "CD9SDPosVRevision";
		System.out.println(String.format("DB config file %s", args[0]) );
		String dbConfigFile= args[0];
		
		System.out.println(String.format("Log config file %s", args[1]) );
		String logConfigFile= args[1];
		
		Configurator.initialize(null, logConfigFile);
		
		DBConnBroker connBrokerObj = new DBConnBroker(dbConfigFile);
		if (connBrokerObj != null) {

			try {
				/*
				 * JDBCConnector jdbcConn = new JDBCConnector("root"); if(jdbcConn != null) {
				 * jdbcConn.connectToTCDatabase(); }
				 */
				connBrokerObj.manageDBConnect(false);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			connBrokerObj.setmItemID(itemID);
			connBrokerObj.setmItemRevID(itemRevID);
			connBrokerObj.setmEndItemID(endItemId);
			connBrokerObj.setmObjType(objectType);

			DBConnResponse resp = connBrokerObj.executeDBAction(true, true, false, null);

			/*
			 * connBrokerObj.setmItemID("C223    01971");
			 * connBrokerObj.setmItemRevID("0001.016");
			 * connBrokerObj.setmEndItemID("DMU_TG_REL");
			 * connBrokerObj.setmObjType("CD9SDPosRevision");
			 * 
			 * DBConnResponse resp = connBrokerObj.executeDBAction(true, true, false, null);
			 * 
			 * //System.out.println("Here");
			 * 
			 * //DBConnResponse resp = null;
			 * 
			 * connBrokerObj.setmItemID("C177    01279");
			 * connBrokerObj.setmItemRevID("0001.012");
			 * connBrokerObj.setmEndItemID("C253_AEJ_ALL_ENG_ASPLM");
			 * connBrokerObj.setmObjType("CD9SDPosRevision");
			 * 
			 * //resp = null;
			 * 
			 * resp = connBrokerObj.executeDBAction(true, true, false, null);
			 * 
			 * connBrokerObj.setmItemID("A0009956490_2");
			 * connBrokerObj.setmItemRevID("001");
			 * connBrokerObj.setmEndItemID("C253_AEJ_ALL_ENG_ASPLM");
			 * connBrokerObj.setmObjType("C9ModelRevision");
			 * 
			 * 
			 * resp = null;
			 * 
			 * resp = connBrokerObj.executeDBAction(false, false, true, "DirectModel");
			 * 
			 */

			try {
				connBrokerObj.manageDBConnect(true);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if( resp != null && resp.isExceptionThrown())
			{
				// System.out.println("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for : "+itemID+" Rev ID : "+itemRevID+" of Type : "+objectType);
				// LOGGER.error("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for : "+itemID+" Rev ID : "+itemRevID+" of Type : "+objectType);
				LOGGER.error("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for Item <" + itemID + "\\" + itemRevID + "> Type <" + objectType + ">");
				return;
			}
			if( resp != null && resp.geteCode()!= null && !resp.geteCode().equals("") && resp.geteCode().equals("001"))
			{
				// System.out.println("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for : "+itemID+" Rev ID : "+itemRevID+" of Type : "+objectType);
				// LOGGER.error("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for : "+itemID+" Rev ID : "+itemRevID+" of Type : "+objectType);
				LOGGER.error("EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY for Item <" + itemID + "\\" + itemRevID + "> Type <" + objectType + ">");
				return;
			}
			else if(resp != null && !resp.isExceptionThrown() && resp.getmMapAttrs() != null && resp.getmMapAttrs().size() <= 1)
			{
				//logger.info("No Data Found for Item ID:  No Mapping Attributes...... : "+itemID+" Rev ID : "+itemRevID+" of Type : "+entry.getKey()+"Revision");
				// System.out.println("Object Not Found in Teamcenter DB. Action will be new");
				// LOGGER.info("Object Not Found in Teamcenter DB. Action will be new");
				LOGGER.info("Object not Found in Teamcenter DB for Item <" + itemID + "\\" + itemRevID + "> Type <" + objectType + ">");
				return;
			}
			else  if(resp != null && !resp.isExceptionThrown() && resp.getmMapAttrs() != null && resp.getmMapAttrs().size() > 1)
			{
				// System.out.println("Object Found in Teamcenter DB for : "+itemID+" Rev ID : "+itemRevID+" of Type : "+objectType);
				// LOGGER.info("Object Found in Teamcenter DB for: " +itemID+" Rev ID : "+itemRevID+" of Type : "+objectType);
				LOGGER.info("Object found in Teamcenter DB for Item <" + itemID + "\\" + itemRevID + "> Type <" + objectType + ">");
				//return;
			}
			
			System.out.println("done --> " + resp.toString());
			
			LOGGER.info("\n\n .. End Test DB connection.\n");
		}
	}

}
