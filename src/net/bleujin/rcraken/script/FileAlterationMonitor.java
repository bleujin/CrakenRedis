/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bleujin.rcraken.script;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.monitor.FileAlterationObserver;

/**
 * It's a runnable that spawns of a monitoring thread triggering the the observers and managing the their listeners.
 */
public final class FileAlterationMonitor {

	private final long interval;
	private final List<FileAlterationObserver> observers;
	private ScheduledExecutorService ses;
	private transient boolean running = true;

	public FileAlterationMonitor(long interval, ScheduledExecutorService ses, FileAlterationObserver first, FileAlterationObserver... observers) {
		this.ses = ses;
		this.observers = new CopyOnWriteArrayList<FileAlterationObserver>();
		this.interval = interval;
		addObserver(first);
		for (FileAlterationObserver observer : observers) {
			addObserver(observer);
		}
	}

	public long getInterval() {
		return interval;
	}

	public void addObserver(FileAlterationObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(FileAlterationObserver observer) {
		while (observers.remove(observer))
			;
	}

	public Iterable getObservers() {
		return observers;
	}

	public synchronized void start() throws Exception {

		Callable<Void> sjob = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					for (FileAlterationObserver o : observers) {
						o.checkAndNotify();
					}
				} catch(Exception e){
					e.printStackTrace(); 
					throw e ;
				} finally {
					ses.schedule(this, interval, TimeUnit.MILLISECONDS);
				}
				return null;
			}
		};
		ses.schedule(sjob, interval, TimeUnit.MILLISECONDS);
	}

	public synchronized void stop() throws Exception {
		this.running = false;
	}

}
