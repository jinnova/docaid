package com.jinnova.docaid.parts;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.jinnova.docaid.DBManager;
import com.jinnova.docaid.Field;
import com.jinnova.docaid.Medicine;

public class MedicinePart {

	private TableViewer viewer;
	private Text name;
	private Text keys;
	private Text unit;
	private Text packageUnit;
	private Text packageSize;
	private Button packageBreakable;
	
	private ArrayList<Medicine> list = new ArrayList<Medicine>();
	
	private final Medicine med = new Medicine();
	private Button saveButton;
	private Button deleteButton;
	private Composite editComp;
	private Label editLabel;
	private Button editButton;
	
	private Composite parent;
	private Button closeButton;
	private Text packageUnitPrice;
	private Text unitPrice;
	private Label packageSizeLabel;
	private Label unitPriceLabel;
	private Label packageUnitPriceLabel;
	
	private class MedFieldModifyListener<T> implements ModifyListener {
		
		private Field<T, String> field;
		
		MedFieldModifyListener(Field<T, String> f) {
			this.field = f;
		}
		
		@Override
		public void modifyText(ModifyEvent e) {
			field.changeValue(((Text) e.widget).getText());
			populateFieldEnablements();
			setButtonEnabled();
		}
	};
	
	public MedicinePart() {
		med.packageBreakable.loadValue(false);
	}
	
	private void createColumn(int width, String caption, ColumnLabelProvider labelProvider) {
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setWidth(width);
		col.getColumn().setText(caption);
		col.setLabelProvider(labelProvider);
	}
	
	private abstract class MedLabelProvider extends ColumnLabelProvider {

		  @Override
		  public String getText(Object element) {
			  return getText((Medicine) element);
		  }
		  
