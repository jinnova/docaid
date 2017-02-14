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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import com.jinnova.docaid.DBManager;
import com.jinnova.docaid.DiagRecord;
import com.jinnova.docaid.FieldBoolean;
import com.jinnova.docaid.Medicine;
import com.jinnova.docaid.Patient;
import com.jinnova.docaid.PatientFamilyMember;
import com.jinnova.docaid.PrescriptionAmount;
import com.jinnova.docaid.PrescriptionItem;
import com.jinnova.docaid.QueueStage;
import com.jinnova.docaid.QueueTicket;
import com.jinnova.docaid.Service;
import com.jinnova.docaid.SettingName;
import com.jinnova.docaid.WaitingList;

public class DiagnosePart {

	@Inject
	private ESelectionService selService;
	
	@Inject
	private MDirtyable dirty;
	
	DiagRecord diag;
	//private QueueTicket queuedPatient;
	//private PrescriptionItem presItem;

	private Text nameInput;
	//private TableViewer tableViewer;

	private Text addressInput;
	//private Text phoneInput;
	private Text dobInput;
	//private Text lastVisitInput;
	private Button closeButton;
	private Button saveButton;

	private Text queueNumberInput;

	private Text weightInput;

	private Text heightInput;

	private Text patientNoteInput;

	private Text symptomInput;

	private Text diagBriefInput;

	private Text treatmentInput;

	Text treatmentDays;

	private Text appointment;
	
	private Button noRevisitIfGood;
	
	private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$

	//private Composite medMetaComp;

	Composite medComp;

	private Text medName;

	//Text medUnit;
	//String medUnit;

	//Text medPackageUnit;
	//String medPackageUnit;

	//Text medPackageSize;
	//float medPackageSize;

	//Button medPackageBreakable;
	//boolean medPackageBreakable;

	private Text medCountPerDay;

	private Text medAmountPerTaking;

	Text medDays;
	
	Text amountTotalUnit;
	
	Text amountTotalPackage;

	Label medAmountPerTakingLabel;

	Label amountTotalUnitLabel;

	Label amountTotalPackageLabel;

	private Text medNote;

	private Button medNoteSTC;

	private Button medNoteSC;

	private Button medNoteS;

	private Button medNoteT;

	private Button medNoteC;

	private Button medNoteToi;

	private Button medNoteBeforeLaunch;

	private Button medNoteAfterLaunch;

	private Button medNoteHungry;

	private Button medNoteFull;

	private Button medDelete;

	private Button medSave;

	private Text ageLabel;

	private Label diagDateLabel;

	//private Label medPackageUnitLabel;

	//private Label medPackageSizeLabel;

	//Label medUnitReadonly;

	//private Label medUnitLabel;

	//private Label medTotalLabel;

	//private Text medTotal;

	private Label pidLabel;

	private Text bodyTemp;

	private FieldAutoComplete patientNoteAuto;

	private Button holdButton;

	private LinkedList<Button> serviceChecks;

	private LinkedList<Text> servicePrices;
	
	private abstract class DiagFieldModifyListener implements ModifyListener {
		
		abstract void pushValue();
		
		@Override
		public void modifyText(ModifyEvent e) {
			if (diag == null) {
				return;
			}
			pushValue();
			boolean changed = diag.patient.isModified();
			/*if (queueItem.diag.patient.isInDB()) {
				closeButton.setEnabled(true);
			} else {
				closeButton.setEnabled(changed);
			}*/
			closeButton.setEnabled(true);
			//saveButton.setEnabled(changed);
			dirty.setDirty(changed);
		}
	};
	
	private abstract class MedFieldModifyListener implements ModifyListener {
		
		abstract void pushValue();
		
		@Override
		public void modifyText(ModifyEvent e) {
			if (diag == null) {
				return;
			}
			pushValue();
			//boolean changed = diag.isModified();
			//setEnabledMedButtons();
			//dirty.setDirty(changed);
			
			/*setEnabledMedButtons();
			boolean b = diag.isModified();
			dirty.setDirty(b);
			closeButton.setEnabled(true);
			saveButton.setEnabled(b);*/
			populateButtonEnablements();
		}
	};
	
	private abstract class MedBooleanFieldSelectionListener implements SelectionListener {
		
		abstract FieldBoolean getField();

		@Override
		public void widgetSelected(SelectionEvent e) {
			getField().changeValue(((Button) e.widget).getSelection());
			//setEnabledMedButtons();
			//dirty.setDirty(diag.isModified());
			populateButtonEnablements();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {	
		}
	}
	
	/*private ModifyListener dirtyMarker = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			dirty.setDirty(true);
			closeButton.setEnabled(true);
			saveButton.setEnabled(true);
		}
	};*/

