﻿/**
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

using System;
using System.Collections.Generic;
using System.Threading;
using Org.Apache.REEF.Network.Group.Config;
using Org.Apache.REEF.Network.Group.Driver.Impl;
using Org.Apache.REEF.Tang.Annotations;
using Org.Apache.REEF.Utilities.Diagnostics;
using Org.Apache.REEF.Utilities.Logging;

namespace Org.Apache.REEF.Network.Group.Task.Impl
{
    /// <summary>
    /// Handles incoming messages sent to this Communication Group.
    /// </summary>
    public class CommunicationGroupNetworkObserver : ICommunicationGroupNetworkObserver
    {
        private static readonly Logger LOGGER = Logger.GetLogger(typeof(CommunicationGroupNetworkObserver));
        private readonly Dictionary<string, IObserver<GroupCommunicationMessage>> _handlers;
        private readonly int _retryCount;
        private readonly int _sleepTime;

        /// <summary>
        /// Creates a new CommunicationGroupNetworkObserver.
        /// </summary>
        [Inject]
        public CommunicationGroupNetworkObserver(
            [Parameter(typeof(GroupCommConfigurationOptions.RetryCountWaitingForHanler))] int retryCount,
            [Parameter(typeof(GroupCommConfigurationOptions.SleepTimeWaitingForHandler))] int sleepTime)
        {
            _handlers = new Dictionary<string, IObserver<GroupCommunicationMessage>>();
            _retryCount = retryCount;
            _sleepTime = sleepTime;
        }

        /// <summary>
        /// Registers the handler with the CommunicationGroupNetworkObserver.
        /// Messages that are to be sent to the operator specified by operatorName
        /// are handled by the given observer.
        /// </summary>
        /// <param name="operatorName">The name of the operator whose handler
        /// will be invoked</param>
        /// <param name="observer">The handler to invoke when messages are sent
        /// to the operator specified by operatorName</param>
        public void Register(string operatorName, IObserver<GroupCommunicationMessage> observer)
        {
            if (string.IsNullOrEmpty(operatorName))
            {
                throw new ArgumentNullException("operatorName");
            }
            if (observer == null)
            {
                throw new ArgumentNullException("observer");
            }

            _handlers[operatorName] = observer;
        }

        /// <summary>
        /// Handles the incoming GroupCommunicationMessage sent to this Communication Group.
        /// Looks for the operator that the message is being sent to and invoke its handler.
        /// </summary>
        /// <param name="message">The incoming message</param>
        public void OnNext(GroupCommunicationMessage message)
        {
            string operatorName = message.OperatorName;

            IObserver<GroupCommunicationMessage> handler = GetOperatorHandler(operatorName, _retryCount, _sleepTime);

            if (handler == null)
            {
                Exceptions.Throw(new ArgumentException("No handler registered with the operator name: " + operatorName), LOGGER);
            }
            else
            {
                handler.OnNext(message);
            }
        }

        /// <summary>
        /// GetOperatorHandler for operatorName
        /// </summary>
        /// <param name="operatorName"></param>
        /// <param name="retry"></param>
        /// <param name="sleepTime"></param>
        /// <returns></returns>
        private IObserver<GroupCommunicationMessage> GetOperatorHandler(string operatorName, int retry, int sleepTime)
        {
            //registration of handler might be delayed while the Network Service has received message from other servers
            for (int i = 0; i < retry; i++)
            {
                if (!_handlers.ContainsKey(operatorName))
                {
                    LOGGER.Log(Level.Info, "handler for operator {0} has not been registered." + operatorName);
                    Thread.Sleep(sleepTime);
                }
                else
                {
                    IObserver<GroupCommunicationMessage> handler;
                    if (!_handlers.TryGetValue(operatorName, out handler))
                    {
                        Exceptions.Throw(new ArgumentException("No handler registered yet with the operator name: " + operatorName), LOGGER);
                    }
                    return handler;
                }
            }
            return null;
        }

        public void OnError(Exception error)
        {
        }

        public void OnCompleted()
        {
        }
    }
}
