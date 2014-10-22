/**
 * Copyright (C) 2014 Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.reef.tests.fail.task;

import com.microsoft.reef.task.Task;
import com.microsoft.reef.task.events.CloseEvent;
import com.microsoft.reef.task.events.TaskStop;
import com.microsoft.reef.tests.library.exceptions.SimulatedTaskFailure;
import com.microsoft.tang.annotations.Unit;
import com.microsoft.wake.EventHandler;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A basic task that just fails when we stop it.
 */
@Unit
public final class FailTaskStop implements Task, EventHandler<TaskStop> {

  private static final Logger LOG = Logger.getLogger(FailTaskStop.class.getName());

  private transient boolean isRunning = true;

  @Inject
  public FailTaskStop() {
    LOG.fine("FailTaskStop created.");
  }

  @Override
  public byte[] call(final byte[] memento) {
    synchronized (this) {
      LOG.fine("FailTaskStop.call() invoked. Waiting for the message.");
      while (this.isRunning) {
        try {
          this.wait();
        } catch (final InterruptedException ex) {
          LOG.log(Level.WARNING, "wait() interrupted.", ex);
        }
      }
    }
    return null;
  }

  @Override
  public void onNext(final TaskStop event) throws SimulatedTaskFailure {
    final SimulatedTaskFailure ex = new SimulatedTaskFailure("FailTaskStop.send() invoked.");
    LOG.log(Level.FINE, "FailTaskStop.onNext() invoked. Raise exception: {0}", ex.toString());
    throw ex;
  }

  public final class CloseEventHandler implements EventHandler<CloseEvent> {
    @Override
    public void onNext(final CloseEvent event) {
      LOG.log(Level.FINEST, "FailTaskStop.CloseEventHandler.onNext() invoked: {0}", event);
      synchronized (FailTaskStop.this) {
        isRunning = false;
        FailTaskStop.this.notify();
      }
    }
  }
}