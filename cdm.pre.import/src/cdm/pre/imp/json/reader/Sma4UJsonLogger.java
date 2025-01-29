package cdm.pre.imp.json.reader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import cdm.pre.imp.dbconnector.DBConnUtilities;

public class Sma4UJsonLogger {
		
	private PrintWriter mLogWriterObj;					// member variable that holds the print writer object
	//private BufferedOutputStream mOutStream;			// member variable that holds the buffered output stream reader 
	
	public Sma4UJsonLogger(String pkgLoc) {
		
		if(pkgLoc != null) {
			String fileName = pkgLoc + "//" + "SMA4ULog_" + System.currentTimeMillis() + ".txt";
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
			System.out.println("WARNING: UNABLE TO WRITE LOG INFORMATION FOR SMA4U. PACKAGE LOCATION IS NOT AVAILABLE OR NOT ACCESIBLE .");
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
			this.mLogWriterObj.println("SMA4U JSON RESPONSE PROCESSING STARTED AT: " + DBConnUtilities.getCurrentTimeStamp());
			//this.mOutStream.write(("ORACLE SQL PROCESSING STARTED AT: " + DBConnUtilities.getCurrentTimeStamp()).getBytes());
			this.mLogWriterObj.flush();
		}
	}
	
	
	public void writeLogFooter() {
		if(this.mLogWriterObj != null) {
			this.mLogWriterObj.println("SMA4U JSON RESPONSE PROCESSING COMPLETED AT: " + DBConnUtilities.getCurrentTimeStamp());
			//this.mOutStream.write(("ORACLE SQL PROCESSING COMPLETED AT: " + DBConnUtilities.getCurrentTimeStamp()).getBytes());
			this.mLogWriterObj.flush();
		}
	}
	
	public PrintWriter getPrintWriter()
	{
		return mLogWriterObj;
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
