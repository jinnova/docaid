package com.jinnova.docaid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;

public class WaitingList {
	
	private LinkedList<QueueTicket> patientList = new LinkedList<QueueTicket>();
	private HashMap<Integer, QueueTicket> patientMap = new HashMap<Integer, QueueTicket>();
	private final LinkedList<TableViewer> patientViewers = new LinkedList<TableViewer>();
	private final Object mapLock = new Object();
	
	private final QueueStage assignedState;
	
	public static final WaitingList diagQueue = new WaitingList(QueueStage.begin);
	public static final WaitingList diagHoldQueue = new WaitingList(QueueStage.beginHold);
	public static final WaitingList medQueue = new WaitingList(QueueStage.med);
	public static final WaitingList doneQueue = new WaitingList(QueueStage.end);

	public static Runnable queueListener;
	
	private final LinkedList<QueuePeekingListener> peekingListeners = new LinkedList<>();
	
	private WaitingList(QueueStage assignedStage) {
		this.assignedState = assignedStage;
	}
	
	public void registerViewer(TableViewer viewer) {
		patientViewers.add(viewer);
		synchronized (mapLock) {
			refreshViewersWithMapSynchronized();
		}
	}
	
	public void addQueuePeekingListener(QueuePeekingListener l) {
		this.peekingListeners.add(l);
	}
	
	public boolean isQueued(int patientId) {
		synchronized (mapLock) {
			return patientMap.containsKey(patientId);
		}
	}
	
	public boolean isNumberAvailable(int nbr) {
		//synchronized (mapLock) {
			for (QueueTicket q : patientList) {
				if (q.queueNumber.getBackupValue() == nbr) {
					return false;
				}
			}
			return true;
		//}
	}
	
	public static QueueTicket getQueueItemAny(int patientId) {
		QueueTicket t = diagQueue.getQueueItem(patientId);
		if (t != null) {
			return t;
		}
		t = diagHoldQueue.getQueueItem(patientId);
		if (t != null) {
			return t;
		}
		t = medQueue.getQueueItem(patientId);
		if (t != null) {
			return t;
		}
		return doneQueue.getQueueItem(patientId);
	}

	public QueueTicket getQueueItem(int patientId) {
		synchronized (mapLock) {
			return patientMap.get(patientId);
		}
	}
	
	public QueueTicket peek() {
		QueueTicket q;
		synchronized (mapLock) {
			q = patientList.peek();
		}
		setViewersSelection(q);
		for (QueuePeekingListener l : peekingListeners) {
			l.queuePeeked(q);
		}
		return q;
	}
	
	public List<QueueTicket> getHeads() {
		synchronized (mapLock) {
			return patientList.subList(0, Math.min(4, patientList.size()));
		}
	}
	
	public int getLastNumber() {
		synchronized (mapLock) {
			if (patientList.isEmpty()) {
				return 0;
			}
			return getQueueNumber(patientList.getLast());
		}
	}
	
	public static int getQueueNumber(QueueTicket q) {
		if (q == null) {
			return 0;
		}
		Integer i = q.queueNumber.getValue();
		if (i == null) {
			return 0;
		}
		return i;
	}

	public void enqueue(QueueTicket q) {
		enqueueDb(q);
		synchronized (mapLock) {
			if (!patientMap.containsKey(q.patient.id.getValue())) {
				patientList.add(q);
				patientMap.put(q.patient.id.getValue(), q);
			}
			refreshViewersWithMapSynchronized();
		}
	}
	
	public void dequeue(int patientId) {
		synchronized (mapLock) {
			QueueTicket q = patientMap.get(patientId);
			patientList.remove(q);
			patientMap.remove(patientId);
			refreshViewersWithMapSynchronized();
		}
	}
		
