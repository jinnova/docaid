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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jinnova.docaid.Patient;
import com.jinnova.docaid.QueueTicket;
import com.jinnova.docaid.SettingName;
import com.jinnova.docaid.WaitingList;

public class PatientInfoPart {

	@Inject
	private ESelectionService selService;
	@Inject
	private MDirtyable dirty;
	
	//private QueuedPatient queueItem = new QueuedPatient(null, new Patient());
	private QueueTicket newTemplate;
	private QueueTicket queueItem = newTemplate;

	private Text nameInput;
	//private TableViewer tableViewer;

	private Text addressInput;
	private Text phoneInput;
	private Text dobInput;
	private Label lastVisitInput;
	private Text weightInput;
	private Text heightInput;
	private Text healthNoteInput;
	private Text symtonsInput;
	
	private Button closeButton;
	private Button saveButton;

	private Label previousAppointment;

	private Text queueNumberInput;
	
	private Label alert;
	private Label idLabel;
	private Text age;
	private Text bodyTemp;
	private FieldAutoComplete healthNoteAuto;
	//private FieldAutoComplete symtomsAutotext;

	//private boolean waitingNumberExisted;

	//private Integer waitingNumber;
	
	public PatientInfoPart() {
		//DiagRecord dr = DiagRecord.newInstance(new Patient());
		newTemplate = new QueueTicket(null, new Patient());
		newTemplate.patient.createTodayDiag();
		//dr.ticket = newTemplate;
	}
	
	private abstract class FieldModifyListener implements ModifyListener {
		
		abstract void pushValue();
		
		@Override
		public void modifyText(ModifyEvent e) {
			if (queueItem == null) {
				return;
			}
			pushValue();
			boolean changed = queueItem.isModified();
			/*if (queueItem.diag.patient.isInDB()) {
				closeButton.setEnabled(true);
			} else {
				closeButton.setEnabled(changed);
			}*/
			closeButton.setEnabled(true);
			saveButton.setEnabled(changed);
			dirty.setDirty(changed);
		}
	};
	
