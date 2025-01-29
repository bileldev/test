package cdm.pre.imp.mod.decision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.dbconnector.DBConnBOMInfo;

public class DuplicationRuleEvaluator {
	final private static Logger LOGGER = LogManager.getLogger(DuplicationRuleEvaluator.class);

	private final List<IDuplicationRule> rules = new ArrayList<>();

	/**
	 * Add the rules in the order you want to execute
	 */
	public DuplicationRuleEvaluator() {
		rules.add(new AllDupOccsWithoutEndItems());
		rules.add(new OnlyOneOfDupOccsHasOneOrMoreEndItems());
		rules.add(new MoreThanOneDuplOccHasEndItem_eachHasOnlyOne());
		rules.add(new MoreThanOneDuplOccHasEndItem_atLeastOneHasMoreThanOneEndItem());
	}

	public boolean evaluateRules(Map<String, DBConnBOMInfo> occBomInfoMap, String currentEndItem,
			Map<String, String> occrenceActionMap) {
		Map<String, BomOccurence> occurences = initializeInput(occBomInfoMap);

		HashMap<String, String> simulationMap = (HashMap<String, String>) occrenceActionMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		rules.stream().filter(r -> r.isRuleApplicable(occurences, currentEndItem)).findFirst().ifPresentOrElse(
				r -> r.executeRule(occurences, currentEndItem, simulationMap),
				() -> LOGGER.error(String.format("BOM occurences status does not matches any Rule")));

		// Validation of simulation result
		int occToDelete = simulationMap.values().stream().filter(o -> o.equals(IDuplicationRule.DELETE)).toList()
				.size();
		int occToKeep = simulationMap.values().stream().filter(o -> o.equals(IDuplicationRule.NONE) || o.equals(IDuplicationRule.CHANGE)).toList().size();
		if (occToDelete >= 1 && occToKeep == 1) {
			simulationMap.entrySet().stream().forEach(e -> occrenceActionMap.put(e.getKey(), e.getValue()));
			LOGGER.info(String.format("Duplicated BOM successfully calculated. %d BOM occurences are marked to be deleted.", occToDelete));
			return true;
		} else {
			LOGGER.error(String.format(
					"duplicte BOM occurences could not be resolved.\n Simulation result:\n  Delete: %d\n  None: %d",
					occToDelete, occToKeep));
			return false;
		}

	}

	private Map<String, BomOccurence> initializeInput(Map<String, DBConnBOMInfo> occBomInfoMap) {

		Map<String, BomOccurence> occurences = occBomInfoMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> new BomOccurence(e.getValue())));

		return occurences;
	}
}