package cdm.pre.imp.app;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

   private static final String PERSPECTIVE_ID = "cdm.pre.imp.perspective";

   @Override
   public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
      return new ApplicationWorkbenchWindowAdvisor(configurer);
   }

   @Override
   public String getInitialWindowPerspectiveId() {
      return PERSPECTIVE_ID;
   }

}
