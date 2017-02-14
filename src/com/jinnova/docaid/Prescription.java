package com.jinnova.docaid;

import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class Prescription extends DynamicFieldSet {
	
	private final LinkedList<PrescriptionItem> items = new LinkedList<PrescriptionItem>();
	
	public PrescriptionItem editingItem;

	private Runnable changeListener;
	
	public final DiagRecord diag;
	
	public Prescription(DiagRecord diag) {
		this.diag = diag;
		editingItem = new PrescriptionItem(this);
		//addField(editingItem);
	}

	public void setChangeListener(Runnable runner) {
		this.changeListener = runner;
	}

	public String getSummary() {
		String s = null;
		for (PrescriptionItem pi : items) {
			if (s != null) {
				s = s + ", "; //$NON-NLS-1$
			} else {
				s = ""; //$NON-NLS-1$
			}
			String sub = null;
			if (pi.dayCount.getValue() != null) {
				sub = pi.dayCount.getValue() + Messages.Prescription_day_comma_spaces;
			}
			if (pi.amountPerTaking.getValue() != null && 
					pi.medUnit != null && pi.takingCountPerDay.getValue() != null) {
				if (sub != null) {
					sub = sub + ", "; //$NON-NLS-1$
				} else {
					sub = ""; //$NON-NLS-1$
				}
				sub = sub + pi.amountPerTaking.getValue() + pi.medUnit + "x" +  //$NON-NLS-1$
							pi.takingCountPerDay.getValue();
			}
			if (sub != null) {
				sub = " (" + sub + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				sub = ""; //$NON-NLS-1$
			}
			s = s + pi.medName.getValue() + sub;
		}
		return s;
	}
	
	public int computeTotalCost() {
		int total = 0;
		for (PrescriptionItem one : items) {
			total += one.computeCost();
		}
		return total;
	}

	public boolean isEditingItemAdded() {
		return items.contains(editingItem);
	}
	
	public void addEditingItem() {
		if (items.contains(editingItem)) {
			items.remove(editingItem);
			removeField(editingItem);
		}
		
		items.add(editingItem);
		addField(editingItem);
		
		editingItem = new PrescriptionItem(this);
		//addField(editingItem);
		if (changeListener != null) {
			changeListener.run();
		}
	}
	
	public void removeEditingItem() {
		items.remove(editingItem);
		removeField(editingItem);
		
		editingItem = new PrescriptionItem(this);
		//addField(editingItem);
		if (changeListener != null) {
			changeListener.run();
		}
	}
	
	public void setEditingItem(PrescriptionItem item) {
		/*if (!items.contains(editingItem)) {
			removeField(editingItem);
		}*/
		editingItem = item;
	}

	@Override
	public boolean isModified() {
		return editingItem.isModified() || super.isModified();
	}

	@Override
	public String validate() {
		
		if (editingItem.isModified()) {
			String err = editingItem.validate();
			if (err != null) {
				return err;
			}
		}
		return super.validate();
	}

	public int indexOf(PrescriptionItem p) {
		return items.indexOf(p);
	}

	public Object[] createItemArray() {
		LinkedList<Object> arr = new LinkedList<Object>();
		for (PrescriptionItem pi : items) {
			for (PrescriptionItemExtra o : pi.getItemLines()) {
				arr.add(o);
			}
		}
		if (!arr.isEmpty()) {
			arr.add(this);
		}
		return arr.toArray();
	}
	
	public String generateJson() {
		JsonArray ja = new JsonArray();
		for (PrescriptionItem pi : items) {
			ja.add(pi.generateJson());
		}
		return new Gson().toJson(ja);
	}
	
	public void loadJson(String s) {
		items.clear();
		JsonArray ja = new Gson().fromJson(s, JsonArray.class);
		if (ja == null) {
			return;
		}
		for (JsonElement e : ja) {
			PrescriptionItem pi = new PrescriptionItem(this);
			pi.loadJson(e.getAsJsonObject());
			items.add(pi);
		}
	}
}
