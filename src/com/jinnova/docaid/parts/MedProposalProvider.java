package com.jinnova.docaid.parts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import com.jinnova.docaid.DBManager;
import com.jinnova.docaid.Medicine;

public class MedProposalProvider implements IContentProposalProvider {
	
	private static HashIndex<MedProposingContent> medIndex;
	
	public static void invalidate() {
		medIndex = null;
	}
	
	private static void loadAlls() {

		ArrayList<Medicine>  allMeds;
		try {
			allMeds = DBManager.selectMeds("");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		ArrayList<MedProposingContent> allContents = new ArrayList<MedProposingContent>();
		for (Medicine m : allMeds) {
			allContents.add(new MedProposingContent(m));
		}
		medIndex = new HashIndex<>(allContents);
	}
	
	public MedProposalProvider() {
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		if (medIndex == null) {
			loadAlls();
		}
		 LinkedList<WeightedContentProposal<MedProposingContent>> l = medIndex.getProposals(contents);
		 return l.toArray(new WeightedContentProposal[l.size()]);
	}

}
