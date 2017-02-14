/*******************************************************************************
 * Copyright (c) 2010 - 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <lars.Vogel@gmail.com> - Bug 419770
 *******************************************************************************/
package com.jinnova.docaid.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.jinnova.docaid.FieldFloat;
import com.jinnova.docaid.QueueStage;
import com.jinnova.docaid.QueueTicket;
import com.jinnova.docaid.SettingName;
import com.jinnova.docaid.WaitingList;

public abstract class WaitingQueuePart {
	
	@Inject
	private ESelectionService selService;

	private TableViewer patientList;

	private Color colorHold;
	
	abstract WaitingList getWaitingList();
	
	private class QueueLabelProvider extends ColumnLabelProvider {

		  @Override
		  public Color getForeground(Object element) {
			  QueueTicket ticket = (QueueTicket) element;
			  if (ticket.stage == QueueStage.beginHold) {
				  return colorHold;
			  }
			  return null;
		  }
	}
	
	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		patientList = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		patientList.setContentProvider(ArrayContentProvider.getInstance());
		colorHold = parent.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		TableBuilder builder = new TableBuilder();
		builder.createColumn(SettingName.queue_table_field_queuenbr,
				30, Messages.WaitingQueuePart_queueNumber_abbr, new QueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  QueueTicket p = (QueueTicket) element;
				  String s = p.queueNumber.getValueAsEditing();
				  if (p.isModified()) {
					  return "*" + s; //$NON-NLS-1$
				  } else {
					  return s;
				  }
			  }
			});
		builder.createColumn(SettingName.queue_table_field_id,
				40, Messages.WaitingQueuePart_id, new QueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  QueueTicket p = (QueueTicket) element;
			    return String.valueOf(p.patient.id.getValueAsEditing());
			  }
			});
		builder.createColumn(SettingName.queue_table_field_name,
				150, Messages.WaitingQueuePart_name, new QueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  QueueTicket p = (QueueTicket) element;
				  String name = p.patient.name.getValueAsEditing();
				  if (p.stage == QueueStage.beginHold) {
					  //return name + Messages.WaitingQueuePart_wait_test_in_brackets;
					  return name + " (" + p.getHoldReason() + ")";
				  } else {
					  return name;
				  }
			  }
			});
		builder.createColumn(SettingName.queue_table_field_dob,
				80, Messages.WaitingQueuePart_dob, new QueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  QueueTicket p = (QueueTicket) element;
			    //return p.diag.patient.dob.getValueAsEditing();
			    if (p.patient.dob.isTodayPreviousYear()) {
			    	return p.patient.dob.getValueAsEditing() + Messages.WaitingQueuePart_birthday_in_brackets;
			    } else {
			    	return p.patient.dob.getValueAsEditing();
			    }
			  }
			});
		builder.createColumn(SettingName.queue_table_field_lastvisit,
				95, Messages.WaitingQueuePart_last_visit, new QueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  QueueTicket p = (QueueTicket) element;
			      //return p.diag.patient.lastVisit.getEasyLabel();
				  return p.previousVisit.getEasyLabel();
			  }
			});
		builder.createColumn(SettingName.queue_table_field_prevappoint,
				80, Messages.WaitingQueuePart_prev_appointment, new QueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  QueueTicket p = (QueueTicket) element;
			      //return p.diag.patient.previousAppointment.getEasyLabel();
				  return p.previousAppointment.getEasyLabel();
			  }
			  @Override
			  public Color getForeground(Object element) {
				  QueueTicket p = (QueueTicket) element;
				  if (p.patient.hasAppointmentToday()) {
					  return parent.getDisplay().getSystemColor(SWT.COLOR_BLUE);
				  }
				  return null;
			  }
			});
		builder.createColumn(SettingName.queue_table_field_address,
				300, Messages.WaitingQueuePart_address, new QueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  QueueTicket p = (QueueTicket) element;
			    return p.patient.address.getValueAsEditing();
			  }
			});
		builder.createColumn(SettingName.queue_table_field_age,
				50, Messages.WaitingQueuePart_age, new QueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  QueueTicket p = (QueueTicket) element;
			    return p.patient.todayDiag.age.getValueAsEditing();
			  }
			});
		builder.createColumn(SettingName.queue_table_field_weight,
				50, Messages.WaitingQueuePart_weight, new QueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  QueueTicket p = (QueueTicket) element;
			      //return FieldFloat.getString(p.patient.todayDiag.weight.getValue());
				  String s = p.patient.todayDiag.weight.getValue();
				  return s == null ? "" : s;
			  }
			});
		builder.build(this.patientList);
		
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 4;
		patientList.getTable().setLayoutData(layoutData);
		final Table table = patientList.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true); 
		patientList.addSelectionChangedListener(new ISelectionChangedListener() {
		    public void selectionChanged(SelectionChangedEvent event) {
		    	System.out.println("sel changed from widget"); //$NON-NLS-1$
		    	//hack selection service to force event always (even with same selection)
		    	selService.setSelection(new Object());
		    	IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		    	selService.setSelection(
		    		selection.size() == 1 ? selection.getFirstElement() : selection.toArray());
		    }
		  });
		
		
		getWaitingList().registerViewer(patientList);
	}
	
	/*private void createColumn(int width, String caption, ColumnLabelProvider labelProvider) {
		TableViewerColumn col = new TableViewerColumn(patientList, SWT.NONE);
		col.getColumn().setWidth(width);
		col.getColumn().setText(caption);
		col.setLabelProvider(labelProvider);
	}*/

	@Focus
	public void setFocus() {
		patientList.getTable().setFocus();
	}
}