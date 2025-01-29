package cdm.pre.imp.mod;

import java.lang.reflect.InvocationTargetException;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdm.pre.imp.reader.IConstants;
import cdm.pre.imp.reader.PLMUtils;

public class ProjectVisitor extends AbstractVisitor {
	private final static Logger LOGGER = LogManager.getLogger(ProjectVisitor.class.getName());

	@Override
	public void visit(TreeElement element) throws NoSuchMethodException, SecurityException, ClassNotFoundException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (element == null) {
			return;
		}
		
		LOGGER.debug(String.format("Visit %s (Class: %s, OBID: %s)", element.getElement().getId(), element.getClazz(), element.getOBID()));
		
		if (element.getOBID() == null) {
			LOGGER.info("element {} has undefined OBID ", element.getElement().getId());
		}
		
		if (!element.getClazz().equals(IConstants.JT)) {	
			// for Part and CDI
			if (element != null && element.getParent() != null) {
				String parentProject = element.getParent().getProject();
				if (parentProject == null) {
					parentProject = element.getParent().getElement().getUpdatedProjectName();
					LOGGER.debug(String.format("Parent updated project name: %s", parentProject));
				}else {
					LOGGER.debug(String.format("Parent project name: %s", parentProject));
				}
				String instanceProject = element.getElement().getUserValues().get(IConstants.ProjectName);
				LOGGER.debug(String.format("Curent element project name: %s", instanceProject));
				
				
				if (parentProject != null && instanceProject != null) {
					if (PLMUtils.isSecuredProject(instanceProject) || PLMUtils.isCommonProject(instanceProject)) {
						element.getElement().setUpdatedProjectName(instanceProject);
					} else {
						if (instanceProject.equals(parentProject)) {
							if (element.getParent().getElement().getUpdatedProjectName() != null
									&& element.getParent().getElement().getUpdatedProjectName().contains("_COP")) {
								element.getElement().setUpdatedProjectName(
										element.getParent().getElement().getUpdatedProjectName());
							} else {
								element.getElement().setUpdatedProjectName(instanceProject);
							}
						} else if (!instanceProject.equals(parentProject)) {
							if (element.getParent().getElement().getUpdatedProjectName() == null) {
								if (PLMUtils.isSecuredProject(parentProject)
										|| PLMUtils.isCommonProject(parentProject)) {
									element.getElement().setUpdatedProjectName(instanceProject);
								} else if (element.getParent().getProject() != null) {
									element.getElement()
											.setUpdatedProjectName(element.getParent().getProject() + "_COP");
								} else {
									element.getElement().setUpdatedProjectName(instanceProject);
								}
							}

							else if (element.getParent().getElement().getUpdatedProjectName().endsWith("_COP")) {
								element.getElement().setUpdatedProjectName(
										element.getParent().getElement().getUpdatedProjectName());
							} else {
								if (PLMUtils.isSecuredProject(element.getParent().getElement().getUpdatedProjectName())
										|| PLMUtils.isCommonProject(
												element.getParent().getElement().getUpdatedProjectName())) {
									element.getElement().setUpdatedProjectName(instanceProject);
								} else {
									element.getElement().setUpdatedProjectName(
											element.getParent().getElement().getUpdatedProjectName() + "_COP");
								}
							}
						}
					}
				}
			}

		} else {

			// for JT
			if (element != null && element.getParent() != null) {
				String parentProject = element.getParent().getProject();
				if (parentProject == null) {
					parentProject = element.getParent().getElement().getUpdatedProjectName();
					LOGGER.debug(String.format("Parent updated project name: %s", parentProject));
				}else {
					LOGGER.debug(String.format("Parent project name: %s", parentProject));
				}
				String instanceProject = element.getElement().getUserValues().get(IConstants.ProjectName);
				LOGGER.debug(String.format("Curent element project name: %s", instanceProject));
				
				if (parentProject != null && instanceProject != null) {
					if (PLMUtils.isSecuredProject(instanceProject) || PLMUtils.isCommonProject(instanceProject)) {
						element.getElement().setUpdatedProjectName(instanceProject);
					} else {
						if (instanceProject.equals(parentProject)) {
							String project = element.getParent().getElement().getUpdatedProjectName();
							if (project == null) {
								project = instanceProject;
							}
							element.getElement().setUpdatedProjectName(project);
						} else if (!instanceProject.equals(parentProject)) {
							if (element.getParent().getElement().getUpdatedProjectName() == null) {
								// forTree.getElement().setUpdatedProjectName(instanceProject);
								if (PLMUtils.isSecuredProject(parentProject)
										|| PLMUtils.isCommonProject(parentProject)) {
									element.getElement().setUpdatedProjectName(instanceProject);
								} else if (element.getParent().getProject() != null) {
									element.getElement()
											.setUpdatedProjectName(element.getParent().getProject() + "_COP");
								} else {
									element.getElement().setUpdatedProjectName(instanceProject);
								}
							}

							else if (element.getParent().getElement().getUpdatedProjectName().endsWith("_COP")) {
								element.getElement().setUpdatedProjectName(
										element.getParent().getElement().getUpdatedProjectName());
							} else {
								if (PLMUtils.isSecuredProject(element.getParent().getElement().getUpdatedProjectName())
										|| PLMUtils.isCommonProject(
												element.getParent().getElement().getUpdatedProjectName())) {
									element.getElement().setUpdatedProjectName(instanceProject);
								} else {
									element.getElement().setUpdatedProjectName(
											element.getParent().getElement().getUpdatedProjectName() + "_COP");
								}

							}
						}
					}

				} else if (instanceProject == null) {
					if (element.getParent().getElement().getUpdatedProjectName() == null) {
						if (PLMUtils.isSecuredProject(parentProject) || PLMUtils.isCommonProject(parentProject)) {
							element.getElement().setUpdatedProjectName(instanceProject);
						} else if (element.getParent().getParent().getElement().getUpdatedProjectName() != null) {
							LOGGER.debug(String.format("Parent of parent updated project name: %s", element.getParent().getParent().getElement().getUpdatedProjectName()));
							element.getElement().setUpdatedProjectName(
									element.getParent().getParent().getElement().getUpdatedProjectName());
						} else if (element.getParent().getProject() != null) {
							element.getElement().setUpdatedProjectName(element.getParent().getProject());
						}
					} else {
						element.getElement().setUpdatedProjectName(element.getParent().getElement().getUpdatedProjectName());
					}
				}
			}

		}

		visitChildren(element);
	}

}