	@PostConstruct
	public void createComposite(Composite parent) {
		
		parent.setLayout(new GridLayout(6, false));
		//queueNumberInput = createField(parent, true,1, "STT", String.valueOf(WaitingList.getNextNumber()));
		new Label(parent, SWT.None);
		new Label(parent, SWT.None);
		new Label(parent, SWT.None);
		new Label(parent, SWT.None);
		new Label(parent, SWT.None);
		//Composite comp = new Composite(parent, SWT.None);
		//comp.setLayout(new GridLayout(2, false));
		
		//Label label = new Label(parent, SWT.None);
		//label.setText(Messages.DiagnosePart_id);
		pidLabel = new Label(parent, SWT.None);
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		//gdata.horizontalSpan = 4;
		//gdata.grabExcessHorizontalSpace = true;
		pidLabel.setLayoutData(gdata);
		
		//label = new Label(parent, SWT.None);
		//label.setText(Messages.DiagnosePart_queueNumber);
		nameInput = createField(parent, true, 4, false, Messages.DiagnosePart_name, Messages.DiagnosePart_patient_fullname);
		nameInput.addModifyListener(new DiagFieldModifyListener() {
			@Override
			void pushValue() {
				diag.patient.name.changeValue(nameInput.getText());
			}
		});
		queueNumberInput = new Text(parent, SWT.BORDER | SWT.CENTER);
		queueNumberInput.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		//queueNumberInput.setMessage(String.valueOf(WaitingList.getNextNumber()));
		queueNumberInput.setEnabled(false);
		FontData fontData = queueNumberInput.getFont().getFontData()[0];
		fontData.setHeight(30);
		queueNumberInput.setFont(new Font(parent.getDisplay(), fontData));
		//queueNumberInput.addModifyListener(dirtyMarker);
		//queueNumberInput.addVerifyListener(new IntVerifyListener());

		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.verticalSpan = 2;
		//gdata.widthHint = 90;
		//gdata.grabExcessVerticalSpace = false;
		//gdata.grabExcessHorizontalSpace = false;
		//gdata.verticalAlignment = SWT.CENTER;
		//gdata.horizontalAlignment = SWT.CENTER;
		queueNumberInput.setLayoutData(gdata);
		
		//dobInput = createField(parent, true, 1, false, "Ngày sinh", "Ngày tháng năm sinh (dd/MM/yyyy)");
		boolean dobInputSet = SettingName.isSet(SettingName.diag_form_inputs_dob.name(), true);
		Label l = new Label(parent, SWT.None);
		if (dobInputSet) {
			l.setText(Messages.DiagnosePart_dob);
		} else {
			l.setText("Age");
		}
		if (dobInputSet) {
			dobInput = new Text(parent, SWT.BORDER);
			dobInput.setMessage("dd/MM/yyyy"); //$NON-NLS-1$
			gdata = new GridData(GridData.FILL_HORIZONTAL);
			gdata.widthHint = 90;
			dobInput.setLayoutData(gdata);
			dobInput.addTraverseListener(nextFieldOnEnterKey);
			dobInput.addModifyListener(new DateModifyListener());
			dobInput.addModifyListener(new DiagFieldModifyListener() {
				@Override
				void pushValue() {
					diag.patient.dob.changeValue(dobInput.getText());
					//diag.age.changeValue(diag.patient.dob.getAgeReading());
					ageLabel.setText(diag.patient.dob.getAgeReading());
				}
			});
		}
		ageLabel = new Text(parent, SWT.BORDER);
		ageLabel.setText("nn tuoi"); //$NON-NLS-1$
		//ageLabel.setEnabled(false);
		gdata = new GridData();
		gdata.widthHint = 70;
		if (dobInput == null) {
			gdata.horizontalSpan = 2;
		}
		ageLabel.setLayoutData(gdata);
		ageLabel.addTraverseListener(nextFieldOnEnterKey);
		/*ageLabel.addModifyListener(new DiagFieldModifyListener() {
			@Override
			void pushValue() {
				diag.age.changeValue(ageLabel.getText());
			}
		});*/
		
		diagDateLabel = new Label(parent, SWT.None);
		diagDateLabel.setText(Messages.DiagnosePart_diagDate_dd_mm_yyyy);
		diagDateLabel.setEnabled(false);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 2;
		diagDateLabel.setLayoutData(gdata);
		
		if (SettingName.isSet(SettingName.diag_form_inputs_address.name() + "_enabled", true)) {
			addressInput = createField(parent, true, 5, false, Messages.DiagnosePart_address, Messages.DiagnosePart_patient_address);
			addressInput.addModifyListener(new DiagFieldModifyListener() {
				@Override
				void pushValue() {
					diag.patient.address.changeValue(addressInput.getText());
				}
			});
		}
		
		int rowFieldCount = 0;
		//weightInput = createField(parent, true, 1, false, "Cân nặng", "(kg)");
		Label label = new Label(parent, SWT.None);
		label.setText(Messages.DiagnosePart_weight);
		weightInput = new Text(parent, SWT.BORDER);
		weightInput.setMessage("(kg)"); //$NON-NLS-1$
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		weightInput.setLayoutData(gdata);
		weightInput.addTraverseListener(nextFieldOnEnterKey);
		//weightInput.addVerifyListener(new FloatVerifyListener());
		weightInput.addModifyListener(new DiagFieldModifyListener() {
			@Override
			void pushValue() {
				diag.weight.changeValue(weightInput.getText());
			}
		});
		rowFieldCount++;
		//Composite comp = new Composite(parent, SWT.None);
		//comp.setLayout(new GridLayout(2, false));
		//gdata = new GridData();
		//gdata.horizontalSpan = 3;
		//comp.setLayoutData(gdata);
		//heightInput = createField(comp, true, 1, false, "Chiều cao", "(cm)");
		if (SettingName.isSet(SettingName.diag_form_inputs_height.name() + "_enabled", true)) {
			label = new Label(parent, SWT.None);
			label.setText(Messages.DiagnosePart_height);
			heightInput = new Text(parent, SWT.BORDER);
			heightInput.setMessage("(cm)"); //$NON-NLS-1$
			heightInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			heightInput.addTraverseListener(nextFieldOnEnterKey);
			heightInput.addVerifyListener(new IntVerifyListener());
			heightInput.addModifyListener(new DiagFieldModifyListener() {
				@Override
				void pushValue() {
					diag.height.changeValue(heightInput.getText());
				}
			});
			rowFieldCount++;
		}
		
		if (SettingName.isSet(SettingName.diag_form_inputs_bodyTemp.name(), true)) {
			label = new Label(parent, SWT.None);
			label.setText(Messages.DiagnosePart_bodyTemp);
			bodyTemp = new Text(parent, SWT.BORDER);
			//bodyTemp.setMessage("(cm)");
			bodyTemp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			bodyTemp.addTraverseListener(nextFieldOnEnterKey);
			bodyTemp.addVerifyListener(new FloatVerifyListener());
			bodyTemp.addModifyListener(new DiagFieldModifyListener() {
				@Override
				void pushValue() {
					diag.bodyTemp.changeValue(bodyTemp.getText());
				}
			});
			rowFieldCount++;
		}
		
		for (int i = rowFieldCount; i < 3; i++) {
			new Label(parent, SWT.None);
			new Label(parent, SWT.None);
		}
		
		if (SettingName.isSet(SettingName.diag_form_inputs_healthNote.name(), true)) {
			patientNoteInput = createField(parent, false, 5, true, Messages.DiagnosePart_histHealthNote, ""); //$NON-NLS-1$
			patientNoteInput.addModifyListener(new DiagFieldModifyListener() {
				@Override
				void pushValue() {
					diag.patient.healthNote.changeValue(patientNoteInput.getText());
				}
			});
			if (SettingName.isSet(SettingName.autotext_field_healthnote_enabled.name(), true)) {
				patientNoteAuto = new FieldAutoComplete(patientNoteInput, FieldAutoComplete.HEALTHNOTE);
			}
		}
		symptomInput = createField(parent, false, 5, true, Messages.DiagnosePart_symptoms, ""); //$NON-NLS-1$
		symptomInput.addModifyListener(new DiagFieldModifyListener() {
			@Override
			void pushValue() {
				diag.symptons.changeValue(symptomInput.getText());
			}
		});
		diagBriefInput = createField(parent, false, 5, true, Messages.DiagnosePart_diagBrief, ""); //$NON-NLS-1$
		diagBriefInput.addModifyListener(new DiagFieldModifyListener() {
			@Override
			void pushValue() {
				diag.diagnosis.changeValue(diagBriefInput.getText());
			}
		});
		treatmentInput = createField(parent, false, 5, true, Messages.DiagnosePart_treatments, ""); //$NON-NLS-1$
		treatmentInput.addModifyListener(new DiagFieldModifyListener() {
			@Override
			void pushValue() {
				diag.treatment.changeValue(treatmentInput.getText());
			}
		});
		
		if (SettingName.isSet(SettingName.diag_form_inputs_treatmentdays.name() + "_enabled", true)) {
			treatmentDays = createField(parent, true, 1, false, Messages.DiagnosePart_treatmentTimeLength, Messages.DiagnosePart_days_in_brackets);
			treatmentDays.addVerifyListener(new IntVerifyListener());
			treatmentDays.addTraverseListener(nextFieldOnEnterKey);
			treatmentDays.addModifyListener(new DiagFieldModifyListener() {
				@Override
				void pushValue() {
					diag.treatmentDays.changeValue(treatmentDays.getText());
					appointment.setText(treatmentDays.getText());
				}
			});
		}
		
		//appointment = createField(parent, true, 1, false, "Hẹn tái khám", "(ngày)");
		//comp = new Composite(parent, SWT.None);
		//comp.setLayout(new GridLayout(3, false));
		//gdata = new GridData(GridData.FILL_HORIZONTAL);
		//comp.setLayoutData(gdata);
		label = new Label(parent, SWT.None);
		label.setText(Messages.DiagnosePart_revisitDays);
		appointment = new Text(parent, SWT.BORDER);
		appointment.setMessage(Messages.DiagnosePart_days_in_brackets);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		appointment.setLayoutData(gdata);
		appointment.addTraverseListener(nextFieldOnEnterKey);
		appointment.addVerifyListener(new IntVerifyListener());
		appointment.addTraverseListener(nextFieldOnEnterKey);
		appointment.addModifyListener(new DiagFieldModifyListener() {
			@Override
			void pushValue() {
				diag.revisitDays.changeValue(appointment.getText());
			}
		});
		
		//new Label(parent, SWT.None);
		//new Label(parent, SWT.None);
		//new Label(parent, SWT.None);
		/*noRevisitIfGood = new Button(parent, SWT.CHECK);
		gdata = new GridData();
		gdata.horizontalSpan = 2;
		noRevisitIfGood.setLayoutData(gdata);
		noRevisitIfGood.setText(Messages.DiagnosePart_norevisite_if_recovered);
		noRevisitIfGood.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (diag == null) {
					return;
				}
				diag.noRevisitIfGood.changeValue(noRevisitIfGood.getSelection());
				boolean changed = diag.patient.isModified();
				closeButton.setEnabled(true);
				saveButton.setEnabled(changed);
				dirty.setDirty(changed);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});*/
		
		createCompositeTabs(parent);
		createCompositeMainButtons(parent);
		
		//populate();
		selService.addSelectionListener(new ISelectionListener() {
			
			@Override
			public void selectionChanged(MPart part, Object selection) {
				DiagnosePart.this.selectionChanged(selection);
			}
		});
		populate();
	}
	
