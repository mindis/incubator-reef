/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.reef.wake.rx.impl;

import org.apache.reef.wake.rx.Observer;
import org.apache.reef.wake.rx.Subject;

import javax.inject.Inject;

/**
 * A Subject that relays all messages to its subscribers.
 *
 * @param <T>
 */
public class SimpleSubject<T> implements Subject<T, T> {

  private final Observer<T> observer;

  /**
   * Constructs a simple subject
   *
   * @param observer the observer
   */
  @Inject
  public SimpleSubject(Observer<T> observer) {
    this.observer = observer;
  }

  /**
   * Provides the observer with the new value
   *
   * @param value the new value
   */
  @Override
  public void onNext(T value) {
    this.observer.onNext(value);
  }

  /**
   * Provides the observer with the error
   *
   * @param error the error
   */
  @Override
  public void onError(Exception error) {
    this.observer.onError(error);
  }

  /**
   * Provides the observer with it has finished sending push-based
   * notifications.
   */
  @Override
  public void onCompleted() {
    this.observer.onCompleted();
  }
}