		  abstract String getText(Medicine m);
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout(1, false));
		viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true); 
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				 IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				 if (!selection.isEmpty()) {
					 Medicine m = (Medicine) selection.getFirstElement();
					 //med.copy(m);
					 //System.out.println("Got name: " + m.name.getValue());
					 //populate();
					 //setButtonEnabled();
					 editLabel.setData(m);
					 editLabel.setText(m.name.getValueAsEditing());
					 editComp.setVisible(true);
					 ((GridData) editComp.getLayoutData()).exclude = false;
					 parent.layout();
				 }
			}
		});
		
		createColumn(30, Messages.MedicinePart_queueNumber_abbr, new MedLabelProvider() {
			  @Override
			  public String getText(Medicine m) {
			      return String.valueOf(list.indexOf(m) + 1);
			  }
			});
		createColumn(500, Messages.MedicinePart_name_keys_in_bracketes, new MedLabelProvider() {
			  @Override
			  public String getText(Medicine m) {
				  String sub;
				  if (m.keywords.getValue() != null && !"".equals(m.keywords.getValue().trim())) { //$NON-NLS-1$
					  sub = " (" + m.keywords.getValue() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				  } else {
					  sub = ""; //$NON-NLS-1$
				  }
			      return m.name.getValue() + sub;
			  }
			});
		int colW = 90;
		createColumn(colW, Messages.MedicinePart_unit, new MedLabelProvider() {
			  @Override
			  public String getText(Medicine m) {
			      return m.unit.getValue();
			  }
			});
		createColumn(colW, Messages.MedicinePart_package, new MedLabelProvider() {
			  @Override
			  public String getText(Medicine m) {
			      return m.packageUnit.getValue();
			  }
			});
		createColumn(colW, Messages.MedicinePart_unit_per_package_abbr, new MedLabelProvider() {
			  @Override
			  public String getText(Medicine m) {
			      return String.valueOf(m.packageSize.getValueAsEditing());
			  }
			});
		createColumn(80, Messages.MedicinePart_package_breakable_abbr, new MedLabelProvider() {
			  @Override
			  public String getText(Medicine m) {
				  if (m.packageBreakable.getValue()) {
					  return Messages.MedicinePart_yes;
				  } else {
					  return Messages.MedicinePart_no;
				  }
			  }
			});
		createColumn(colW, Messages.MedicinePart_unit_price, new MedLabelProvider() {
			  @Override
			  public String getText(Medicine m) {
			      return String.valueOf(m.unitPrice.getValueAsEditing());
			  }
			});
		createColumn(colW, Messages.MedicinePart_package_price, new MedLabelProvider() {
			  @Override
			  public String getText(Medicine m) {
			      return String.valueOf(m.packageUnitPrice.getValueAsEditing());
			  }
			});
		
		editComp = new Composite(parent, SWT.None);
		editComp.setLayout(new GridLayout(2, false));
		GridData gdata = new GridData();
		//gdata.horizontalSpan = 4;
		gdata.exclude = true;
		editComp.setLayoutData(gdata);
		editComp.setVisible(false);
		
		editLabel = new Label(editComp, SWT.None);
		editLabel.setText(Messages.MedicinePart_select_colon);
		editLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		editButton = new Button(editComp, SWT.None);
		editButton.setText(Messages.MedicinePart_edit);
		editButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Medicine m = (Medicine) editLabel.getData();
				if (m == null) {
					return;
				}
				med.copy(m, true);
				populate();
				setButtonEnabled();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		Composite comp = new Composite(parent, SWT.None);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comp.setLayout(new GridLayout(10, false));
		
		Label l = new Label(comp, SWT.None);
		l.setText(Messages.MedicinePart_name);
		
		Composite nameComp = new Composite(comp, SWT.None);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 9;
		//gdata.horizontalIndent = 0;
		nameComp.setLayoutData(gdata);
		nameComp.setLayout(new GridLayout(3, false));
		
		name = new Text(nameComp, SWT.None);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		//gdata.horizontalSpan = 6;
		name.setLayoutData(gdata);
		name.addModifyListener(new MedFieldModifyListener<String>(med.name));
		name.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				load();
			}
		});
		
		l = new Label(nameComp, SWT.None);
		l.setText(Messages.MedicinePart_keys);
		keys = new Text(nameComp, SWT.None);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		//gdata.horizontalSpan = 2;
		keys.setLayoutData(gdata);
		keys.addModifyListener(new MedFieldModifyListener<String>(med.keywords));
		
		l = new Label(comp, SWT.None);
		l.setText(Messages.MedicinePart_unit);

		/*comp = new Composite(formComp, SWT.None);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 3;
		//gdata.horizontalIndent = 0;
		comp.setLayoutData(gdata);
		comp.setLayout(new GridLayout(9, false));*/
		
		unit = new Text(comp, SWT.None);
		gdata = new GridData();
		int w = 70;
		gdata.widthHint = w;
		//gdata.horizontalIndent = 0;
		unit.setLayoutData(gdata);
		unit.addModifyListener(new MedFieldModifyListener<String>(med.unit));
		
		l = new Label(comp, SWT.None);
		l.setText(Messages.MedicinePart_package);
		packageUnit = new Text(comp, SWT.None);
		gdata = new GridData();
		gdata.widthHint = w;
		packageUnit.setLayoutData(gdata);
		packageUnit.addModifyListener(new MedFieldModifyListener<String>(med.packageUnit));
		
		packageSizeLabel = new Label(comp, SWT.None);
		packageSizeLabel.setText(Messages.MedicinePart_units_per_package);
		packageSize = new Text(comp, SWT.None);
		gdata = new GridData();
		gdata.widthHint = w;
		packageSize.setLayoutData(gdata);
		packageSize.addVerifyListener(new FloatVerifyListener());
		packageSize.addModifyListener(new MedFieldModifyListener<Float>(med.packageSize));
		
		packageBreakable = new Button(comp, SWT.CHECK);
		packageBreakable.setText(Messages.MedicinePart_package_breakable);
		gdata = new GridData();
		//gdata.widthHint = w;
		//gdata.grabExcessHorizontalSpace = true;
		packageBreakable.setLayoutData(gdata);
		packageBreakable.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				med.packageBreakable.loadValue(packageBreakable.getSelection());
				populateFieldEnablements();
				setButtonEnabled();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		Composite buttonComp = new Composite(comp, SWT.None);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 3;
		buttonComp.setLayoutData(gdata);
		buttonComp.setLayout(new GridLayout(3, true));
		
		saveButton = new Button(buttonComp, SWT.None);
		saveButton.setText(Messages.MedicinePart_save);
		gdata = new GridData(GridData.FILL_BOTH);
		//gdata.widthHint = w;
		gdata.heightHint = 20;
		//gdata.grabExcessHorizontalSpace = true;
		saveButton.setLayoutData(gdata);
		saveButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				save();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		deleteButton = new Button(buttonComp, SWT.None);
		deleteButton.setText(Messages.MedicinePart_delete);
		gdata = new GridData(GridData.FILL_BOTH);
		//gdata.widthHint = w;
		gdata.heightHint = 20;
		//gdata.grabExcessHorizontalSpace = true;
		deleteButton.setLayoutData(gdata);
		deleteButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				delete();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		closeButton = new Button(buttonComp, SWT.None);
		closeButton.setText(Messages.MedicinePart_close);
		gdata = new GridData(GridData.FILL_BOTH);
		//gdata.widthHint = w;
		gdata.heightHint = 20;
		//gdata.grabExcessHorizontalSpace = true;
		closeButton.setLayoutData(gdata);
		closeButton.setEnabled(false);
		closeButton.setSelection(false);
		closeButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		unitPriceLabel = new Label(comp, SWT.None);
		unitPriceLabel.setText(Messages.MedicinePart_unit_price);

		/*comp = new Composite(formComp, SWT.None);
		gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.horizontalSpan = 3;
		//gdata.horizontalIndent = 0;
		comp.setLayoutData(gdata);
		comp.setLayout(new GridLayout(9, false));*/
		
		unitPrice = new Text(comp, SWT.None);
		gdata = new GridData();
		gdata.widthHint = w;
		unitPrice.setLayoutData(gdata);
		unitPrice.addVerifyListener(new IntVerifyListener());
		unitPrice.addModifyListener(new MedFieldModifyListener<Integer>(med.unitPrice));
		
		packageUnitPriceLabel = new Label(comp, SWT.None);
		packageUnitPriceLabel.setText(Messages.MedicinePart_package_price_abbr);
		packageUnitPrice = new Text(comp, SWT.None);
		gdata = new GridData();
		gdata.widthHint = w;
		packageUnitPrice.setLayoutData(gdata);
		packageUnitPrice.addVerifyListener(new IntVerifyListener());
		packageUnitPrice.addModifyListener(new MedFieldModifyListener<Integer>(med.packageUnitPrice));
		
		load();
		populate();
		setButtonEnabled();
	}
	
	private static <T> boolean isEmpty(Field<T, String> f) {
		String s = f.getValueAsEditing();
		return s == null || "".equals(s.trim()); //$NON-NLS-1$
	}
	
	private void setButtonEnabled() {
		String s = med.name.getValue();
		deleteButton.setEnabled(s != null && !s.trim().equals("")); //$NON-NLS-1$
		saveButton.setEnabled(med.validate() == null);
		
		Boolean breakable = med.packageBreakable.getValueAsEditing();
		closeButton.setEnabled(
				!isEmpty(med.name) ||
				!isEmpty(med.unit) ||
				!isEmpty(med.keywords) ||
				(breakable != null && breakable) ||
				!isEmpty(med.packageSize) ||
				!isEmpty(med.packageUnit));
	}
	
	private void populate() {
		name.setText(med.name.getValueAsEditing());
		unit.setText(med.unit.getValueAsEditing());
		unitPrice.setText(med.unitPrice.getValueAsEditing());
		keys.setText(med.keywords.getValueAsEditing());
		packageBreakable.setSelection(med.packageBreakable.getValueAsEditing());
		packageSize.setText(med.packageSize.getValueAsEditing());
		packageUnit.setText(med.packageUnit.getValueAsEditing());
		packageUnitPrice.setText(med.packageUnitPrice.getValueAsEditing());
		//populateFieldEnablements();
	}
	
	private void populateFieldEnablements() {
		
		boolean b = !isEmpty(med.packageUnit) && !med.packageUnit.getValue().equals(med.unit.getValue());
		packageSize.setVisible(b);
		packageSizeLabel.setVisible(b);
		
		packageUnitPrice.setVisible(b);
		packageUnitPriceLabel.setVisible(b);

		packageBreakable.setVisible(b);
		
		b = isEmpty(med.packageUnit) ||
				med.packageBreakable.getValue() != null && med.packageBreakable.getValue();
		unitPrice.setVisible(b);
		unitPriceLabel.setVisible(b);
	}
	
	private void load() {
		try {
			list = DBManager.selectMeds(med.name.getValueAsEditing());
		} catch (SQLException e) {
			e.printStackTrace();
			viewer.setInput(null);
		}
		viewer.setInput(list.toArray());
		
		editComp.setVisible(false);
		((GridData) editComp.getLayoutData()).exclude = true;
		parent.layout();
	}

	private void save() {
		
		//float size = Float.parseFloat(packageSize.getText());
		try {
			/*DBManager.insertMed(name.getText(), unit.getText(), keys.getText(), 
					packageUnit.getText(), size, packageBreakable.getSelection());*/
			
			int count = DBManager.updateMed(med);
			if (count == 0) {
				DBManager.insertMed(med);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		clearSecondaryFields();
		populate();
		load();
		setButtonEnabled();
		MedProposalProvider.invalidate();
	}
	
	private void delete() {
		try {
			DBManager.deleteMed(med.name.getValue(), med.unit.getValue());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		clearSecondaryFields();
		populate();
		load();
		setButtonEnabled();
		MedProposalProvider.invalidate();
	}
	
	private void close() {
		med.name.changeValue(""); //$NON-NLS-1$
		clearSecondaryFields();
		populate();
		load();
		setButtonEnabled();
	}
	
	private void clearSecondaryFields() {
		med.unit.changeValue(""); //$NON-NLS-1$
		med.keywords.changeValue(""); //$NON-NLS-1$
		med.packageBreakable.changeValue(false);
		med.packageSize.changeValue(""); //$NON-NLS-1$
		med.packageUnit.changeValue(""); //$NON-NLS-1$
		med.unitPrice.changeValue(""); //$NON-NLS-1$
		med.packageUnitPrice.changeValue(""); //$NON-NLS-1$
	}
}
