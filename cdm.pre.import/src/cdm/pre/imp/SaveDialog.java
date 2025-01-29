package cdm.pre.imp;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SaveDialog extends TitleAreaDialog {

   private String endItem;
   private String fileLocation;
   private Text   textEndItem;
   private Text   textFileLocation;

   public String getEndItem() {
      return endItem;
   }

   public String getFileLocation() {
      return fileLocation;
   }

   public SaveDialog(Shell parentShell) {
      this(parentShell, null, null);
   }

   public SaveDialog(final Shell parentShell, final String oldFileLocation, final String rootEndItem) {
      super(parentShell);
      endItem = rootEndItem;
      fileLocation = oldFileLocation;
   }

   @Override
   public void create() {
      super.create();
      setTitle("Save Import XML");
      setMessage("Define EndItem and location for the cdm.tc.import.prod.");
      getShell().setSize(550, 280);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite comp = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 3;
      comp.setLayout(layout);

      GridData gridData = new GridData();
      gridData.grabExcessHorizontalSpace = true;
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessVerticalSpace = true;
      gridData.verticalAlignment = SWT.FILL;
      comp.setLayoutData(gridData);

      Label label1 = new Label(comp, SWT.NONE);
      label1.setText("EndItem Name:");

      gridData = new GridData();
      gridData.grabExcessHorizontalSpace = true;
      gridData.horizontalAlignment = GridData.FILL;
      gridData.horizontalSpan = 2;

      textEndItem = new Text(comp, SWT.BORDER);
      textEndItem.setLayoutData(gridData);
      if (endItem != null) {
         textEndItem.setText(endItem);
         textEndItem.setEnabled(false);
      }

      Label label2 = new Label(comp, SWT.NONE);
      label2.setText("File:");
      // You should not re-use GridData
      gridData = new GridData();
      gridData.grabExcessHorizontalSpace = true;
      gridData.horizontalAlignment = GridData.FILL;
      textFileLocation = new Text(comp, SWT.BORDER);
      textFileLocation.setLayoutData(gridData);

      if (fileLocation != null) {
         textFileLocation.setText(fileLocation);
      }

      Button fileButton = new Button(comp, SWT.PUSH);
      fileButton.setText("File...");
      fileButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            FileDialog dlg = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
            dlg.setFilterExtensions(new String[] { "*.xml" });
            textFileLocation.setText(dlg.open());
         }
      });

      return comp;
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, OK, "OK", true);
      createButton(parent, CANCEL, "Cancel", false);
   }

   @Override
   protected void okPressed() {
      if ("".equals(textFileLocation.getText()) || "".equals(textEndItem.getText())) {
         setMessage("Please enter values", IMessageProvider.ERROR);
      } else {
         fileLocation = textFileLocation.getText();
         if (!(fileLocation.endsWith(".xml"))) {
            fileLocation = fileLocation + ".xml";
         }
         endItem = textEndItem.getText();
         super.okPressed();
      }
   }

   @Override
   protected boolean isResizable() {
      return true;
   }
}
