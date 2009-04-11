package com.rainskit.smuganizer.waitcursoreventqueue;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * Suggested serving size:
 * Toolkit.getDefaultToolkit().getSystemEventQueue().push(new WaitCursorEventQueue(70));
 */
public class WaitCursorEventQueue extends EventQueue
	implements DelayTimerCallback {

	private final CursorManager cursorManager;
	private final DelayTimer waitTimer;
	
	public WaitCursorEventQueue(int delay) {
		this.waitTimer = new DelayTimer(this, delay);
		this.cursorManager = new CursorManager(waitTimer);
	}

	public void close() {
		waitTimer.quit();
		pop();
	}

	protected void dispatchEvent(AWTEvent event) {
		cursorManager.push(event.getSource());
		waitTimer.startTimer();
		try {
			super.dispatchEvent(event);
		} finally {
			waitTimer.stopTimer();
			cursorManager.pop();
		}
	}

	public AWTEvent getNextEvent() throws InterruptedException {
		waitTimer.stopTimer(); //started by pop(), this catches modal dialogs 
								//closing that do work afterwards
		return super.getNextEvent();
	}

	public void trigger() {
		cursorManager.setCursor();
	}
}
