package cdm.pre.imp.dbconnector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author amit.rath
 *
 */

public class DBConnUtilities {
	final private static Logger LOGGER = LogManager.getLogger(DBConnUtilities.class);

	// defines for the SQL query names as defined in the query.properties

	// item SQL queries
	public static final String QRY_BR_ITEM			= "brItemQry";
	public static final String QRY_HM_ITEM 			= "hmItemQry";
	public static final String QRY_MOD_ITEM 		= "modItemQry";
	public static final String QRY_SMOD_ITEM 		= "smodItemQry";
	public static final String QRY_POS_ITEM 		= "posItemQry";
	public static final String QRY_POSV_ITEM 		= "posvItemQry";
	public static final String QRY_LAGE_ITEM 		= "lageItemQry";
	public static final String QRY_PART_ITEM 		= "partItemQry";
	public static final String QRY_MODEL_ITEM 		= "modelItemQry";
	public static final String QRY_INT_ITEM 		= "intItemQry";
	public static final String QRY_FLEX_ITEM 		= "flexItemQry";
	public static final String QRY_VWCMP_ITEM 		= "vwcmpItemQry";
	public static final String QRY_ARRG_ITEM 		= "argItemQry";
	public static final String QRY_STD_ITEM 		= "stdItemQry";
	public static final String QRY_SUPP_ITEM 		= "supItemQry";
	public static final String QRY_CARBR_ITEM 		= "carbrItemQry";
	public static final String QRY_CMP_ITEM			= "cmpItemQry";


	// item revision effectivity SQL query
	public static final String QRY_ITEMREV_EFF 		= "itemRevEffQry";

	// ps occurrence SQL query
	public static final String QRY_PS_OCC 			= "psoccQry";


	// output file separator
	public static final String SEP_OUT_FILE			= "||";
	public static final String SEP_QRY_FILE			= "@";

	// environment variable defines
	public static final String ENV_CDMIMPORT_LOG	= "CDM_IMPORTER_LOG";


	// defines for log levels
	public static final String LOG_LEV_INFO			= "INFO";
	public static final String LOG_LEV_WARNING		= "WARNING";
	public static final String LOG_LEV_ERROR		= "ERROR";


	// defines for the command line arguments
	public static final String CFG_ARG_USER			= "user";
	public static final String CFG_ARG_PWDFILE		= "passwordfile";
	public static final String CFG_ARG_PORT			= "port";
	public static final String CFG_ARG_SID			= "sid";
	public static final String CFG_ARG_SERVICE		= "service";
	public static final String CFG_ARG_LOGLEV		= "logLevel";
	public static final String CFG_ARG_OPDIR		= "opdir";
	public static final String CFG_ARG_HOST			= "host";
	public static final String CFG_ARG_TCOWNUSER	= "owner";
	public static final String CFG_ARG_SQLQRYFILE	= "sqlQryFile";

	// defines for command line argument values
	public static final String CMD_ARG_LOGLEVEL		= "logLevel";
	public static final String ARG_MODE_INFO		= "INFO";
	public static final String ARG_MODE_TRACE		= "TRACE";


	// defines for SQL Queries
	public static final String QRY_CHECKOBJ_EXISTS	= "CheckObjExists";
	public static final String QRY_PSOCC_WOEFF		= "PSOCC_WOEFF";
	public static final String QRY_PSOCC_WITHEFF	= "PSOCC_WITHEFF";
	public static final String QRY_DATASET			= "Dataset";

	// defines for database column names 
	public static final String COL_DATE_SEQ 		= "sequence";
	public static final String COL_EFF_DATE 		= "effective_date";
	public static final String COL_PROJ_ID			= "project_id";
	public static final String COL_ITEM_PUID		= "item_puid";
	public static final String COL_REV_PUID			= "rev_puid";
	public static final String COL_ENDITEM_ID		= "end_item";
	public static final String COL_BV_PUID			= "bv_puid";
	public static final String COL_BVR_PUID			= "bvr_puid";
	public static final String COL_CHILD_ITEMID		= "child_item_id";
	public static final String COL_CHILD_PUID		= "child_item_puid";
	public static final String COL_CHILDOCC_PUID	= "occ_puid";
	public static final String COL_OCC_RELCOUNT		= "C9RelCount";
	public static final String COL_OCC_CODERULE		= "C9CodeRule";
	public static final String COL_NOTE_TYPE		= "note_name";
	public static final String COL_NOTE_VALUE		= "note_value";


	public static final String QRY_SCOPE_ELEM		= "element";
	public static final String QRY_SCOPE_CONN		= "connection";
	public static final String QRY_SCOPE_DATASET	= "dataset";


