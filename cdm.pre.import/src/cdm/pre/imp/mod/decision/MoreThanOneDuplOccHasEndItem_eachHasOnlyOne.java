package cdm.pre.imp.mod.decision;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MoreThanOneDuplOccHasEndItem_eachHasOnlyOne extends AbstractRuleForDuplication {
	final private static Logger LOGGER = LogManager.getLogger(MoreThanOneDuplOccHasEndItem_eachHasOnlyOne.class);

	private boolean occWithendItemDifferentThanCurrentFound = false;
	
	public MoreThanOneDuplOccHasEndItem_eachHasOnlyOne() {
		super();
		ruleName = "More than one duplicated occurences has endItem:each of them has only one -> take the first one different from current endItem";
	}

	@Override
	protected boolean isApplicable(Map<String, BomOccurence> occurences, String currentEndItem) {
		return occurences.values().stream().filter(o -> o.getEndItemsList().size() > 1).toList().size() == 0;
	}

	@Override
	protected void execute(Map<String, BomOccurence> occurences, String currentEndItem,
			Map<String, String> occrenceActionMap) {

		
		occurences.entrySet().stream()
				.filter(e -> !e.getValue().getEndItemsList().isEmpty()
						&& !currentEndItem.equals(e.getValue().getEndItemsList().get(0)))
				.findFirst().ifPresent(f -> {occrenceActionMap.put(f.getKey(), CHANGE); occWithendItemDifferentThanCurrentFound = true;});
		
		
		if (!occWithendItemDifferentThanCurrentFound) {
			occurences.entrySet().stream().filter(e -> !e.getValue().getEndItemsList().isEmpty())
			.findFirst().ifPresent(f -> occrenceActionMap.put(f.getKey(), NONE));
		}
	}

}
