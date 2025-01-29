package cdm.pre.imp.dbconnector;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import cdm.pre.imp.json.reader.Sma4UJsonLogger;

public class DBConnLogger {
		
	private PrintWriter mLogWriterObj;					// member variable that holds the print writer object
	//private BufferedOutputStream mOutStream;			// member variable that holds the buffered output stream reader 
	
	public DBConnLogger() {
		
		if(System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) != null) {
			String fileName = System.getenv(DBConnUtilities.ENV_CDMIMPORT_LOG) + "//" + "SQLConnLog_" + System.currentTimeMillis() + ".txt";
			try {
				this.mLogWriterObj = new PrintWriter(fileName);
				//this.mOutStream = new BufferedOutputStream(new FileOutputStream(fileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// provide warning that the logging will not be possible because the environment variable is not defined
			System.out.println("WARNING: UNABLE TO WRITE LOG INFORMATION FOR THE SQL DB CONNECTOR AS THE ENV. VARIABLE - CDM_IMPORTER_LOG IS NOT SET ");
		}
		
	}
	
public DBConnLogger(Sma4UJsonLogger sma4uLogger) {
		
		if(sma4uLogger != null) {
			this.mLogWriterObj = sma4uLogger.getPrintWriter();
			//this.mOutStream = new BufferedOutputStream(new FileOutputStream(fileName));
		}
		else {
			// provide warning that the logging will not be possible because the environment variable is not defined
			System.out.println("WARNING: UNABLE TO WRITE LOG INFORMATION FOR THE SQL DB CONNECTOR AS THE ENV. VARIABLE - CDM_IMPORTER_LOG IS NOT SET ");
		}
		
	}
	
	/**
	 * Utility method to write out a log entry to the log file
	 * @param logMsg		: Message to write to the output log file
	 * @param logMsgType	: 3 possible types - INFO, WARNING, ERROR
	 */
	public void writeToLogFile(String logMsg, String logMsgType) {
		
		if(this.mLogWriterObj != null && logMsg != null && logMsgType != null) {
			this.mLogWriterObj.println(DBConnUtilities.getCurrentTimeStamp() + ":" + logMsgType + ":" + logMsg);
			//this.mOutStream.write((DBConnUtilities.getCurrentTimeStamp() + ":" + logMsgType + ":" + logMsg).getBytes());
			this.mLogWriterObj.flush();
		}
	}
	
	/**
	 * 
	 * @param logHeader
	 */
	public void writeLogHeader() {
		if(this.mLogWriterObj != null) {
			this.mLogWriterObj.println("ORACLE SQL PROCESSING STARTED AT: " + DBConnUtilities.getCurrentTimeStamp());
			//this.mOutStream.write(("ORACLE SQL PROCESSING STARTED AT: " + DBConnUtilities.getCurrentTimeStamp()).getBytes());
			this.mLogWriterObj.flush();
		}
	}
	
	
	public void writeLogFooter() {
		if(this.mLogWriterObj != null) {
			this.mLogWriterObj.println("ORACLE SQL PROCESSING COMPLETED AT: " + DBConnUtilities.getCurrentTimeStamp());
			//this.mOutStream.write(("ORACLE SQL PROCESSING COMPLETED AT: " + DBConnUtilities.getCurrentTimeStamp()).getBytes());
			this.mLogWriterObj.flush();
		}
	}
	
	/**
	 * closes the member printer writer object
	 */
	public void closeWriter() {

		/*if(this.mLogWriterObj != null) {
			this.mLogWriterObj.close();
		}*/
		if(this.mLogWriterObj != null) {
			this.mLogWriterObj.flush();
			this.mLogWriterObj.close();
		}
	}
}