	private Text createField(Composite parent, boolean traverseHandling, 
			int hspan, boolean multilines, String labelText, String msg) {
		
		int style;
		if (multilines) {
			style = SWT.MULTI | /*SWT.H_SCROLL |*/ SWT.V_SCROLL | SWT.SCROLL_LINE;
		} else {
			style = SWT.None;
		}
		Label label = new Label(parent, SWT.None);
		label.setText(labelText);

		Text text = new Text(parent, SWT.BORDER | style);
		text.setMessage(msg);
		if (traverseHandling) {
			text.addTraverseListener(new TraverseListener() {
				
				@Override
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_RETURN) {
						if (queueItem.patient.isInDB() && e.widget == queueNumberInput) {
							save();
						} else {
							text.traverse(SWT.TRAVERSE_TAB_NEXT);
						}
					}
				}
			});
		}
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = hspan;
		if (multilines) {
			gdata.heightHint = 40;
		}
		text.setLayoutData(gdata);
		return text;
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		
		parent.setLayout(new GridLayout(6, false));
		new Label(parent, SWT.None);
		
		alert = new Label(parent, SWT.None);
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 4;
		//gdata.grabExcessHorizontalSpace = true;
		//gdata.widthHint = 250;
		alert.setLayoutData(gdata);
		//alert.setText("alert message here");
		alert.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
		//alert.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
		
		//Composite comp = new Composite(parent, SWT.None);
		//comp.setLayout(new GridLayout(2, false));
		//gdata = new GridData(/*GridData.FILL_HORIZONTAL*/);
		//gdata.horizontalSpan = 4;
		//comp.setLayoutData(gdata);

		//Label label = new Label(comp, SWT.None);
		//label.setText(Messages.PatientInfoPart_id);
		
		idLabel = new Label(parent, SWT.None);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		//gdata.widthHint = 50;
		idLabel.setLayoutData(gdata);
		
		//Label stt = new Label(parent, SWT.None);
		//stt.setText(Messages.PatientInfoPart_queueNumber_abbr);
		nameInput = createField(parent, true, 3, false, Messages.PatientInfoPart_name, Messages.PatientInfoPart_patient_fullname);
		nameInput.addModifyListener(new FieldModifyListener() {
			@Override
			void pushValue() {
				queueItem.patient.name.changeValue(nameInput.getText());
			}
		});
		new Label(parent, SWT.None);
		
		queueNumberInput = new Text(parent, SWT.BORDER | SWT.CENTER);
		queueNumberInput.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		queueNumberInput.setMessage(String.valueOf(WaitingList.getNextNumber()));
		FontData fontData = queueNumberInput.getFont().getFontData()[0];
		fontData.setHeight(30);
		queueNumberInput.setFont(new Font(parent.getDisplay(), fontData));
		//queueNumberInput.addModifyListener(modifyListener);
		queueNumberInput.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					if (queueItem.patient.isInDB() && e.widget == queueNumberInput) {
						save();
					} else {
						queueNumberInput.traverse(SWT.TRAVERSE_TAB_NEXT);
					}
				}
			}
		});
		
		Color colorRed = queueNumberInput.getDisplay().getSystemColor(SWT.COLOR_RED);
		Color colorBlue = queueNumberInput.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		queueNumberInput.addModifyListener(new FieldModifyListener() {
			@Override
			void pushValue() {
				queueNumberInput.setForeground(colorBlue);
				queueItem.queueNumber.changeValue(queueNumberInput.getText());
				if (!queueItem.queueNumber.isModified()) {
					return;
				}
				Integer i = queueItem.queueNumber.getValue();
				if (i == null) {
					return;
				}
				if (!WaitingList.diagQueue.isNumberAvailable(i)) {
					queueNumberInput.setForeground(colorRed);
				}
			}
		});
		gdata = new GridData(GridData.FILL_BOTH);
		gdata.verticalSpan = 2;
		gdata.grabExcessVerticalSpace = false;
		gdata.grabExcessHorizontalSpace = false;
		//gdata.verticalAlignment = SWT.CENTER;
		//gdata.horizontalAlignment = SWT.CENTER;
		queueNumberInput.setLayoutData(gdata);
		queueNumberInput.addVerifyListener(new IntVerifyListener());
		
		dobInput = createField(parent, true, 1, false, Messages.PatientInfoPart_dob, Messages.PatientInfoPart_dob_full_ddmmyyyy);
		dobInput.addModifyListener(new DateModifyListener());
		dobInput.addModifyListener(new FieldModifyListener() {
			@Override
			void pushValue() {
				queueItem.patient.dob.changeValue(dobInput.getText());
				age.setText(queueItem.patient.dob.getAgeReading());
			}
		});
		Label label = new Label(parent, SWT.None);
		label.setText(Messages.PatientInfoPart_age);
		age = new Text(parent, SWT.BORDER);
		age.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(parent, SWT.None);
		
		addressInput = createField(parent, true, 3, false, Messages.PatientInfoPart_address, Messages.PatientInfoPart_patient_address);
		addressInput.addModifyListener(new FieldModifyListener() {
			@Override
			void pushValue() {
				queueItem.patient.address.changeValue(addressInput.getText());
			}
		});
		phoneInput = createField(parent, true, 1, false, Messages.PatientInfoPart_phone, Messages.PatientInfoPart_contact_phone);
		phoneInput.addModifyListener(new FieldModifyListener() {
			@Override
			void pushValue() {
				queueItem.patient.phone.changeValue(phoneInput.getText());
			}
		});
		weightInput = createField(parent, true, 1, false, Messages.PatientInfoPart_weight, "(kg)"); //$NON-NLS-2$
		weightInput.addVerifyListener(new FloatVerifyListener());
		weightInput.addModifyListener(new FieldModifyListener() {
			@Override
			void pushValue() {
				queueItem.patient.todayDiag.weight.changeValue(weightInput.getText());
			}
		});
		heightInput = createField(parent, true, 1, false, Messages.PatientInfoPart_height, "(cm)"); //$NON-NLS-2$
		heightInput.addVerifyListener(new IntVerifyListener());
		heightInput.addModifyListener(new FieldModifyListener() {
			@Override
			void pushValue() {
				queueItem.patient.todayDiag.height.changeValue(heightInput.getText());
			}
		});
		label = new Label(parent, SWT.None);
		label.setText(Messages.PatientInfoPart_bodyTemp);
		bodyTemp = new Text(parent, SWT.BORDER);
		bodyTemp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		bodyTemp.addVerifyListener(new FloatVerifyListener());
		bodyTemp.addModifyListener(new FieldModifyListener() {
			@Override
			void pushValue() {
				queueItem.patient.todayDiag.bodyTemp.changeValue(bodyTemp.getText());
			}
		});
		
		healthNoteInput = createField(parent, false, 5, true, Messages.PatientInfoPart_histNote, ""); //$NON-NLS-2$
		if (SettingName.isSet(SettingName.autotext_field_healthnote_enabled.name(), true)) {
			healthNoteAuto = new FieldAutoComplete(healthNoteInput, FieldAutoComplete.HEALTHNOTE);
		}
		healthNoteInput.addModifyListener(new FieldModifyListener() {
			@Override
			void pushValue() {
				queueItem.patient.healthNote.changeValue(healthNoteInput.getText());
			}
		});
		symtonsInput = createField(parent, false, 5, true, Messages.PatientInfoPart_symptoms, ""); //$NON-NLS-2$
		//symtomsAutotext = new FieldAutoComplete(symtonsInput, "symptom");
		symtonsInput.addModifyListener(new FieldModifyListener() {
			@Override
			void pushValue() {
				queueItem.patient.todayDiag.symptons.changeValue(symtonsInput.getText());
			}
		});
		/*symtonsInput.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					save();
				}
			}
		});*/
		
		//lastVisitInput = createField(parent, false, 1, false, "Lần khám trước", "");
		//lastVisitInput.setEnabled(false);
		label = new Label(parent, SWT.None);
		label.setText(Messages.PatientInfoPart_last_visit);
		lastVisitInput = new Label(parent, SWT.None);
		lastVisitInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		//previousAppointment = createField(parent, false, 1, false, "Hẹn tái khám (lần trước)", "");
		//previousAppointment.setEnabled(false);
		label = new Label(parent, SWT.None);
		label.setText(Messages.PatientInfoPart_prev_appointment);
		previousAppointment = new Label(parent, SWT.None);
		previousAppointment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(parent, SWT.None);
		new Label(parent, SWT.None);
		
		//new Label(parent, SWT.None); //empty label for place holder
		Composite buttonPane = new Composite(parent, SWT.BORDER_SOLID);
		GridData buttonPaneLayoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		buttonPaneLayoutData.horizontalSpan = 6;
		buttonPane.setLayoutData(buttonPaneLayoutData);
		buttonPane.setLayout(new GridLayout(2, true));
		//buttonPane.s
		//buttonPane.set
		closeButton = new Button(buttonPane, SWT.NORMAL);
		closeButton.setText(Messages.PatientInfoPart_close);
		gdata = new GridData(GridData.FILL_HORIZONTAL /*| GridData.FILL_VERTICAL*/);
		gdata.heightHint = 50;
		closeButton.setLayoutData(gdata);
		saveButton = new Button(buttonPane, SWT.None);
		saveButton.setText(Messages.PatientInfoPart_save);
		gdata = new GridData(GridData.FILL_HORIZONTAL /*| GridData.FILL_VERTICAL*/);
		gdata.heightHint = 50;
		saveButton.setLayoutData(gdata);
		buttonPane.setTabList(new Control[] {saveButton});
		
		closeButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		saveButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (queueItem == null) {
					queueItem = newTemplate;
					populate();
				} else {
					save();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		populate();
		selService.addSelectionListener(new ISelectionListener() {
			
			@Override
			public void selectionChanged(MPart part, Object selection) {
				//System.out.println("sel changed notified by service"); //$NON-NLS-1$
				QueueTicket selQueueItem = null;
				//waitingNumberExisted = false;
				if (selection instanceof Patient) {
					//selQueueItem = DBManager.loadDiagQueueNoException(((Patient) selection).id.getValue());
					Patient p = (Patient) selection;
					if (p.id.getValue() != null) {
						selQueueItem = WaitingList.getQueueItemAny(p.id.getValue());
						if (selQueueItem == null) {
							//DiagRecord dr = DiagRecord.newInstance(p);
							selQueueItem = new QueueTicket(null, p);
							//dr.ticket = selQueueItem;
							//selQueueItem.previousVisit.loadValue(p.lastVisit.getValue());
							//selQueueItem.previousAppointment.loadValue(p.previousAppointment.getValue());
							selQueueItem.propagateData(/*p*/);
						}
					}
				} else if (selection instanceof QueueTicket) {
					selQueueItem = ((QueueTicket) selection);
				}
				//if (selQueueItem != null && !selQueueItem.equals(queueItem)) {
				if (selQueueItem != null && selQueueItem != queueItem) {
					//System.out.println("current: " + queueItem + ", new selItem: " + selQueueItem); //$NON-NLS-1$ //$NON-NLS-2$
					//save current editing
					//push();
					//System.out.println("modified: " + queueItem.isModified());
					//show selection
					queueItem = selQueueItem;
					populate();
					System.out.println("sel done"); //$NON-NLS-1$
				}
			}
		});
	}
	
	/*private void push() {
		if (queueItem == null) {
			return;
		}
		System.out.println(new Date() + " Pushing " + nameInput.getText() + " " + queueItem);
		queueItem.diag.patient.name.changeValue(nameInput.getText());
		queueItem.diag.patient.dob.changeValue(dobInput.getText());
		queueItem.diag.patient.address.changeValue(addressInput.getText());
		queueItem.diag.patient.phone.changeValue(phoneInput.getText());
		queueItem.diag.weight.changeValue(weightInput.getText());
		queueItem.diag.height.changeValue(heightInput.getText());
		queueItem.diag.patient.healthNote.changeValue(healthNoteInput.getText());
		queueItem.diag.symptons.changeValue(symtonsInput.getText());
		
		//queueItem.diag.patient.weight.changeValue(weightInput.getText());
		//queueItem.diag.patient.height.changeValue(heightInput.getText());
	}*/
	
	private void setEnable(boolean b) {
		queueNumberInput.setEnabled(b);
		nameInput.setEnabled(b);
		dobInput.setEnabled(b);
		age.setEnabled(b);
		addressInput.setEnabled(b);
		phoneInput.setEnabled(b);
		weightInput.setEnabled(b);
		heightInput.setEnabled(b);
		age.setEnabled(b);
		healthNoteInput.setEnabled(b);
		symtonsInput.setEnabled(b);
	}
	
	private void populate() {
		
		setEnable(queueItem != null);
		if (queueItem == null) {
			queueNumberInput.setMessage(String.valueOf(WaitingList.getNextNumber()));
			queueNumberInput.setText(""); //$NON-NLS-1$
			
			idLabel.setText(""); //$NON-NLS-1$
			nameInput.setText(""); //$NON-NLS-1$
			dobInput.setText(""); //$NON-NLS-1$
			age.setText(""); //$NON-NLS-1$
			addressInput.setText(""); //$NON-NLS-1$
			phoneInput.setText(""); //$NON-NLS-1$
			weightInput.setText(""); //$NON-NLS-1$
			heightInput.setText(""); //$NON-NLS-1$
			bodyTemp.setText(""); //$NON-NLS-1$
			healthNoteInput.setText(""); //$NON-NLS-1$
			symtonsInput.setText(""); //$NON-NLS-1$
			
			lastVisitInput.setText(""); //$NON-NLS-1$
			previousAppointment.setText(""); //$NON-NLS-1$
			alert.setText(""); //$NON-NLS-1$

			saveButton.setText(Messages.PatientInfoPart_new_patient);
			saveButton.setEnabled(true);
			closeButton.setEnabled(false);
			dirty.setDirty(false);
			return;
		}

		dirty.setDirty(queueItem.isModified());
		//System.out.println(new Date() + " Populating " + queueItem.patient.name.getValue() + " " + queueItem); //$NON-NLS-1$ //$NON-NLS-2$
		if (queueItem.queueNumber.getValue() != null) {
			alert.setText(Messages.PatientInfoPart_patient_queued_already);
		} else {
			alert.setText(""); //$NON-NLS-1$
		}
		
		queueNumberInput.setMessage(String.valueOf(WaitingList.getNextNumber()));
		queueNumberInput.setText(queueItem.queueNumber.getValueAsEditing());
		
		idLabel.setText(Messages.PatientInfoPart_id + ": " + queueItem.patient.id.getValueAsEditing());
		nameInput.setText(queueItem.patient.name.getValueAsEditing());
		dobInput.setText(queueItem.patient.dob.getValueAsEditing());
		age.setText(queueItem.patient.todayDiag.age.getValueAsEditing());
		addressInput.setText(queueItem.patient.address.getValueAsEditing());
		phoneInput.setText(queueItem.patient.phone.getValueAsEditing());
		weightInput.setText(queueItem.patient.todayDiag.weight.getValueAsEditing());
		heightInput.setText(queueItem.patient.todayDiag.height.getValueAsEditing());
		bodyTemp.setText(queueItem.patient.todayDiag.bodyTemp.getValueAsEditing());
		healthNoteInput.setText(queueItem.patient.healthNote.getValueAsEditing());
		symtonsInput.setText(queueItem.patient.todayDiag.symptons.getValueAsEditing());
		
		lastVisitInput.setText(queueItem.patient.lastVisit.getEasyLabel());
		previousAppointment.setText(queueItem.patient.previousAppointment.getEasyLabel());
		//alert.setText("");
		//waitingNumber = null;
			
		//boolean buttonsEnabled = queueItem.diag.patient.isInDB() && !waitingNumberExisted;
		//boolean buttonsEnabled = queueItem.diag.patient.isInDB() && queueItem.queueNumber.getValue() == null;
		
		boolean saveEnabled;
		boolean closeEnabled;
		if (queueItem.patient.isInDB()) {
			closeEnabled = true;
			saveEnabled = queueItem.queueNumber.getValue() == null || queueItem.isModified();
		} else {
			//new patient
			boolean modified = queueItem.isModified();
			closeEnabled = modified;
			saveEnabled = modified;
		}
		
		if (queueItem.patient.id.getValue() == null) {
			saveButton.setText(Messages.PatientInfoPart_save_new_patient);
		} else {
			saveButton.setText(Messages.PatientInfoPart_queue_up);
		}
		saveButton.setEnabled(saveEnabled);
		closeButton.setEnabled(closeEnabled);
		
		//population occurs on selection changing. Can't take focus in this case.
		//setFocus();
	}

	@Focus
	public void setFocus() {
		//tableViewer.getTable().setFocus();
		//queueNumberInput.setFocus();
		//nameInput.setFocus();
	}
	
	private void close() {
		if (queueItem != null && queueItem.isModified()) {
			MessageBox dialog = new MessageBox(nameInput.getShell(), 
					SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
					dialog.setText(Messages.PatientInfoPart_confirm_not_save);
					dialog.setMessage(Messages.PatientInfoPart_confirm_not_save_message);

			// open dialog and await user selection
			int returnCode = dialog.open();
			if (returnCode != SWT.OK) {
				return;
			}
		}
		
		//proceed to close
		queueItem.cancelChanges();
		if (queueItem != newTemplate) {
			if (newTemplate.isModified()) {
				queueItem = null;
			} else {
				queueItem = newTemplate;
			}
		}
		populate();
		//selService.setSelection(new Object());
	}
	
	static boolean validateTicket(Shell parent, QueueTicket ticket) {
		String err = ticket.validate();
		if (err != null) {
			MessageBox dialog = new MessageBox(parent, 
					SWT.ICON_ERROR | SWT.OK);
					dialog.setText(Messages.PatientInfoPart_invalid_input_data);
					dialog.setMessage(err);
			dialog.open();
			return false;
		}
		return true;
	}

	@Persist
	public void save() {
		if (queueItem == null) {
			return;
		}
		
		if (healthNoteAuto != null) {
			healthNoteAuto.proposalProvider.learn(healthNoteInput.getText());
		}
		//symtomsAutotext.proposalProvider.learn(symtonsInput.getText());
		
		//push();
		Integer manualQueueNumber = queueItem.queueNumber.getValue();
		if (queueItem.queueNumber.isModified() && manualQueueNumber != null && !WaitingList.diagQueue.isNumberAvailable(manualQueueNumber)) {
			MessageBox dialog = new MessageBox(queueNumberInput.getShell(), 
					SWT.ICON_ERROR | SWT.OK);
					dialog.setText(Messages.PatientInfoPart_duplicated_ticket);
					dialog.setMessage(Messages.PatientInfoPart_ticket_number + manualQueueNumber + 
							Messages.PatientInfoPart_already_given_to_other_patient);

			dialog.open();
			return;
		}
		
		if (queueItem.queueNumber.getBackupValue() != null && queueItem.queueNumber.isModified()) {
			String newNumber;
			if (queueItem.queueNumber.getValue() == null) {
				newNumber = String.valueOf(WaitingList.getNextNumber());
			} else {
				newNumber = queueItem.queueNumber.getValueAsEditing();
			}
			MessageBox dialog = new MessageBox(queueNumberInput.getShell(), 
					SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
					dialog.setText(Messages.PatientInfoPart_confirm_ticket_change);
					dialog.setMessage(Messages.PatientInfoPart_change_ticket_from + queueItem.queueNumber.getBackupValueAsEditing() + 
							Messages.PatientInfoPart_to_spaces + newNumber + "?"); //$NON-NLS-2$

			// open dialog and await user selection
			int returnCode = dialog.open();
			if (returnCode != SWT.OK) {
				return;
			}
		}
		
		//System.out.println("saving");
		//patient.save(nameInput.getText(), dobInput.getText(), addressInput.getText(), phoneInput.getText());
		queueItem.patient.todayDiag.age.changeValue(age.getText());
		if (!validateTicket(queueNumberInput.getShell(), queueItem)) {
			return;
		}
		
		/*DBManager.save(queueItem.diag.patient);
		if (waitingNumber == null || waitingNumberChanged) {
			WaitingList.enqueue(queueNumberInput.getText(), queueItem.diag.patient, waitingNumberChanged);
		}*/
		WaitingList.diagQueue.enqueue(queueItem);
		if (queueItem == newTemplate || !newTemplate.isModified()) {
			//DiagRecord dr = DiagRecord.newInstance(new Patient());
			newTemplate = new QueueTicket(null, new Patient());
			newTemplate.patient.createTodayDiag();
			//dr.ticket = newTemplate;
			queueItem = newTemplate;
		} else {
			queueItem = null;
		}
		populate();
		//dirty.setDirty(false);
		//setFocus();
	}
}

class DateModifyListener implements ModifyListener {
	
	private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
	
	@Override
	public void modifyText(ModifyEvent e) {
		Text text = (Text) e.getSource();
		try {
			df.parse(text.getText());
			text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		} catch (ParseException e1) {
			text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
		}
	}
}

class IntVerifyListener implements VerifyListener {
	
	@Override
	public void verifyText(VerifyEvent e) {
		Text text = (Text) e.getSource();

        // get old text and create new text by using the VerifyEvent.text
        final String oldS = text.getText();
        String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
        if ("".equals(newS)) { //$NON-NLS-1$
        	return;
        }
		try {
			Integer.parseInt(newS);
		} catch (NumberFormatException ne) {
			e.doit = false;
		}
	}
}

class FloatVerifyListener implements VerifyListener {
	
	@Override
	public void verifyText(VerifyEvent e) {
		Text text = (Text) e.getSource();

        // get old text and create new text by using the VerifyEvent.text
        final String oldS = text.getText();
        String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
        if ("".equals(newS)) { //$NON-NLS-1$
        	return;
        }
		try {
			Float.parseFloat(newS);
		} catch (NumberFormatException ne) {
			e.doit = false;
		}
	}
}