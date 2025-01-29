package cdm.pre.imp.mod.decision;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Not used
 */
public class MoreThanOneDupOccurencesHasEndItem_eachIsSameAsActive extends AbstractRuleForDuplication {
	final private static Logger LOGGER = LogManager
			.getLogger(MoreThanOneDupOccurencesHasEndItem_eachIsSameAsActive.class);

	public MoreThanOneDupOccurencesHasEndItem_eachIsSameAsActive() {
		super();
		ruleName = "More than one duplicated occurences has endItem:each of them has only one and is same as current EndItem";
	}

	@Override
	protected boolean isApplicable(Map<String, BomOccurence> occurences, String currentEndItem) {
		return occurences.values().stream()
				.filter(o -> o.getEndItemsList().size() > 1
						|| o.getEndItemsList().size() == 1 && !currentEndItem.equals(o.getEndItemsList().get(0)))
				.toList().size() == 0;

		// return occurences.values().stream().filter(o ->
		// !o.getEndItemsList().isEmpty()).toList().size() == 1;
	}

	@Override
	protected void execute(Map<String, BomOccurence> occurences, String currentEndItem,
			Map<String, String> occrenceActionMap) {

		occurences.entrySet().stream()
				.filter(e -> !e.getValue().getEndItemsList().isEmpty()
						&& currentEndItem.equals(e.getValue().getEndItemsList().get(0)))
				.findFirst().ifPresent(f -> occrenceActionMap.put(f.getKey(), NONE));
	}

}
