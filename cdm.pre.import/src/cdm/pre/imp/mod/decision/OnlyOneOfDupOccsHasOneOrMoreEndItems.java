package cdm.pre.imp.mod.decision;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OnlyOneOfDupOccsHasOneOrMoreEndItems extends AbstractRuleForDuplication {
	final private static Logger LOGGER = LogManager.getLogger(OnlyOneOfDupOccsHasOneOrMoreEndItems.class);

	public OnlyOneOfDupOccsHasOneOrMoreEndItems() {
		super();
		ruleName = "Only one of the duplicated occurences has one more or more endItems";
	}

	@Override
	protected boolean isApplicable(Map<String, BomOccurence> occurences, String currentEndItem) {
		return occurences.values().stream().filter(o -> !o.getEndItemsList().isEmpty()).toList().size() == 1;
	}

	@Override
	protected void execute(Map<String, BomOccurence> occurences, String currentEndItem,
			Map<String, String> occrenceActionMap) {

		occurences.entrySet().stream().filter(e -> !e.getValue().getEndItemsList().isEmpty()).findFirst()
				.ifPresent(f -> occrenceActionMap.put(f.getKey(), NONE));
	}

}
