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

namespace Apache.Ignite.Core.Tests.Memory
{
    using System;
    using Apache.Ignite.Core.Impl.Memory;
    using NUnit.Framework;

    /// <summary>
    /// Tests for interop memory.
    /// </summary>
    public class InteropMemoryTest
    {
        /// <summary>
        /// Test pooled memory.
        /// </summary>
        [Test]
        public void TestPooled()
        {
            PlatformMemoryManager mgr = new PlatformMemoryManager(256);

            var mem1 = mgr.Allocate();
            Assert.IsTrue(mem1 is PlatformPooledMemory);
            Assert.IsTrue(mem1.Capacity >= 256);
            Assert.IsTrue(mem1.Pointer > 0);
            Assert.IsTrue(mem1.Data > 0);
            Assert.AreEqual(0, mem1.Length);

            mem1.Reallocate(512);

            Assert.IsTrue(mem1.Capacity >= 512);
            Assert.IsTrue(mem1.Pointer > 0);
            Assert.IsTrue(mem1.Data > 0);
            Assert.AreEqual(0, mem1.Length);

            mem1.Length = 128;
            Assert.AreEqual(128, mem1.Length);

            mem1.Release();

            Assert.AreSame(mem1, mgr.Allocate());
            Assert.IsTrue(mem1.Capacity >= 512);
            Assert.IsTrue(mem1.Pointer > 0);
            Assert.IsTrue(mem1.Data > 0);
            Assert.AreEqual(128, mem1.Length);

            IPlatformMemory mem2 = mgr.Allocate();
            Assert.IsTrue(mem2 is PlatformPooledMemory);

            IPlatformMemory mem3 = mgr.Allocate();
            Assert.IsTrue(mem3 is PlatformPooledMemory);

            mem1.Release();
            Assert.AreSame(mem1, mgr.Allocate());

            mem2.Release();
            Assert.AreSame(mem2, mgr.Allocate());

            mem3.Release();
            Assert.AreSame(mem3, mgr.Allocate());

            mem1.Release();
            mem2.Release();

            Assert.AreSame(mem1, mgr.Allocate());
            Assert.AreSame(mem2, mgr.Allocate());

            IPlatformMemory unpooled = mgr.Allocate();

            try
            {
                Assert.IsTrue(unpooled is PlatformUnpooledMemory);
            }
            finally
            {
                unpooled.Release();
            }
        }

        /// <summary>
        /// Test unpooled memory.
        /// </summary>
        [Test]
        public void TestUnpooled()
        {
            PlatformMemoryManager mgr = new PlatformMemoryManager(256);

            for (int i = 0; i < 3; i++)
                mgr.Allocate();

            IPlatformMemory mem1 = mgr.Allocate();
            Assert.IsTrue(mem1 is PlatformUnpooledMemory);
            Assert.IsTrue(mem1.Capacity >= 256);
            Assert.IsTrue(mem1.Pointer > 0);
            Assert.IsTrue(mem1.Data > 0);
            Assert.AreEqual(0, mem1.Length);

            mem1.Reallocate(512);
            Assert.IsTrue(mem1.Capacity >= 512);
            Assert.IsTrue(mem1.Pointer > 0);
            Assert.IsTrue(mem1.Data > 0);
            Assert.AreEqual(0, mem1.Length);

            mem1.Length = 128;
            Assert.AreEqual(128, mem1.Length);

            mem1.Release();

            IPlatformMemory mem2 = mgr.Allocate();
            Assert.AreNotSame(mem1, mem2);
            Assert.IsTrue(mem2.Capacity >= 256);
            Assert.IsTrue(mem2.Pointer > 0);
            Assert.IsTrue(mem2.Data > 0);
            Assert.AreEqual(0, mem2.Length);

            mem2.Release();
        }

        /// <summary>
        /// Test pooled memory stream reallocation initiated from stream.
        /// </summary>
        [Test]
        public void TestPooledStreamReallocate()
        {
            var mem = new PlatformMemoryManager(256).Allocate();

            Assert.IsTrue(mem is PlatformPooledMemory);

            CheckStreamReallocate(mem);
        }

        /// <summary>
        /// Test unpooled memory stream reallocation initiated from stream.
        /// </summary>
        [Test]
        public void TestUnpooledStreamReallocate()
        {
            PlatformMemoryManager mgr = new PlatformMemoryManager(256);

            for (int i = 0; i < 3; i++)
                mgr.Allocate();

            var mem = mgr.Allocate();

            Assert.IsTrue(mem is PlatformUnpooledMemory);

            CheckStreamReallocate(mem);
        }

        /// <summary>
        /// Check stream reallocation.
        /// </summary>
        /// <param name="mem">Memory.</param>
        private void CheckStreamReallocate(IPlatformMemory mem)
        {
            Assert.IsTrue(mem.Capacity >= 256);

            int dataLen = 2048 + 13;

            Random rand = new Random();

            byte[] data = new byte[dataLen];

            for (int i = 0; i < data.Length; i++)
                data[i] = (byte)rand.Next(0, 255);

            using (var stream = mem.GetStream())
            {
                stream.WriteByteArray(data);

                stream.SynchronizeOutput();

                Assert.IsTrue(mem.Capacity >= dataLen);

                stream.Reset();

                stream.SynchronizeInput();

                byte[] data0 = stream.ReadByteArray(dataLen);

                Assert.AreEqual(data, data0);
            }
        }
    }
}
