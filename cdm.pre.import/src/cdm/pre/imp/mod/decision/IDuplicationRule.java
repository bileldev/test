package cdm.pre.imp.mod.decision;

import java.util.Map;

public interface IDuplicationRule {
	static final String DELETE = "delete";
	static final String NONE = "none";
	static final String CHANGE = "change";
	
	public boolean isRuleApplicable(Map<String, BomOccurence> occurences, String currentEndItem);

	public void executeRule(Map<String, BomOccurence> occurences, String currentEndItem,
			Map<String, String> occrenceActionMap);
}