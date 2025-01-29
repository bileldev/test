package cdm.pre.imp.mod.decision;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractRuleForDuplication implements IDuplicationRule {
	final private static Logger LOGGER = LogManager.getLogger(AbstractRuleForDuplication.class);

	protected String ruleName;

	@Override
	public String toString() {
		return ruleName;
	}

	@Override
	public final boolean isRuleApplicable(Map<String, BomOccurence> occurences, String currentEndItem) {
		boolean isEligible = isApplicable(occurences, currentEndItem);

		LOGGER.debug(String.format("Can apply rule %s :  %b", ruleName, isEligible));
		
		return isEligible;
	}

	protected abstract boolean isApplicable(Map<String, BomOccurence> occurences, String currentEndItem);

	@Override
	public final void executeRule(Map<String, BomOccurence> occurences, String currentEndItem,
			Map<String, String> occrenceActionMap) {
		LOGGER.debug(String.format("Rule %s will be applied", ruleName));

		occurences.entrySet().stream().forEach(e -> { occrenceActionMap.put(e.getKey(), DELETE); System.out.println("iterate over" + e.getKey());});
		
		execute(occurences, currentEndItem, occrenceActionMap);

		LOGGER.debug(String.format("Rule result: %s ", occrenceActionMap.toString()));
	}

	protected abstract void execute(Map<String, BomOccurence> occurences, String currentEndItem,
			Map<String, String> occrenceActionMap);

}