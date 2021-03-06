/*
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
package org.apache.reef.wake.remote.impl;

import org.apache.reef.tang.annotations.Parameter;
import org.apache.reef.wake.EventHandler;
import org.apache.reef.wake.remote.Codec;
import org.apache.reef.wake.remote.RemoteConfiguration;
import org.apache.reef.wake.remote.RemoteManager;
import org.apache.reef.wake.remote.RemoteManagerFactory;
import org.apache.reef.wake.remote.address.LocalAddressProvider;
import org.apache.reef.wake.remote.ports.TcpPortProvider;

import javax.inject.Inject;

/**
 * Default implementation of RemoteManagerFactory.
 */
public class DefaultRemoteManagerFactory implements RemoteManagerFactory {

  private final Codec<?> codec;
  private final EventHandler<Throwable> errorHandler;
  private final boolean orderingGuarantee;
  private final int numberOfTries;
  private final int retryTimeout;
  private final LocalAddressProvider localAddressProvider;
  private final TcpPortProvider tcpPortProvider;

  @Inject
  private DefaultRemoteManagerFactory(
      final @Parameter(RemoteConfiguration.MessageCodec.class) Codec<?> codec,
      final @Parameter(RemoteConfiguration.ErrorHandler.class) EventHandler<Throwable> errorHandler,
      final @Parameter(RemoteConfiguration.OrderingGuarantee.class) boolean orderingGuarantee,
      final @Parameter(RemoteConfiguration.NumberOfTries.class) int numberOfTries,
      final @Parameter(RemoteConfiguration.RetryTimeout.class) int retryTimeout,
      final LocalAddressProvider localAddressProvider,
      final TcpPortProvider tcpPortProvider) {
    this.codec = codec;
    this.errorHandler = errorHandler;
    this.orderingGuarantee = orderingGuarantee;
    this.numberOfTries = numberOfTries;
    this.retryTimeout = retryTimeout;
    this.localAddressProvider = localAddressProvider;
    this.tcpPortProvider = tcpPortProvider;
  }

  @Override
  public RemoteManager getInstance(final String name) {
    return new DefaultRemoteManagerImplementation(name,
        DefaultRemoteManagerImplementation.UNKNOWN_HOST_NAME, // Indicate to use the localAddressProvider
        0, // Indicate to use the tcpPortProvider
        this.codec,
        this.errorHandler,
        this.orderingGuarantee,
        this.numberOfTries,
        this.retryTimeout,
        this.localAddressProvider,
        this.tcpPortProvider);
  }


  @Override
  public <T> RemoteManager getInstance(final String name,
                                       final String hostAddress,
                                       final int listeningPort,
                                       final Codec<T> codec,
                                       final EventHandler<Throwable> errorHandler,
                                       final boolean orderingGuarantee,
                                       final int numberOfTries,
                                       final int retryTimeout,
                                       final LocalAddressProvider localAddressProvider,
                                       final TcpPortProvider tcpPortProvider) {
    return new DefaultRemoteManagerImplementation(name,
        hostAddress,
        listeningPort,
        codec,
        errorHandler,
        orderingGuarantee,
        numberOfTries,
        retryTimeout,
        localAddressProvider,
        tcpPortProvider);
  }

  @Override
  public <T> RemoteManager getInstance(final String name,
                                       final String hostAddress,
                                       final int listeningPort,
                                       final Codec<T> codec,
                                       final EventHandler<Throwable> errorHandler,
                                       final boolean orderingGuarantee,
                                       final int numberOfTries,
                                       final int retryTimeout) {
    return new DefaultRemoteManagerImplementation(name,
        hostAddress,
        listeningPort,
        codec,
        errorHandler,
        orderingGuarantee,
        numberOfTries,
        retryTimeout,
        this.localAddressProvider,
        this.tcpPortProvider);

  }

  @Override
  public <T> RemoteManager getInstance(String name, Codec<T> codec, EventHandler<Throwable> errorHandler) {
    return new DefaultRemoteManagerImplementation(name,
        DefaultRemoteManagerImplementation.UNKNOWN_HOST_NAME, // Indicate to use the localAddressProvider
        0, // Indicate to use the tcpPortProvider,
        codec,
        errorHandler,
        this.orderingGuarantee,
        this.numberOfTries,
        this.retryTimeout,
        this.localAddressProvider,
        this.tcpPortProvider);
  }

  @Override
  public <T> RemoteManager getInstance(final String name,
                                       final int listeningPort,
                                       final Codec<T> codec,
                                       final EventHandler<Throwable> errorHandler) {
    return new DefaultRemoteManagerImplementation(name,
        DefaultRemoteManagerImplementation.UNKNOWN_HOST_NAME, // Indicate to use the localAddressProvider
        listeningPort,
        codec,
        errorHandler,
        this.orderingGuarantee,
        this.numberOfTries,
        this.retryTimeout,
        this.localAddressProvider,
        this.tcpPortProvider);
  }
}