	private static final String mSecretKey 			= "ssshhhhhhhhhhh!!!!";
	/**
	 * 
	 * @param objDBConResp
	 * @param queryName
	 * @param queryTxt
	 * @param endItemID
	 * @param qryScope
	 * @param bIsUsingPrepStmt
	 * @param mapQryArgIndx2Val
	 * @param qryArgs
	 */
	public static void executeSQLQuery(DBConnResponse objDBConResp, String queryName, String queryTxt, String endItemID,
			String qryScope, boolean bIsUsingPrepStmt, HashMap<Integer, String> mapQryArgIndx2Val, Object... qryArgs) {

		LOGGER.debug(String.format(
				"Execute SQL query >> - queryName: %s queryTxt: %s, endItemID: %s, qryScope: %s, bIsUsingPrepStmt: %b, mapQryArgIndx2Val: %s, qryArgs: %s",
				queryName, queryTxt, endItemID, qryScope, bIsUsingPrepStmt, mapQryArgIndx2Val.toString(),
				Arrays.toString(qryArgs)));

		// int iCountQryResp = 0; // variable to hold the count of rows returned by the
		// query

		long timeBefore = 0L; // variable to hold the start time of the operation
		long timeAfter = 0L; // variable to hold the end time of the operation

		ResultSet rs = null; // variable to hold the object of the ResultSet
		PreparedStatement prepStmt = null;
		Statement stmt = null;

		//HashMap<String, String> colIndex2ValMap; // map that holds the map between the column name and the corresponding
													// column value as returned from the SQL query
		Vector<HashMap<String, String>> dbColValVect; // vector that holds several maps that hold individual query row
														// output

		if ((DBConnSingleton.getInstance().getmDBConnInst() != null) && (queryName != null)) {

			if (queryTxt == null) {
				if (DBConnSingleton.getInstance().getmLogObj() != null) {
					LOGGER.error(String.format(
							"NO QUERY TEXT FOUND IN THE query.properties FILE FOR THE QUERY: %s PROCESSING THE NEXT QUERY IN THE PROPERTIES FILE...",
							queryName));
					return;
				}
			}

			try {
				// generate the actual query text by replacing the variables with actual text
				if (qryArgs != null && qryArgs.length > 0) {
					// in case of Prepared Statement, there is always one argument - database table
					// owner name
					queryTxt = MessageFormat.format(queryTxt, qryArgs);
				}

				if (bIsUsingPrepStmt == true) {
					try {
						// try to get the PreparedStatement from the Singleton member map
						if (DBConnSingleton.getInstance().getmMapQryName2StmtObj() != null
								&& !DBConnSingleton.getInstance().getmMapQryName2StmtObj().isEmpty()) {
							if (DBConnSingleton.getInstance().getmMapQryName2StmtObj().containsKey(queryName)) {
								if (!DBConnSingleton.getInstance().getmMapQryName2StmtObj().get(queryName).isClosed()) {
									// assigns the Prepared Statement from the Singleton map
									prepStmt = DBConnSingleton.getInstance().getmMapQryName2StmtObj().get(queryName);
									LOGGER.debug(String.format(
											"existing prepared statement for query %s got from singelton map",
											queryName));
								} else {
									// creates a Prepared Statement
									LOGGER.debug(String.format(
											"new prepared statement for query %s is created because the existing one is closed",
											queryName));
									prepStmt = createPreparedStatement(queryName, queryTxt);
								}
							} else {
								// creates a Prepared Statement
								LOGGER.debug(String.format(
										"new prepared statement for query %s is created because not found in singelton map",
										queryName));
								prepStmt = createPreparedStatement(queryName, queryTxt);
							}
						} else {
							// when the singleton map is empty
							// creates a Prepared Statement
							LOGGER.debug(String.format(
									"new prepared statement for query %s is created because singelton map is empty",
									queryName));
							prepStmt = createPreparedStatement(queryName, queryTxt);
						}

						// population of the query variables
						if (prepStmt != null) {
							if (mapQryArgIndx2Val != null && !mapQryArgIndx2Val.isEmpty()) {
								LOGGER.debug(String.format("mapQryArgIndx2Val: %s", mapQryArgIndx2Val.toString()));
								// iterate through the map. Here the assumption is that all the query variables
								// are of type string
								for (int iInx = 0; iInx < mapQryArgIndx2Val.size(); ++iInx) {
									// sets the variables to the query statement
									LOGGER.debug(String.format(
											"QUERY: %s SETTING THE PARAMETER WITH INDEX: %s AND VALUE: %s", queryName,
											(iInx + 1), mapQryArgIndx2Val.get(iInx + 1)));
									prepStmt.setString((iInx + 1), mapQryArgIndx2Val.get(iInx + 1));
								}
							}
						} else {
							stmt = DBConnSingleton.getInstance().getmDBConnInst().createStatement();
						}

						/*
						 * if(DBConnSingleton.getInstance().getmMode() != null &&
						 * !DBConnSingleton.getInstance().getmMode().isEmpty()) {
						 * if(DBConnSingleton.getInstance().getmMode().equals(DBConnUtilities.
						 * ARG_MODE_TRACE)) { stmt =
						 * DBConnSingleton.getInstance().getmDBConnInst().createStatement(ResultSet.
						 * TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); } }
						 */

						// loop that executes the query and retrieves the query result
						if (stmt != null || prepStmt != null) {
							// log output for trace log level
							LOGGER.debug(
									String.format("EXECUTING THE SQL QUERY: %s  WITH TEXT %s", queryName, queryTxt));
							timeBefore = System.currentTimeMillis();

							if (prepStmt != null) {
								// executes the SQL query
								// System.out.println("Query :::: \n "+prepStmt);
								LOGGER.debug(String.format("run sql prepared statement"));
								rs = prepStmt.executeQuery();
								// String executedQuery = rs.getStatement().toString();
								// System.out.println("Query :::::: \n "+executedQuery);
							} else if (stmt != null) {
								LOGGER.debug(String.format("run sql normal statement"));
								rs = stmt.executeQuery(queryTxt);
							}

							// checks if the query returned a result set or not
							if (rs != null && rs.next()) {
								try {
									// log output for trace log level
									timeAfter = System.currentTimeMillis();
									String prepQry = prepStmt.toString();
									if (prepQry != null && !prepQry.isEmpty()) {
										LOGGER.debug(String.format("EQUERY: %s TIME TAKEN IS ----->  %d ms.", queryTxt,
												(timeAfter - timeBefore)));
									}

									// initialize the vector that will hold the values read from the database
									dbColValVect = new Vector<HashMap<String, String>>();
									LOGGER.trace(String.format("Extract values from DB query result"));
									// LOGGER.trace(String.format("+ Check DB query result status"));
									// LOGGER.trace(String.format(" - isClosed: %b", rs.isClosed()));
									// LOGGER.trace(String.format(" - isFirst: %b", rs.isFirst()));
									// LOGGER.trace(String.format(" - isBeforeFirst: %b", rs.isBeforeFirst()));
									// loops through the result set and retrieves column specific data from the
									// query output
									extractDBResultEntry(rs, dbColValVect);
									while (rs.next()) {
										// re-initialization for each entry in the while loop
										extractDBResultEntry(rs, dbColValVect);
									}
									// closes the ResultSet and the Statement objects
								} finally {
									rs.close();
								}

								StringBuilder queryResponse = new StringBuilder();
								dbColValVect
										.forEach((n) -> queryResponse.append(String.format("\n  %s", n.toString())));
								LOGGER.debug(String.format("Query %s (Scope : %s) result is: %s", queryName, qryScope,
										queryResponse.toString()));

								// block of code that will initiate the population of the DBConnResponse object
								if (dbColValVect != null && !(dbColValVect.isEmpty())) {
									if (!qryScope.isEmpty()) {
										if (qryScope.equals(DBConnUtilities.QRY_SCOPE_ELEM) == true) {
											// processing of the information of queried item / item revision information
											objDBConResp.processObjQueryOutput(dbColValVect, endItemID);
										} else if (qryScope.equals((DBConnUtilities.QRY_SCOPE_CONN)) == true) {
											// processing of the information of queried ps occurrence information
											objDBConResp.processOccQueryOutput(queryName, dbColValVect);
										} else if (qryScope.equals((DBConnUtilities.QRY_SCOPE_DATASET)) == true) {
											// processing of the information of queried dataset information
											if (DBConnSingleton.getInstance().getmLogObj() != null) {
												if (dbColValVect != null && dbColValVect.size() > 0) {
													DBConnSingleton.getInstance().getmLogObj().writeToLogFile(
															"SIZE OF THE DATASET QUERY RESULT-SET IS: "
																	+ dbColValVect.size(),
															DBConnUtilities.LOG_LEV_INFO);
												} else {
													DBConnSingleton.getInstance().getmLogObj().writeToLogFile(
															"SIZE OF THE DATASET QUERY RESULT-SET IS NULL",
															DBConnUtilities.LOG_LEV_INFO);
												}
											}
											objDBConResp.processDatasetQueryOutput(queryName, dbColValVect);

										}
									}
								}

								// prepStmt.close();
							} else {
								LOGGER.debug(String.format("Empty query result"));
								// Release - Feb, 04, 2020 - Handling of empty result set
								// In case the result set is null for the query - PSOCC_WITHEFF
								if (queryName != null && !queryName.isEmpty()
										&& queryName.equals(DBConnUtilities.QRY_PSOCC_WITHEFF)) {
									String strNewQry = null;
									// execute the query for PSOCC without effectivity input - PSOCC_WOEFF
									if (DBConnSingleton.getInstance().getmMapQryName2Text() != null) {
										strNewQry = DBConnSingleton.getInstance().getmMapQryName2Text()
												.get(DBConnUtilities.QRY_PSOCC_WOEFF);
									}
									if (strNewQry != null && !strNewQry.isEmpty()) {
										DBConnUtilities.executeSQLQuery(objDBConResp, DBConnUtilities.QRY_PSOCC_WOEFF,
												strNewQry, endItemID, DBConnUtilities.QRY_SCOPE_CONN, bIsUsingPrepStmt,
												mapQryArgIndx2Val, qryArgs);

									}
								}
							}
						}
					} finally {
						if (prepStmt != null)
							prepStmt.close();
					}
				}
				
			} catch (SQLException e) {
				objDBConResp.setExceptionThrown(true);
				LOGGER.error("THE FOLLOWING EXCEPTION HAS BEEN ENCOUNTERED IN THE QUERY " + queryName + " : "
						+ e.getMessage(), e);
			}
		}
	}

