package com.jinnova.docaid.parts;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.jinnova.docaid.FieldFloat;
import com.jinnova.docaid.Patient;
import com.jinnova.docaid.PatientFamilyMember;
import com.jinnova.docaid.QueueTicket;
import com.jinnova.docaid.SettingName;
import com.jinnova.docaid.WaitingList;

public class PatientTableBuilder {

	private Color queuedColor;

	private Color revisitColor;
	
	private TableViewer patientList;
	
	private class PatientQueueLabelProvider extends ColumnLabelProvider {

		  @Override
		  public Color getForeground(Object element) {
			  
			  Patient p = getPatient(element);
			  if (p == null) {
				  return null;
			  }
			  /*if (WaitingList.diagQueue.isQueued(p.id.getValue()) ||
					  WaitingList.diagHoldQueue.isQueued(p.id.getValue()) ||
					  WaitingList.medQueue.isQueued(p.id.getValue()) ||
					  WaitingList.doneQueue.isQueued(p.id.getValue())) {
				  return queuedColor;
			  }*/
			  if (WaitingList.getQueueItemAny(p.id.getValue()) != null) {
				  return queuedColor;
			  }

			  Integer days = p.lastVisit.getDaysFromNow();
			  //if (p.hasAppointmentToday()) {
			  if (days != null && days <= 90) {
				  return revisitColor;
			  }
			  return null;
		  }

			/*@Override
			public Font getFont(Object element) {
				Patient p = (Patient) element;
				Integer days = p.lastVisit.getDaysFromNow();
				if (days != null && days <= 31) {
					  return boldFont;
				}
				return super.getFont(element);
			}*/
	}
	
	private static Patient getPatient(Object element) {
		  if (element instanceof PatientFamilyMember) {
			  return ((PatientFamilyMember) element).patient;
		  } else if (element instanceof Patient) {
			  return (Patient) element;
		  }
		  return null;
	}
	
	private static String maskNull(String s) {
		if (s == null) {
			return ""; //$NON-NLS-1$
		}
		return s;
	}
	
	/*private static String maskNull(Float f) {
		if (f == null || f == 0) {
			return ""; //$NON-NLS-1$
		}
		return String.valueOf(f);
	}*/
	
	TableViewer builderViewer(Composite parent) {

		patientList = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		patientList.setContentProvider(ArrayContentProvider.getInstance());

		queuedColor = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		revisitColor = parent.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		//FontData fd = patientList.getTable().getFont().getFontData()[0];
		//fd.setStyle(SWT.BOLD);
		//Font boldFont = new Font(parent.getDisplay(), fd);
		TableBuilder builder = new TableBuilder();
		builder.createColumn(SettingName.patient_search_result_field_id, 
				40, Messages.PatientTableBuilder_id, new PatientQueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
				if (element instanceof PatientFamilyMember) {
					return ((PatientFamilyMember) element).id + ""; //$NON-NLS-1$
				}
			    Patient p = getPatient(element);
			    if (p == null) {
			    	return ""; //$NON-NLS-1$
			    }
			    String s = p.id.getValueAsEditing();
				  if (p.isModified()) {
					  return "*" + s; //$NON-NLS-1$
				  } else {
					  return s;
				  }
			  }
			});
		builder.createColumn(SettingName.patient_search_result_field_name,
				150, Messages.PatientTableBuilder_name, new PatientQueueLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PatientFamilyMember) {
					return ((PatientFamilyMember) element).name;
				}
				Patient p = getPatient(element);
				if (p == null) {
					return ""; //$NON-NLS-1$
				}
				String name = p.name.getValue();
				if (WaitingList.diagQueue.isQueued(p.id.getValue())) {
					return name + " (chờ khám)";
				} else if (WaitingList.medQueue.isQueued(p.id.getValue())) {
					return name + Messages.PatientTableBuilder_waitMed_in_brackets;
				} else if (WaitingList.doneQueue.isQueued(p.id.getValue())) {
					return name + Messages.PatientTableBuilder_gotMed_in_brackets;
				}
				QueueTicket holdTicket = WaitingList.diagHoldQueue.getQueueItem(p.id.getValue());
				if (holdTicket != null) {
					return name + " (" + holdTicket.getHoldReason() + ")";
				}
				return name;
			}
			});
		builder.createColumn(SettingName.patient_search_result_field_dob,
				80, Messages.PatientTableBuilder_dob, new PatientQueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
					Patient p = getPatient(element);
					if (p == null) {
						return ""; //$NON-NLS-1$
					}
			    if (p.dob.isTodayPreviousYear()) {
			    	return p.dob.getValueAsEditing() + Messages.PatientTableBuilder_birthday_in_brackets;
			    } else {
			    	return p.dob.getValueAsEditing();
			    }
			  }
			});
		builder.createColumn(SettingName.patient_search_result_field_lastvisit,
				95, Messages.PatientTableBuilder_last_viset, new PatientQueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
					Patient p = getPatient(element);
					if (p == null) {
						return ""; //$NON-NLS-1$
					}
			    return p.lastVisit.getEasyLabel();
			  }
			});
		builder.createColumn(SettingName.patient_search_result_field_prevappoint,
				80, Messages.PatientTableBuilder_prev_appointmenet, new PatientQueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
					Patient p = getPatient(element);
					if (p == null) {
						return ""; //$NON-NLS-1$
					}
			    return p.previousAppointment.getEasyLabel();
			  }
			});
		builder.createColumn(SettingName.patient_search_result_field_address,
				300, Messages.PatientTableBuilder_address, new PatientQueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
					Patient p = getPatient(element);
					if (p == null) {
						return ""; //$NON-NLS-1$
					}
			    return p.address.getValue();
			  }
			});
		builder.createColumn(SettingName.patient_search_result_field_age,
				50, Messages.PatientTableBuilder_age, new PatientQueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
					Patient p = getPatient(element);
					if (p == null) {
						return ""; //$NON-NLS-1$
					}
			    return maskNull(p.age);
			  }
			});
		builder.createColumn(SettingName.patient_search_result_field_weight,
				50, Messages.PatientTableBuilder_weight, new PatientQueueLabelProvider() {
			  @Override
			  public String getText(Object element) {
					Patient p = getPatient(element);
					if (p == null) {
						return "";
					}
			    //return FieldFloat.getString(p.weight);
				return maskNull(p.weight);
			  }
			});
		
		builder.build(patientList);
		return patientList;
	}
}