	private void createCompositeMainButtons(Composite parent) {
		//new Label(parent, SWT.None); //empty label for place holder
		Composite buttonPane = new Composite(parent, SWT.None);
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gdata.horizontalSpan = 6;
		gdata.verticalIndent = 0;
		//gdata.heightHint = 20;
		buttonPane.setLayoutData(gdata);
		buttonPane.setLayout(new GridLayout(2, true));
		//buttonPane.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		//buttonPane.s
		//buttonPane.set
		closeButton = new Button(buttonPane, SWT.NORMAL);
		//closeButton.setText("Đóng");
		gdata = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		//gdata.heightHint = 25;
		gdata.verticalIndent = 0;
		closeButton.setLayoutData(gdata);
		saveButton = new Button(buttonPane, SWT.None);
		saveButton.setText("xxx"); //$NON-NLS-1$
		gdata = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		//gdata.heightHint = 25;
		gdata.verticalIndent = 0;
		saveButton.setLayoutData(gdata);
		//buttonPane.setTabList(new Control[] {saveButton});
		
		closeButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				NamedAction action = getAction2();
				if (action != null) {
					action.run();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		saveButton.addSelectionListener(new SelectionListener() {
			
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
	
	private void createCompositeTabs(Composite parent) {
		
		/*CTabFolder folder = new CTabFolder(parent, SWT.NONE);
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 6;
		gdata.verticalIndent = 0;
		//gdata.heightHint = medComp.
		folder.setLayoutData(gdata);
		folder.setLayout(new GridLayout(1, true));
		
		CTabItem medTab = new CTabItem(folder, SWT.BORDER);
		medTab.setText(Messages.DiagnosePart_medicine);
		Composite medComp = createCompositeMed(folder, parent);
		//((GridData) medComp.getLayoutData()).exclude = true;
		//medComp.setVisible(false);
		medTab.setControl(medComp);

		CTabItem serviceTab = new CTabItem(folder, SWT.BORDER);
		serviceTab.setText(Messages.DiagnosePart_tests_services);
		Composite serviceComp = createCompositeService(folder);
		serviceTab.setControl(serviceComp);
		
		int defTab = SettingName.getInt(SettingName.diag_form_defaulttab.name(), 1);
		if (defTab == 2) {
			folder.setSelection(serviceTab);
		} else {
			folder.setSelection(medTab);
		}*/
		
		Composite comp = new Composite(parent, SWT.None);
		comp.setLayout(new GridLayout(1, true));
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 6;
		gdata.verticalIndent = 0;
		//gdata.heightHint = medComp.
		comp.setLayoutData(gdata);
		createCompositeService(comp);
	}
	
	private Composite createCompositeService(Composite parent) {
		Composite serviceComp = new Composite(parent, SWT.NONE);
		serviceComp.setLayout(new GridLayout(4, true));
		serviceComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		/*ComboViewer combo = new ComboViewer(serviceComp, SWT.BORDER | SWT.READ_ONLY);
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		//gdata.heightHint = 50;
		combo.getCombo().setLayoutData(gdata);
		//Rectangle clientArea = serviceComp.getClientArea();
		//combo.getCombo().setBounds(clientArea.x, clientArea.y, 200, 200);
		combo.setContentProvider(new ArrayContentProvider());
		combo.setInput(new String[] {"Alpha", "Bravo", "Charlie"});
		
		ListViewer serviceList = new ListViewer(serviceComp, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		serviceList.setContentProvider(ArrayContentProvider.getInstance());
		serviceList.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));*/
		
		serviceChecks = new LinkedList<Button>();
		servicePrices = new LinkedList<Text>();
		for (Service serv : Service.allServices.values()) {
			//boolean priceEnabled = SettingName.isSet(SettingName.services_id.name() + "_" + serv.id + "_price_enabled", true);
			Button servCheck = new Button(serviceComp, SWT.CHECK);
			servCheck.setText(serv.name);
			servCheck.setData(serv);
			serviceChecks.add(servCheck);
			GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
			if (!serv.priceAdjustable) {
				gdata.horizontalSpan = 2;
			}
			servCheck.setLayoutData(gdata);
			
			Text servPrice = null;
			if (serv.priceAdjustable) {
				servPrice = new Text(serviceComp, SWT.BORDER);
				servPrice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				servPrice.setData(serv);
				servicePrices.add(servPrice);
			}
			
			Text servPriceFinal = servPrice;
			servCheck.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (servCheck.getSelection()) {
						Integer price = serv.price;
						try {
							if (servPriceFinal != null) {
								String s = servPriceFinal.getText();
								if (!s.trim().isEmpty()) {
									price = Integer.parseInt(s);
								}
							}
						} catch (NumberFormatException ne) {
							//diag.servicePrices.put(serv, serv.price);
							//price = null;
							throw new RuntimeException(ne);
						}
						diag.servicePrices.put(serv, price);
					} else {
						diag.servicePrices.remove(serv);
					}
					if (servPriceFinal != null) {
						servPriceFinal.setEnabled(servCheck.getSelection());
					}

					/*boolean holdRequired = SettingName.isSet(
							SettingName.services_id + "_" + serv.id + "_holdrequired", true);
					if (servCheck.getSelection() && holdRequired) {
						holdButton.setSelection(true);
						setTicketHold();
					}*/
					determineHoldRequirement();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					
				}
			});
			if (servPriceFinal != null) {
				servPrice.addModifyListener(new ModifyListener() {
					
					@Override
					public void modifyText(ModifyEvent e) {
						if (!servCheck.getSelection()) {
							return;
						}
						try {
							String s = servPriceFinal.getText();
							if (!s.trim().isEmpty()) {
								diag.servicePrices.put(serv, Integer.parseInt(s));
							}
						} catch (NumberFormatException ne) {
							//diag.servicePrices.put(serv, serv.price);
						}
					}
				});
				servPrice.addVerifyListener(new IntVerifyListener());
			}
		}
		
		/*Label l = new Label(serviceComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gdata = new GridData();
		gdata.horizontalSpan = 2;
		l.setLayoutData(gdata);*/
		holdButton = new Button(serviceComp, SWT.CHECK);
		holdButton.setVisible(false); //TODO
		holdButton.setText(Messages.DiagnosePart_waiting_test_results);
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 4;
		holdButton.setLayoutData(gdata);
		holdButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//diag.hold.changeValue(holdButton.getSelection());
				setTicketHold();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		return serviceComp;
	}
	
	private void determineHoldRequirement() {
		
		if (diag == null) {
			return;
		}
		boolean holdRequired = false;
		for (Service serv : this.diag.servicePrices.keySet()) {
			boolean b = SettingName.isSet(
					SettingName.services_id + "_" + serv.id + "_holdrequired", true);
			if (b) {
				holdRequired = true;
				break;
			}
		}
		holdButton.setSelection(holdRequired);
		setTicketHold();
	}
	
	private void setTicketHold() {
		if (holdButton.getSelection()) {
			diag.patient.ticket.stage = QueueStage.beginHold;
		} else {
			diag.patient.ticket.stage = QueueStage.begin;
		}
		saveButton.setText(getActionName());
		//saveButton.setEnabled(holdButton.getSelection() || diag.patient.isModified());
		saveButton.setEnabled(true);
	}
	
	private Composite createCompositeMed(Composite parent, Composite grandParent) {

		medComp = new Composite(parent, SWT.None);
		medComp.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		//medComp.setText("Thuốc");
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL /*| GridData.FILL_VERTICAL*/);
		//gdata.horizontalSpan = 6;
		medComp.setLayoutData(gdata);
		medComp.setLayout(new GridLayout(8, false));
		Label l = new Label(medComp, SWT.None);
		l.setText(Messages.DiagnosePart_medicineName);
		medName = new Text(medComp, SWT.BORDER);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 5;
		medName.setLayoutData(gdata);
		medName.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.medName.changeValue(medName.getText());
			}
		});
		
		new MedAutoCompleteField(medName, this);
		
		/*medUnitReadonly = new Label(medComp, SWT.BORDER);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		//gdata.widthHint = 50;
		gdata.horizontalSpan = 3;
		medUnitReadonly.setLayoutData(gdata);
		medUnitReadonly.setText("                 ");
		
		Button bt = new Button(medComp, SWT.CHECK); 
		bt.setText("TT bao bì");
		gdata = new GridData();
		gdata.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		bt.setLayoutData(gdata);
		bt.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//((GridData) medMetaComp.getLayoutData()).exclude = !((GridData) medMetaComp.getLayoutData()).exclude;
				//medMetaComp.setVisible(!medMetaComp.isVisible());
				boolean visible = bt.getSelection();
				//medPackageSizeComp.setVisible(visible);
				//medPackageBreakableComp.setVisible(visible);
				medPackageBreakable.setVisible(visible);
				medPackageSize.setVisible(visible);
				medPackageSizeLabel.setVisible(visible);
				medPackageUnit.setVisible(visible);
				medPackageUnitLabel.setVisible(visible);
				medUnit.setVisible(visible);
				medUnitLabel.setVisible(visible);
				medTotal.setVisible(visible);
				medTotalLabel.setVisible(visible);
				//medUnitTotal.setVisible(visible);

				//((GridData) medPackageSizeComp.getLayoutData()).exclude = !visible;
				//((GridData) medPackageBreakableComp.getLayoutData()).exclude = !visible;
				((GridData) medPackageBreakable.getLayoutData()).exclude = !visible;
				((GridData) medPackageSize.getLayoutData()).exclude = !visible;
				((GridData) medPackageSizeLabel.getLayoutData()).exclude = !visible;
				((GridData) medPackageUnit.getLayoutData()).exclude = !visible;
				((GridData) medPackageUnitLabel.getLayoutData()).exclude = !visible;
				((GridData) medUnit.getLayoutData()).exclude = !visible;
				((GridData) medUnitLabel.getLayoutData()).exclude = !visible;

				//((GridData) medUnitTotal.getLayoutData()).exclude = !visible;
				((GridData) medTotal.getLayoutData()).exclude = !visible;
				((GridData) medTotalLabel.getLayoutData()).exclude = !visible;
				
				if (visible) {
					((GridData) medDays.getLayoutData()).horizontalSpan = 1;
				} else {
					((GridData) medDays.getLayoutData()).horizontalSpan = 3;
				}
				grandParent.layout();
				//parent.pack();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		medUnitLabel = new Label(medComp, SWT.None);
		medUnitLabel.setText("Đơn vị");
		gdata = new GridData();
		gdata.exclude = true;
		medUnitLabel.setLayoutData(gdata);
		
		medUnit = new Text(medComp, SWT.BORDER);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		//gdata.widthHint = 50;
		gdata.exclude = true;
		medUnit.setLayoutData(gdata);
		medUnit.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.medUnit.changeValue(medUnit.getText());
				medUnitReadonly.setText(medUnit.getText());
			}
		});
		
		medPackageUnitLabel = new Label(medComp, SWT.None);
		gdata = new GridData();
		gdata.exclude = true;
		medPackageUnitLabel.setLayoutData(gdata);
		medPackageUnitLabel.setText("Bao bì");
		
		medPackageUnit = new Text(medComp, SWT.BORDER);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.exclude = true;
		medPackageUnit.setLayoutData(gdata);
		medPackageUnit.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.medPackageUnit.changeValue(medPackageUnit.getText());
			}
		});
		
		medPackageSizeLabel = new Label(medComp, SWT.None);
		medPackageSizeLabel.setText("ĐV/BB");
		gdata = new GridData();
		gdata.exclude = true;
		medPackageSizeLabel.setLayoutData(gdata);
		
		medPackageSize = new Text(medComp, SWT.BORDER);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.exclude = true;
		medPackageSize.setLayoutData(gdata);
		medPackageSize.addVerifyListener(new FloatVerifyListener());
		medPackageSize.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.medPackageSize.changeValue(medPackageSize.getText());
			}
		});
		medPackageBreakable = new Button(medComp, SWT.CHECK);
		medPackageBreakable.setText("BB chia nhỏ được");
		gdata = new GridData();
		gdata.horizontalSpan = 2;
		gdata.exclude = true;
		medPackageBreakable.setLayoutData(gdata);
		medPackageBreakable.setVisible(false);
		//medPackageBreakable.setText("BB tach được");
		medPackageBreakable.addSelectionListener(new MedBooleanFieldSelectionListener() {
			
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.medPackageBreakable;
			}
		});*/
		l = new Label(medComp, SWT.None);
		l.setText(Messages.DiagnosePart_medDays);
		medDays = new Text(medComp, SWT.BORDER);
		medDays.setMessage(Messages.DiagnosePart_days_in_brackets);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		//gdata.horizontalSpan = 3;
		medDays.setLayoutData(gdata);
		medDays.addVerifyListener(new IntVerifyListener());
		medDays.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.dayCount.changeValue(medDays.getText());
				calculateAndPopulateAmount();
			}
		});
		
		medAmountPerTakingLabel = new Label(medComp, SWT.None);
		medAmountPerTakingLabel.setText(Messages.DiagnosePart_amountPerTaking);
		gdata = new GridData();
		int unitLabelWidth = 50;
		gdata.widthHint = 60 + unitLabelWidth;
		medAmountPerTakingLabel.setLayoutData(gdata);
		medAmountPerTaking = new Text(medComp, SWT.BORDER);
		medAmountPerTaking.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		medAmountPerTaking.addVerifyListener(new FloatVerifyListener());
		medAmountPerTaking.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.amountPerTaking.changeValue(medAmountPerTaking.getText());
				calculateAndPopulateAmount();
			}
		});

		l = new Label(medComp, SWT.None);
		l.setText(Messages.DiagnosePart_takingCountPerDay);
		medCountPerDay = new Text(medComp, SWT.BORDER);
		//medCountPerDay.setMessage("(lần)");
		medCountPerDay.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		medCountPerDay.addVerifyListener(new IntVerifyListener());
		medCountPerDay.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.takingCountPerDay.changeValue(medCountPerDay.getText());
				calculateAndPopulateAmount();
			}
		});
		
		amountTotalUnitLabel = new Label(medComp, SWT.None);
		amountTotalUnitLabel.setText(Messages.DiagnosePart_medAmount);
		gdata = new GridData();
		gdata.widthHint = 30 + unitLabelWidth;
		amountTotalUnitLabel.setLayoutData(gdata);
		amountTotalUnit = new Text(medComp, SWT.BORDER);
		amountTotalUnit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		amountTotalUnit.addVerifyListener(new FloatVerifyListener());
		/*amountTotalUnit.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.amountTotalUnit.changeValue(amountTotalUnit.getText());
			}
		});*/
		
		amountTotalPackageLabel = new Label(medComp, SWT.None);
		amountTotalPackageLabel.setText(Messages.DiagnosePart_medAmount);
		gdata = new GridData();
		gdata.widthHint = 30 + unitLabelWidth;
		amountTotalPackageLabel.setLayoutData(gdata);
		amountTotalPackageLabel.setVisible(false);
		amountTotalPackage = new Text(medComp, SWT.BORDER);
		//gdata = new GridData();
		//gdata.exclude = true;
		//amountTotalPackage.setLayoutData(gdata);
		amountTotalPackage.setVisible(false);
		amountTotalPackage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		amountTotalPackage.addVerifyListener(new FloatVerifyListener());
		/*amountTotalPackage.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.amountTotalPackage.changeValue(amountTotalPackage.getText());
			}
		});*/
		
		//total
		/*medTotalLabel = new Label(medComp, SWT.None);
		medTotalLabel.setVisible(false);
		medTotalLabel.setText("Tổng lượng");
		gdata = new GridData();
		gdata.exclude = true;
		medTotalLabel.setLayoutData(gdata);
		
		medTotal = new Text(medComp, SWT.BORDER);
		medTotal.setVisible(false);
		medTotal.setMessage("Số lượng (đv)");
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.exclude = true;
		medTotal.setLayoutData(gdata);
		//medTotal.addVerifyListener(new FloatVerifyListener());
		medTotal.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.amountTotalUnit.changeValue(medTotal.getText());
			}
		});*/
		
		l = new Label(medComp, SWT.None);
		l.setText(Messages.DiagnosePart_medNote);
		medNote = new Text(medComp, SWT.BORDER);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 3;
		//gdata.heightHint = 20;
		medNote.setLayoutData(gdata);
		medNote.addModifyListener(new MedFieldModifyListener() {
			@Override
			void pushValue() {
				diag.prescription.editingItem.note.changeValue(medNote.getText());
			}
		});
		
		Composite medButtonComp = new Composite(medComp, SWT.None);
		medButtonComp.setLayout(new GridLayout(2, true));
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 4;
		gdata.verticalIndent = 0;
		medButtonComp.setLayoutData(gdata);
		
		medDelete = new Button(medButtonComp, SWT.None);
		medDelete.setText(Messages.DiagnosePart_delete);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.heightHint = 25;
		gdata.verticalIndent = 0;
		medDelete.setLayoutData(gdata);
		medDelete.setEnabled(false);
		medDelete.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				diag.prescription.removeEditingItem();
				populateMed();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		medSave = new Button(medButtonComp, SWT.None);
		medSave.setText(Messages.DiagnosePart_save);
		medSave.setEnabled(false);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.heightHint = 25;
		gdata.verticalIndent = 0;
		medSave.setLayoutData(gdata);
		medSave.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				diag.prescription.editingItem.amountTotalPackage = parseInt(amountTotalPackage.getText());
				diag.prescription.editingItem.amountTotalUnit = parseInt(amountTotalUnit.getText());
				diag.prescription.addEditingItem();
				//diag.prescription.editingItem = diag.prescription.editingItem;
				populateMed();
			}
			
			private Integer parseInt(String s) {
				if (s == null || "".equals(s.trim())) { //$NON-NLS-1$
					return null;
				}
				return Integer.parseInt(s);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		//place holder
		new Label(medComp, SWT.None);
		
		Composite notes = new Composite(medComp, SWT.None);
		//notes.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		notes.setLayout(new GridLayout(6, true));
		gdata = new GridData(GridData.FILL_BOTH);
		gdata.horizontalSpan = 7;
		gdata.verticalIndent = 0;
		notes.setLayoutData(gdata);
		//new Label(medComp, SWT.None);
		medNoteSTC = new Button(notes, SWT.CHECK);
		medNoteSTC.setText(Messages.DiagnosePart_morning_noon_dinnertime);
		medNoteSTC.addSelectionListener(new MedBooleanFieldSelectionListener() {
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.noteSTC;
			}
		});
		medNoteSC = new Button(notes, SWT.CHECK);
		medNoteSC.setText(Messages.DiagnosePart_morning_dinnertime);
		medNoteSC.addSelectionListener(new MedBooleanFieldSelectionListener() {
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.noteSC;
			}
		});
		medNoteS = new Button(notes, SWT.CHECK);
		medNoteS.setText(Messages.DiagnosePart_morning);
		medNoteS.addSelectionListener(new MedBooleanFieldSelectionListener() {
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.noteS;
			}
		});
		medNoteT = new Button(notes, SWT.CHECK);
		medNoteT.setText(Messages.DiagnosePart_noon);
		medNoteT.addSelectionListener(new MedBooleanFieldSelectionListener() {
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.noteT;
			}
		});
		medNoteC = new Button(notes, SWT.CHECK);
		medNoteC.setText(Messages.DiagnosePart_dinnertime);
		medNoteC.addSelectionListener(new MedBooleanFieldSelectionListener() {
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.noteC;
			}
		});
		medNoteToi = new Button(notes, SWT.CHECK);
		medNoteToi.setText(Messages.DiagnosePart_bedtime);
		medNoteToi.addSelectionListener(new MedBooleanFieldSelectionListener() {
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.noteToi;
			}
		});

		medNoteBeforeLaunch = new Button(notes, SWT.CHECK);
		medNoteBeforeLaunch.setText(Messages.DiagnosePart_before_meals);
		medNoteBeforeLaunch.addSelectionListener(new MedBooleanFieldSelectionListener() {
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.noteBeforeLaunch;
			}
		});
		medNoteAfterLaunch = new Button(notes, SWT.CHECK);
		medNoteAfterLaunch.setText(Messages.DiagnosePart_after_meals);
		medNoteAfterLaunch.addSelectionListener(new MedBooleanFieldSelectionListener() {
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.noteAfterLaunch;
			}
		});
		medNoteHungry = new Button(notes, SWT.CHECK);
		medNoteHungry.setText(Messages.DiagnosePart_hungry);
		medNoteHungry.addSelectionListener(new MedBooleanFieldSelectionListener() {
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.noteHungry;
			}
		});
		medNoteFull = new Button(notes, SWT.CHECK);
		medNoteFull.setText(Messages.DiagnosePart_full);
		medNoteFull.addSelectionListener(new MedBooleanFieldSelectionListener() {
			@Override
			FieldBoolean getField() {
				return diag.prescription.editingItem.noteFull;
			}
		});
		return medComp;
	}
	
	private TraverseListener nextFieldOnEnterKey = new TraverseListener() {
		
		@Override
		public void keyTraversed(TraverseEvent e) {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				((Text) e.widget).traverse(SWT.TRAVERSE_TAB_NEXT);
			}
		}
	};
	
	private Text createField(Composite parent, boolean traverseHandling, 
			int hspan, boolean multilines, String labelText, String msg) {
		
		Label label = new Label(parent, SWT.None);
		label.setText(labelText);

		int style;
		if (multilines) {
			style = SWT.None; //SWT.MULTI | /*SWT.H_SCROLL |*/ SWT.V_SCROLL | SWT.SCROLL_LINE;
		} else {
			style = SWT.None;
		}
		Text text = new Text(parent, SWT.BORDER | style);
		text.setMessage(msg);
		//text.addModifyListener(dirtyMarker);
		//if (traverseHandling) {
			text.addTraverseListener(nextFieldOnEnterKey);
		//}
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = hspan;
		if (multilines) {
			gdata.heightHint = 30;
		}
		text.setLayoutData(gdata);
		return text;
	}

	/*@Focus
	public void setFocus() {
		//tableViewer.getTable().setFocus();
		//queueNumberInput.setFocus();
		nameInput.setFocus();
	}*/
	
	private void selectionChanged(Object selection) {

		//System.out.println("Diag part got selection: " + selection);
		DiagRecord newDiag = null;
		//QueueTicket newQueuePatient = null;
		//PrescriptionItem newPresItem = null;
		if (selection instanceof QueueTicket) {
			newDiag = ((QueueTicket) selection).patient.todayDiag;
			//newQueuePatient = (QueueTicket) selection;
		} else if (selection instanceof DiagRecord) {
			newDiag = (DiagRecord) selection;
			/*if (newDiag != null) {
				newQueuePatient = newDiag.ticket;
			}*/
		} else if (selection instanceof Patient && 
				(diag == null || selection != diag.patient)) {
			QueueTicket q = WaitingList.getQueueItemAny(
					((Patient) selection).id.getValue());
			if (q != null) {
				newDiag = ((QueueTicket) q).patient.todayDiag;
				//newQueuePatient = q;
			} else {
				Patient p = (Patient) selection;
				//QueueTicket newQueuePatient = p.getOrCreateDirectTicket();
				QueueTicket newQueuePatient = new QueueTicket(null, p);
				newQueuePatient.propagateData();
				newDiag = newQueuePatient.patient.todayDiag;
			}
		} else if (selection instanceof PatientFamilyMember) {
			PatientFamilyMember m = (PatientFamilyMember) selection;
			if (m.patient != null && (diag == null || !diag.patient.equals(m.patient))) {
				QueueTicket q = WaitingList.getQueueItemAny(m.patient.id.getValue());
				if (q != null) {
					newDiag = ((QueueTicket) q).patient.todayDiag;
					//newQueuePatient = q;
				} else {
					//QueueTicket newQueuePatient = m.patient.getOrCreateDirectTicket();
					QueueTicket newQueuePatient = new QueueTicket(null, m.patient);
					newDiag = newQueuePatient.patient.todayDiag;
				}
			}
		}
		
		if (newDiag != null && newDiag != diag) {
			//push();
			diag = newDiag;
			//queuedPatient = newQueuePatient;
			populate();
			//newPresItem = newDiag.prescription.editingItem;
		}
		
		if (selection instanceof PrescriptionItem) {
			//newPresItem = (PrescriptionItem) selection;
			populateMed();
		}
		/*if (newPresItem != null) {
			presItem = newPresItem;
			populateMed();
		}*/
	}
	
	private void setFieldEnabled(boolean enabled) {
		nameInput.setEnabled(enabled);
		if (dobInput != null) {
			dobInput.setEnabled(enabled);
		}
		ageLabel.setEnabled(enabled);
		if (addressInput != null) {
			addressInput.setEnabled(enabled);
		}
		weightInput.setEnabled(enabled);
		if (heightInput != null) {
			heightInput.setEnabled(enabled);
		}
		if (bodyTemp != null) {
			bodyTemp.setEnabled(enabled);
		}
		if (patientNoteInput != null) {
			patientNoteInput.setEnabled(enabled);
		}
		symptomInput.setEnabled(enabled);
		diagBriefInput.setEnabled(enabled);
		treatmentInput.setEnabled(enabled);
		if (treatmentDays != null) {
			treatmentDays.setEnabled(enabled);
		}
		appointment.setEnabled(enabled);
		if (noRevisitIfGood != null) {
			noRevisitIfGood.setEnabled(enabled);
		}
		holdButton.setEnabled(enabled);
		for (Button b : serviceChecks) {
			b.setEnabled(enabled);
		}
		for (Text t : servicePrices) {
			//System.out.println("price enabled: " + enabled);
			//Thread.dumpStack();
			t.setEnabled(enabled);
		}
	}
	
	private void setFieldEnabledMed(boolean enabled) {
		if (true) return;
		medAmountPerTaking.setEnabled(enabled);
		medCountPerDay.setEnabled(enabled);
		medDays.setEnabled(enabled);
		amountTotalPackage.setEnabled(enabled);
		amountTotalUnit.setEnabled(enabled);
		medDelete.setEnabled(enabled);
		medName.setEnabled(enabled);
		medNote.setEnabled(enabled);
		medNoteAfterLaunch.setEnabled(enabled);
		medNoteBeforeLaunch.setEnabled(enabled);
		medNoteC.setEnabled(enabled);
		medNoteFull.setEnabled(enabled);
		medNoteHungry.setEnabled(enabled);
		medNoteS.setEnabled(enabled);
		medNoteSC.setEnabled(enabled);
		medNoteSTC.setEnabled(enabled);
		medNoteT.setEnabled(enabled);
		medNoteToi.setEnabled(enabled);
		/*medPackageBreakable.setEnabled(enabled);
		medPackageSize.setEnabled(enabled);
		medPackageUnit.setEnabled(enabled);
		medUnit.setEnabled(enabled);
		medTotal.setEnabled(enabled);*/
	}

	private void populateButtonEnablements2() {
		NamedAction action2 = getAction2();
		closeButton.setEnabled(action2 != null);
		if (action2 != null) {
			closeButton.setText(action2.name);
		}
	}
	private void populateButtonEnablements() {
		populateButtonEnablements2();
		NamedAction action = getAction();
		if (action == actionNext) {
			dirty.setDirty(false);
			setFieldEnabled(false);
			setFieldEnabledMed(false);
			saveButton.setEnabled(true);
			//closeButton.setEnabled(false);
			saveButton.setText(action.name);
			return;
		}
		
		boolean modified = diag.patient.isModified();
		dirty.setDirty(modified);
		if (action == actionComplete || action == actionSave) {
			setFieldEnabled(true);
			setFieldEnabledMed(true);
			//saveButton.setEnabled(holdButton.getSelection() || modified);
			saveButton.setEnabled(true);
			saveButton.setText(action.name);
			//closeButton.setEnabled(true);
			
			modified = diag.prescription.editingItem.isModified();
			if (medSave != null) {
				medSave.setEnabled(modified && diag.prescription.editingItem.validate() == null);
				medDelete.setEnabled(modified || diag.prescription.isEditingItemAdded());
			}
			//medSave.setEnabled(true);
			//medDelete.setEnabled(true);
		} else if (action == actionReopen) {
			setFieldEnabled(false);
			setFieldEnabledMed(false);
			saveButton.setEnabled(true);
			saveButton.setText(actionReopen.name);
			//closeButton.setEnabled(true);
			if (medSave != null) {
				medSave.setEnabled(false);
				medDelete.setEnabled(false);
			}
		} else {
			setFieldEnabled(false);
			setFieldEnabledMed(false);
			saveButton.setEnabled(false);
			saveButton.setText(""); //$NON-NLS-1$
			//closeButton.setEnabled(true);
			if (medSave != null) {
				medSave.setEnabled(false);
				medDelete.setEnabled(false);
			}
		}
	}
	
	private void populate() {

		if (diag == null) {
			//setEnabledMed(false);
			//setFieldEnabled(false);
			//setFieldEnabledMed(false);
			populateButtonEnablements();
			queueNumberInput.setText(""); //$NON-NLS-1$
			pidLabel.setText(""); //$NON-NLS-1$
			nameInput.setText(""); //$NON-NLS-1$
			if (dobInput != null) {
				dobInput.setText(""); //$NON-NLS-1$
			}
			ageLabel.setText(""); //$NON-NLS-1$
			diagDateLabel.setText(""); //$NON-NLS-1$
			if (addressInput != null) {
				addressInput.setText(""); //$NON-NLS-1$
			}
			weightInput.setText(""); //$NON-NLS-1$
			if (heightInput != null) {
				heightInput.setText(""); //$NON-NLS-1$
			}
			if (bodyTemp != null) {
				bodyTemp.setText(""); //$NON-NLS-1$
			}
			if (patientNoteInput != null) {
				patientNoteInput.setText(""); //$NON-NLS-1$
			}
			symptomInput.setText(""); //$NON-NLS-1$
			diagBriefInput.setText(""); //$NON-NLS-1$
			treatmentInput.setText(""); //$NON-NLS-1$
			if (treatmentDays != null) {
				treatmentDays.setText(""); //$NON-NLS-1$
			}
			appointment.setText(""); //$NON-NLS-1$
			if (noRevisitIfGood != null) {
				noRevisitIfGood.setSelection(true);
			}
			//holdButton.setSelection(false);
			dirty.setDirty(false);
			//setEnabled(false);
			//saveButton.setEnabled(true);
			//closeButton.setEnabled(false);
			//saveButton.setText("Bệnh nhân kế tiếp");
			populateMed();
			
			//must be at the end of any populating, as populating will trigger populateButtonEnablements()
			for (Button b : serviceChecks) {
				b.setSelection(false);
			}
			for (Text t : servicePrices) {
				Service serv = (Service) t.getData();
				t.setText(serv.price == 0 ? "" : String.valueOf(serv.price));
			}
			return;
		}

		//setFieldEnabled(true);
		//setFieldEnabledMed(true);
		populateButtonEnablements();
		if (diag.patient.ticket == null) {
			queueNumberInput.setText(""); //$NON-NLS-1$
		} else {
			queueNumberInput.setText(diag.patient.ticket.queueNumber.getValueAsEditing());
		}
		pidLabel.setText(Messages.DiagnosePart_id + ": " + diag.patient.id.getValueAsEditing());
		nameInput.setText(diag.patient.name.getValueAsEditing());
		if (dobInput != null) {
			dobInput.setText(diag.patient.dob.getValueAsEditing());
		}
		//ageLabel.setText(diag.patient.dob.getAgeReading());
		ageLabel.setText(diag.age.getValueAsEditing());
		diagDateLabel.setText(Messages.DiagnosePart_diagDate_colon + (diag.isInDayNotEnd() ? Messages.DiagnosePart_new_in_brackets : diag.date.getEasyLabel()));
		if (addressInput != null) {
			addressInput.setText(diag.patient.address.getValueAsEditing());
		}
		weightInput.setText(diag.weight.getValueAsEditing());
		if (heightInput != null) {
			heightInput.setText(diag.height.getValueAsEditing());
		}
		if (bodyTemp != null) {
			bodyTemp.setText(diag.bodyTemp.getValueAsEditing());
		}
		if (patientNoteInput != null) {
			patientNoteInput.setText(diag.patient.healthNote.getValueAsEditing());
		}
		symptomInput.setText(diag.symptons.getValueAsEditing());
		diagBriefInput.setText(diag.diagnosis.getValueAsEditing());
		treatmentInput.setText(diag.treatment.getValueAsEditing());
		if (treatmentDays != null) {
			treatmentDays.setText(diag.treatmentDays.getValueAsEditing());
		}
		appointment.setText(diag.revisitDays.getValueAsEditing());
		if (noRevisitIfGood != null) {
			noRevisitIfGood.setSelection(diag.noRevisitIfGood.getValueAsEditing());
		}
		holdButton.setSelection(diag.patient.ticket != null && diag.patient.ticket.stage == QueueStage.beginHold);
		populateMed();

		//must be at the end of any populating, as populating will trigger populateButtonEnablements()
		for (Button b : serviceChecks) {
			Service serv = (Service) b.getData();
			//Integer price = diag.servicePrices.get(serv);
			//b.setSelection(price != null);
			b.setSelection(diag.servicePrices.containsKey(serv));
		}
		for (Text t : servicePrices) {
			Service serv = (Service) t.getData();
			Integer price = diag.servicePrices.get(serv);
			if (price != null) {
				if (price != 0) {
					t.setText(String.valueOf(price));
				} else {
					t.setText(""); //$NON-NLS-1$
				}
				t.setEnabled(diag.patient.ticket != null && 
						(diag.patient.ticket.stage == QueueStage.begin || 
						diag.patient.ticket.stage == QueueStage.beginHold));
				//System.out.println("price: true" + price);
			} else {
				t.setText(serv.price == 0 ? "" : String.valueOf(serv.price));
				t.setEnabled(false);
				//System.out.println("price: false");
			}
		}

		//presItem = null;
		//dirty.setDirty(diag.isModified());
		
		//population occurs on selection changing. Can't take focus in this case.
		//setFocus();

		/*if (diag.ticket != null) {
			if (diag.ticket.stage == QueueStage.begin) {
				setEnabled(true);
				saveButton.setEnabled(diag.isModified());
				saveButton.setText("Lưu");
				
				closeButton.setEnabled(true);
			} else {
				setEnabled(false);
				saveButton.setEnabled(true);
				saveButton.setText("Mở lại");
				
				closeButton.setEnabled(true);
			}
		} else {
			setEnabled(false);
			saveButton.setEnabled(false);
			saveButton.setText("");
			
			closeButton.setEnabled(true);
		}*/
	}
	
	/*private void setEnabledMedButtons() {
		if (diag == null || !diag.isInStageBegin()) {
			medSave.setEnabled(false);
			medDelete.setEnabled(false);
			return;
		}
		boolean modified = diag.prescription.editingItem.isModified();
		medSave.setEnabled(modified && diag.prescription.editingItem.validate() == null);
		medDelete.setEnabled(modified || diag.prescription.isEditingItemAdded());
	}*/
	
	private void clearMedFields() {
		if (true) return;
		medName.setText(""); //$NON-NLS-1$
		/*medTotal.setText("");
		medUnit.setText("");
		medPackageUnit.setText("");
		medPackageSize.setText("");
		medPackageBreakable.setSelection(false);*/

		medCountPerDay.setText(""); //$NON-NLS-1$
		medAmountPerTaking.setText(""); //$NON-NLS-1$
		medDays.setText(""); //$NON-NLS-1$
		amountTotalPackage.setText(""); //$NON-NLS-1$
		amountTotalUnit.setText(""); //$NON-NLS-1$
		
		medNote.setText(""); //$NON-NLS-1$
		medNoteAfterLaunch.setSelection(false);
		medNoteBeforeLaunch.setSelection(false);
		medNoteC.setSelection(false);
		medNoteFull.setSelection(false);
		medNoteHungry.setSelection(false);
		medNoteS.setSelection(false);
		medNoteSC.setSelection(false);
		medNoteSTC.setSelection(false);
		medNoteT.setSelection(false);
		medNoteToi.setSelection(false);
	}
	
	private void calculateAndPopulateAmount() {
		PrescriptionAmount amount = diag.prescription.editingItem.computeTotalAmount();
		System.out.println("amount: " + amount.packageAmount + " " + amount.unitAmount); //$NON-NLS-1$ //$NON-NLS-2$
		amountTotalPackage.setText(amount.getPackageAmount());
		amountTotalUnit.setText(amount.getUnitAmount());
	}
	
	private void populateMed() {
		
		if (true) return;
		//boolean enabled = this.diag != null && this.diag.isInStageBegin();
		//setFieldEnabledMed(enabled);
		//medSave.setEnabled(enabled);
		//medDelete.setEnabled(enabled);
		
		PrescriptionItem presItem = null;
		if (this.diag != null) {
			presItem = diag.prescription.editingItem;
		}
		if (presItem == null) {
			clearMedFields();
			return;
		}
		
		medName.setText(presItem.medName.getValueAsEditing());
		/*medTotal.setText(presItem.amountTotalUnit.getValueAsEditing());
		medUnit.setText(presItem.medUnit.getValueAsEditing());
		medPackageUnit.setText(presItem.medPackageUnit.getValueAsEditing());
		medPackageSize.setText(presItem.medPackageSize.getValueAsEditing());
		medPackageBreakable.setSelection(presItem.medPackageBreakable.getValueAsEditing());*/

		medCountPerDay.setText(presItem.takingCountPerDay.getValueAsEditing());
		medAmountPerTaking.setText(presItem.amountPerTaking.getValueAsEditing());
		medDays.setText(presItem.dayCount.getValueAsEditing());
		
		//must be after medCountPerDay, medAmountPerTaking, medDays
		amountTotalPackage.setText(String.valueOf(presItem.amountTotalPackage));
		amountTotalUnit.setText(String.valueOf(presItem.amountTotalUnit));
		
		medNote.setText(presItem.note.getValueAsEditing());
		medNoteAfterLaunch.setSelection(presItem.noteAfterLaunch.getValueAsEditing());
		medNoteBeforeLaunch.setSelection(presItem.noteBeforeLaunch.getValueAsEditing());
		medNoteC.setSelection(presItem.noteC.getValueAsEditing());
		medNoteFull.setSelection(presItem.noteFull.getValueAsEditing());
		medNoteHungry.setSelection(presItem.noteHungry.getValueAsEditing());
		medNoteS.setSelection(presItem.noteS.getValueAsEditing());
		medNoteSC.setSelection(presItem.noteSC.getValueAsEditing());
		medNoteSTC.setSelection(presItem.noteSTC.getValueAsEditing());
		medNoteT.setSelection(presItem.noteT.getValueAsEditing());
		medNoteToi.setSelection(presItem.noteToi.getValueAsEditing());
		//setEnabledMedButtons();
		populateMedEnablements();
	}
	
	private static String maskNull(String s) {
		if (s == null) {
			return ""; //$NON-NLS-1$
		}
		return s;
	}
	
	void populateMedEnablements() {
		if (true) return;
		PrescriptionItem pi = diag.prescription.editingItem;
		medAmountPerTakingLabel.setText(Messages.DiagnosePart_per_taking + " (" + maskNull(pi.medUnit) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		amountTotalPackageLabel.setText(Messages.DiagnosePart_amount + " (" + maskNull(pi.medPackage) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		amountTotalUnitLabel.setText(Messages.DiagnosePart_amount + " (" + maskNull(pi.medUnit) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		
		String medDaysNumber = null;
		if (treatmentDays != null) {
			medDaysNumber = treatmentDays.getText();
		} else if (appointment != null) {
			medDaysNumber = appointment.getText();
		}
		if (medDaysNumber != null) {
			medDays.setText(medDaysNumber);
		}
		boolean twoUnit = Medicine.isTwoUnit(pi.medUnit, pi.medPackage);
		if (twoUnit) {
			if (Medicine.isBreakable(pi.medPackageBreakable)) {
				amountTotalUnit.setVisible(true);
				amountTotalUnitLabel.setVisible(true);
				amountTotalPackage.setVisible(true);
				amountTotalPackageLabel.setVisible(true);
			} else {
				amountTotalUnit.setVisible(false);
				amountTotalUnitLabel.setVisible(false);
				amountTotalPackage.setVisible(true);
				amountTotalPackageLabel.setVisible(true);
			}
		} else {
			amountTotalUnit.setVisible(true);
			amountTotalUnitLabel.setVisible(true);
			amountTotalPackage.setVisible(false);
			amountTotalPackageLabel.setVisible(false);
		}
	}
	
	/*private void push() {
		
		if (diag == null) {
			dirty.setDirty(false);
			saveButton.setEnabled(false);
			closeButton.setEnabled(false);
			return;
		}
		
		diag.patient.name.changeValue(nameInput.getText());
		diag.patient.dob.changeValue(dobInput.getText());
		diag.patient.address.changeValue(addressInput.getText());
		diag.patient.healthNote.changeValue(patientNoteInput.getText());
		diag.symptons.changeValue(symptomInput.getText());
		diag.treatment.changeValue(treatmentInput.getText());
		diag.revisitDays.changeValue(appointment.getText());
		diag.noRevisitIfGood.changeValue(noRevisitIfGood.getSelection());
	}*/
	
	private NamedAction action2Close = new NamedAction(Messages.DiagnosePart_action_close) {
		
		@Override
		void run() {

			if (diag == null) {
				return;
			}
			if (diag.patient.isModified()) {
				MessageBox dialog = new MessageBox(nameInput.getShell(), 
						SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
						dialog.setText(Messages.DiagnosePart_confirm_notsave);
						dialog.setMessage(Messages.DiagnosePart_confirm_notsave_message);

				// open dialog and await user selection
				int returnCode = dialog.open();
				if (returnCode != SWT.OK) {
					return;
				}
				diag.cancelChanges();
			}

			//queuedPatient = null;
			diag = null;
			populate();
			//setFocus();
			//selService.setSelection(Patient.patientSelectionNull);
			Patient.triggerPatientSelectionEmptyEvent();
		}
	};
	
	private NamedAction action2New = new NamedAction(Messages.DiagnosePart_action_new_patient) {
		
		@Override
		void run() {
			//Patient p = new Patient();
			//diag = DiagRecord.newInstance(p);
			//QueueTicket ticket = p.getOrCreateDirectTicket();
			QueueTicket ticket = new QueueTicket(null, new Patient());
			ticket.patient.createTodayDiag();
			ticket.propagateData(/*p*/);
			diag = ticket.patient.todayDiag;
			populate();
			selService.setSelection(ticket.patient);
		}
	};
	
	private NamedAction getAction2() {
		if (diag == null) {
			return action2New;
		}
		return action2Close;
	}
	
	/*private void close() {
		if (diag == null) {
			return;
		}
		if (diag.isModified()) {
			MessageBox dialog = new MessageBox(nameInput.getShell(), 
					SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
					dialog.setText("Xác nhận không lưu");
					dialog.setMessage("Không lưu thông tin trên màn hình?");

			// open dialog and await user selection
			int returnCode = dialog.open();
			if (returnCode != SWT.OK) {
				return;
			}
			diag.cancelChanges();
		}

		//queuedPatient = null;
		diag = null;
		populate();
		//setFocus();
		//selService.setSelection(Patient.patientSelectionNull);
		Patient.triggerPatientSelectionEmptyEvent();
	}*/
	
	private NamedAction actionReopen = new NamedAction(Messages.DiagnosePart_action_reopen) {
		
		@Override
		void run() {
			try {
				int c = DBManager.updateWaitingStage(diag.patient.id.getValue(), QueueStage.begin.name());
				if (c > 0) {
					diag.patient.ticket.stage = QueueStage.begin;
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			//diag = null;
			populate();
			Patient.triggerPatientSelectionEmptyEvent();
		}
	};
	
	private NamedAction actionNext = new NamedAction(Messages.DiagnosePart_action_next_patient) {
		
		@Override
		void run() {
			//QueueTicket q = WaitingList.diagQueue.peek();
			QueueTicket queueTicket = WaitingList.diagQueue.peek();
			if (queueTicket != null) {
				diag = queueTicket.patient.todayDiag;
				//selService.setSelection(queuedPatient);
			} else {
				diag = null;
			}
			populate();
			//selService.setSelection(diag);
			if (queueTicket != null) {
				selService.setSelection(queueTicket);
			}
		}
	};
	
	private NamedAction actionComplete = new NamedAction(Messages.DiagnosePart_action_to_med) {
		
		@Override
		void run() {
			if (patientNoteAuto != null) {
				patientNoteAuto.proposalProvider.learn(patientNoteInput.getText());
			}
			//for direct diagnosis
			/*Integer queueNumber = diag.patient.ticket.queueNumber.getValue();
			if (queueNumber != null && queueNumber == -1) {
				if (diag.patient.id.getValue() == null) {
					try {
						int newId = DBManager.insertPatient(diag.patient);
						diag.patient.id.loadValue(newId);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
				QueueTicket x = WaitingList.getQueueItemAny(diag.patient.id.getValue());
				if (x != null) {
					diag.patient.ticket.queueNumber.loadValue(x.queueNumber.getValue());
				} else {
					diag.patient.ticket.queueNumber.loadValue(null);
					WaitingList.diagQueue.enqueue(diag.patient.ticket);
				}
			}*/
			//direct diagnosis
			if (!PatientInfoPart.validateTicket(saveButton.getShell(), diag.patient.ticket)) {
				return;
			}
			enqueueTicketIfNeccessary();
			
			diag.age.loadValue(ageLabel.getText());
			diag.date.loadValue(new Timestamp(System.currentTimeMillis()));
			String err = diag.validate();
			if (err != null) {
				MessageBox dialog = new MessageBox(queueNumberInput.getShell(), 
						SWT.ICON_ERROR | SWT.OK);
						dialog.setText(Messages.DiagnosePart_err_invalid_inputs);
						dialog.setMessage(err);
				dialog.open();
				return;
			}
			
			//propagate data from diag to patient
			diag.patient.propagateData(diag);
			diag.patient.lastVisit.changeValue(diag.date.getValueAsEditing());
			
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(new java.util.Date(diag.date.getValue().getTime()));
			Integer days = diag.revisitDays.getValue();
			if (days == null) {
				days = 0;
			}
			cal.add(Calendar.DATE, days);
			diag.patient.previousAppointment.changeValue(df.format(cal.getTime()));
			
			try {
				//update or insert patient
				if (diag.patient.id.getValue() != null) {
					DBManager.updatePatient(diag.patient);
				} else {
					int newId = DBManager.insertPatient(diag.patient);
					diag.patient.id.loadValue(newId);
				}
				diag.patient.applyChanges();

				//diag
				if (diag.id.getValue() == null || diag.id.getValue() == -1) {
					int diagId = DBManager.insertDiag(diag);
					diag.id.loadValue(diagId);
				} else {
					DBManager.updateDiag(diag);
				}
				diag.applyChanges();
				
				//queue stage
				if (SettingName.isSet(SettingName.diag_skipMedStage.name())) {
					diag.patient.ticket.stage = QueueStage.end;
				} else {
					diag.patient.ticket.stage = QueueStage.med;
				}
				
				//update patient & diag in waiting queue
				if (DBManager.updateWaitingList(diag.patient.ticket) == 0) {
					//direcct diagnosis (unqueued patient)
					DBManager.insertWaitingList(diag.patient.ticket);
				}
				diag.patient.ticket.applyChanges();
				
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			diag.applyChanges();
			
			//dequeue
			WaitingList.diagQueue.dequeue(diag.patient.id.getValue());
			//diag.applyChanges();
			diag = null;
			//queuedPatient = null;
			populate();
			Patient.triggerPatientSelectionEmptyEvent();
		}
	};
	
	private void enqueueTicketIfNeccessary() {

		QueueTicket queuedTicket = null;
		if (diag.patient.id.getValue() != null) {
			queuedTicket = WaitingList.getQueueItemAny(diag.patient.id.getValue());
		}
		if (queuedTicket != null) {
			if (queuedTicket != diag.patient.ticket) {
				diag.patient.ticket.queueNumber.loadValue(queuedTicket.queueNumber.getValue());
			}
		} else {
			WaitingList.diagQueue.enqueue(diag.patient.ticket);
		}
	}
	
	private NamedAction actionSave= new NamedAction(Messages.DiagnosePart_action_save) {
		
		@Override
		void run() {
			if (patientNoteAuto != null) {
				patientNoteAuto.proposalProvider.learn(patientNoteInput.getText());
			}
			//for direct diagnosis
			/*Integer queueNumber = diag.patient.ticket.queueNumber.getValue();
			if (queueNumber != null && queueNumber == -1) {
				QueueTicket ticket = null;
				if (diag.patient.id.getValue() != null) {
					ticket = WaitingList.getQueueItemAny(diag.patient.id.getValue());
				}
				if (ticket != null) {
					diag.patient.ticket.queueNumber.loadValue(ticket.queueNumber.getValue());
				} else {
					diag.patient.ticket.queueNumber.loadValue(null);
					WaitingList.diagQueue.enqueue(diag.patient.ticket);
				}
			}*/
			
			//direct diagnosis
			if (!PatientInfoPart.validateTicket(saveButton.getShell(), diag.patient.ticket)) {
				return;
			}
			enqueueTicketIfNeccessary();
			
			diag.age.loadValue(ageLabel.getText());
			/*diag.date.loadValue(new Timestamp(System.currentTimeMillis()));
			String err = diag.validate();
			if (err != null) {
				MessageBox dialog = new MessageBox(queueNumberInput.getShell(), 
						SWT.ICON_ERROR | SWT.OK);
						dialog.setText("Giá trị nhập không hợp lệ");
						dialog.setMessage(err);
				dialog.open();
				return;
			}*/
			
			//propagate data from diag to patient
			/*diag.patient.propagateData(diag);
			diag.patient.lastVisit.changeValue(diag.date.getValueAsEditing());
			
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(new java.util.Date(diag.date.getValue().getTime()));
			Integer days = diag.revisitDays.getValue();
			if (days == null) {
				days = 0;
			}
			cal.add(Calendar.DATE, days);
			diag.patient.previousAppointment.changeValue(df.format(cal.getTime()));*/
			
			try {
				//update or insert patient
				/*if (diag.patient.id.getValue() != null) {
					DBManager.updatePatient(diag.patient);
				} else {
					int newId = DBManager.insertPatient(diag.patient);
					diag.patient.id.loadValue(newId);
				}
				diag.patient.applyChanges();

				//diag
				if (diag.id.getValue() == null || diag.id.getValue() == -1) {
					int diagId = DBManager.insertDiag(diag);
					diag.id.loadValue(diagId);
				} else {
					DBManager.updateDiag(diag);
				}
				diag.applyChanges();*/
				
				//queue stage
				//diag.ticket.stage = QueueStage.beginHold;
				
				//update patient & diag in waiting queue
				if (DBManager.updateWaitingList(diag.patient.ticket) == 0) {
					//direcct diagnosis (unqueued patient)
					DBManager.insertWaitingList(diag.patient.ticket);
				}
				//diag.ticket.applyChanges();
				
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			diag.applyChanges();
			
			//dequeue
			if (diag.patient.ticket.stage != QueueStage.begin) {
				WaitingList.diagQueue.dequeue(diag.patient.id.getValue());
			}
			//diag.applyChanges();
			diag = null;
			//queuedPatient = null;
			populate();
			Patient.triggerPatientSelectionEmptyEvent();
		}
	};
	
	private String getActionName() {
		NamedAction a = getAction();
		if (a == null) {
			return ""; //$NON-NLS-1$
		}
		return a.name;
	}
	
	private NamedAction getAction() {
		if (diag == null) {
			return actionNext;
		}
		
		if (diag.patient.ticket == null) {
			return null;
		}
		if (diag.patient.ticket.stage == QueueStage.begin) {
			return actionComplete;
		} else if (diag.patient.ticket.stage == QueueStage.beginHold) {
			//if (holdButton.getSelection()) {
			//Boolean hold = diag.hold.getValue();
			return actionSave;
		} else {
			return actionReopen;
		}
	}

	@Persist
	public void save() {
		NamedAction action = getAction();
		if (action == actionSave || action == actionComplete) {
			action.run();
		}
	}
}