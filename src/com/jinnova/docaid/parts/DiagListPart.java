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
import java.util.Comparator;
import java.util.LinkedList;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.jinnova.docaid.DBManager;
import com.jinnova.docaid.DiagRecord;
import com.jinnova.docaid.Patient;
import com.jinnova.docaid.QueuePeekingListener;
import com.jinnova.docaid.QueueTicket;
import com.jinnova.docaid.WaitingList;

public class DiagListPart {
	
	@Inject
	private ESelectionService selService;

	private TableViewer diagListViewer;
	
	private Patient currentPatient;
	private LinkedList<DiagRecord> diagList;
	
	private Color colorOpening;

	private Label patientNameLabel;
	
	private class DiagLabelProvider extends ColumnLabelProvider {

		  @Override
		  public Color getForeground(Object element) {
			  DiagRecord d = (DiagRecord) element;
			  //if (d.id.getValue() == null) {
			  if (d.isInDayNotEnd()) {
				  return colorOpening;
			  }
			  return null;
		  }
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		colorOpening = parent.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		
		patientNameLabel = new Label(parent, SWT.None);
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		//gdata.widthHint = 200;
		patientNameLabel.setLayoutData(gdata);
		patientNameLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		
		diagListViewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		//patientList.add("Sample item 1");
		//patientList.add("Sample item 2");
		diagListViewer.setContentProvider(ArrayContentProvider.getInstance());
		//patientList.setInput(new String[] {"a", "b"});

