package cdm.pre.imp.mod.decision;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MoreThanOneDuplOccHasEndItem_atLeastOneHasMoreThanOneEndItem extends AbstractRuleForDuplication {
	final private static Logger LOGGER = LogManager
			.getLogger(MoreThanOneDuplOccHasEndItem_atLeastOneHasMoreThanOneEndItem.class);

	public MoreThanOneDuplOccHasEndItem_atLeastOneHasMoreThanOneEndItem() {
		super();
		ruleName = "More than one duplicated occurences hast endItem: at least one of them hast more than one enditem-> take the first one which has multiple";
	}

	@Override
	protected boolean isApplicable(Map<String, BomOccurence> occurences, String currentEndItem) {
		return occurences.values().stream().filter(o -> o.getEndItemsList().size() > 1).toList().size() > 0;
	}

	@Override
	protected void execute(Map<String, BomOccurence> occurences, String currentEndItem,
			Map<String, String> occrenceActionMap) {

		occurences.entrySet().stream().filter(e -> e.getValue().getEndItemsList().size() > 1).findFirst()
				.ifPresent(f -> occrenceActionMap.put(f.getKey(), f.getValue().getEndItemsList().contains(currentEndItem)?NONE:CHANGE));
	}

}