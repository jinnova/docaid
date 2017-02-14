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

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.jinnova.docaid.PatientFamilyMember;
import com.jinnova.docaid.DBManager;
import com.jinnova.docaid.Patient;
import com.jinnova.docaid.PatientFamilyCandidate;
import com.jinnova.docaid.QueueTicket;
import com.jinnova.docaid.WaitingList;

public class FamilyPart {
	
	@Inject
	private ESelectionService selService;

	private TableViewer patientList;

	private Label candidateName;
	
	private Patient patient;
	
	private PatientFamilyCandidate candidate;

	private Button addButton;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		candidateName = new Label(parent, SWT.None);
		candidateName.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		candidateName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton = new Button(parent, SWT.PUSH);
		addButton.setText("        "); //$NON-NLS-1$
		addButton.addSelectionListener(new SelectionListener() {
			
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
		
		patientList = new PatientTableBuilder().builderViewer(parent);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		patientList.getTable().setLayoutData(layoutData);
		final Table table = patientList.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true); 
		patientList.addSelectionChangedListener(new ISelectionChangedListener() {
		    public void selectionChanged(SelectionChangedEvent event) {
		      
		      IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		      if (selection.size() < 1) {
		    	  return;
		      }
		      
		      PatientFamilyMember m = (PatientFamilyMember) selection.getFirstElement();
		      if (m.patient != null) {
		    	  selService.setSelection(m);
		      }

		      candidate = null;
		      populateCandidate();
		    }
		  });

		selService.addSelectionListener(new ISelectionListener() {
			
			@Override
			public void selectionChanged(MPart part, Object selection) {

				//System.out.println("got selection");
				Patient p = null;
				if (selection instanceof Patient) {
					p = (Patient) selection;
				} else if (selection instanceof QueueTicket) {
					p = ((QueueTicket) selection).patient;
				}
				if (p != null && !p.equals(patient)) {
					patient = p;
					candidate = null;
					new Thread() {
						public void run() {
							loadMemberPatients();
						}
					}.start();
					populate();
					return;
				}
				
				if (selection instanceof PatientFamilyCandidate) {
					PatientFamilyCandidate newCandidate = (PatientFamilyCandidate) selection;
					if (candidate == null || !newCandidate.patient.equals(candidate.patient)) {
						candidate = newCandidate;
						populateCandidate();
					}
				}
			}
		});
	}
	
	private void loadMemberPatients() {
		if (patient == null) {
			return;
		}
		try {
			for (PatientFamilyMember m : patient.getFamilyMembers()) {
				if (m.patient != null) {
					continue;
				}
				QueueTicket t = WaitingList.getQueueItemAny(m.id);
				if (t != null) {
					m.patient = t.patient;
				} else {
					m.patient = DBManager.loadPatient(m.id);
				}
			}
		} catch (SQLException e) {
			//ignore
			e.printStackTrace();
		}
		patientList.getTable().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				patientList.refresh();
			}
		});
	}
	
	private void populate() {
		if (patient == null) {
			patientList.setInput(null);
		} else {
			patientList.setInput(patient.getFamilyMembers());
		}
		populateCandidate();
	}
	
	private void populateCandidate() {
		if (candidate != null) {
			candidateName.setText(candidate.patient.name.getValue());
		} else {
			PatientFamilyMember member = getSelectedMember();
			if (member != null) {
				candidateName.setText(member.name);
			} else {
				candidateName.setText(""); //$NON-NLS-1$
			}
		}
		NamedAction action = getAction();
		addButton.setEnabled(action != null);
		if (action == null) {
			addButton.setText(""); //$NON-NLS-1$
		} else {
			addButton.setText(action.name);
		}
	}
	
	private PatientFamilyMember getSelectedMember() {
		IStructuredSelection sel = (IStructuredSelection) this.patientList.getSelection();
		if (sel != null && !sel.isEmpty()) {
			return (PatientFamilyMember) sel.getFirstElement();
		}
		return null;
	}
	
	private NamedAction getAction() {
		if (patient == null) {
			return null;
		}
		if (candidate != null) {
			if (candidate.patient.id.getValue() == patient.id.getValue()) {
				return null;
			} else if (patient.isInFamily(candidate.patient.id.getValue())) {
				return removeAction;
			} else {
				return addAction;
			}
		}
		PatientFamilyMember member = getSelectedMember();
		if (member != null && patient.id.getValue() != member.id) {
			return removeAction;
		}
		return null;
	}
	
	private NamedAction addAction = new NamedAction(Messages.FamilyPart_action_add) {
		
		@Override
		void run() {
			if (candidate == null || patient == null) {
				return;
			}
			try {
				patient.addFamilyMember(candidate.patient);
				candidate = null;
				populate();
			} catch (SQLException e1) {
				throw new RuntimeException(e1);
			}
		}
	};
	
	private NamedAction removeAction = new NamedAction(Messages.FamilyPart_action_remove) {
		
		@Override
		void run() {
			if (patient == null) {
				return;
			}
			
			Integer removingId = null;
			Patient removingPatient = null;
			if (candidate != null && patient.isInFamily(candidate.patient.id.getValue())) {
				removingId = candidate.patient.id.getValue();
				removingPatient = candidate.patient;
			} else {
				PatientFamilyMember m = getSelectedMember();
				if (m != null) {
					removingId = m.id;
					removingPatient = m.patient;
				}
			}
			if (removingId == null) {
				return;
			}
			try {
				patient.removeFamilyMember(removingId, removingPatient);
				candidate = null;
				populate();
			} catch (SQLException e1) {
				throw new RuntimeException(e1);
			}
		}
	};
}