		createColumn(30, Messages.DiagListPart_queueNumberAbbr, new DiagLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  DiagRecord p = (DiagRecord) element;
				  //int i = diagList.size() - diagList.indexOf(p);
			      return String.valueOf(diagList.indexOf(p) + 1);
			  }
			});
		createColumn(100, Messages.DiagListPart_date, new DiagLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  DiagRecord d = (DiagRecord) element;
				  //if (d.id.getValue() == null || d.id.getValue() == -1) {
				  if (d.isInDayNotEnd()) {
					  return Messages.DiagListPart_new_in_brackets;
				  } else {
					  return d.date.getEasyLabel();
				  }
			  }
			});
		createColumn(50, Messages.DiagListPart_weight, new DiagLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  DiagRecord d = (DiagRecord) element;
			    return String.valueOf(d.weight.getValueAsEditing());
			  }
			});
		createColumn(50, Messages.DiagListPart_height, new DiagLabelProvider() {
			  @Override
			  public String getText(Object element) {
				  DiagRecord d = (DiagRecord) element;
			    return String.valueOf(d.height.getValueAsEditing());
			  }
			});
		createColumn(800, "", new DiagLabelProvider() { //$NON-NLS-1$
			  @Override
			  public String getText(Object element) {
				  DiagRecord d = (DiagRecord) element;
				  if (d.id.getValue() == null || d.id.getValue() == -1) {
					  return ""; //$NON-NLS-1$
				  }
				  String s = null;
				  s = append(s, Messages.DiagListPart_small_symtoms_colon, d.symptons.getValue());
				  s = append(s, Messages.DiagListPart_small_diag_colon, d.diagnosis.getValue());
				  s = append(s, Messages.DiagListPart_small_treatments_colon, d.treatment.getValue());
				  s = append(s, Messages.DiagListPart_small_prescription_colon, d.prescription.getSummary());
				  if (s == null) {
					  return ""; //$NON-NLS-1$
				  } else {
					  return s.substring(0, 1).toUpperCase() + s.substring(1);
				  }
			  }
			  
			  private String append(String dest, String title, String value) {
				  if (value == null || "".equals(value.trim())) { //$NON-NLS-1$
					  return dest;
				  }
				  if (dest == null) {
					  return title + value;
				  } else {
					  return dest + ", " + title + value; //$NON-NLS-1$
				  }
			  }
			});
		
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 4;
		diagListViewer.getTable().setLayoutData(layoutData);
		final Table table = diagListViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true); 
		diagListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
		    public void selectionChanged(SelectionChangedEvent event) {
		      IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		      selService.setSelection(
		          selection.size() == 1 ? selection.getFirstElement() : selection.toArray());
		    }
		  });
		
		//diagListViewer.setInput(diagList);
		//WaitingList.registerViewer(diagListViewer);
		selService.addSelectionListener(new ISelectionListener() {

			//private Patient newPatient;
			//private LinkedList<DiagRecord> newDiagList;
			
			@Override
			public void selectionChanged(MPart part, Object selection) {
				Patient newPatient = null;
				DiagRecord newDiag = null;
				if (selection instanceof QueueTicket) {
					newDiag = ((QueueTicket) selection).patient.todayDiag;
					newPatient = newDiag.patient;
				} else if (selection instanceof Patient) {
					newPatient = (Patient) selection;
					newDiag = null;
					if (newPatient.id.getValue() != null) {
						QueueTicket q = WaitingList.diagQueue.getQueueItem(newPatient.id.getValue());
						if (q != null) {
							newDiag = q.patient.todayDiag;
						}
					}
					if (newDiag == null) {
						//newDiag = newPatient.getOrCreateDirectTicket().diag;
						newDiag = newPatient.todayDiag;
					}
				}
				
				if (newPatient == null || newPatient == currentPatient) {
					return;
				}
				/*if (diag != null) {
					diagList = new LinkedList<DiagRecord>();
					diagList.add(((QueuedPatient) selection).diag);
					diagListViewer.setInput(diagList);
					diagListViewer.setSelection(new StructuredSelection(diagList.getFirst()), true);
				}*/
				
				setPatient(newPatient, newDiag);
			}
			
			/*private void loadViewer(LinkedList<DiagRecord> newDiagList) {
				patientNameLabel.setText(currentPatient.name.getValueAsEditing() + " (" +
						currentPatient.dob.getValueAsEditing() + ")");
				diagList = newDiagList;
				diagListViewer.setInput(newDiagList.toArray());
			}*/
		});
		
		Patient.addPatientSelectionEmptyListener(new Runnable() {
			
			@Override
			public void run() {
				/*patientNameLabel.setText("");
				diagList = null;
				diagListViewer.setInput(null);*/
				setPatient(null, null);
			}
		});
		WaitingList.diagQueue.addQueuePeekingListener(new QueuePeekingListener() {
			
			@Override
			public void queuePeeked(QueueTicket ticket) {
				if (ticket == null) {
					setPatient(null, null);
				} else {
					setPatient(ticket.patient, ticket.patient.todayDiag);
				}
			}
		});
	}
	
	public void setPatient(Patient newPatient, DiagRecord newDiag) {
		
		if (newPatient == null) {
			populate(null, new LinkedList<>());
			return;
		}
		LinkedList<DiagRecord> newDiagList = new LinkedList<DiagRecord>();
		//if (diag != null && (diag.id.getValue() == null || diag.id.getValue() == -1)) {
		//boolean retainQueuedDiag = newDiag != null && newDiag.isInDayNotEnd();
		boolean retainQueuedDiag = newDiag != null && newDiag.isInDay();
		if (retainQueuedDiag) {
			newDiagList.add(newDiag);
		}
		new Thread() {
			public void run() {
				
				try {
					LinkedList<DiagRecord> loaded = DBManager.loadDiagRecords(newPatient);
					if (retainQueuedDiag) {
						loaded.remove(newDiag);
					}
					loaded.sort(new Comparator<DiagRecord>() {
						@Override
						public int compare(DiagRecord o1, DiagRecord o2) {
							//use backup value to get timestamp
							return o2.date.getBackupValue().compareTo(o1.date.getBackupValue());
						}
					});
					newDiagList.addAll(loaded);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				diagListViewer.getTable().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						populate(newPatient, newDiagList);
					}
				});
			}
		}.start();
	}
	
	private void populate(Patient newPatient, LinkedList<DiagRecord> newDiagList) {

		currentPatient = newPatient;
		if (currentPatient != null) {
			patientNameLabel.setText(currentPatient.name.getValueAsEditing() + " (" + //$NON-NLS-1$
				currentPatient.dob.getValueAsEditing() + ")"); //$NON-NLS-1$
		} else {
			patientNameLabel.setText(""); //$NON-NLS-1$
		}
		diagList = newDiagList;
		diagListViewer.setInput(newDiagList.toArray());
	}
	
	private void createColumn(int width, String caption, ColumnLabelProvider labelProvider) {
		TableViewerColumn col = new TableViewerColumn(diagListViewer, SWT.NONE);
		col.getColumn().setWidth(width);
		col.getColumn().setText(caption);
		col.setLabelProvider(labelProvider);
	}

	@Focus
	public void setFocus() {
		diagListViewer.getTable().setFocus();
	}
}