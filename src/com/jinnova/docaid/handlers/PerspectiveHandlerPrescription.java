package com.jinnova.docaid.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class PerspectiveHandlerPrescription {
	
	@Execute
	public void execute(MApplication app, EPartService partService, EModelService modelService) {
		MPerspective element = (MPerspective) modelService.find("com.jinnova.docaid.perspective.prescription", app);
		// now switch perspective
		partService.switchPerspective(element);
	}

}
