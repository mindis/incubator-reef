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
using Org.Apache.REEF.Common.Tasks;
using Org.Apache.REEF.Network.Group.Config;
using Org.Apache.REEF.Network.Group.Driver.Impl;
using Org.Apache.REEF.Network.NetworkService;
using Org.Apache.REEF.Tang.Annotations;
using Org.Apache.REEF.Tang.Formats;
using Org.Apache.REEF.Tang.Implementations.Tang;
using Org.Apache.REEF.Tang.Interface;
using Org.Apache.REEF.Tang.Util;
using Org.Apache.REEF.Wake.Remote.Impl;

namespace Org.Apache.REEF.Network.Group.Task.Impl
{
    /// <summary>
    /// Used by Tasks to fetch CommunicationGroupClients.
    /// </summary>
    public class GroupCommClient : IGroupCommClient
    {
        private readonly Dictionary<string, ICommunicationGroupClient> _commGroups;

        private readonly INetworkService<GroupCommunicationMessage> _networkService;

        /// <summary>
        /// Creates a new GroupCommClient and registers the task ID with the Name Server.
        /// </summary>
        /// <param name="groupConfigs">The set of serialized Group Communication configurations</param>
        /// <param name="taskId">The identifier for this task</param>
        /// <param name="groupCommNetworkObserver">The network handler to receive incoming messages
        /// for this task</param>
        /// <param name="networkService">The network service used to send messages</param>
        /// <param name="configSerializer">Used to deserialize Group Communication configuration</param>
        [Inject]
        public GroupCommClient(
            [Parameter(typeof(GroupCommConfigurationOptions.SerializedGroupConfigs))] ISet<string> groupConfigs,
            [Parameter(typeof(TaskConfigurationOptions.Identifier))] string taskId,
            IGroupCommNetworkObserver groupCommNetworkObserver,
            NetworkService<GroupCommunicationMessage> networkService,
            AvroConfigurationSerializer configSerializer)
        {
            _commGroups = new Dictionary<string, ICommunicationGroupClient>();
            _networkService = networkService;
            networkService.Register(new StringIdentifier(taskId));

            foreach (string serializedGroupConfig in groupConfigs)
            {
                IConfiguration groupConfig = configSerializer.FromString(serializedGroupConfig);

                IInjector injector = TangFactory.GetTang().NewInjector(groupConfig);
                injector.BindVolatileParameter(GenericType<TaskConfigurationOptions.Identifier>.Class, taskId);
                injector.BindVolatileInstance(GenericType<IGroupCommNetworkObserver>.Class, groupCommNetworkObserver);
                injector.BindVolatileInstance(GenericType<NetworkService<GroupCommunicationMessage>>.Class, networkService);

                ICommunicationGroupClient commGroup = injector.GetInstance<ICommunicationGroupClient>();
                _commGroups[commGroup.GroupName] = commGroup;
            }
        }

        /// <summary>
        /// Gets the CommunicationGroupClient for the given group name.
        /// </summary>
        /// <param name="groupName">The name of the CommunicationGroupClient</param>
        /// <returns>The CommunicationGroupClient</returns>
        public ICommunicationGroupClient GetCommunicationGroup(string groupName)
        {
            if (string.IsNullOrEmpty(groupName))
            {
                throw new ArgumentNullException("groupName");
            }
            if (!_commGroups.ContainsKey(groupName))
            {
                throw new ArgumentException("No CommunicationGroupClient with name: " + groupName);
            }

            return _commGroups[groupName];
        }

        /// <summary>
        /// Disposes of the GroupCommClient's services.
        /// </summary>
        public void Dispose()
        {
            _networkService.Unregister();
            _networkService.Dispose();
        }
    }
}
