package com.jinnova.docaid.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.eclipse.jface.fieldassist.ContentProposal;

/*public class HashIndex {

}*/

interface ProposingContent {
	
	String getProposalText();
	
	String getKeywords();
}

class WeightedContentProposal<E extends ProposingContent> extends ContentProposal {
	
	E med;
	int weight;

	public WeightedContentProposal(E med, int weight) {
		super(med.getProposalText());
		this.med = med;
		this.weight = weight;
	}
	
}

/*class HashIndexEntry<E> {
	
	E content;
	int count;
	
	HashIndexEntry(E content, int count) {
		this.content = content;
		this.count = count;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.content).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof HashIndexEntry)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		HashIndexEntry<E> t = (HashIndexEntry<E>) obj;
		return new EqualsBuilder().append(this.content, t.content).isEquals();
	}
}*/

public class HashIndex<E extends ProposingContent> {
	
	private HashMap<String, HashMap<E, Integer>> map;
	
	private static final String REGEX_KEY_DELIM = " |\\(|\\)|\\.\\,";
	
	HashIndex(ArrayList<E>  allContents) {
		//<key, <entry, appearCount>>
		//HashMap<String, HashMap<E, Integer>> tempMap = new HashMap<>();
		map = new HashMap<String, HashMap<E,Integer>>();
		for (E proposingContent : allContents) {
			String proposingText = proposingContent.getProposalText().trim().toLowerCase();
			int prefixMax = Math.min(proposingText.length(), 10);
			LinkedList<String> keys = new LinkedList<String>();
			for (int i = 0; i < prefixMax; i++) {
				keys.add(proposingText.substring(0, i + 1));
			}
			/*String s = proposingContent.getProposalText() + " " + 
					proposingContent.getKeywords().trim().toLowerCase();*/
			String s;
			String keywords = proposingContent.getKeywords();
			if (keywords != null) {
				s = proposingText + " " + keywords.trim().toLowerCase();
			} else {
				s = proposingText;
			}
			keys.addAll(Arrays.asList(s.split(REGEX_KEY_DELIM)));
			for (String k : keys) {
				HashMap<E, Integer> subMap = map.get(k);
				if (subMap == null) {
					subMap = new HashMap<>();
					map.put(k, subMap);
				}
				Integer count = subMap.get(proposingContent);
				if (count == null) {
					count = 0;
				}
				subMap.put(proposingContent, count + 1);
			}
		}
		
		/*map = new HashMap<String, LinkedList<HashIndexEntry<E>>>();
		for (Entry<String, HashMap<E, Integer>> e : tempMap.entrySet()) {
			HashMap<E, Integer> subMap = e.getValue();
			LinkedList<HashIndexEntry<E>> entryList = new LinkedList<HashIndexEntry<E>>();
			for (Entry<E, Integer> subE : subMap.entrySet()) {
				entryList.add(new HashIndexEntry<E>(subE.getKey(), subE.getValue()));
			}
			map.put(e.getKey(), entryList);
		}*/
	}
	
	void add(E proposingContent) {
		String proposingText = proposingContent.getProposalText().trim().toLowerCase();
		int prefixMax = Math.min(proposingText.length(), 10);
		LinkedList<String> keys = new LinkedList<String>();
		for (int i = 0; i < prefixMax; i++) {
			keys.add(proposingText.substring(0, i + 1));
		}
		
		String s;
		String keywords = proposingContent.getKeywords();
		if (keywords != null) {
			s = proposingText + " " + keywords.trim().toLowerCase();
		} else {
			s = proposingText;
		}
		
		keys.addAll(Arrays.asList(s.split(REGEX_KEY_DELIM)));
		
		for (String k : keys) {
			HashMap<E, Integer> contentCountMap = map.get(k);
			if (contentCountMap == null) {
				contentCountMap = new HashMap<>();
				map.put(k, contentCountMap);
			}
			
			Integer currentCount = contentCountMap.get(proposingContent);
			if (currentCount == null) {
				currentCount = 0;
			}
			contentCountMap.put(proposingContent, currentCount++);
		}
	}
	
	LinkedList<WeightedContentProposal<E>> getProposals(String s) {
		String[] keys = s.trim().toLowerCase().split(REGEX_KEY_DELIM);
		//HashMap<E, Integer> resultMap = new HashMap<>();
		LinkedList<WeightedContentProposal<E>> result = new LinkedList<>();
		for (String k : keys) {
			HashMap<E, Integer> contentCountMap = map.get(k);
			if (contentCountMap == null) {
				continue;
			}
			for (Entry<E, Integer> entry : contentCountMap.entrySet()) {
				/*Integer i = resultMap.get(e.content);
				if (i == null) {
					i = 1;
				}
				resultMap.put(e.content, e.count + i);*/
				result.add(new WeightedContentProposal<E>(entry.getKey(), entry.getValue()));
			}
		}
		
		/*for (Entry<E, Integer> e : resultMap.entrySet()) {
			result.add(new WeightedContentProposal<E>(e.getKey(), e.getValue()));
		}*/
		result.sort(new Comparator<WeightedContentProposal<E>>() {

			@Override
			public int compare(WeightedContentProposal<E> o1, WeightedContentProposal<E> o2) {
				return o1.weight - o2.weight;
			}
		});
		return result;
	}
}
