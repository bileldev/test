package cdm.pre.imp;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

import cdm.pre.imp.actions.SourceProvider;
import cdm.pre.imp.batch.BatchException;
import cdm.pre.imp.map.JTFileManagerUtils;
import cdm.pre.imp.map.NXPartFileManagerUtils;
import cdm.pre.imp.mod.TreeElement;
import cdm.pre.imp.prefs.PreImpConfig;
import cdm.pre.imp.prefs.PreferenceConstants;
import cdm.pre.imp.reader.PLMUtils;
import cdm.pre.imp.reader.PLMUtils.SortedElements;
import cdm.pre.imp.reader.ReaderSingleton;
import cdm.pre.imp.reader.TruckException;

public class StructView extends ViewPart implements IViewOptions {
	private final static Logger    logger     = LogManager.getLogger("cdm.pre.imp.tracelog");
	public static final String     ID         = "cdm.pre.imp.view";

	final private Button[]         reloadBtns = new Button[2];
	private Mode                   mode;
	private Tree                   treeTable;
	private TreeViewer             treeViewer;
	private TreeElementComp        treeElemComp;
	private Control                focusCtrl;
	private Composite              filterComp;
	private List<TreeElement>      roots1;
	private List<TreeElement>      roots2;
	private String                 oldFileLocation;
	private File                   plmxmlFile1;
	private File                   plmxmlFile2;
	private Date                   lastModifPLMXML1;
	private Date                   lastModifPLMXML2;

	private Text[]                 fileTexts  = new Text[2];
	private Date                   lastModifDate;
	private IStatusLineManager     statusLine;
	final private SimpleDateFormat df         = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
	private WriterJob writerJob;
	private ReaderJob readerJob;

	public StructView()
	{
		String version = VersionInfo.getVersionNumber();
		if(version == null)
		{
			version = "";
			logger.warn("version number is not available. Please define Version.properties");
		}
		else
		{
			logger.info("Version Number : "+version);
		}

		try 
		{
			String buildDate;
			buildDate = VersionInfo.getBuildDate();
			String startDateString = buildDate;
			DateFormat df = new SimpleDateFormat("dd.MM.yyyy"); 
			Date startDate;
			try {
				startDate = df.parse(startDateString);
				String newDateString = df.format(startDate);

				final SimpleDateFormat df1         = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
				newDateString =  df1.format(startDate);
				logger.info(" Build Date :: "+newDateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	void checkWidgets(final int which, final boolean jobFinished) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				SourceProvider sp = getSP(SourceProvider.S1);
				sp.enableS1(roots1 != null);
				sp.enableS2(roots2 != null);
				getAltHierarchyState().setValue(Boolean.FALSE);
				filterComp.setEnabled((roots1 != null) || (roots2 != null));
				reloadBtns[which - 1].setEnabled(jobFinished);
				Date date = null;
				File file = null;
				List<TreeElement> roots = null;
				if (which == 1) {
					date = lastModifPLMXML1;
					file = plmxmlFile1;
					roots = roots1;
				} else {
					date = lastModifPLMXML2;
					file = plmxmlFile2;
					roots = roots2;
				}

				StringBuilder sb = new StringBuilder("File: " + file.getAbsolutePath());
				logger.info("File: " + file.getAbsolutePath());
				sb.append('\n');
				if (date != null) {
					sb.append("Date: " + DateDefinitions.SDF_DISPLAY.format(date));
					logger.info("Date: " + DateDefinitions.SDF_DISPLAY.format(date));
					sb.append('\n');
				}
				sb.append("File Size (kb): " + file.length() / 1024);
				logger.info("File Size (kb): " + file.length() / 1024);
				if (roots != null && roots.size() > 0 && roots.get(0) != null && roots.get(0).getRefConfigName() != null) {
					sb.append('\n');
					sb.append("RefConfig: " + roots.get(0).getRefConfigName());
					logger.info("RefConfig: " + roots.get(0).getRefConfigName());
				}
				fileTexts[which - 1].setToolTipText(sb.toString());
			}
		});
	}

	// private to protected -- changes by Krishna

