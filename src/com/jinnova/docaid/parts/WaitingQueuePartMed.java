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
import com.jinnova.docaid.WaitingList;

public class WaitingQueuePartMed extends WaitingQueuePart {

	@Override
	WaitingList getWaitingList() {		
		return WaitingList.medQueue;
	}
	
}