package com.jinnova.docaid.parts;

import com.jinnova.docaid.WaitingList;

public class WaitingQueuePartDiag extends WaitingQueuePart {

	@Override
	WaitingList getWaitingList() {		
		return WaitingList.diagQueue;
	}

}
