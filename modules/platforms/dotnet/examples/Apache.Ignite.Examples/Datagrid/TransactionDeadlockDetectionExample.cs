﻿/*
 *                   GridGain Community Edition Licensing
 *                   Copyright 2019 GridGain Systems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License") modified with Commons Clause
 * Restriction; you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * 
 * Commons Clause Restriction
 * 
 * The Software is provided to you by the Licensor under the License, as defined below, subject to
 * the following condition.
 * 
 * Without limiting other conditions in the License, the grant of rights under the License will not
 * include, and the License does not grant to you, the right to Sell the Software.
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you
 * under the License to provide to third parties, for a fee or other consideration (including without
 * limitation fees for hosting or consulting/ support services related to the Software), a product or
 * service whose value derives, entirely or substantially, from the functionality of the Software.
 * Any license notice or attribution required by the License must also include this Commons Clause
 * License Condition notice.
 * 
 * For purposes of the clause above, the “Licensor” is Copyright 2019 GridGain Systems, Inc.,
 * the “License” is the Apache License, Version 2.0, and the Software is the GridGain Community
 * Edition software provided with this notice.
 */

namespace Apache.Ignite.Examples.Datagrid
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Threading;
    using System.Threading.Tasks;
    using Apache.Ignite.Core;
    using Apache.Ignite.Core.Cache;
    using Apache.Ignite.Core.Cache.Configuration;
    using Apache.Ignite.Core.Transactions;

    /// <summary>
    /// This example demonstrates transaction deadlock detection mechanism.
    /// <para />
    /// The feature simplifies debugging of distributed deadlocks that may be caused by your code. 
    /// To enable the feature you should start an Ignite transaction with a non-zero timeout 
    /// and catch TransactionDeadlockException that will contain deadlock details. 
    /// For more information refer to https://apacheignite-net.readme.io/docs/transactions#deadlock-detection-in-pessimistic-transactions
    /// <para />
    /// 1) Build the project Apache.Ignite.ExamplesDll (select it -> right-click -> Build).
    ///    Apache.Ignite.ExamplesDll.dll must appear in %IGNITE_HOME%/platforms/dotnet/examples/Apache.Ignite.ExamplesDll/bin/${Platform]/${Configuration} folder.
    /// 2) Set this class as startup object (Apache.Ignite.Examples project -> right-click -> Properties ->
    ///     Application -> Startup object);
    /// 3) Start example (F5 or Ctrl+F5).
    /// <para />
    /// This example can be run with standalone Apache Ignite.NET node:
    /// 1) Run %IGNITE_HOME%/platforms/dotnet/bin/Apache.Ignite.exe:
    /// Apache.Ignite.exe -configFileName=platforms\dotnet\examples\apache.ignite.examples\app.config -assembly=[path_to_Apache.Ignite.ExamplesDll.dll]
    /// 2) Start example.
    /// </summary>
    public class TransactionDeadlockDetectionExample
    {
        /// <summary>Cache name.</summary>
        private const string CacheName = "dotnet_cache_tx_deadlock";

        /// <summary>
        /// Runs the example.
        /// </summary>
        [STAThread]
        public static void Main()
        {
            using (var ignite = Ignition.StartFromApplicationConfiguration())
            {
                Console.WriteLine();
                Console.WriteLine(">>> Transaction deadlock detection example started.");

                var cache = ignite.GetOrCreateCache<int, int>(new CacheConfiguration
                {
                    Name = CacheName,
                    AtomicityMode = CacheAtomicityMode.Transactional
                });

                // Clean up caches on all nodes before run.
                cache.Clear();

                var keys = Enumerable.Range(1, 100).ToArray();

                // Modify keys in reverse order to cause a deadlock.
                var task1 = Task.Factory.StartNew(() => UpdateKeys(cache, keys, 1));
                var task2 = Task.Factory.StartNew(() => UpdateKeys(cache, keys.Reverse(), 2));

                Task.WaitAll(task1, task2);

                Console.WriteLine("\n>>> Example finished, press any key to exit ...");
                Console.ReadKey();
            }
        }

        /// <summary>
        /// Updates the specified keys.
        /// </summary>
        private static void UpdateKeys(ICache<int, int> cache, IEnumerable<int> keys, int threadId)
        {
            var txs = cache.Ignite.GetTransactions();

            try
            {
                using (var tx = txs.TxStart(TransactionConcurrency.Pessimistic, TransactionIsolation.ReadCommitted,
                    TimeSpan.FromSeconds(2), 0))
                {
                    foreach (var key in keys)
                    {
                        cache[key] = threadId;
                    }

                    // Introduce a delay to ensure lock conflict.
                    Thread.Sleep(TimeSpan.FromSeconds(3));

                    tx.Commit();
                }
            }
            catch (TransactionDeadlockException e)
            {
                // Print out detected deadlock details (participating nodes, keys, etc):
                Console.WriteLine("\n>>> Transaction deadlock in thread {0}: {1}", threadId, e.Message);
            }
            catch (Exception e)
            {
                Console.WriteLine("\n>>> Update failed in thread {0}: {1}", threadId, e);
            }
        }
    }
}
