package com.jinnova.docaid.parts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.swt.widgets.Control;

import com.jinnova.docaid.AutoText;
import com.jinnova.docaid.DBManager;

public class FieldAutoComplete {
	
	public static final String HEALTHNOTE = "healthnote";
	
	private ContentProposalAdapter adapter;
	public final FieldProposalProvider proposalProvider;

	public FieldAutoComplete(Control control, String fieldId) {
		proposalProvider = FieldProposalProvider.getInstance(fieldId);
		adapter = new ContentProposalAdapter(
				control, new PartialTextContentAdapter(), proposalProvider, null, null);
		adapter.setPropagateKeys(true);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);
	}

}

class FieldProposalProvider implements IContentProposalProvider {
	
	private HashIndex<AutotextProposingContent> indices;
	
	private LinkedList<String> allPhrases = new LinkedList<String>();
	
	private final String fieldId;
	
	private static final HashMap<String, FieldProposalProvider> providers = new HashMap<>();
	
	static FieldProposalProvider getInstance(String fieldId) {
		FieldProposalProvider p = providers.get(fieldId);
		if (p == null) {
			p = new FieldProposalProvider(fieldId);
			providers.put(fieldId, p);
		}
		return p;
	}
	
	private FieldProposalProvider(String fieldId) {
		this.fieldId = fieldId;
		ArrayList<AutoText>  allTexts;
		try {
			allTexts = DBManager.selectAutotexts(fieldId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		ArrayList<AutotextProposingContent> allContents = new ArrayList<>();
		for (AutoText t : allTexts) {
			allContents.add(new AutotextProposingContent(t));
			allPhrases.add(t.content);
		}
		indices = new HashIndex<>(allContents);
	}
	
	public void learn(String content) {
		String[] phrases = content.trim().toLowerCase().split(",|\\.");
		for (String one : phrases) {
			one = one.trim();
			if (one.isEmpty() || allPhrases.contains(one)) {
				continue;
			}
			AutoText at = new AutoText();
			at.fieldId = this.fieldId;
			at.content = one;
			try {
				DBManager.insertAutotext(at);
			} catch (SQLException e) {
				try {
					DBManager.updateAutotext(at);
				} catch (SQLException e1) {
					throw new RuntimeException(e);
				}
			}
			indices.add(new AutotextProposingContent(at));
		}
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		System.out.println("propsing for: " + contents + " at " + position);
		int[] sel = PartialTextContentAdapter.selectPhrase(contents, position);
		LinkedList<WeightedContentProposal<AutotextProposingContent>> l = 
				 indices.getProposals(contents.substring(sel[0], sel[1]));
		 return l.toArray(new WeightedContentProposal[l.size()]);
	}

}

class AutotextProposingContent implements ProposingContent {
	
	AutoText at;
	
	public AutotextProposingContent(AutoText at) {
		this.at = at;
	}

	@Override
	public String getProposalText() {
		return at.content;
	}

	@Override
	public String getKeywords() {
		return at.keywords;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.at).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AutotextProposingContent)) {
			return false;
		}
		AutotextProposingContent c = (AutotextProposingContent) obj;
		return new EqualsBuilder().append(this.at, c.at).isEquals();
	}
	
}