	private static void enqueueDb(QueueTicket q) {
		try {
			
			//q.diag.patient.weight.loadValue(q.diag.weight.getValue());
			//q.diag.patient.height.loadValue(q.diag.height.getValue());
			if (q.patient.id.getValue() != null) {
				DBManager.updatePatient(q.patient);
			} else {
				int newId = DBManager.insertPatient(q.patient);
				q.patient.id.loadValue(newId);
			}
			q.patient.applyChanges();
			
			Integer queueNumber = q.queueNumber.getValue();
			//boolean newQueueing = q.queueNumber.getBackupValue() == null;
			int currentNext = getNextNumber();
			if (queueNumber == null) {
				queueNumber = currentNext;
				q.queueNumber.loadValue(queueNumber);
				DBManager.updateSetting(SettingName.queueing_next.name(), currentNext + 1);
			} else {
				if (currentNext <= queueNumber) {
					DBManager.updateSetting(SettingName.queueing_next.name(), queueNumber + 1);
				}
			}

			//boolean newQueueing = q.queueNumber.getBackupValue() != null;
			/*if (newQueueing) {
				DBManager.insertWaitingList(q);
			} else {
				DBManager.updateWaitingList(q);
			}*/
			if (DBManager.updateWaitingList(q) != 1) {
				DBManager.insertWaitingList(q);
			}
			q.applyChanges();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static int getNextNumber() {
		try {
			String s = DBManager.loadSetting(SettingName.queueing_next.name());
			if (s == null || "".equals(s.trim())) {
				return 1;
			}
			return Integer.parseInt(s);
		} catch (NumberFormatException | SQLException e) {
			return -1;
		}
	}

	/*public static int getNextNumber() {
		try {
			return DBManager.getNextWaitingNumber();
		} catch (SQLException e) {
			try {
				Activator.initDbCon();
				return DBManager.getNextWaitingNumber();
			} catch (SQLException e1) {
				return -1;
			}
			
		}
	}*/

	public static void startBackgroundLoading() {
		new Thread() {
			public void run() {

				int diagQueueLoading = 0;
				while (true) {
					try {
						Thread.sleep(1000);
						diagQueueLoading++;
						if (diagQueueLoading == 3) {
							diagQueueLoading = 0;
							LinkedList<QueueTicket> queueAllStages = DBManager.loadQueue();
							queueAllStages.sort(new Comparator<QueueTicket>() {
								@Override
								public int compare(QueueTicket o1, QueueTicket o2) {
									return o1.queueNumber.getValue() - o2.queueNumber.getValue();
								}
							});
							
							diagQueue.applyInput(queueAllStages);
							diagHoldQueue.applyInput(queueAllStages);
							medQueue.applyInput(queueAllStages);
							doneQueue.applyInput(queueAllStages);
							
							/*LinkedList<QueueTicket> loadedDiags = new LinkedList<QueueTicket>();
							LinkedList<QueueTicket> loadedMeds = new LinkedList<QueueTicket>();
							LinkedList<QueueTicket> loadedDones = new LinkedList<QueueTicket>();
							for (QueueTicket q : queueAllStages) {
								if (q.stage == QueueStage.begin) {
									loadedDiags.add(q);
								} else if (q.stage == QueueStage.med) {
									loadedMeds.add(q);
								} else if (q.stage == QueueStage.end) {
									loadedDones.add(q);
								}
							}
							
							//diags
							synchronized (diagQueue.mapLock) {
								loadedDiags.replaceAll(diagQueue.changeChecker);
							}
							synchronized (diagHoldQueue.mapLock) {
								loadedDiags.replaceAll(diagHoldQueue.changeChecker);
							}
							synchronized (medQueue.mapLock) {
								loadedDiags.replaceAll(medQueue.changeChecker);
							}
							synchronized (doneQueue.mapLock) {
								loadedDiags.replaceAll(doneQueue.changeChecker);
							}
							HashMap<Integer, QueueTicket> diagMap = buildMap(loadedDiags);
							HashMap<Integer, QueueTicket> medMap = buildMap(loadedMeds);
							HashMap<Integer, QueueTicket> doneMap = buildMap(loadedDones);
							
							Table ui = null;
							if (!diagQueue.patientViewers.isEmpty()) {
								ui = diagQueue.patientViewers.getFirst().getTable();
							} else if (!medQueue.patientViewers.isEmpty()) {
								ui = medQueue.patientViewers.getFirst().getTable();
							} else if (!doneQueue.patientViewers.isEmpty()) {
								ui = doneQueue.patientViewers.getFirst().getTable();
							}
							if (ui != null && !ui.isDisposed()) {
								ui.getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										diagQueue.applyInput(true, loadedDiags, diagMap);
										medQueue.applyInput(true, loadedMeds, medMap);
										doneQueue.applyInput(true, loadedDones, doneMap);
									}
								});
							} else {
								diagQueue.applyInput(false, loadedDiags, diagMap);
								medQueue.applyInput(false, loadedMeds, medMap);
								doneQueue.applyInput(false, loadedDones, doneMap);
							}*/
							
							if (queueListener != null) {
								queueListener.run();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			
		}.start();
	}
	
	private UnaryOperator<QueueTicket> changeChecker = new UnaryOperator<QueueTicket>() {
		
		@Override
		public QueueTicket apply(QueueTicket q) {
			QueueTicket existingItem = patientMap.get(q.patient.id.getValue());
			if (existingItem == null) {
				return q;
			}
			existingItem.copy(q);
			return existingItem;
		}
	};
	
	public static HashMap<Integer, QueueTicket> buildMap(LinkedList<QueueTicket> loadedPatients) {
		HashMap<Integer, QueueTicket> map = new HashMap<Integer, QueueTicket>();
		for (QueueTicket q : loadedPatients) {
			map.put(q.patient.id.getValue(), q);
		}
		return map;
	}
	
	private void applyInput(LinkedList<QueueTicket> queueAllStages) {
		LinkedList<QueueTicket> loadedTickets = new LinkedList<QueueTicket>();
		Iterator<QueueTicket> it = queueAllStages.iterator();
		while (it.hasNext()) {
			QueueTicket q = it.next();
			if (q.stage == assignedState) {
				it.remove();
				loadedTickets.add(q);
			}
		}
		
		//diags
		synchronized (mapLock) {
			loadedTickets.replaceAll(changeChecker);
			HashMap<Integer, QueueTicket> ticketMap = buildMap(loadedTickets);
		
			patientList = loadedTickets;
			patientMap = ticketMap;
		
			if (patientViewers.isEmpty()) {
				return;
			}
			Table ui = patientViewers.getFirst().getTable();
			ui.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (TableViewer viewer : patientViewers) {
						if (viewer.getTable().isDisposed()) {
							continue;
						}
						synchronized (mapLock) {
							refreshViewersWithMapSynchronized();
						}
					}
				}
			});
		}
	}
	
	/*private void applyInput(boolean updateUI, 
			LinkedList<QueueTicket> newList, HashMap<Integer, QueueTicket> newMap) {
		synchronized (mapLock) {
			patientList = newList;
			patientMap = newMap;
			//if (patientViewers == null) {
			//	return;
			//}
			for (TableViewer viewer : patientViewers) {
				if (viewer.getTable().isDisposed()) {
					continue;
				}
				QueueTicket sel = (QueueTicket) viewer.getStructuredSelection().getFirstElement();
				viewer.setInput(patientList.toArray());
				if (sel != null) {
					QueueTicket q = patientMap.get(sel.diag.patient.id.getValue());
					if (q != null) {
						viewer.setSelection(new StructuredSelection(q));
					}
				}
			}
		}
	}*/
	
	private void refreshViewersWithMapSynchronized() {
		for (TableViewer viewer : patientViewers) {
			QueueTicket sel = (QueueTicket) viewer.getStructuredSelection().getFirstElement();
			//viewer.refresh();
			if (assignedState == QueueStage.begin) {
				ArrayList<QueueTicket> list = new ArrayList<QueueTicket>();
				list.addAll(diagHoldQueue.patientList);
				list.addAll(patientList);
				viewer.setInput(list.toArray());
			} else {
				viewer.setInput(patientList.toArray());
			}
			if (sel != null) {
				//viewer.setSelection(new StructuredSelection(sel));
				
				//sel maybe already removed
				//already synchronized on mapLock
				QueueTicket q = patientMap.get(sel.patient.id.getValue());
				if (q != null) {
					viewer.setSelection(new StructuredSelection(q));
				}
			}
		}
	}
	
	private void setViewersSelection(QueueTicket patient) {
		if (patientViewers.isEmpty() || patient == null) {
			return;
		}
		for (TableViewer viewer : patientViewers) {
			viewer.setSelection(new StructuredSelection(patient));
		}
	}
	
	/*public static void selectFirst() {
		if (viewer == null) {
			return;
		}
		viewer.getTable().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				synchronized (patientMap) {
					QueuedPatient first = patientList.getFirst();
					if (first != null) {
						viewer.setSelection(new StructuredSelection(first));
					}
				}
			}
		});
	}*/
}
