package com.jinnova.docaid.parts;

import java.util.Comparator;
import java.util.LinkedList;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;

import com.jinnova.docaid.SettingName;

public class TableBuilder {

	
	private class ColumnInfo {
		int order;
		int width;
		String caption;
		ColumnLabelProvider labelProvider;
	}
	
	private LinkedList<ColumnInfo> allColumns = new LinkedList<ColumnInfo>();
	
	void createColumn(SettingName fieldId, int width, 
			String caption, ColumnLabelProvider labelProvider) {
		
		if (!SettingName.isSet(fieldId.name() + "_enabled", true)) {
			return;
		}
		
		int order = SettingName.getInt(fieldId.name() + "_order", -1);
		ColumnInfo c = new ColumnInfo();
		c.order = order;
		c.width = width;
		c.caption = caption;
		c.labelProvider = labelProvider;
		allColumns.add(c);
	}
	
	void build(TableViewer viewer) {
		allColumns.sort(new Comparator<ColumnInfo>() {

			@Override
			public int compare(ColumnInfo o1, ColumnInfo o2) {
				if (o1.order == -1 && o2.order == -1) {
					return 0;
				}
				if (o1.order == -1) {
					return 1;
				}
				if (o2.order == -1) {
					return -1;
				}
				return o1.order - o2.order;
			}
		});
		for (ColumnInfo c : allColumns) {
			createColumn(viewer, c);
		}
	}
	
	private void createColumn(TableViewer viewer, ColumnInfo c) {
		
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setWidth(c.width);
		col.getColumn().setText(c.caption);
		col.setLabelProvider(c.labelProvider);
	}
}
