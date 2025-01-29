package cdm.pre.imp.configmap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DateUtils {
	final private static Logger LOGGER = LogManager.getLogger(DateUtils.class);
	
	public static String dateConverter(String obid, String attrName, String inputDate, String sysStartDate) throws  MappingException {

		boolean isAfterStartDate = false;								// boolean variable that states if the input date is after system start date

		String processedDate 	= null;									// output variable for the mapping function

		Date plmxmlDate 		= null;									// Date representation for the string input from the PLMXML file
		Date sysBeginDate 		= null;	
		String formattedTime = null;

		// Date representation for the system begin date
		
		//:::::::: inputDate ::::::::::: 2014-07-21T04:57:22Z
		//:::::::: inputDate ::::::::::: 2014-07-10T11:22:28.213Z

		DateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS");

		if(sysStartDate != null)
		{
			//SMA4U_LOGGER.writeToLogFile(":::::::: sysStartDate ::::::::::: "+sysStartDate,"INFO");
			//System.out.println(":::::::: sysStartDate ::::::::::: "+sysStartDate);
			LOGGER.info(":::::::: sysStartDate ::::::::::: "+sysStartDate);
			if(df != null) 
			{
				/*SimpleDateFormat sdf =null;
				String[] dateSplit = inputDate.split("\\.");
				if(dateSplit != null && dateSplit.length > 0)
				{
					String secsStr = dateSplit[dateSplit.length-1];
					if(secsStr.length() == 3)
					{
						sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
					}
					else 
					{
						sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					}
				}*/
				//LocalDateTime dateTime = LocalDateTime.parse(inputDate);
				
			//	Instant instant = Instant.parse ( inputDate );
				
				
				//System.out.println("  dateTime ::: "+instant.toString());
				SimpleDateFormat sdf =null;
				if( inputDate.contains("T"))
				{
					String[] dateSplit = inputDate.split(":");
					if(dateSplit != null && dateSplit.length > 0)
					{
						String secsStr = dateSplit[dateSplit.length-1];
						if(secsStr.length() == 3)
						{
							sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'");
						}
						else 
						{
							sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						}
					}
				}
				else 
				{
					sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss:SSS");
				}

				Date plmxmlInputDate = null;
				try {
					//SMA4U_LOGGER.writeToLogFile(":::::::: inputDate ::::::::::: "+inputDate,"INFO");
					LOGGER.info(":::::::: inputDate ::::::::::: "+inputDate);
					//System.out.println(":::::::: inputDate ::::::::::: "+inputDate);
					plmxmlInputDate = sdf.parse(inputDate);

					formattedTime = df.format(plmxmlInputDate);

					plmxmlDate = df.parse(formattedTime);
					sysBeginDate = df.parse(sysStartDate);

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					//SMA4U_LOGGER.writeToLogFile("Date Parse Exception : "+e.getMessage(),"ERROR");
					LOGGER.error("Date Parse Exception : "+e.getMessage());
					LOGGER.error("Date Parse Exception : @ OBID : "+obid+"  -- Attribute Name : "+attrName+" -- Date : "+inputDate);
					LOGGER.error("Date Parse Exception : Expected Format : yyyy-MM-dd'T'HH:mm:SS'Z' OR yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					
					System.out.println("Date Parse Exception : "+e.getMessage());
					System.out.println("Date Parse Exception : @ OBID : "+obid+"   -- Attribute Name : "+attrName+"  --Date : "+inputDate);
					System.out.println("Date Parse Exception : Expected Format : yyyy-MM-dd'T'HH:mm:SS'Z' OR yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					return null;
				}
			}
		} else {
			//SMA4U_LOGGER.writeToLogFile("System start date as provided in the mapping file is null","INFO"); 
			LOGGER.info("System start date as provided in the mapping file is null"); 
		}


		// compare the date
		if(plmxmlDate != null && sysBeginDate != null) {
			// compares the dates and stores the 
			isAfterStartDate = plmxmlDate.after(sysBeginDate);
		}

		// checks value of the boolean before the format transformation
		if(isAfterStartDate == true) {
			if(formattedTime != null) {
				String[] tmpDateArray = formattedTime.split("-");
				if(tmpDateArray != null) {
					if(tmpDateArray.length == 2) {
						processedDate = tmpDateArray[0];
						String[] tmpTimeArray = tmpDateArray[1].split(":");
						if(tmpTimeArray != null) {
							if(tmpTimeArray.length == 4) {
								processedDate = processedDate + "/" + tmpTimeArray[0] + "/" + tmpTimeArray[1] + "/" + tmpTimeArray[2];  
							}
						}
					}
				}
			}
		}
		// sets the default start date as stated in the mapping file as the attribute value
		else {
			if(sysStartDate != null) {
				String[] tmpDateArray = sysStartDate.split("-");
				if(tmpDateArray != null) {
					if(tmpDateArray.length == 2) {
						processedDate = tmpDateArray[0];
						String[] tmpTimeArray = tmpDateArray[1].split(":");
						if(tmpTimeArray != null) {
							if(tmpTimeArray.length == 4) {
								processedDate = processedDate + "/" + tmpTimeArray[0] + "/" + tmpTimeArray[1] + "/" + tmpTimeArray[2];  
							}
						}
					}
				}
			}
		}
		//System.out.println(":::::::: processedDate ::::::::::: "+processedDate);
		return processedDate;
	}
	
}
