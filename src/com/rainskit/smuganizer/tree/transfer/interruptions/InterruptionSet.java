package com.rainskit.smuganizer.tree.transfer.interruptions;

import java.util.ArrayList;

public class InterruptionSet {
	private ArrayList<TransferInterruption> interruptions;
	
	public InterruptionSet() {
		interruptions = new ArrayList<TransferInterruption>();
	}

	public void add(TransferInterruption newInterruption) {
		interruptions.add(newInterruption);
	}

	public TransferInterruption getLastInterruption() {
		if (interruptions.isEmpty()) {
			return null;
		} else {
			return interruptions.get(interruptions.size() - 1);
		}
	}

	public boolean hasInterruption(Class<? extends TransferInterruption> interruptionClass) {
		return (getInterruption(interruptionClass) != null);
	}

	public TransferInterruption getInterruption(Class<? extends TransferInterruption> interruptionClass) {
		for (TransferInterruption each : interruptions) {
			if (interruptionClass.isInstance(each)) {
				return each;
			}
		}
		return null;
	}
}
