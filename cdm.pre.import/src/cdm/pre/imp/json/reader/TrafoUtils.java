package cdm.pre.imp.json.reader;

public class TrafoUtils {
	public static String[] generateTrafoMatrix(JSONElelment elem)
	{
		String[] trans = new String[16];
		if(elem != null)
		{
			//<t v0="1.0" v1="0.0" v2="0.0" v3="0.0" v4="0.0" v5="1.0" v6="0.0" v7="0.0" v8="0.0" v9="0.0" v10="1.0" v11="0.0" v12="0.0" v13="0.0" v14="0.0" v15="1.0"/>
			String tMatrix11Val = (String)elem.getRelAttributes().get("TMatrix11");
			if(tMatrix11Val == null)
			{
				tMatrix11Val = "1.0";
			}
			trans[0] =  tMatrix11Val;
			//trans[1] = (String) elem.getRelAttributes().get("TMatrix12");
			String tMatrix12Val = (String)elem.getRelAttributes().get("TMatrix12");
			if(tMatrix12Val == null)
			{
				tMatrix12Val = "0.0";
			}
			trans[1] =  tMatrix12Val;

			//trans[2] = (String) elem.getRelAttributes().get("TMatrix13");

			String tMatrix13Val = (String)elem.getRelAttributes().get("TMatrix13");
			if(tMatrix13Val == null)
			{
				tMatrix13Val = "0.0";
			}
			trans[2] =  tMatrix13Val;

			//trans[3] = (String) elem.getRelAttributes().get("TMatrix14");
			String tMatrix14Val = (String)elem.getRelAttributes().get("TMatrix14");
			if(tMatrix14Val == null)
			{
				tMatrix14Val = "0.0";
			}
			trans[3] =  tMatrix14Val;

			//trans[4] = (String) elem.getRelAttributes().get("TMatrix21");
			String tMatrix21Val = (String)elem.getRelAttributes().get("TMatrix21");
			if(tMatrix21Val == null)
			{
				tMatrix21Val = "0.0";
			}
			trans[4] =  tMatrix21Val;


			//trans[5] = (String) elem.getRelAttributes().get("TMatrix22");

			String tMatrix22Val = (String)elem.getRelAttributes().get("TMatrix22");
			if(tMatrix22Val == null)
			{
				tMatrix22Val = "1.0";
			}
			trans[5] =  tMatrix22Val;

			//trans[6] = (String) elem.getRelAttributes().get("TMatrix23");

			String tMatrix23Val = (String)elem.getRelAttributes().get("TMatrix23");
			if(tMatrix23Val == null)
			{
				tMatrix23Val = "0.0";
			}
			trans[6] =  tMatrix23Val;

			//trans[7] = (String) elem.getRelAttributes().get("TMatrix24");

			String tMatrix24Val = (String)elem.getRelAttributes().get("TMatrix24");
			if(tMatrix24Val == null)
			{
				tMatrix24Val = "0.0";
			}
			trans[7] =  tMatrix24Val;

			//trans[8] = (String) elem.getRelAttributes().get("TMatrix31");

			String tMatrix31Val = (String)elem.getRelAttributes().get("TMatrix31");
			if(tMatrix31Val == null)
			{
				tMatrix31Val = "0.0";
			}
			trans[8] =  tMatrix31Val;

			//trans[9] = (String) elem.getRelAttributes().get("TMatrix32");

			String tMatrix32Val = (String)elem.getRelAttributes().get("TMatrix32");
			if(tMatrix32Val == null)
			{
				tMatrix32Val = "0.0";
			}
			trans[9] =  tMatrix32Val;

			//trans[10] = (String) elem.getRelAttributes().get("TMatrix33");

			String tMatrix33Val = (String)elem.getRelAttributes().get("TMatrix33");
			if(tMatrix33Val == null)
			{
				tMatrix33Val = "1.0";
			}
			trans[10] =  tMatrix33Val;

			//	trans[11] = (String) elem.getRelAttributes().get("TMatrix34");

			String tMatrix34Val = (String)elem.getRelAttributes().get("TMatrix34");
			if(tMatrix34Val == null)
			{
				tMatrix34Val = "0.0";
			}
			trans[11] =  tMatrix34Val;

			//	trans[12] = (String) elem.getRelAttributes().get("TMatrix41");

			String tMatrix41Val = (String)elem.getRelAttributes().get("TMatrix41");
			if(tMatrix41Val == null)
			{
				tMatrix41Val = "0.0";
			}
			else
			{
				
				tMatrix41Val = convertTrafoMatrix(tMatrix41Val);
				if(tMatrix41Val == null)
				{
					tMatrix41Val = "0.0";
				}
			}
			trans[12] =  tMatrix41Val;

			//	trans[13] = (String) elem.getRelAttributes().get("TMatrix42");

			String tMatrix42Val = (String)elem.getRelAttributes().get("TMatrix42");
			if(tMatrix42Val == null)
			{
				tMatrix42Val = "0.0";
			}
			else
			{
				
				tMatrix42Val = convertTrafoMatrix(tMatrix42Val);
				if(tMatrix42Val == null)
				{
					tMatrix42Val = "0.0";
				}
			}
			trans[13] =  tMatrix42Val;

			//trans[14] = (String) elem.getRelAttributes().get("TMatrix43");

			String tMatrix43Val = (String)elem.getRelAttributes().get("TMatrix43");
			if(tMatrix43Val == null)
			{
				tMatrix43Val = "0.0";
			}
			else
			{
				
				tMatrix43Val = convertTrafoMatrix(tMatrix43Val);
				if(tMatrix43Val == null)
				{
					tMatrix43Val = "0.0";
				}
			}
			trans[14] =  tMatrix43Val;

			//trans[15] = (String) elem.getRelAttributes().get("TMatrix44");

			String tMatrix44Val = (String)elem.getRelAttributes().get("TMatrix44");
			if(tMatrix44Val == null)
			{
				tMatrix44Val = "1.0";
			}
			trans[15] =  tMatrix44Val;
			//System.out.println(Arrays.toString(trans));
		}

		return trans;
	}
	
	public static boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	public static String convertTrafoMatrix(String tMatrixStrVal)
	{
		double tMatrixValDouble;
		String tMatrixNewVal = null;
		if(isNumeric(tMatrixStrVal))
		{
			tMatrixValDouble = Double.parseDouble(tMatrixStrVal);
			tMatrixValDouble = tMatrixValDouble/1000;
			tMatrixNewVal = Double.toString(tMatrixValDouble);
		}
		return tMatrixNewVal;
	}
	
	
}
