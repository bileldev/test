package cdm.pre.imp.mod.decision;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AllDupOccsWithoutEndItems extends AbstractRuleForDuplication {
	final private static Logger LOGGER = LogManager.getLogger(AllDupOccsWithoutEndItems.class);

	public AllDupOccsWithoutEndItems() {
		super();
		ruleName = "All Duplicated occurences are without end Items";
	}

	@Override
	protected boolean isApplicable(Map<String, BomOccurence> occurences, String currentEndItem) {
		return occurences.values().stream().filter(o -> !o.getEndItemsList().isEmpty()).toList().isEmpty();
	}

	@Override
	protected void execute(Map<String, BomOccurence> occurences, String currentEndItem,
			Map<String, String> occrenceActionMap) {

		occurences.entrySet().stream().peek(e -> occrenceActionMap.put(e.getKey(), DELETE)).findFirst()
				.ifPresent(f -> occrenceActionMap.put(f.getKey(), NONE));

	}
}