	private static void extractDBResultEntry(ResultSet rs, Vector<HashMap<String, String>> dbColValVect)
			throws SQLException {
		HashMap<String, String> colIndex2ValMap;
		int coulmsCount = rs.getMetaData().getColumnCount();
		//LOGGER.trace(String.format("+ Extract values from DB query result entry"));
		//LOGGER.trace(String.format("  - Columns count %d", coulmsCount));
		colIndex2ValMap = new HashMap<String, String>();
		for (int iInx = 1; iInx < (coulmsCount + 1); ++iInx) {
			// maps the column name to the column value as returned from the SQL query
			//LOGGER.trace(String.format("  - Iteration nr° %d", iInx));
			//LOGGER.trace(String.format("    * ColumnName: %s",
			//		rs.getMetaData().getColumnName(iInx)));
			//LOGGER.trace(String.format("    * Column value: %s", rs.getString(iInx)));
			
			try {
				colIndex2ValMap.put(rs.getMetaData().getColumnName(iInx), rs.getString(iInx));
			} catch (SQLException e) {
				LOGGER.error(String.format("Error while getting value from SQL result entry"), e);
				LOGGER.trace(String.format("  ResultSet is closed: %b", rs.isClosed()));
				LOGGER.trace(String.format("  Column index: %d", iInx));
				LOGGER.trace(String.format("  Columns count: %d", coulmsCount));
				
				throw  new SQLException(e);
			}
		}

		// pushes each row of the query result as a map to the vector
		if (colIndex2ValMap != null) {
			dbColValVect.add(colIndex2ValMap);
		}
	}

	private static PreparedStatement createPreparedStatement(String queryName, String queryTxt) throws SQLException {
		PreparedStatement prepStmt;
		prepStmt = DBConnSingleton.getInstance().getmDBConnInst().prepareStatement(queryTxt);
		// Workaround: Statement are not saved because when use within many threads there is race conditions issue
		/*
		if(prepStmt != null) {
			// populates the newly created map to the Singleton map
			if(DBConnSingleton.getInstance().getmMapQryName2StmtObj() != null) {
				DBConnSingleton.getInstance().getmMapQryName2StmtObj().put(queryName, prepStmt);
			}
		}
		*/
		return prepStmt;
	}

