package com.jinnova.docaid.parts;

import com.jinnova.docaid.WaitingList;

public class WaitingQueuePartDone extends WaitingQueuePart {

	@Override
	WaitingList getWaitingList() {		
		return WaitingList.doneQueue;
	}
	
}
