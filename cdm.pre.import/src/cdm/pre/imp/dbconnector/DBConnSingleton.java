package cdm.pre.imp.dbconnector;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Properties;

import cdm.pre.imp.json.reader.Sma4UJsonLogger;

/**
 * 
 * @author amit.rath
 *
 */


public class DBConnSingleton {

	private Long mStartTime;
	private Long mEndTime;
	
	private String mDBUser;
	private String mDBPassword;						// de-crypted Oracle database password

	private String mMode;

	private String mOutFilePath;
	
	private Connection mDBConnInst;
	private Properties mCfgFileProp;
	private Properties mQryFileProp;
	
	private HashMap<String, String> mMapCmdArgs;
	private HashMap<String, Long> mMapSQLLog;
	private HashMap<String, String> mMapQryName2Text;
	private HashMap<String, PreparedStatement> mMapQryName2StmtObj;
	
	private DBConnLogger mLogObj;
	
	private static DBConnSingleton objSingleton = null;
	
	private DBConnSingleton() {
		this.mDBConnInst = null;
		this.mLogObj = null;
		this.mStartTime = 0L;
		this.mEndTime = 0L;
		this.mMapCmdArgs = new HashMap<String, String>();
		this.mMapSQLLog = new HashMap<String, Long>();
		this.mMapQryName2Text = new HashMap<String, String>();
		this.mMapQryName2StmtObj = new HashMap<String, PreparedStatement>();
	}
	
	public String getmDBPassword() {
		return mDBPassword;
	}

	public void setmDBPassword(String mDBPassword) {
		this.mDBPassword = mDBPassword;
	}
	
	public String getmMode() {
		return mMode;
	}

	public void setmMode(String mMode) {
		this.mMode = mMode;
	}
	

	public String getmDBUser() {
		return mDBUser;
	}

	public void setmDBUser(String mDBUser) {
		this.mDBUser = mDBUser;
	}


	public Long getmStartTime() {
		return mStartTime;
	}


	public void setmStartTime(Long mStartTime) {
		this.mStartTime = mStartTime;
	}
	
	
	public Long getmEndTime() {
		return mEndTime;
	}


	public void setmEndTime(Long mEndTime) {
		this.mEndTime = mEndTime;
	}
	
	public HashMap<String, String> getMapCmdArgs() {
		return this.mMapCmdArgs;
	}


	public void setMapCmdArgs(String argName, String argValue) {
		if(argName != null && argValue != null) {
			this.mMapCmdArgs.put(argName, argValue);
		}
	}
	
	public HashMap<String, Long> getmMapSQLLog() {
		return mMapSQLLog;
	}
	
	public HashMap<String, PreparedStatement> getmMapQryName2StmtObj() {
		return mMapQryName2StmtObj;
	}

	public void setmMapQryName2StmtObj(String qryName, PreparedStatement objPrepStmt) {
		if(qryName != null && objPrepStmt != null) {
			if(!this.mMapQryName2StmtObj.containsKey(qryName)) {
				this.mMapQryName2StmtObj.put(qryName, objPrepStmt);
			}
		}
	}


	public void setmMapSQLLog(String qryName, Long timeTaken) {
		if(this.mMapSQLLog != null && !this.mMapSQLLog.isEmpty()) {
			if(this.mMapSQLLog.containsKey(qryName)) {
				long tmpTimeTaken = 0;
				// adds the input time taken to the already existing time taken
				tmpTimeTaken = timeTaken + this.mMapSQLLog.get(qryName);
				// removes the entry in the map with the old time
				this.mMapSQLLog.remove(qryName);
				// adds an entry in the map for the query with the new time
				this.mMapSQLLog.put(qryName, tmpTimeTaken);
				tmpTimeTaken = 0;
			}
			// in case the query name does not already have an entry in the map
			else {
				this.mMapSQLLog.put(qryName, timeTaken);
			}
		}
		// in case this is the first entry in the map
		else {
			this.mMapSQLLog.put(qryName, timeTaken);
		}	
	}

	public DBConnLogger getmLogObj() {
		return mLogObj;
	}


	public void setmLogObj(DBConnLogger mLogObj) {
		this.mLogObj = mLogObj;
	}


	public Connection getmDBConnInst() {
		return mDBConnInst;
	}

	public void setmDBConnInst(Connection mDBConnInst) {
		this.mDBConnInst = mDBConnInst;
	} 
	
	
	public String getmOutFilePath() {
		return mOutFilePath;
	}


	public void setmOutFilePath(String mOutFilePath) {
		this.mOutFilePath = mOutFilePath;
	}
	
	public static DBConnSingleton getInstance() 
    { 
        if (objSingleton == null) 
        	objSingleton = new DBConnSingleton(); 
  
        return objSingleton; 
    }
	
	public void getConfigFileHandle() {
			 
		String propFileName = null;					// variable that holds the name of the property file
		
		InputStream objIS;							// input stream instance to read the content of the property file
		
		this.mCfgFileProp = new Properties();
		
		propFileName = "config.properties";
		objIS = getClass().getClassLoader().getResourceAsStream(propFileName);
		if(objIS != null) {
			try {
				this.mCfgFileProp.load(objIS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Properties getmCfgFileProp() {
		if(this.mCfgFileProp == null) {
			this.getConfigFileHandle();
		}
		return this.mCfgFileProp;
	}


	public Properties getmQryFileProp() {
		if(this.mQryFileProp == null) {
			this.getQueryFileHandle();
		}
		return this.mQryFileProp;
	}


	public static DBConnSingleton getObjSingleton() {
		return objSingleton;
	}


	public void getQueryFileHandle() {
		
		String propFileName = null;					// variable that holds the name of the property file
		
		InputStream objIS;							// input stream instance to read the content of the property file
		
		this.mQryFileProp = new Properties();
		propFileName = "query.properties";
		
		objIS = getClass().getClassLoader().getResourceAsStream(propFileName);
		if(objIS != null) {
			try {
				this.mQryFileProp.load(objIS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void initializeLogger() {		
		this.mLogObj = new DBConnLogger(); 	
	}
	public void initializeLogger(Sma4UJsonLogger sma4uLogger) {		
		this.mLogObj = new DBConnLogger(sma4uLogger); 	
	}
	
	public HashMap<String, String> getmMapQryName2Text() {
		return mMapQryName2Text;
	}

	/**
	 * Setter for the query name to text member map
	 * @param qryName
	 * @param qryText
	 */
	public void setmMapQryName2Text(String qryName, String qryText) {
		if(qryName != null && qryText != null) {
			// checks if the map is empty or not and checks if the query name is already present in the map or not
			if(this.mMapQryName2Text != null) {
				if(!this.mMapQryName2Text.containsKey(qryText)) {
					this.mMapQryName2Text.put(qryName, qryText);
				}
			}
			// first entry in the member map
			else {
				this.mMapQryName2Text.put(qryName, qryText);
			}
		}
	}
	
}