	/**
	 * 
	 * @return
	 */
	public static HashMap<String, String> getAllQueries() {

		HashMap<String, String> mapQryName2Text = null;				// output variable for the method

		// output variable initialization
		mapQryName2Text = new HashMap<String, String>();

		Properties qryFileProp = new Properties();
		qryFileProp = DBConnSingleton.getInstance().getmQryFileProp();
		if(qryFileProp != null) {
			Enumeration<?> enumPropName = qryFileProp.propertyNames();
			if(enumPropName != null) {
				// loops through the enumeration
				while(enumPropName.hasMoreElements()) {
					String currQryName = (String) enumPropName.nextElement();
					if(currQryName != null && !currQryName.isEmpty()) {
						String qryText = MessageFormat.format((String) qryFileProp.get(currQryName), 
								DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER));
						if(qryText != null) {
							mapQryName2Text.put(currQryName, qryText);
							qryText = null;
						}
					}
					currQryName = null;
				}
			}
		}
		return mapQryName2Text;
	}

	/**
	 * 
	 * @param args
	 */
	public static void readCmdLineArgs(String[] args) {

		if(args != null && args.length == 1) {
			// iterates through the args
			for(String arg : args) {
				// split the args based on the scheme <arg name>=<arg value>
				String[] argEntries = arg.split("=");
				if(argEntries != null && argEntries.length == 2) {
					// fills up the Singleton argument map
					DBConnUtilities.readDBCfgFile(argEntries[1]);
				}
				else {
					if(DBConnSingleton.getInstance().getmLogObj() != null) {
						DBConnSingleton.getInstance().getmLogObj().writeToLogFile("INVALID COMMAND LINE ARGUMENT ENTRY:" + arg + ". "
								+ "PROPER FORMAT IS: <ARGUMENT NAME>=<ARGUMENT VALUE>." , DBConnUtilities.LOG_LEV_WARNING);
					}
					System.out.println("WARNING: INVALID COMMAND LINE ARGUMENT ENTRY:" + arg + ". PROPER FORMAT IS: <ARGUMENT NAME>=<ARGUMENT VALUE>");
				}
			}
		}
		else {
			// error for no command line arguments
			if(DBConnSingleton.getInstance().getmLogObj() != null) {
				DBConnSingleton.getInstance().getmLogObj().writeToLogFile("INCORRECT NUMBER OF ARGUMENTS SUPPLIED TO THE TOOL. "
						+ "HENCE EXITING THE UTILITY... " , DBConnUtilities.LOG_LEV_ERROR);
				DBConnSingleton.getInstance().getmLogObj().closeWriter();
			}
			System.out.println("FATAL ERROR: INCORRECT NUMBER OF ARGUMENTS SUPPLIED TO THE TOOL. HENCE EXITING THE UTILITY");
			//DBConnUtilities.printProgramHelp();
			System.exit(-1);
		}

	}



	/**
	 * 
	 * @param cfgFilePath
	 */
	public static void readDBCfgFile(String cfgFilePath) {

		// check if the file exists or not
		File fileObj = new File(cfgFilePath);
		if(fileObj != null) {
			if(!fileObj.exists()) {
				// error for no command line arguments
				if(DBConnSingleton.getInstance().getmLogObj() != null) {
					DBConnSingleton.getInstance().getmLogObj().writeToLogFile("DATABASE CONFIGURATION FILE: " + cfgFilePath + " DOES NOT EXIST. "
							+ "HENCE EXITING THE UTILITY...", DBConnUtilities.LOG_LEV_ERROR);
					DBConnSingleton.getInstance().getmLogObj().closeWriter();
				}
				System.out.println("FATAL ERROR: DATABASE CONFIGURATION FILE: " + cfgFilePath + " DOES NOT EXIST. HENCE EXITING THE UTILITY...");
				//DBConnUtilities.printProgramHelp();
				//System.exit(-1);
				return;
			}
			else {
				// read the configuration file details from the configuration file
				try {
					BufferedReader bufReadObj = new BufferedReader(new FileReader(fileObj));
					if(bufReadObj != null) {
						String line = null;
						while((line = bufReadObj.readLine()) != null) {
							String[] argEntries = line.split("=");
							if(argEntries != null && argEntries.length == 2) {
								DBConnSingleton.getInstance().setMapCmdArgs(argEntries[0], argEntries[1]);
							}
							else {
								if(DBConnSingleton.getInstance().getmLogObj() != null) {
									DBConnSingleton.getInstance().getmLogObj().writeToLogFile("INVALID COMMAND LINE ARGUMENT ENTRY:" + line + 
											". PROPER FORMAT IS: <ARGUMENT NAME>=<ARGUMENT VALUE>", DBConnUtilities.LOG_LEV_WARNING);
								}
								System.out.println("WARNING: INVALID COMMAND LINE ARGUMENT ENTRY:" + line + 
										". PROPER FORMAT IS: <ARGUMENT NAME>=<ARGUMENT VALUE>");
							}
						}
						// validates the mandatory arguments
						if(DBConnSingleton.getInstance().getMapCmdArgs() != null && DBConnSingleton.getInstance().getMapCmdArgs().size() > 0 ) {

							DBConnSingleton.getInstance().getmLogObj().writeToLogFile("Reading DB config properties : ","INFO");
							DBConnSingleton.getInstance().getMapCmdArgs().entrySet().forEach(entry -> {
								DBConnSingleton.getInstance().getmLogObj().writeToLogFile(((Entry<String, String>) entry).getKey() + " " + ((Entry<String, String>) entry).getValue(),"INFO");
							});

							// checks for the presence of the key in the map and a corresponding value
							if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_USER) &&
									DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_USER) == null) {
								if(DBConnSingleton.getInstance().getmLogObj() != null) {
									DBConnSingleton.getInstance().getmLogObj().writeToLogFile("TC ORACLE USER NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...", 
											DBConnUtilities.LOG_LEV_ERROR);
									DBConnSingleton.getInstance().getmLogObj().closeWriter();
								}
								System.out.println("FATAL ERROR: TC ORACLE USER NOT SPECIFIED IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
								//DBConnUtilities.printProgramHelp();
								System.exit(-1);
							}
							if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_PWDFILE) &&
									DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_PWDFILE) == null) {
								if(DBConnSingleton.getInstance().getmLogObj() != null) {
									DBConnSingleton.getInstance().getmLogObj().writeToLogFile("TC ORACLE ENCRYPETD PWD FILE NOT SPECIFIED "
											+ "IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...", DBConnUtilities.LOG_LEV_ERROR);
									DBConnSingleton.getInstance().getmLogObj().closeWriter();
								}
								System.out.println("FATAL ERROR: TC ORACLE ENCRYPETD PWD FILE NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								//DBConnUtilities.printProgramHelp();
								//System.exit(-1);
								//break;
							}
							// retrieves the de-crypyted password
							else {
								// retrieves the de-crypted Oracle database password and stores it in a member of the Singelton class
								DBConnSingleton.getInstance().setmDBPassword(DBConnUtilities.getDBPassword());
								// error handling in case of null password
								if(DBConnSingleton.getInstance().getmDBPassword() == null) {
									if(DBConnSingleton.getInstance().getmLogObj() != null) {
										DBConnSingleton.getInstance().getmLogObj().writeToLogFile("TC ORACLE PWD NOT RETRIEVED. "
												+ "HENCE CANNOT PERFORM DB OPERATIONS...", DBConnUtilities.LOG_LEV_ERROR);
										DBConnSingleton.getInstance().getmLogObj().closeWriter();
									}
									System.out.println("FATAL ERROR: TC ORACLE PWD NOT RETRIEVED. HENCE EXITING THE UTILITY...");
									//DBConnUtilities.printProgramHelp();
									//System.exit(-1);
									//return;
								}
							}
							if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_HOST) &&
									DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_HOST) == null) {
								if(DBConnSingleton.getInstance().getmLogObj() != null) {
									DBConnSingleton.getInstance().getmLogObj().writeToLogFile("TC ORACLE DB SERVER HOSTNAME NOT SPECIFIED "
											+ "IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...", DBConnUtilities.LOG_LEV_ERROR);
									DBConnSingleton.getInstance().getmLogObj().closeWriter();
								}
								System.out.println("FATAL ERROR: TC ORACLE DB SERVER HOSTNAME NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								//DBConnUtilities.printProgramHelp();
								//System.exit(-1);
								//return;
							}
							if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_PORT) &&
									DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_PORT) == null) {
								if(DBConnSingleton.getInstance().getmLogObj() != null) {
									DBConnSingleton.getInstance().getmLogObj().writeToLogFile("TC ORACLE DB LISTENER PORT NOT SPECIFIED "
											+ "IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...", DBConnUtilities.LOG_LEV_ERROR);
									DBConnSingleton.getInstance().getmLogObj().closeWriter();
								}
								System.out.println("FATAL ERROR: TC ORACLE DB LISTENER PORT NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								//DBConnUtilities.printProgramHelp();
								//System.exit(-1);
								//return;
							}
							if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_SID) &&
									DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SID) == null) {
								if(DBConnSingleton.getInstance().getmLogObj() != null) {
									DBConnSingleton.getInstance().getmLogObj().writeToLogFile("TC ORACLE DB SID NOT SPECIFIED "
											+ "IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...", DBConnUtilities.LOG_LEV_ERROR);
									DBConnSingleton.getInstance().getmLogObj().closeWriter();
								}
								System.out.println("FATAL ERROR: TC ORACLE DB SID NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								//DBConnUtilities.printProgramHelp();
								//System.exit(-1);
								//return;
							}
							if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_TCOWNUSER) &&
									DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_TCOWNUSER) == null) {
								if(DBConnSingleton.getInstance().getmLogObj() != null) {
									DBConnSingleton.getInstance().getmLogObj().writeToLogFile("TC TABLES' ORACLE OWNING USER NOT SPECIFIED "
											+ "IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...", DBConnUtilities.LOG_LEV_ERROR);
									DBConnSingleton.getInstance().getmLogObj().closeWriter();
								}
								System.out.println("FATAL ERROR: TC TABLES' ORACLE OWNING USER"
										+ " NOT SPECIFIED IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
								//DBConnUtilities.printProgramHelp();
								//System.exit(-1);
								//return;
							}
							if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_SQLQRYFILE) &&
									DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SQLQRYFILE) == null) {
								if(DBConnSingleton.getInstance().getmLogObj() != null) {
									DBConnSingleton.getInstance().getmLogObj().writeToLogFile("SQL QUERY FILE NOT PROVIDED IN THE DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...", 
											DBConnUtilities.LOG_LEV_ERROR);
									DBConnSingleton.getInstance().getmLogObj().closeWriter();
								}
								System.out.println("FATAL ERROR: TC ORACLE USER NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								//DBConnUtilities.printProgramHelp();
								//System.exit(-1);
								//	return;
							}
						}
						bufReadObj.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}


	public static void readDBCfgFile(String cfgFilePath, Logger sLF4J_LOGGER) {
		// check if the file exists or not
		LOGGER.info("tentative to read database configuration file {}", cfgFilePath);

		File fileObj = new File(cfgFilePath);
		if (fileObj != null) {
			if (!fileObj.exists()) {
				// error for no command line arguments
				LOGGER.error("DATABASE CONFIGURATION FILE: " + cfgFilePath + " DOES NOT EXIST. "
						+ "HENCE EXITING THE UTILITY...");
				System.out.println("FATAL ERROR: DATABASE CONFIGURATION FILE: " + cfgFilePath
						+ " DOES NOT EXIST. HENCE EXITING THE UTILITY...");
				// DBConnUtilities.printProgramHelp();
				// System.exit(-1);
				return;
			} else {
				// read the configuration file details from the configuration file
				try {
					BufferedReader bufReadObj = new BufferedReader(new FileReader(fileObj));
					if (bufReadObj != null) {
						String line = null;
						while ((line = bufReadObj.readLine()) != null) {
							LOGGER.info("DB config line : {}", line);
							String[] argEntries = line.split("=");
							if (argEntries != null && argEntries.length == 2) {
								LOGGER.info("DB configuration [entry , value] pair  read : [{},{}]",
										argEntries[0], argEntries[1]);
								DBConnSingleton.getInstance().setMapCmdArgs(argEntries[0].trim(), argEntries[1].trim());
							} else {
								LOGGER.error("WARNING: INVALID COMMAND LINE ARGUMENT ENTRY:" + line
										+ ". PROPER FORMAT IS: <ARGUMENT NAME>=<ARGUMENT VALUE>");
							}
						}
						// validates the mandatory arguments
						if (DBConnSingleton.getInstance().getMapCmdArgs() != null
								&& DBConnSingleton.getInstance().getMapCmdArgs().size() > 0) {

							LOGGER.info("Reading DB config properties : {}",
									DBConnSingleton.getInstance().getMapCmdArgs());

							// checks for the presence of the key in the map and a corresponding value
							LOGGER.info("checks for the presence of the key in the map and a corresponding value");
							if (!DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_USER)
									|| null == DBConnSingleton.getInstance().getMapCmdArgs()
											.get(DBConnUtilities.CFG_ARG_USER)) {
								LOGGER.error(
										"TC ORACLE USER NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								LOGGER.error(
										"FATAL ERROR: TC ORACLE USER NOT SPECIFIED IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
							}
							if (!DBConnSingleton.getInstance().getMapCmdArgs()
									.containsKey(DBConnUtilities.CFG_ARG_PWDFILE)
									|| null == DBConnSingleton.getInstance().getMapCmdArgs()
											.get(DBConnUtilities.CFG_ARG_PWDFILE)) {
								LOGGER.error("TC ORACLE ENCRYPETD PWD FILE NOT SPECIFIED "
										+ "IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
								LOGGER.error(
										"FATAL ERROR: TC ORACLE ENCRYPETD PWD FILE NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");

								// DBConnUtilities.printProgramHelp();
								// System.exit(-1);
								// break;
							}
							// retrieves the de-crypyted password
							else {
								// retrieves the de-crypted Oracle database password and stores it in a member
								// of the Singelton class
								String dbPassword = getDBPassword();
								DBConnSingleton.getInstance().setmDBPassword(dbPassword);
								// error handling in case of null password
								if (DBConnSingleton.getInstance().getmDBPassword() == null) {
									if (DBConnSingleton.getInstance().getmLogObj() != null) {
										LOGGER.info("TC ORACLE PWD NOT RETRIEVED. "
												+ "HENCE CANNOT PERFORM DB OPERATIONS...");
									}
									// System.out.println("FATAL ERROR: TC ORACLE PWD NOT RETRIEVED. HENCE EXITING
									// THE UTILITY...");
									LOGGER.info(
											"FATAL ERROR: TC ORACLE PWD NOT RETRIEVED. HENCE EXITING THE UTILITY...");

									// DBConnUtilities.printProgramHelp();
									// System.exit(-1);
									// return;
								}
							}
							if (!DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_HOST)
									|| DBConnSingleton.getInstance().getMapCmdArgs()
											.get(DBConnUtilities.CFG_ARG_HOST) == null) {
								LOGGER.info("TC ORACLE DB SERVER HOSTNAME NOT SPECIFIED "
										+ "IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
								System.out.println(
										"FATAL ERROR: TC ORACLE DB SERVER HOSTNAME NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								LOGGER.info(
										"FATAL ERROR: TC ORACLE DB SERVER HOSTNAME NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								// DBConnUtilities.printProgramHelp();
								// System.exit(-1);
								// return;
							}
							if (!DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_PORT)
									|| DBConnSingleton.getInstance().getMapCmdArgs()
											.get(DBConnUtilities.CFG_ARG_PORT) == null) {
								LOGGER.info("TC ORACLE DB LISTENER PORT NOT SPECIFIED "
										+ "IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
								System.out.println(
										"FATAL ERROR: TC ORACLE DB LISTENER PORT NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								LOGGER.info(
										"FATAL ERROR: TC ORACLE DB LISTENER PORT NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								// DBConnUtilities.printProgramHelp();
								// System.exit(-1);
								// return;
							}
							if (!DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_SID)
									|| DBConnSingleton.getInstance().getMapCmdArgs()
											.get(DBConnUtilities.CFG_ARG_SID) == null) {
								sLF4J_LOGGER.info("TC ORACLE DB SID NOT SPECIFIED "
										+ "IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
								sLF4J_LOGGER.info(
										"FATAL ERROR: TC ORACLE DB SID NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");

								// DBConnUtilities.printProgramHelp();
								// System.exit(-1);
								// return;
							}
							if (!DBConnSingleton.getInstance().getMapCmdArgs()
									.containsKey(DBConnUtilities.CFG_ARG_TCOWNUSER)
									|| DBConnSingleton.getInstance().getMapCmdArgs()
											.get(DBConnUtilities.CFG_ARG_TCOWNUSER) == null) {
								sLF4J_LOGGER.info("TC TABLES' ORACLE OWNING USER NOT SPECIFIED "
										+ "IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
								// System.out.println("FATAL ERROR: TC TABLES' ORACLE OWNING USER"
								// + " NOT SPECIFIED IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
								sLF4J_LOGGER.info("FATAL ERROR: TC TABLES' ORACLE OWNING USER"
										+ " NOT SPECIFIED IN DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
								// DBConnUtilities.printProgramHelp();
								// System.exit(-1);
								// return;
							}
							if (!DBConnSingleton.getInstance().getMapCmdArgs()
									.containsKey(DBConnUtilities.CFG_ARG_SQLQRYFILE)
									|| DBConnSingleton.getInstance().getMapCmdArgs()
											.get(DBConnUtilities.CFG_ARG_SQLQRYFILE) == null) {
								sLF4J_LOGGER.info(
										"SQL QUERY FILE NOT PROVIDED IN THE DB CONFIG FILE. HENCE CANNOT PERFORM DB OPERATIONS...");
								// System.out.println("FATAL ERROR: TC ORACLE USER NOT SPECIFIED IN DB CONFIG
								// FILE. HENCE EXITING THE UTILITY...");
								sLF4J_LOGGER.info(
										"FATAL ERROR: TC ORACLE USER NOT SPECIFIED IN DB CONFIG FILE. HENCE EXITING THE UTILITY...");
								// DBConnUtilities.printProgramHelp();
								// System.exit(-1);
								// return;
							}
						}
						bufReadObj.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					sLF4J_LOGGER.error(e.getMessage());
				}

			}
		}
	}

	/**
	 * 
	 * @param qryFilePath
	 */
	public static void readSQLQueryFile(String qryFilePath) {
		// check if the file exists or not
		File fileObj = new File(qryFilePath);
		if(fileObj != null) {
			if(!fileObj.exists()) {
				// error for no command line arguments
				if(DBConnSingleton.getInstance().getmLogObj() != null) {
					DBConnSingleton.getInstance().getmLogObj().writeToLogFile("QUERY FILE: " + qryFilePath + " DOES NOT EXIST. "
							+ "HENCE EXITING THE UTILITY...", DBConnUtilities.LOG_LEV_ERROR);
					DBConnSingleton.getInstance().getmLogObj().closeWriter();
				}
				System.out.println("FATAL ERROR: QUERY FILE: " + qryFilePath + " DOES NOT EXIST. HENCE EXITING THE UTILITY...");
				System.exit(-1);
			}
			else {
				// block that reads the queries from the query properties file
				BufferedReader bufReadObj;
				try {
					bufReadObj = new BufferedReader(new FileReader(fileObj));

					if(bufReadObj != null) {
						String line = null;
						while((line = bufReadObj.readLine()) != null) {
							String[] argEntries = line.split(DBConnUtilities.SEP_QRY_FILE);
							if(argEntries != null && argEntries.length == 2) {
								// sets the query name and the query text to the Singleton member map
								DBConnSingleton.getInstance().setmMapQryName2Text(argEntries[0], argEntries[1]);
							}
							else {
								if(DBConnSingleton.getInstance().getmLogObj() != null) {
									DBConnSingleton.getInstance().getmLogObj().writeToLogFile("INVALID COMMAND LINE ARGUMENT ENTRY:" + line + 
											". PROPER FORMAT IS: <ARGUMENT NAME>=<ARGUMENT VALUE>", DBConnUtilities.LOG_LEV_WARNING);
								}
								System.out.println("WARNING: INVALID COMMAND LINE ARGUMENT ENTRY:" + line + 
										". PROPER FORMAT IS: <ARGUMENT NAME>=<ARGUMENT VALUE>");
							}
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns the current time stamp to the calling method
	 * @return String time stamp
	 */
	public static String getCurrentTimeStamp() {

		String currTimeStmp = null;

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		currTimeStmp = dateFormat.format(date);

		return currTimeStmp;
	}

	/**
	 * Checks the input command line arguments for the tool
	 */
	public static void checkCmdArguments() {

		// gets the command line arguments and values from the Singleton instance
		if(DBConnSingleton.getInstance().getMapCmdArgs() != null && !DBConnSingleton.getInstance().getMapCmdArgs().isEmpty()) {
			// check for mandatory argument values

		}
	}

	/**
	 * Method that prints out the tool help to the console.
	 */
	/*public static void printProgramHelp() {	
		System.out.println("************************************************************************************************************");
		System.out.println("*****************************************TOOL SYNTAX********************************************************");
		System.out.println("java -jar tc.sql.dbconnector -cfgFile=<Absolute path to the tool configuration file.>");
		System.out.println("THE CONTENTS OF THE CONFIGURATION FILE ARE AS FOLLOWS:");
		System.out.println("owner=<Teamcenter Oracle DB User that is the owner of all the TC DB Tables (MANDATORY).>");
		System.out.println("user=<Teamcenter Oracle DB User needed for login (MANDATORY).>");
		System.out.println("passwordfile=<Absolute file path to the encrypted Oracle DB Password file (MANDATORY).> ");
		System.out.println("port=<Port Number of Oracle Listener (MANDATORY).>");
		System.out.println("sid=<Oracle SID of the Teamcenter database instance (MANDATORY).>");
		System.out.println("host=<Host name of the machine hosting the Oracle DB Server (MANDATORY).>");
		System.out.println("logLevel=<logging level for the tool. Valid values are INFO & TRACE (OPTIONAL).>");
		System.out.println("opdir=<Absolute directory path where the output files generated by the tool will be written to (MANDATORY).>");
		System.out.println("************************************************************************************************************");
	}*/

	/**
	 * 
	 * @param strToDecrypt
	 * @param secret
	 * @return
	 */
	public static String decryptText(String strToDecrypt, String secret) {
		LOGGER.info("Decrypting text: {}", strToDecrypt);
		SecretKeySpec secretKey = null;
		try
		{
			secretKey = setKey();
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		}
		catch (Exception e)
		{
			LOGGER.error("Error while decrypting {}", strToDecrypt, e);
			return null;
		}		
	}

	/**
	 * 
	 * @param myKey
	 */
	public static SecretKeySpec setKey()
	{
		SecretKeySpec secretKey = null;
		MessageDigest sha = null;

		try {
			byte[] key = DBConnUtilities.mSecretKey.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return secretKey;
	}

	
	/**
	 * 
	 * @return
	 */
	/*
	public static String getDBPassword() {

		String dbPassword = null;							// decrypted password for Teamcenter oracle database

		File pwdFileObj = null;								// File object to hold the encrypted password file

		// retrieve the db password file location
		LOGGER.info("retrieving the db password file location ..");
		if(DBConnSingleton.getInstance().getMapCmdArgs() != null) {
			if(DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_PWDFILE)) {
				String passwodfilePath = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_PWDFILE);
				LOGGER.info("going to read the contents of the password file {}", passwodfilePath);
				pwdFileObj = new File(passwodfilePath);
				if(pwdFileObj != null) {
					LOGGER.info("read the contents of the file {} and pass it on to the decryption method", passwodfilePath);
					// read the contents of the file and pass it on to the decryption method
					BufferedReader brReadOBj;

					try {
						brReadOBj = new BufferedReader((new FileReader(pwdFileObj)));
						if(brReadOBj != null) 
						{
							String line = null;
							try {
								while((line = brReadOBj.readLine()) != null) {
									LOGGER.info("Line to be decrypted : {}", line);
									dbPassword = DBConnUtilities.decryptText(line, DBConnUtilities.mSecretKey);
									if(dbPassword != null && !dbPassword.equals(""))
									{
										LOGGER.info("decrypted line : {}", dbPassword);
									}
									else
									{
										LOGGER.error("failed to decrypt password : {} ", line);
									}
									// only one line in the file.. hence breaking the loop here

									break;
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
//								if(DBConnSingleton.getInstance().getmLogObj() != null) {
//									DBConnSingleton.getInstance().getmLogObj().writeToLogFile("EXCEPTION CAUGHT IN METHOD: " + 
//											DBConnUtilities.class.getClass().getEnclosingMethod().getName() + " WITH TEXT:  " + e.getMessage(), DBConnUtilities.LOG_LEV_ERROR);
//								}
								LOGGER.error("Error while reading from password file {}", passwodfilePath, e);
							}
						}
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
//						if(DBConnSingleton.getInstance().getmLogObj() != null) {
//							DBConnSingleton.getInstance().getmLogObj().writeToLogFile("EXCEPTION CAUGHT IN METHOD: " + 
//									DBConnUtilities.class.getClass().getEnclosingMethod().getName() + " WITH TEXT:  " + e1.getMessage(), DBConnUtilities.LOG_LEV_ERROR);
//						}
						LOGGER.error("Password file not found {}", passwodfilePath, e1);
					}
				}
				else {
					LOGGER.error("Password file is not found in the mentioned path {} ", passwodfilePath);
				}
			}
			else {
				// empty command line arguments... already handled before in the tool
				LOGGER.info("key not found {}", DBConnUtilities.CFG_ARG_PWDFILE);
			}

		}else {
			LOGGER.info("command arguments are null");
		}

		return dbPassword;
	}
*/
	/**
	 * 
	 * @return
	 */
	public static String getDBPassword() {

		String dbPassword = null;							// decrypted password for Teamcenter oracle database

		File pwdFileObj = null;								// File object to hold the encrypted password file

		// retrieve the db password file location
		LOGGER.info("tentative to retrieve the db password file location from MapCmdArguments {}", DBConnSingleton.getInstance().getMapCmdArgs());
		if(DBConnSingleton.getInstance().getMapCmdArgs() != null) 
		{
			String passwordFilePathTest = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_PWDFILE);
			LOGGER.info("1 retrieved db password file location from MapCmdArguments is: {}", passwordFilePathTest);
			
			String passwordFilePathTest2 = DBConnSingleton.getInstance().getMapCmdArgs().get("passwordfile");
			LOGGER.info("2 retrieved db password file location from MapCmdArguments is: {}", passwordFilePathTest2);
			
			
			String userTest = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_USER);
			LOGGER.info("3 retrieved db password file location from MapCmdArguments is: {}", userTest);
			
			
			if(passwordFilePathTest != null) {///DBConnSingleton.getInstance().getMapCmdArgs().containsKey(DBConnUtilities.CFG_ARG_PWDFILE)) {				
				String passwordFilePath = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_PWDFILE);
				LOGGER.info("going to read the contents of the password file {}", passwordFilePath);
				pwdFileObj = new File(passwordFilePath);
				if(pwdFileObj != null) {
					// read the contents of the file and pass it on to the decryption method
					LOGGER.info("read the contents of the file {} and pass it on to the decryption method", passwordFilePath);
					BufferedReader brReadOBj;

					try {
						brReadOBj = new BufferedReader((new FileReader(pwdFileObj)));
						if(brReadOBj != null) 
						{
							String line = null;
							try {
								while((line = brReadOBj.readLine()) != null) {
									LOGGER.info("Line to be decrypted : " + line);
									dbPassword = DBConnUtilities.decryptText(line, DBConnUtilities.mSecretKey);
									if(dbPassword != null && !dbPassword.equals(""))
									{
										LOGGER.info("Decrypted line : " + dbPassword);
									}
									else
									{
										LOGGER.error("failed to decrypt password : {} ", line);
									}
									// only one line in the file.. hence breaking the loop here

									break;
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
//								if(DBConnSingleton.getInstance().getmLogObj() != null) {
//									logger.error("EXCEPTION CAUGHT IN METHOD: " + 
//											DBConnUtilities.class.getClass().getEnclosingMethod().getName() + " WITH TEXT:  " + e.getMessage());
//								}
								LOGGER.error("Error while reading from password file {}", passwordFilePath, e);
							}
						}
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						//if(DBConnSingleton.getInstance().getmLogObj() != null) {
						//logger.error("EXCEPTION CAUGHT IN METHOD: " + 
						//			DBConnUtilities.class.getClass().getEnclosingMethod().getName() + " WITH TEXT:  " + e1.getMessage());
						//}
						
						LOGGER.error("Password file not found {}", passwordFilePath, e1);
					}
				}
				else {
					LOGGER.error("Password file is not found in the mentioned path {} ", passwordFilePath);
				}
			}
			else 
			{
				// empty command line arguments... already handled before in the tool
				LOGGER.info("key not found: {}", DBConnUtilities.CFG_ARG_PWDFILE);
				LOGGER.info("empty command line arguments... already handled before in the tool");
			}

		}
		else
		{
			LOGGER.info("DBConnSingleton.getInstance().getMapCmdArgs() :: NULL");
		}

		return dbPassword;
	}


	/**
	 * Method that compares the two dates and returns the later date to the calling method
	 * @param date1
	 * @param date2
	 * @param laterDate
	 * @return
	 */
	public static String getLaterDate(String date1, String date2) {

		String laterDate = null;							// output date for the method

		// progarmmed to the Teamcenter Oracle Database format
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			Date dateFirst = dateFormat.parse(date1);
			Date dateSecond = dateFormat.parse(date2);

			if (dateFirst.compareTo(dateSecond) <= 0) {
				laterDate = date2;
			}
			else {
				laterDate = date1;
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return laterDate;

	}

}
