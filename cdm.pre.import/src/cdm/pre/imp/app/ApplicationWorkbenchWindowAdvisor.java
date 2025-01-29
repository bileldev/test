package cdm.pre.imp.app;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceNode;

import cdm.pre.imp.VersionInfo;

@SuppressWarnings("restriction")
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
   private final static Logger logger = Logger.getLogger(ApplicationWorkbenchWindowAdvisor.class.getName());

   public ApplicationWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
      super(configurer);
   }

   @Override
   public ActionBarAdvisor createActionBarAdvisor(final IActionBarConfigurer configurer) {
      return new ApplicationActionBarAdvisor(configurer);
   }

   @SuppressWarnings("deprecation")
@Override
   public void preWindowOpen() {
      IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
      configurer.setInitialSize(new Point(700, 500));
      //configurer.setShowFastViewBars(false);
      configurer.setShowCoolBar(false);
      configurer.setShowMenuBar(false);
      configurer.setShowPerspectiveBar(false);
      configurer.setShowStatusLine(true);
      configurer.setShowProgressIndicator(true);
      StringBuilder sb = new StringBuilder("PLMXML Compare" + " - Version " + "[" + VersionInfo.getVersionNumber() + "]");
      try {
         String[] manifestVersion = VersionInfo.getManifestVersionNumber();
         if (manifestVersion != null && manifestVersion.length > 0) {
            sb.append(", Build: " + manifestVersion[0]);
         }
      } catch (IOException ignore) {
         logger.throwing(ApplicationWorkbenchWindowAdvisor.class.getName(), "preWindowOpen", ignore);
      }
      configurer.setTitle(sb.toString());
   }


   /**
    * Method to perform some actions post to the window creation when the application starts up
    */
   @SuppressWarnings({ "rawtypes" })
   @Override
   public void postWindowCreate() {
	   /**
	    * START : This change is to show only "cdm.pre.imp.prefs.PreferencesPage" and block other preference pages
	    */
	   PreferenceManager preferenceManager = PlatformUI.getWorkbench().getPreferenceManager();
       List elements = preferenceManager.getElements(PreferenceManager.PRE_ORDER);
       for (Object o: elements) {
    	   WorkbenchPreferenceNode node = (WorkbenchPreferenceNode)o;
    	   if (!node.getId().startsWith("cdm.pre.imp.prefs.PreferencesPage")) {
    		   preferenceManager.remove(node);
    	   }
       }
       /**
        * END : This change is to show only "cdm.pre.imp.prefs.PreferencesPage" and block other preference pages
        */
   }
   
}