	protected void readPLMXML(final String filename, final int which) {
		logger.info("Reading PLMXML...... ");
		//System.out.println("Inside readPLMXML file Method");
		if (which == 1) {
			roots1 = null;
			plmxmlFile1 = new File(filename);
			lastModifPLMXML1 = new Date(plmxmlFile1.lastModified());
		} else {
			roots2 = null;
			plmxmlFile2 = new File(filename);
			lastModifPLMXML2 = new Date(plmxmlFile2.lastModified());
		}
		checkWidgets(which, false);
		readerJob = new ReaderJob(filename);
		readerJob.schedule();
		readerJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				if (event.getResult().getSeverity() == IStatus.OK) {
					boolean activateAltHier = false;
					final List<TreeElement> roots = readerJob.getRoots();
					if (roots != null) {
						activateAltHier = true;
						for (TreeElement root : roots) {
							if (!root.hasAltHier()) {
								activateAltHier = false;
							}
						}
					}
					getSP(SourceProvider.ALTHIER_ACTIVE).enableAltHier(activateAltHier);
					if (which == 1) {
						lastModifPLMXML1 = readerJob.getExportDate();
						roots1 = roots;
					} else {
						lastModifPLMXML2 = readerJob.getExportDate();
						roots2 = roots;
					}
					checkWidgets(which, true);
				}
			}
		});
		//	logger.info("EXIT readPLMXML file Method");
		//	System.out.println("EXIT readPLMXML file Method");
	}


	public boolean validateInputPLMXML( List<TreeElement> roots )
	{
		boolean isValidInput = true;
		// Validating Version Number
		Display display = Display.getDefault();// PlatformUI.createDisplay();
		//MessageDialog md = new MessageDialog(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex)
		if( roots != null)
		{
			// Validating Pre Importer Version Number
			if(VersionInfo.getVersionNumber() == null)
			{
				isValidInput = false;
				MessageDialog.openError(display.getActiveShell(), "Pre Importer Error",
						"Pre Importer Version Number details missing.  \" " 
								+ " Please contact Administrator...");
				logger.error("Pre Importer Error...............\n Pre Importer Version Number details missing. Please contact Administrator........ \n");
			}

			// Validating Reference Configuration or ENDITEM Name

			else if(roots.get(0).getRefConfigName() == null)
			{
				isValidInput = false;
				MessageDialog.openError(display.getActiveShell(), "Pre Importer Error",
						" Reference Configuration information is missing.   \n " 
								+ " Please validate the input PLMXML file ...");
				logger.error("Pre Importer Error...............\n Reference Configuration information is missing. Please validate the input PLMXML file ........ \n");
			}

			// Validating Root or Main Item PARTNUMBER

			else if(roots.get(0).getPartNumber() == null)
			{
				isValidInput = false;
				MessageDialog.openError(display.getActiveShell(), "Pre Importer Error",
						"Root Item PARTNUMBER missing.  \" " 
								+ " Please validate the input PLMXML file ...");
				logger.error("Pre Importer Error...............\n Root Item PARTNUMBER missing.  Please validate the input PLMXML file ........ \n");
			}

		}

		else
		{
			isValidInput = false;
			MessageDialog.openError(display.getActiveShell(), "Pre Importer Error",
					"Root or Main Item missing.  \" " 
							+ " Please validate the input PLMXML file ...");
			logger.error("Pre Importer Error...............\n Root or Main Item missing. . Please validate the input PLMXML file ........ \n");
		}
		return isValidInput;





		// More No of Root Items in the PLMXML


	}

	public void showPLMXML1() {
		logger.info("Displaying PLMXML1....");
		//System.out.println("show PLMXML1 Method");
		mode = Mode.PLMXML1;
		showRoots(roots1);
		getSP(SourceProvider.R1).setR1(true);
		getSP(SourceProvider.R2).setR2(false);
		getSP(SourceProvider.DELTA).setDelta(false);
		lastModifDate = lastModifPLMXML1;
		//logger.info("EXIT -- show PLMXML1 Method");
		//System.out.println("EXIT -- show PLMXML1 Method");
	}

	public void showPLMXML2() {
		logger.info("Displaying PLMXML....");
		mode = Mode.PLMXML2;
		showRoots(roots2);
		getSP(SourceProvider.R2).setR1(false);
		getSP(SourceProvider.R2).setR2(true);
		getSP(SourceProvider.DELTA).setDelta(false);
		lastModifDate = lastModifPLMXML2;
	}

	public void compareRoots() {
		List<TreeElement> compRes = TreeElement.compareRoots(roots1, roots2);
		mode = Mode.Delta;
		showRoots(compRes);
		getSP(SourceProvider.R1).setR1(false);
		getSP(SourceProvider.R2).setR2(false);
		getSP(SourceProvider.DELTA).setDelta(true);
		lastModifDate = lastModifPLMXML1;
		boolean enableDeltaExpVal = (lastModifPLMXML1.getTime() - lastModifPLMXML2.getTime()) > 0;
		if (enableDeltaExpVal && roots1.get(0) != null && roots2.get(0) != null) {
			String refConf1 = roots1.get(0).getRefConfigName();
			String refConf2 = roots2.get(0).getRefConfigName();
			if (refConf1 != null) {
				enableDeltaExpVal = refConf1.equals(refConf2);
			} else {
				enableDeltaExpVal = refConf2 == null;
			}
		}
		getSP(SourceProvider.EXPVAL).setDeltaExpVal(enableDeltaExpVal);
	}

	private void showRoots(final List<TreeElement> roots) {
		logger.info("Mode is " + mode);
		int stat[] = new int[3];
		stat[0] = stat[1] = stat[2] = 0;
		for (TreeElement root : roots) {
			int statR[] = root.getStatistics();
			stat[0] += statR[0];
			stat[1] += statR[1];
			stat[2] += statR[2];
		}
		double delta = 0.;
		if (stat[0] != 0) {
			delta = stat[1] * 100.0 / stat[0];
		}
		final NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		final StringBuilder sb = new StringBuilder("Showing " + mode);
		if (lastModifPLMXML1 != null) {
			sb.append(", Timestamp 1: " + df.format(lastModifPLMXML1));
			logger.info("Timestamp 1: " + df.format(lastModifPLMXML1));
		}
		if (lastModifPLMXML2 != null) {
			sb.append(", Timestamp 2: " + df.format(lastModifPLMXML2));
			logger.info("Timestamp 2: " + df.format(lastModifPLMXML2));
			if (lastModifPLMXML1 != null) {
				final long diff = lastModifPLMXML1.getTime() - lastModifPLMXML2.getTime();
				final double div = 60 * 60 * 1000 * 24.0;
				final double dd = diff / div;

				sb.append(", Time Diff: " + nf.format(dd) + " days");
				logger.info("Time Diff: " + nf.format(dd) + " days");
			}
		}
		if (mode == Mode.Delta) {
			sb.append(", Delta: " + nf.format(delta) + " %");
			logger.info(" Delta: " + nf.format(delta) + " %");
		}
		// writeStatusInfo("Showing " + mode + ", Number of all occcurrences: " +
		// stat[0] + ", Delta: " + nf.format(delta)
		// + " %, number of elements " + stat[2]);
		writeStatusInfo(sb.toString());
		treeViewer.setInput(roots);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent) {
		statusLine = getViewSite().getActionBars().getStatusLineManager();
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		mainComp.setLayout(layout);

		Composite headerComp = new Composite(mainComp, SWT.NONE);
		headerComp.setLayout(new GridLayout(2, false));
		headerComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		Composite ctrlComp = new Composite(headerComp, SWT.NONE);
		ctrlComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		treeViewer = new TreeViewer(mainComp, SWT.FULL_SELECTION);

		Composite statusComp = new Composite(mainComp, SWT.NONE);
		statusComp.setLayout(new GridLayout(3, false));
		statusComp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		createFilterComp(headerComp).setLayoutData(new GridData(SWT.RIGHT, SWT.BEGINNING, false, false));
		layout = new GridLayout(4, false);
		ctrlComp.setLayout(layout);

		for (int i = 1; i <= 2; i++) {
			final int step = i;
			Label label = new Label(ctrlComp, SWT.RIGHT);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			label.setText("PLMXML " + i + ":");
			final Text text = new Text(ctrlComp, SWT.SINGLE | SWT.LEAD | SWT.BORDER | SWT.READ_ONLY);
			fileTexts[i - 1] = text;
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			if (step == 1) {
				focusCtrl = text;
			}

			Button buttonFile = new Button(ctrlComp, SWT.PUSH);
			buttonFile.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			buttonFile.setText("File ...");

			final Button buttonReload = new Button(ctrlComp, SWT.PUSH);
			buttonReload.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			buttonReload.setText("Reload");
			buttonReload.setEnabled(false);
			reloadBtns[i - 1] = buttonReload;
			buttonFile.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(final SelectionEvent e) {
					/*FileDialog dlg = new FileDialog(parent.getShell(), SWT.OPEN);
					String filename = dlg.open();
					text.setToolTipText(filename);
					if (filename != null) {
						text.setText(filename);
						List<TreeElement> roots = step == 1 ? roots1 : roots2;
						if (roots != null && !roots.isEmpty()) {
							// roots.get(0).getElement().
						}
						StructView.this.readPLMXML(filename, step);
					}
					buttonReload.setEnabled(text.getText() != null && !"".equals(text.getText()));*/

					String configMappingFilePath = PreImpConfig.getInstance().getConfigMappingFilePath();
					if(configMappingFilePath != null && !configMappingFilePath.equals(""))
					{
						logger.info(" Mapping File : "+configMappingFilePath);
						logger.info(" Vehicle Type : "+PreImpConfig.getInstance().getVehicleType());
						FileDialog dlg = new FileDialog(parent.getShell(), SWT.OPEN);
						String filename = dlg.open();
						text.setToolTipText(filename);
						if (filename != null) {
							text.setText(filename);
							List<TreeElement> roots = step == 1 ? roots1 : roots2;
							if (roots != null && !roots.isEmpty()) {
								// roots.get(0).getElement().
							}
							StructView.this.readPLMXML(filename, step);
						}
						buttonReload.setEnabled(text.getText() != null && !"".equals(text.getText()));
					}
					else
					{
						MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_WARNING | SWT.ABORT );

						messageBox.setText("Warning");
						logger.warn("Warning");
						messageBox.setMessage("Configuration Mapping file is not set. please set the location for Configuration mapping file and Vehicle Type in Preferences and Re-open the pre-importer");
						logger.warn("Configuration Mapping file is not set. please set the location for Configuration mapping file and Vehicle Type in Preferences and Re-open the pre-importer");
						int buttonID = messageBox.open();
						switch(buttonID) {
						case SWT.YES:
							// saves changes ...
						case SWT.NO:
							// exits here ...
							break;
						case SWT.CANCEL:
							// does nothing ...
						}
					}

				}

				@Override
				public void widgetDefaultSelected(final SelectionEvent e) {
				}
			});

			buttonReload.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(final SelectionEvent e) {
					StructView.this.readPLMXML(text.getText(), step);
				}

				@Override
				public void widgetDefaultSelected(final SelectionEvent e) {
				}
			});
		}
		treeViewer.setContentProvider(new TreeContentProvider(this));
		treeViewer.setAutoExpandLevel(2);
		treeViewer.setUseHashlookup(true);

		ColumnViewerToolTipSupport.enableFor(treeViewer);
		treeTable = treeViewer.getTree();
		MenuManager mm = new MenuManager();
		treeTable.setMenu(mm.createContextMenu(treeTable));
		getSite().registerContextMenu("cdm.pre.import.tree.menu", mm, treeViewer);
		getSite().setSelectionProvider(treeViewer);
		treeTable.setHeaderVisible(true);
		treeTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeElemComp = new TreeElementComp();
		treeViewer.setComparator(treeElemComp);

		final TreeViewerColumn partDisplayCol = new TreeViewerColumn(treeViewer, SWT.LEFT);
		partDisplayCol.getColumn().setText("Display Name");
		partDisplayCol.getColumn().setWidth(400);

		TreeCellLabelProvider labelPrv = new TreeCellLabelProvider();
		partDisplayCol.setLabelProvider(new DecoratingStyledCellLabelProvider(labelPrv, PlatformUI.getWorkbench().getDecoratorManager()
				.getLabelDecorator(), null));
		int column = 1;
		final TreeViewerColumn partNumberCol = new TreeViewerColumn(treeViewer, SWT.LEFT);
		partNumberCol.getColumn().setText("Part Number");
		partNumberCol.getColumn().setWidth(150);
		partNumberCol.getColumn().setMoveable(true);
		partNumberCol.getColumn().addSelectionListener(createSelectionAdapter(partNumberCol, column++));
		partNumberCol.setLabelProvider(labelPrv);

		TreeViewerColumn partNameCol = new TreeViewerColumn(treeViewer, SWT.LEFT);
		partNameCol.getColumn().setText("Nomenclature");
		partNameCol.getColumn().setWidth(150);
		partNameCol.getColumn().setMoveable(true);
		partNameCol.getColumn().addSelectionListener(createSelectionAdapter(partNameCol, column++));
		partNameCol.setLabelProvider(labelPrv);

		TreeViewerColumn revisionCol = new TreeViewerColumn(treeViewer, SWT.RIGHT);
		revisionCol.getColumn().setText("Revision");
		revisionCol.getColumn().setWidth(80);
		revisionCol.getColumn().setMoveable(true);
		revisionCol.getColumn().addSelectionListener(createSelectionAdapter(revisionCol, column++));
		revisionCol.setLabelProvider(labelPrv);

		TreeViewerColumn sequenceCol = new TreeViewerColumn(treeViewer, SWT.RIGHT);
		sequenceCol.getColumn().setText("Sequence");
		sequenceCol.getColumn().setWidth(80);
		sequenceCol.getColumn().setMoveable(true);
		sequenceCol.getColumn().addSelectionListener(createSelectionAdapter(sequenceCol, column++));
		sequenceCol.setLabelProvider(labelPrv);

		TreeViewerColumn folderTypeCol = new TreeViewerColumn(treeViewer, SWT.LEFT);
		folderTypeCol.getColumn().setText("Folder Type");
		folderTypeCol.getColumn().setWidth(100);
		folderTypeCol.getColumn().setMoveable(true);
		folderTypeCol.getColumn().addSelectionListener(createSelectionAdapter(folderTypeCol, column++));
		folderTypeCol.setLabelProvider(labelPrv);

		TreeViewerColumn modTypeCol = new TreeViewerColumn(treeViewer, SWT.LEFT);
		modTypeCol.getColumn().setText("Change");
		modTypeCol.getColumn().setWidth(100);
		modTypeCol.getColumn().setMoveable(true);
		modTypeCol.getColumn().addSelectionListener(createSelectionAdapter(modTypeCol, column++));
		modTypeCol.setLabelProvider(labelPrv);

		TreeViewerColumn relCntCol = new TreeViewerColumn(treeViewer, SWT.RIGHT);
		relCntCol.getColumn().setText("Rel.Count");
		relCntCol.getColumn().setWidth(50);
		relCntCol.getColumn().setMoveable(true);
		relCntCol.getColumn().addSelectionListener(createSelectionAdapter(relCntCol, column++));
		relCntCol.setLabelProvider(labelPrv);

		TreeViewerColumn clazzCol = new TreeViewerColumn(treeViewer, SWT.LEFT);
		clazzCol.getColumn().setText("Sma. Class");
		clazzCol.getColumn().setWidth(100);
		clazzCol.getColumn().setMoveable(true);
		clazzCol.getColumn().addSelectionListener(createSelectionAdapter(clazzCol, column++));
		clazzCol.setLabelProvider(labelPrv);

		TreeViewerColumn idCol = new TreeViewerColumn(treeViewer, SWT.LEFT);
		idCol.getColumn().setText("id");
		idCol.getColumn().setWidth(60);
		idCol.getColumn().setMoveable(true);
		idCol.getColumn().addSelectionListener(createSelectionAdapter(idCol, column++));
		idCol.setLabelProvider(labelPrv);

		TreeViewerColumn projCol = new TreeViewerColumn(treeViewer, SWT.LEFT);
		projCol.getColumn().setText("Project");
		projCol.getColumn().setWidth(100);
		projCol.getColumn().setMoveable(true);
		projCol.getColumn().addSelectionListener(createSelectionAdapter(projCol, column++));
		projCol.setLabelProvider(labelPrv);

		TreeViewerColumn transCol = new TreeViewerColumn(treeViewer, SWT.LEFT);
		transCol.getColumn().setText("Matrix");
		transCol.getColumn().setWidth(100);
		transCol.getColumn().setMoveable(true);
		transCol.setLabelProvider(labelPrv);
	}

	private SelectionAdapter createSelectionAdapter(final TreeViewerColumn column, final int index) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Tree tree = treeViewer.getTree();
				int sortDir = tree.getSortDirection();
				int treeElemSort = 0;
				if (sortDir == 0 || sortDir == SWT.NONE) {
					sortDir = SWT.UP;
					treeElemSort = 1;
				} else if (sortDir == SWT.UP) {
					sortDir = SWT.DOWN;
					treeElemSort = -1;
				} else if (sortDir == SWT.DOWN) {
					sortDir = SWT.UP;
					treeElemSort = 0;
				}
				treeElemComp.setColumnIndex(index);
				treeElemComp.setSortDir(treeElemSort);
				tree.setSortDirection(sortDir);
				tree.setSortColumn(column.getColumn());
				treeViewer.refresh();
				if (treeElemSort == 0) {
					tree.setSortDirection(SWT.NONE);
				}
			}
		};
	}

	@Override
	public void setFocus() {
		if (focusCtrl != null) {
			focusCtrl.setFocus();
		}
	}

	private Composite createFilterComp(final Composite root) {
		filterComp = new Composite(root, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 0;
		filterComp.setLayout(gl);

		Composite comp1 = new Composite(filterComp, SWT.NONE);
		comp1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 0;
		comp1.setLayout(gl);

		Composite comp2 = new Composite(filterComp, SWT.NONE);
		comp2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 0;
		comp2.setLayout(gl);
		final String[] diffTexts = { "In PLMXML 1 but not in PLMXML 2.", "In PLMXML 2 but not in PLMXML 1.",
		"In PLMXML 1 and in PLMXML 2 but not identical." };
		final int[] colors = new int[] { TreeCellLabelProvider.NEW_CLR, TreeCellLabelProvider.MISSING_CLR, TreeCellLabelProvider.MOD_CLR };
		final Button[] diffBtns = new Button[diffTexts.length];
		for (int i = 0; i < 3; i++) {
			Label label = new Label(comp1, SWT.CENTER);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
			label.setText(diffTexts[i]);
			label.setForeground(Display.getCurrent().getSystemColor(colors[i]));
			final Button btn = new Button(comp1, SWT.CHECK);
			diffBtns[i] = btn;
			btn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		}
		Label labelInvalid = new Label(comp1, SWT.NONE);
		labelInvalid.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		labelInvalid.setText("Show invalid SmaDia2 Elements.");
		Button checkInvalid = new Button(comp1, SWT.CHECK);
		checkInvalid.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		Label labelFind = new Label(comp2, SWT.NONE);
		labelFind.setText("Find Name/Part Number");
		Text text = new Text(comp2, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		GridData textGD = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		textGD.widthHint = 170;
		text.setLayoutData(textGD);

		// add filter elements
		FilterSelectionListener fsl = new FilterSelectionListener(treeViewer);
		fsl.setDiffBtns(diffBtns);
		fsl.setText(text);
		fsl.setInvalidBtn(checkInvalid);
		return filterComp;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void writeImportXML() throws BatchException, TruckException {
		logger.info("Writing Intermediate XML...");
		@SuppressWarnings("unchecked")

		List<TreeElement> roots = (List<TreeElement>) treeViewer.getInput();
		if (roots.size() == 1) 
		{
			roots.get(0).setLastModifDate(lastModifDate);
			if (oldFileLocation == null) {
				oldFileLocation = PreImpConfig.getInstance().getImportXMLFile();
			}

			// check to see if the vehicle type is Truck
			if(ReaderSingleton.getReaderSingleton().getFzgTypeName() != null && !ReaderSingleton.getReaderSingleton().getFzgTypeName().isEmpty()
					&& ReaderSingleton.getReaderSingleton().getFzgTypeName().equals(PreferenceConstants.P_FZG_TYP_LKW)) {
				// 17/05/2017 Amit - added code to generate the truck hierarchy and main project in case the input vehicle type is Truck
				// sets the Truck project name in the singleton for the BCS elements in the DMU XML file
				if(ReaderSingleton.getReaderSingleton().getBcsProjectName() == null) {
					ReaderSingleton.getReaderSingleton().setBcsProjectName(roots.get(0).getPartNumber());
				}
				// call to generate the alternate hierarchy
				roots = PLMUtils.generateTruckHierarchy(roots);
			}

			SaveDialog dlg = new SaveDialog(Display.getCurrent().getActiveShell(), oldFileLocation, roots.get(0).getRefConfigName());
			dlg.create();
			if (dlg.open() == Window.OK) 
			{
				logger.info("Intermediate XML File Name : "+dlg.getFileLocation());
				oldFileLocation = dlg.getFileLocation();
				NXPartFileManagerUtils.setPlmXmlFileLoc(oldFileLocation); // Added this line to store the file location (UG Part Handling)
				JTFileManagerUtils.setPlmXmlFileLoc(oldFileLocation);

				writerJob = new WriterJob(new XMLFileData(roots.get(0), dlg.getFileLocation(), dlg.getEndItem(), plmxmlFile1, plmxmlFile2,mode));
				writerJob.schedule();
			}
		}
	}

	private void writeStatusInfo(final String msg) {
		logger.info("StatusInfo   ::::::  "+msg);
		statusLine.setMessage(msg);
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void toggleAltHier(final boolean oldValue) {
		getSP(SourceProvider.ALTHIER_SET).setAltHier((Boolean) getAltHierarchyState().getValue());
	}

	private SourceProvider getSP(final String name) {
		ISourceProviderService sourceProviderService = (ISourceProviderService) getSite().getWorkbenchWindow().getService(
				ISourceProviderService.class);
		return (SourceProvider) sourceProviderService.getSourceProvider(name);
	}

	private State getAltHierarchyState() {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command altHierCommand = service.getCommand("cdm.pre.import.althier.handler");
		return altHierCommand.getState("org.eclipse.ui.commands.toggleState");
	}

	@Override
	public boolean isAltHierarchyMode() {
		return (Boolean) getAltHierarchyState().getValue();
	}

	/*
	 * getPlmxmlFile1() method added by Krishna
	 * 
	 */
	public String getPlmxmlFile1()
	{
		if(plmxmlFile1 != null)
		{
			return plmxmlFile1.getAbsolutePath();
		}
		return null;
	}

	/*
	 * getPlmxmlFile2() method added by Krishna
	 * 
	 */
	public String getPlmxmlFile2()
	{
		if(plmxmlFile2 != null)
		{
			return plmxmlFile2.getAbsolutePath();
		}
		return null;
	}

	/*
	 * getPlmxmlFile2() method added by Krishna
	 * 
	 */
	public String getIntermediateXMLFile()
	{
		if(oldFileLocation != null)
		{
			return oldFileLocation;
		}
		return null;
	}

	/*
	 * getWriterJob() method added by Krishna
	 * 
	 */

	public WriterJob getWriterJob() {
		// TODO Auto-generated method stub
		return writerJob;
	}

	/*
	 * getSortedElements() method added by Krishna
	 * 
	 */
	public SortedElements getSortedElements()
	{
		return readerJob.getSortedElements();
	}

	public Date getlastModifPLMXML1()
	{
		System.out.println("******************************lastModifPLMXML1****************************** : "+lastModifDate);
		return lastModifPLMXML1;
	}

}