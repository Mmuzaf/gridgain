/*
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

namespace Apache.Ignite.Core.Cache.Event
{
    /// <summary>
    /// Cache event type.
    /// </summary>
    public enum CacheEntryEventType
    {
        /// <summary>
        /// An event type indicating that the cache entry was created.
        /// </summary>
        Created = 0,

        /// <summary>
        /// An event type indicating that the cache entry was updated. i.e. a previous
        /// mapping existed.
        /// </summary>
        Updated = 1,

        /// <summary>
        /// An event type indicating that the cache entry was removed.
        /// </summary>
        Removed = 2,
    }
}