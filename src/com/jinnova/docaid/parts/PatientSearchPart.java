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
import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.jinnova.docaid.DBManager;
import com.jinnova.docaid.Patient;
import com.jinnova.docaid.PatientFamilyCandidate;

public class PatientSearchPart {
	
	@Inject
	private ESelectionService selService;

	private Text nameInput;
	private TableViewer patientList;
	private Button searchButton;
	
	private boolean familying = false;
	
	public PatientSearchPart() {
		this(false);
	}
	
	public PatientSearchPart(boolean familying) {
		this.familying = familying;
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(4, false));
		Label label = new Label(parent, SWT.None);
		label.setText(Messages.PatientSearchPart_name);

		nameInput = new Text(parent, SWT.BORDER);
		nameInput.setMessage(Messages.PatientSearchPart_enter_name_id_message);
		//nameInput.addModifyListener(modifyListener);
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 2;
		nameInput.setLayoutData(gdata);
		nameInput.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				search();
			}
		});
		nameInput.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					search();
				}
			}
		});

		searchButton = new Button(parent, SWT.None);
		searchButton.setText(Messages.PatientSearchPart_search);
		//searchButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		
		searchButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				search();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		/*Button newButton = new Button(parent, SWT.None);
		newButton.setText("Bệnh nhân mới");
		newButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				selService.setSelection(new Patient());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});*/
		
		patientList = new PatientTableBuilder().builderViewer(parent);
		
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 4;
		patientList.getTable().setLayoutData(layoutData);
		final Table table = patientList.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true); 
		patientList.addSelectionChangedListener(new ISelectionChangedListener() {
		    public void selectionChanged(SelectionChangedEvent event) {
		    	//System.out.println("sel changed from search widget");
		    	//hack selection service to force event always (even with same selection)
		    	selService.setSelection(new Object());
		      IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		      // set the selection to the service
		      //selService.setSelection(
		      //    selection.size() == 1 ? selection.getFirstElement() : selection.toArray());
		      Object obj;
		      if (selection.size() > 0) {
		    	  obj = selection.getFirstElement();
		      } else {
		    	  return;
		      }
		      
		      if (!familying) {
		    	  selService.setSelection(obj);
		      } else {
		    	  selService.setSelection(new PatientFamilyCandidate((Patient) obj));
		      }
		    }
		  });
	}
	
	private void search() {
		try {
			String name = nameInput.getText();
			LinkedList<Patient> patients;
			if ("".equals(name.trim())) { //$NON-NLS-1$
				patients = DBManager.lastVisits();
			} else {
				try {
					int id = Integer.parseInt(name);
					Patient p = DBManager.loadPatient(id);
					patients = new LinkedList<Patient>();
					if (p != null) {
						patients.add(p);
					}
				} catch (NumberFormatException ne) {
					patients = DBManager.searchName(name);
				}	
			}
			patientList.setInput(patients.toArray());
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Focus
	public void setFocus() {
		//tableViewer.getTable().setFocus();
		nameInput.setFocus();
	}
}