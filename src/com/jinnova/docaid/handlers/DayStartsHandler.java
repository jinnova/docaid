package com.jinnova.docaid.handlers;

import java.sql.SQLException;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import com.jinnova.docaid.DBManager;
import com.jinnova.docaid.SettingName;

public class DayStartsHandler {
	
	@Execute
	public void execute(MApplication app, EPartService partService, EModelService modelService) {
		try {
			DBManager.deleteAllQueueItems();
			DBManager.updateSetting(SettingName.queueing_next.name(), 1);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
