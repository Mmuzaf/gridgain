﻿/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 * 
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

namespace Apache.Ignite.Core.Impl.Cache.Expiry
{
    using Apache.Ignite.Core.Cache.Expiry;
    using Apache.Ignite.Core.Common;

    /// <summary>
    /// Factory.
    /// </summary>
    internal class ExpiryPolicyFactory : IFactory<IExpiryPolicy>
    {
        /** */
        private readonly IExpiryPolicy _expiryPolicy;

        /// <summary>
        /// Initializes a new instance of the <see cref="ExpiryPolicyFactory"/> class.
        /// </summary>
        /// <param name="expiryPolicy">The expiry policy.</param>
        public ExpiryPolicyFactory(IExpiryPolicy expiryPolicy)
        {
            _expiryPolicy = expiryPolicy;
        }

        /** <inheritdoc /> */
        public IExpiryPolicy CreateInstance()
        {
            return _expiryPolicy;
        }
    }
}
