package com.jinnova.docaid.parts;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Control;

import com.jinnova.docaid.Medicine;

public class MedAutoCompleteField {
	
	//private SimpleContentProposalProvider proposalProvider;
	
	private ContentProposalAdapter adapter;

	/**
	 * Construct an AutoComplete field on the specified control, whose
	 * completions are characterized by the specified array of Strings.
	 *
	 * @param control
	 *            the control for which autocomplete is desired. May not be
	 *            <code>null</code>.
	 * @param controlContentAdapter
	 *            the <code>IControlContentAdapter</code> used to obtain and
	 *            update the control's contents. May not be <code>null</code>.
	 * @param proposals
	 *            the array of Strings representing valid content proposals for
	 *            the field.
	 */
	public MedAutoCompleteField(Control control, DiagnosePart part/*,
			IControlContentAdapter controlContentAdapter, String[] proposals*/) {
		//proposalProvider = new SimpleContentProposalProvider(new String[] {"a", "b"});
		//proposalProvider.setFiltering(true);
		//adapter = new ContentProposalAdapter(control, controlContentAdapter, proposalProvider, null, null);
		adapter = new ContentProposalAdapter(control, new TextContentAdapter(), new MedProposalProvider(), null, null);
		adapter.setPropagateKeys(true);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		
		adapter.addContentProposalListener(new IContentProposalListener() {
			
			@Override
			public void proposalAccepted(IContentProposal proposal) {
				@SuppressWarnings("unchecked")
				Medicine med = ((WeightedContentProposal<MedProposingContent>) proposal).med.med;
				part.diag.prescription.editingItem.medPackageBreakable = med.packageBreakable.getValue();
				part.diag.prescription.editingItem.medPackageSize = med.packageSize.getValue();
				part.diag.prescription.editingItem.medPackage = med.packageUnit.getValue();
				part.diag.prescription.editingItem.medUnit = med.unit.getValue();
				part.diag.prescription.editingItem.medUnitPrice = med.unitPrice.getValue();
				part.diag.prescription.editingItem.medPackagePrice = med.packageUnitPrice.getValue();
				//part.medUnitReadonly.setText(med.unit.getValueAsEditing());
				part.populateMedEnablements();
				/*part.medAmountPerTakingLabel.setText("Mỗi lần (" + med.unit.getValueAsEditing() + ")");
				part.amountTotalPackageLabel.setText("SL (" + med.packageUnit.getValueAsEditing() + ")");
				part.amountTotalUnitLabel.setText("SL (" + med.unit.getValueAsEditing() + ")");
				part.medDays.setText(part.treatmentDays.getText());
				boolean twoUnit = med.isTwoUnit();
				if (twoUnit) {
					if (med.isBreakable()) {
						part.amountTotalUnit.setVisible(true);
						part.amountTotalUnitLabel.setVisible(true);
						part.amountTotalPackage.setVisible(true);
						part.amountTotalPackageLabel.setVisible(true);
					} else {
						part.amountTotalUnit.setVisible(false);
						part.amountTotalUnitLabel.setVisible(false);
						part.amountTotalPackage.setVisible(true);
						part.amountTotalPackageLabel.setVisible(true);
					}
				} else {
					part.amountTotalUnit.setVisible(true);
					part.amountTotalUnitLabel.setVisible(true);
					part.amountTotalPackage.setVisible(false);
					part.amountTotalPackageLabel.setVisible(false);
				}*/
			}
		});
	}

	/**
	 * Set the Strings to be used as content proposals.
	 *
	 * @param proposals
	 *            the array of Strings to be used as proposals.
	 */
	/*public void setProposals(String[] proposals) {
		proposalProvider.setProposals(proposals);
	}*/
}

class MedProposingContent implements ProposingContent {
	
	Medicine med;
	
	public MedProposingContent(Medicine m) {
		this.med = m;
	}

	@Override
	public String getProposalText() {
		String s = med.name.getValue();
		if (s == null) {
			return "";
		}
		return s;
	}

	@Override
	public String getKeywords() {
		String s = med.keywords.getValue();
		if (s == null) {
			return "";
		}
		return s;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.med).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MedProposingContent)) {
			return false;
		}
		MedProposingContent c = (MedProposingContent) obj;
		return new EqualsBuilder().append(this.med, c.med).isEquals();
	}
	
}
