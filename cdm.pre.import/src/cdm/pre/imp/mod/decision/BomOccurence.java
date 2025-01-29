package cdm.pre.imp.mod.decision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.dbconnector.DBConnBOMInfo;

public class BomOccurence {

	final private static Logger LOGGER = LogManager.getLogger(BomOccurence.class);

	private List<String> endItemsList = new ArrayList<String>();

	//private String action = "none";

	private BomOccurence() {
		super();
	}

	public BomOccurence(DBConnBOMInfo bomInfo) {
		this();
		
		HashMap<String, HashMap<String, String>> endItem2effMap = bomInfo.getmMapEndItem2Eff();
		if( endItem2effMap != null && endItem2effMap.size() > 0)
		{
			Object[] endItem2effMapKeys = endItem2effMap.keySet().toArray();
			ArrayList<String> refConfigList = new ArrayList<>();
			for (Object refCName : endItem2effMapKeys)
			{
				endItemsList.add((String) refCName);
			}
		}
	}

	public List<String> getEndItemsList() {
		return endItemsList;
	}

	//public String getAction() {
	//	return action;
	//}
}
