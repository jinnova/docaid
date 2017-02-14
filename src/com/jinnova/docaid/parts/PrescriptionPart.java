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

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.jinnova.docaid.DBManager;
import com.jinnova.docaid.DiagRecord;
import com.jinnova.docaid.Patient;
import com.jinnova.docaid.Prescription;
import com.jinnova.docaid.PrescriptionItem;
import com.jinnova.docaid.PrescriptionItemExtra;
import com.jinnova.docaid.QueuePeekingListener;
import com.jinnova.docaid.QueueStage;
import com.jinnova.docaid.QueueTicket;
import com.jinnova.docaid.WaitingList;

public class PrescriptionPart {
	
	@Inject
	private ESelectionService selService;

	private TableViewer tableViewer;

	private Button doneButton;

	private Label name;

	private Label healthNote;

	private Label symptoms;

	private Label diagBrief;

	private Label treatment;
	
	// ---model data---
	private Prescription prescription;
	
	private QueueTicket queueTicket;

	private Label diagDate;
	
	private final boolean showPatientDetails;

	private Label idLabel;

	private Label weight;

	private Label serviceCost;

	private Label presCost;

	private Label totalCost;
	
	private abstract class PrescriptionLabelProvider extends ColumnLabelProvider {
		  @Override
		  public String getText(Object element) {
			  if (element instanceof Prescription) {
				  return ""; //$NON-NLS-1$
			  }
			  PrescriptionItemExtra e = (PrescriptionItemExtra) element;
			  if (e.lineCount == 2 && !e.amountPackage) {
				  return ""; //$NON-NLS-1$
			  }
			  return getText(e.item);
		  }
		  
		  abstract String getText(PrescriptionItem item);
		  
		  boolean isUndefined(Integer i) {
			  return i == null || i == 0;
		  }
		  
		  boolean isUndefined(Float i) {
			  return i == null || i == 0;
		  }
		};
	
	public PrescriptionPart() {
		this(false);
	}
	
	public PrescriptionPart(boolean showPatientDetails) {
		this.showPatientDetails = showPatientDetails;
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(4, false));
		
		Color colorBlue = parent.getDisplay().getSystemColor(SWT.COLOR_BLUE);

