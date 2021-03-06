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
package org.apache.reef.runtime.yarn.client;

import org.apache.reef.annotations.audience.ClientSide;
import org.apache.reef.annotations.audience.Public;
import org.apache.reef.client.parameters.DriverConfigurationProviders;
import org.apache.reef.tang.ConfigurationProvider;
import org.apache.reef.runtime.common.client.CommonRuntimeConfiguration;
import org.apache.reef.runtime.common.client.api.JobSubmissionHandler;
import org.apache.reef.runtime.common.files.RuntimeClasspathProvider;
import org.apache.reef.runtime.common.parameters.JVMHeapSlack;
import org.apache.reef.runtime.yarn.YarnClasspathProvider;
import org.apache.reef.runtime.yarn.client.parameters.JobPriority;
import org.apache.reef.runtime.yarn.client.parameters.JobQueue;
import org.apache.reef.runtime.yarn.util.YarnConfigurationConstructor;
import org.apache.reef.tang.formats.ConfigurationModule;
import org.apache.reef.tang.formats.ConfigurationModuleBuilder;
import org.apache.reef.tang.formats.OptionalImpl;
import org.apache.reef.tang.formats.OptionalParameter;
import org.apache.reef.util.logging.LoggingSetup;
import org.apache.reef.wake.remote.address.LocalAddressProvider;
import org.apache.reef.wake.remote.ports.TcpPortProvider;

/**
 * A ConfigurationModule for the YARN resourcemanager.
 */
@Public
@ClientSide
public class YarnClientConfiguration extends ConfigurationModuleBuilder {
  static {
    LoggingSetup.setupCommonsLogging();
  }

  public static final OptionalParameter<String> YARN_QUEUE_NAME = new OptionalParameter<>();
  public static final OptionalParameter<Integer> YARN_PRIORITY = new OptionalParameter<>();

  public static final OptionalParameter<Double> JVM_HEAP_SLACK = new OptionalParameter<>();

  /**
   * Configuration provides whose Configuration will be merged into all Driver Configuration.
   */
  public static final OptionalImpl<ConfigurationProvider> DRIVER_CONFIGURATION_PROVIDERS = new OptionalImpl<>();

  /**
   * The class used to restrict tcp port ranges for listening
   * Note that you will likely want to bind the same class also to DRIVER_CONFIGURATION_PROVIDERS to make sure that
   * the Driver (and the Evaluators) also use it.
   */
  public static final OptionalImpl<TcpPortProvider> TCP_PORT_PROVIDER = new OptionalImpl<>();

  /**
   * The class used to resolve the local address for Wake and HTTP to bind to.
   * Note that you will likely want to bind the same class also to DRIVER_CONFIGURATION_PROVIDERS to make sure that
   * the Driver (and the Evaluators) also use it.
   */
  public static final OptionalImpl<LocalAddressProvider> LOCAL_ADDRESS_PROVIDER = new OptionalImpl<>();

  public static final ConfigurationModule CONF = new YarnClientConfiguration()
      .merge(CommonRuntimeConfiguration.CONF)
          // Bind YARN
      .bindImplementation(JobSubmissionHandler.class, YarnJobSubmissionHandler.class)
          // Bind the parameters given by the user
      .bindNamedParameter(JobQueue.class, YARN_QUEUE_NAME)
      .bindNamedParameter(JobPriority.class, YARN_PRIORITY)
      .bindNamedParameter(JVMHeapSlack.class, JVM_HEAP_SLACK)
      .bindImplementation(RuntimeClasspathProvider.class, YarnClasspathProvider.class)
          // Bind external constructors. Taken from  YarnExternalConstructors.registerClientConstructors
      .bindConstructor(org.apache.hadoop.yarn.conf.YarnConfiguration.class, YarnConfigurationConstructor.class)
      .bindSetEntry(DriverConfigurationProviders.class, DRIVER_CONFIGURATION_PROVIDERS)
          // Bind LocalAddressProvider
      .bindImplementation(LocalAddressProvider.class, LOCAL_ADDRESS_PROVIDER)
      .bindImplementation(TcpPortProvider.class, TCP_PORT_PROVIDER)
      .build();

}
