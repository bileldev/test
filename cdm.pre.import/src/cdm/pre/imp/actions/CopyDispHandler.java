package cdm.pre.imp.actions;

import cdm.pre.imp.mod.TreeElement;

public class CopyDispHandler extends CopyHandler {

   public CopyDispHandler() {
   }

   @Override
   protected String getTextToCopy(TreeElement elem) {
      return elem.getDisplayText();
   }

}
