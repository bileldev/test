package cdm.pre.imp.dbconnector;
/**
 * 
 * @author amit.rath
 *
 */


import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class JDBCConnector {
	final private static Logger LOGGER = LogManager.getLogger(JDBCConnector.class);
	
	private String mDriverName;
	private String mHostName;
	private String mDBUserName;
	private String mPortNo;
	private String mSID;
	private String mService;
	private String mDBPassword;
	

	/**
	 * Constructor for the class
	 * @param userName		- Teamcenter specific user name of the Oracle Database
	 * @param password		- password for the Teamcenter Oracle user 
	 * @param instanceID	- instance ID of the Teamcenter Oracle Database
	 * @param hostName		- Host name of the Oracle database server
	 * @param portNo		- Port number to establish a connection with the Teamcenter Oracle Database
	 */
	public JDBCConnector(String dbPwd) {
		
		this.mHostName = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_HOST);
		this.mDBUserName = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_USER);
		this.mHostName = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_HOST);
		this.mPortNo = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_PORT);
		if(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SID) != null &&
				!DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SID).isEmpty()) {
			this.mSID = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SID);
		}
		if(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SERVICE) != null &&
				!DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SERVICE).isEmpty()) {
			this.mService = DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_SERVICE);
		}
		this.mDBPassword = dbPwd;
	}
	
	
	/**
	 * Method that establishes a connection with the Teamcenter database & assigns the DB connection object as a class member.
	 */
	public void connectToTCDatabase() {
		
		String sqlConnectStr = null;				// Connect String that needs to be passed to the JDBC connection API
		
		// retrieves the driver name from config.properties file
		this.getDriverName();
		
		LOGGER.info("Databse Credentials:\nDriver name: {}, user name: {}, password: {}, sid: {}, host: {}, port: {}, service: {}", mDriverName, mDBUserName, mDBPassword, mSID, mHostName, mPortNo, mService);
		
		// terminates the session in case the driver name is not found
		if(this.mDriverName == null) {
			LOGGER.error("UNABLE TO FIND DRIVER NAME FOR CONNECTING TO TEAMCENTER DATABASE. HENCE EXITING FROM"
						+ " THE DB SQL CONNECTOR COMPONENT");
		}
		
		// checks for the required database credentials for establishing the connection with the Teamcenter database
		if(this.mDBUserName != null && this.mDBPassword != null && this.mSID != null && this.mHostName != null && this.mPortNo != null) {
			sqlConnectStr = "jdbc:oracle:thin:@" + this.mHostName +
					":" + this.mPortNo + ":" + this.mSID;
		}
		else if(this.mDBUserName != null && this.mDBPassword != null && this.mService != null && this.mHostName != null && this.mPortNo != null) {
			sqlConnectStr = "jdbc:oracle:thin:@" + this.mHostName +
					":" + this.mPortNo + "/" + this.mService;
		} 
		else {
			// error handling for the non-availability of database login credentials to exit the code
			LOGGER.error("Database login credentials are not available");
		}
		if(sqlConnectStr != null && !sqlConnectStr.isEmpty()) {
			LOGGER.info("Database connection string to be used: {}", sqlConnectStr);
			try {
				//Class.forName("oracle.jdbc.driver.OracleDriver");
				// debug statement
				this.mDriverName = "oracle.jdbc.OracleDriver";
				// debug statement
				Class.forName(this.mDriverName);

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
//				if(DBConnSingleton.getInstance().getmLogObj() != null) {
//					DBConnSingleton.getInstance().getmLogObj().writeToLogFile("EXCEPTION ENCOUNTERED WHILE CONNECTING TO TEAMCENTER DATABASE WITH USER: <" + this.mDBUserName
//							+ "> ----> SID: <" + this.mSID + "> ----> HOST <" + this.mHostName + ">" 
//							+ " ----> THE EXCEPTION TEXT IS: " + e.getMessage(), DBConnUtilities.LOG_LEV_ERROR);
//					// closes the log writer
//					DBConnSingleton.getInstance().getmLogObj().closeWriter();
//				}
				LOGGER.error("EXCEPTION ENCOUNTERED WHILE CONNECTING TO TEAMCENTER DATABASE WITH USER: <" + this.mDBUserName
						+ "> ----> SID: <" + this.mSID + "> ----> HOST <" + this.mHostName + ">" 
						+ " ----> THE EXCEPTION TEXT IS: " + e.getMessage());
			}
			
			try {
				DBConnSingleton.getInstance().setmDBConnInst(DriverManager.getConnection(sqlConnectStr, this.mDBUserName, this.mDBPassword));
				//DBConnSingleton.getInstance().setmDBConnInst(DriverManager.getConnection(sqlConnectStr, "infodba", "infodba"));
				LOGGER.info("CONNECTION ESTABLISHED WITH TEAMCENTER DATABASE ON : <" + this.mHostName 
						+ "> AT PORT NO: <" + this.mPortNo + "> WITH THE ORACLE USER: <" + this.mDBUserName + ">");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				LOGGER.error("EXCEPTION ENCOUNTERED WHILE CONNECTING TO TEAMCENTER DATABASE.. THE EXCEPTION TEXT IS: " + e.getMessage());
			}
		}
	}
	
	/**
	 * This method terminates the TC Database connection.
	 */
	public void terminateDBConnect() {
		
		// checks if the connection instance is available or not
		if(DBConnSingleton.getInstance().getmDBConnInst() != null) {
			try {
				// checks if the connection is already closed or not
				if(!DBConnSingleton.getInstance().getmDBConnInst().isClosed()) {
					// call to terminate the Teamcenter database connection
					DBConnSingleton.getInstance().getmDBConnInst().close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Reads the driver name from the config.properties file and sets it to the class member
	 */
	public void getDriverName() {

		Properties cfgFileProp = new Properties();
		
		cfgFileProp = DBConnSingleton.getInstance().getmCfgFileProp();

		if(cfgFileProp != null) {
			if(cfgFileProp.getProperty("driverName") != null) {
				this.mDriverName = cfgFileProp.getProperty("driverName");
			}
		}
	}
	
	public String getmService() {
		return mService;
	}


	public void setmService(String mService) {
		this.mService = mService;
	}

}