		Label l = new Label(parent, SWT.None);
		l.setText(Messages.PrescriptionPart_name_colon);
		name = new Label(parent, SWT.None);
		name.setForeground(colorBlue);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL /*| GridData.GRAB_HORIZONTAL*/));
		
		l = new Label(parent, SWT.None);
		l.setText(Messages.PrescriptionPart_id_colon);
		idLabel = new Label(parent, SWT.None);
		GridData gdata = new GridData();
		final int secondColWidth = 60;
		gdata.widthHint = secondColWidth;
		idLabel.setLayoutData(gdata);
		idLabel.setForeground(colorBlue);
		
		l = new Label(parent, SWT.None);
		l.setText(Messages.PrescriptionPart_diagDate_colon);
		diagDate = new Label(parent, SWT.None);
		diagDate.setForeground(colorBlue);
		//diagDate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		//gdata.horizontalSpan = 3;
		diagDate.setLayoutData(gdata);
		
		l = new Label(parent, SWT.None);
		l.setText(Messages.PrescriptionPart_weight_colon);
		weight = new Label(parent, SWT.None);
		weight.setForeground(colorBlue);
		//weight.setText("  50kg  ");
		gdata = new GridData(/*GridData.FILL_HORIZONTAL*/);
		gdata.widthHint = secondColWidth;
		weight.setLayoutData(gdata);
		
		if (!showPatientDetails) {
			/*new Label(parent, SWT.None);
			new Label(parent, SWT.None);
			l = new Label(parent, SWT.None);
			l.setText("Toa thuốc:");
			presCost = new Label(parent, SWT.None);
			presCost.setForeground(colorBlue);
			gdata = new GridData();
			gdata.widthHint = secondColWidth;
			presCost.setLayoutData(gdata);*/
		} else {
			l = new Label(parent, SWT.None);
			l.setText(Messages.PrescriptionPart_histNote_colon);
			healthNote = new Label(parent, SWT.None);
			//healthNote.setForeground(colorBlue);
			//healthNote.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			gdata = new GridData(GridData.FILL_HORIZONTAL);
			gdata.horizontalSpan = 3;
			healthNote.setLayoutData(gdata);
			healthNote.setForeground(colorBlue);
	
			l = new Label(parent, SWT.None);
			l.setText(Messages.PrescriptionPart_symptoms_colon);
			symptoms = new Label(parent, SWT.None);
			//symptoms.setForeground(colorBlue);
			symptoms.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			gdata = new GridData(GridData.FILL_HORIZONTAL);
			//gdata.horizontalSpan = 3;
			symptoms.setLayoutData(gdata);
			symptoms.setForeground(colorBlue);
			
			l = new Label(parent, SWT.None);
			l.setText(Messages.PrescriptionPart_services_colon);
			serviceCost = new Label(parent, SWT.None);
			serviceCost.setForeground(colorBlue);
			gdata = new GridData();
			gdata.widthHint = secondColWidth;
			serviceCost.setLayoutData(gdata);
			
			l = new Label(parent, SWT.None);
			l.setText(Messages.PrescriptionPart_diagBrief_colon);
			diagBrief = new Label(parent, SWT.None);
			//diagBrief.setForeground(colorBlue);
			diagBrief.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			gdata = new GridData(GridData.FILL_HORIZONTAL);
			//gdata.horizontalSpan = 3;
			diagBrief.setLayoutData(gdata);
			diagBrief.setForeground(colorBlue);
			
			l = new Label(parent, SWT.None);
			l.setText(Messages.PrescriptionPart_prescription_colon);
			presCost = new Label(parent, SWT.None);
			presCost.setForeground(colorBlue);
			gdata = new GridData();
			gdata.widthHint = secondColWidth;
			presCost.setLayoutData(gdata);
			
			l = new Label(parent, SWT.None);
			l.setText(Messages.PrescriptionPart_treatments_colon);
			treatment = new Label(parent, SWT.None);
			//treatment.setForeground(colorBlue);
			treatment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			gdata = new GridData(GridData.FILL_HORIZONTAL);
			//gdata.horizontalSpan = 3;
			treatment.setLayoutData(gdata);
			treatment.setForeground(colorBlue);
			
			l = new Label(parent, SWT.None);
			l.setText(Messages.PrescriptionPart_total_colon);
			totalCost = new Label(parent, SWT.None);
			totalCost.setForeground(colorBlue);
			gdata = new GridData();
			gdata.widthHint = secondColWidth;
			totalCost.setLayoutData(gdata);
		}
		
		tableViewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		gdata = new GridData(GridData.FILL_BOTH);
		gdata.horizontalSpan = 4;
		tableViewer.getTable().setLayoutData(gdata);

		createColumn(30, Messages.PrescriptionPart_queueNumber_abbr, new PrescriptionLabelProvider() {
			  @Override
			  public String getText(PrescriptionItem p) {
			      return String.valueOf(prescription.indexOf(p) + 1);
			  }
			});
		createColumn(150, Messages.PrescriptionPart_medName, new PrescriptionLabelProvider() {
			  @Override
			  public String getText(PrescriptionItem p) {
			      return p.medName.getValue();
			  }
			});
		createColumn(70, Messages.PrescriptionPart_medDosage, new PrescriptionLabelProvider() {
			  @Override
			  public String getText(PrescriptionItem p) {
				  if (isUndefined(p.amountPerTaking.getValue()) ||
						  isUndefined(p.takingCountPerDay.getValue()) || p.medUnit == null) {
					  return ""; //$NON-NLS-1$
				  }
			      return p.amountPerTaking.getValue() + " " + p.medUnit + " x " +  //$NON-NLS-1$ //$NON-NLS-2$
						  p.takingCountPerDay.getValue(); 
			  }
			});
		createColumn(50, Messages.PrescriptionPart_days, new PrescriptionLabelProvider() {
			  @Override
			  public String getText(PrescriptionItem p) {
				  if (isUndefined(p.dayCount.getValue())) {
					  return ""; //$NON-NLS-1$
				  }
			      return p.dayCount.getValue() + Messages.PrescriptionPart_days_spaceprefix;
			  }
			});
		createColumn(50, Messages.PrescriptionPart_amount, new ColumnLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  /*if (element instanceof PrescriptionItemExtra) {
					  PrescriptionItem pi = (PrescriptionItem) ((PrescriptionItemExtra) element).item;
					  return pi.amountTotalUnit + " " + pi.medUnit;
				  }
				  PrescriptionItem p = (PrescriptionItem) element;
				  if (p.amountTotalPackage != null && p.amountTotalPackage != 0) {
					  return p.amountTotalPackage + " " + p.medPackage;
				  } else if (p.amountTotalUnit != null && p.amountTotalUnit != 0) {
					  return p.amountTotalUnit + " " + p.medUnit;
				  }
			      return "";*/
				  if (element instanceof PrescriptionItemExtra) {
					  PrescriptionItemExtra extra = (PrescriptionItemExtra) element;
					  PrescriptionItem pi = extra.item;
					  if (extra.amountPackage) {
						  if (pi.amountTotalPackage == null || pi.amountTotalPackage == 0) {
							  return ""; //$NON-NLS-1$
						  }
						  return pi.amountTotalPackage + " " + pi.medPackage; //$NON-NLS-1$
					  } else {
						  if (pi.amountTotalUnit == null || pi.amountTotalUnit == 0) {
							  return ""; //$NON-NLS-1$
						  }
						  return pi.amountTotalUnit + " " + pi.medUnit; //$NON-NLS-1$
					  }
				  }
			      return ""; //$NON-NLS-1$
			  }
			});
		
		TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.RIGHT);
		col.getColumn().setWidth(90);
		col.getColumn().setText(Messages.PrescriptionPart_cost);
		col.setLabelProvider(new ColumnLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  /*if (element instanceof PrescriptionItemExtra) {
					  PrescriptionItem pi = (PrescriptionItem) ((PrescriptionItemExtra) element).item;
					  if (pi.amountTotalUnit == null || pi.medUnitPrice == null) {
						  return "";
					  }
					  return pi.amountTotalUnit * pi.medUnitPrice + "";
				  }
				  PrescriptionItem p = (PrescriptionItem) element;
				  if (p.amountTotalPackage != null && p.amountTotalPackage != 0) {
					  if (p.amountTotalPackage == null || p.medPackagePrice == null) {
						  return "";
					  }
					  return p.amountTotalPackage * p.medPackagePrice + "";
				  } else if (p.amountTotalUnit != null && p.amountTotalUnit != 0) {
					  if (p.amountTotalUnit == null || p.medUnitPrice == null) {
						  return "";
					  }
					  return p.amountTotalUnit * p.medUnitPrice + "";
				  }
			      return "";*/
				  if (element instanceof PrescriptionItemExtra) {
					  PrescriptionItemExtra extra = (PrescriptionItemExtra) element;
					  PrescriptionItem pi = extra.item;
					  if (extra.amountPackage) {
						  if (pi.amountTotalPackage == null || pi.medPackagePrice == null) {
							  return ""; //$NON-NLS-1$
						  }
						  return pi.amountTotalPackage * pi.medPackagePrice + ""; //$NON-NLS-1$
					  } else {
						  if (pi.amountTotalUnit == null || pi.medUnitPrice == null) {
							  return ""; //$NON-NLS-1$
						  }
						  return pi.amountTotalUnit * pi.medUnitPrice + ""; //$NON-NLS-1$
					  }
				  }
				  
				  if (element instanceof Prescription) {
					  return ((Prescription) element).computeTotalCost() + ""; //$NON-NLS-1$
				  }
			      return ""; //$NON-NLS-1$
			  }
			});
		createColumn(150, Messages.PrescriptionPart_note, new PrescriptionLabelProvider() {
			  @Override
			  public String getText(PrescriptionItem p) {
			      return p.getNoteReading();
			  }
			});
		
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 4;
		tableViewer.getTable().setLayoutData(layoutData);
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true); 
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
		    public void selectionChanged(SelectionChangedEvent event) {
		      IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		      if (selection.size() > 0) {
		    	  Object o = selection.getFirstElement();
		    	  PrescriptionItem pi = null;
		    	  if (o instanceof PrescriptionItem) {
		    		  pi = (PrescriptionItem) o;
		    	  } else if (o instanceof PrescriptionItemExtra) {
		    		  pi = ((PrescriptionItemExtra) o).item;
		    	  }
		    	  if (pi != null) {
		    		  pi.prescription.setEditingItem(pi);
				      selService.setSelection(pi);
		    	  }
		      }
		    }
		  });

		if (showPatientDetails /*|| SettingName.isSet(SettingName.diag_skipMedStage.name())*/) {
			doneButton = new Button(parent, SWT.None);
			gdata = new GridData(GridData.FILL_HORIZONTAL);
			gdata.horizontalSpan = 4;
			gdata.heightHint = 40;
			doneButton.setLayoutData(gdata);
			doneButton.setText(Messages.PrescriptionPart_complete);
			doneButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					
					NamedAction action = getAction();
					if (action != null) {
						action.run();
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		
		//diagListViewer.setInput(diagList);
		//WaitingList.registerViewer(diagListViewer);
		selService.addSelectionListener(new ISelectionListener() {

			//private DiagRecord newDiag;
			
			@Override
			public void selectionChanged(MPart part, Object selection) {
				DiagRecord newDiag = null;
				QueueTicket newTicket = null;
				if (selection instanceof QueueTicket) {
					newTicket = (QueueTicket) selection;
					newDiag = newTicket.patient.todayDiag;
				} else if (selection instanceof DiagRecord) {
					newDiag = (DiagRecord) selection;
				} else if (selection instanceof Patient) {
					//newDiag = ((Patient) selection).getOrCreateDirectTicket().diag;
					newDiag = ((Patient) selection).todayDiag;
				}
				
				if (newDiag == null || newDiag.prescription == prescription) {
					return;
				}
				
				queueTicket = newTicket;
				prescription = newDiag.prescription;
				populate();
			}
		});
		
		Patient.addPatientSelectionEmptyListener(new Runnable() {
			
			@Override
			public void run() {
				queueTicket = null;
				prescription = null;
				populate();
				//tableViewer.setInput(null);
			}
		});
		
		WaitingList.diagQueue.addQueuePeekingListener(new QueuePeekingListener() {
			
			@Override
			public void queuePeeked(QueueTicket ticket) {
				queueTicket = ticket;
				if (ticket == null) {
					prescription = null;
				} else {
					prescription = ticket.patient.todayDiag.prescription;
				}
				populate();
			}
		});
		populate();
	}
	
	/*private boolean shouldDoNext() {
		return queueTicket != null && queueTicket.stage != QueueStage.med;
	}*/
	
	private NamedAction nextAction = new NamedAction(Messages.PrescriptionPart_next_patient) {
		@Override
		void run() {
			queueTicket = WaitingList.medQueue.peek();
			if (queueTicket != null) {
				prescription = queueTicket.patient.todayDiag.prescription;
			} else {
				prescription = null;
			}
			populate();
		}
	};
	
	private NamedAction closeAction = new NamedAction(Messages.PrescriptionPart_close) {
		
		@Override
		void run() {
			queueTicket = null;
			prescription = null;
			populate();
		}
	};
	
	private NamedAction saveAction = new NamedAction(Messages.PrescriptionPart_complete) {
		
		@Override
		void run() {
			queueTicket.stage = QueueStage.end;
			int patientId = queueTicket.patient.id.getValue();
			try {
				DBManager.updateWaitingStage(patientId, queueTicket.stage.name());
			} catch (SQLException e1) {
				throw new RuntimeException();
			}
			WaitingList.medQueue.dequeue(patientId);
			queueTicket = null;
			prescription = null;
			populate();
		}
	};
	
	private NamedAction getAction() {
		
		if (queueTicket != null && queueTicket.stage == QueueStage.med) {
			return saveAction;
		}
		
		if (prescription == null) {
			return nextAction;
		}
		
		return closeAction;
	}
	
	private void populate() {
		
		/*if (queueTicket == null) {
			doneButton.setText("Bệnh nhân kế tiếp");
			doneButton.setEnabled(true);
		} else if (queueTicket.stage != QueueStage.med) {
			doneButton.setText("Đóng");
			doneButton.setEnabled(true);
		} else {
			doneButton.setText("Hoàn tất");
			doneButton.setEnabled(true);
		}*/

		if (doneButton != null) {
			doneButton.setText(getAction().name);
		}
		if (prescription != null) {

			prescription.setChangeListener(new Runnable() {
				
				@Override
				public void run() {
					//tableViewer.refresh();
					tableViewer.setInput(prescription.createItemArray());
				}
			});
			tableViewer.setInput(prescription.createItemArray());
			if (prescription.diag.isInDayNotEnd()) {
				diagDate.setText(Messages.PrescriptionPart_new_in_brackets);
			} else {
				diagDate.setText(prescription.diag.date.getEasyLabel());
			}
			idLabel.setText(prescription.diag.patient.id.getValueAsEditing());
			name.setText(prescription.diag.patient.name.getValueAsEditing() + " (" + //$NON-NLS-1$
						prescription.diag.getAgeReading() + ")"); //$NON-NLS-1$
			weight.setText(prescription.diag.weight.getValueAsEditing());
				//System.out.println("weight" + weight.getText());
			int c2 = prescription.computeTotalCost();
			if (showPatientDetails) {
				presCost.setText(String.valueOf(c2));
				healthNote.setText(prescription.diag.patient.healthNote.getValueAsEditing());
				symptoms.setText(prescription.diag.symptons.getValueAsEditing());
				diagBrief.setText(prescription.diag.diagnosis.getValueAsEditing());
				treatment.setText(prescription.diag.treatment.getValueAsEditing());
				int c1 = prescription.diag.computeServiceCost();
				serviceCost.setText(String.valueOf(c1));
				//int c2 = prescription.computeTotalCost();
				//presCost.setText(String.valueOf(c2));
				totalCost.setText(String.valueOf(c1 + c2));
			}
		} else {
			tableViewer.setInput(null);
			diagDate.setText(""); //$NON-NLS-1$
			idLabel.setText(""); //$NON-NLS-1$
			name.setText(""); //$NON-NLS-1$
			weight.setText(""); //$NON-NLS-1$
			if (showPatientDetails) {
				presCost.setText(""); //$NON-NLS-1$
				healthNote.setText(""); //$NON-NLS-1$
				symptoms.setText(""); //$NON-NLS-1$
				diagBrief.setText(""); //$NON-NLS-1$
				treatment.setText(""); //$NON-NLS-1$
				serviceCost.setText(""); //$NON-NLS-1$
				//presCost.setText("");
				totalCost.setText(""); //$NON-NLS-1$
				tableViewer.setInput(null);
			}
		}
		//doneButton.setEnabled(queueTicket != null && queueTicket.stage == QueueStage.med);
		//tableViewer.setInput(newDiag.prescription.createItemArray());
	}
	
	private void createColumn(int width, String caption, ColumnLabelProvider labelProvider) {
		TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.NONE);
		col.getColumn().setWidth(width);
		col.getColumn().setText(caption);
		col.setLabelProvider(labelProvider);
	}

	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}
}