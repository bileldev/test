package cdm.pre.imp.dbconnector;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Vector;

public class DBConnOutFileWriter {

	private PrintWriter mPrintWriterObj;					// member variable that holds the print writer object
	
	/**
	 * class constructor that initiates the 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public DBConnOutFileWriter(String qryName) throws FileNotFoundException, UnsupportedEncodingException {	
		if(DBConnSingleton.getInstance().getmOutFilePath() != null) {
			mPrintWriterObj = new PrintWriter(DBConnSingleton.getInstance().getMapCmdArgs().get(DBConnUtilities.CFG_ARG_OPDIR) + "//" + qryName + ".txt");
		}
	}
	
	/**
	 * Prints out the query output to the file
	 * The information is encapsulated as described below in the input vector
	 * For each (vector element)
	 * 
	 * @param dbColValVect
	 */
	public void writeQryOutput(Vector<HashMap<Integer, String> > dbColValVect ) {
		
		String dbStrToWrite;
		
		if((dbColValVect != null) && !(dbColValVect.isEmpty())) {
			// loops through the vector
			for(int iInx=0; iInx < dbColValVect.size(); ++iInx) {
				// re-initialization for each iteration of the loop
				dbStrToWrite = null;
				// each iteration provides a map where the key is the column index and the value is the column value
				// loop through the map
				if(dbColValVect.get(iInx) != null) {
					// iterate through the map
					for(int iJnx=0; iJnx < dbColValVect.get(iInx).size(); ++iJnx) {
						// condition where the string is filled for the first time, i.e., for the first column
						if(dbStrToWrite == null) {
							// jInx + 1 because the key starts at 1 for the database column index and not 0.
							dbStrToWrite = dbColValVect.get(iInx).get(iJnx+1);
						}
						// subsequent appends to the string for the additional columns
						else {
							dbStrToWrite = dbStrToWrite + DBConnUtilities.SEP_OUT_FILE + dbColValVect.get(iInx).get(iJnx+1); 
						}
					}
				}
				if(dbStrToWrite != null && !(dbStrToWrite.isEmpty())) {
					this.mPrintWriterObj.println(dbStrToWrite);
				}
			}
		}
	}
	
	/**
	 * prints out the delimited column names to the output text file
	 * @param headerTxt
	 */
	public void writeOutputFileHeader(String headerTxt) {
		
		if(headerTxt != null && mPrintWriterObj != null) {
			this.mPrintWriterObj.println(headerTxt);
		}
	}
	
	/**
	 * closes the member printer writer object
	 */
	public void closeWriter() {

		if(this.mPrintWriterObj != null) {
			this.mPrintWriterObj.close();
		}
	}
}
