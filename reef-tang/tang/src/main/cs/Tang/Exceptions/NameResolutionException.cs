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
﻿using System;

namespace Com.Microsoft.Tang.Exceptions
{
    public class NameResolutionException : BindException
    {
        private static readonly long serialVersionUID = 1L;
        public NameResolutionException(String name, String longestPrefix) :
            base(string.Format("Could not resolve {0}.  Search ended at prefix {1}. This can happen due to typos in class names that are passed as strings, or because Tang uses Assembly loader other than the one that generated the class reference ((make sure you use the full name of a class)",
                name, longestPrefix))
        {
        }

        public NameResolutionException(string message, Exception innerException)
            : base(message, innerException)
        {
        }
    }
}