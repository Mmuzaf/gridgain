/*
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

namespace Apache.Ignite.Core.Tests.Compute
{
    using System;
    using Apache.Ignite.Core.Binary;
    using Apache.Ignite.Core.Compute;
    using NUnit.Framework;

    /// <summary>
    /// Closure execution tests for binary objects.
    /// </summary>
    [TestFixture]
    public class BinarizableClosureTaskTest : ClosureTaskTest
    {
        /// <summary>
        /// Constructor.
        /// </summary>
        public BinarizableClosureTaskTest() : base(false) { }

        /// <summary>
        /// Constructor.
        /// </summary>
        /// <param name="fork">Fork flag.</param>
        protected BinarizableClosureTaskTest(bool fork) : base(fork) { }

        /** <inheritDoc /> */
        protected override IComputeFunc<object> OutFunc(bool err)
        {
            return new BinarizableOutFunc(err);
        }

        /** <inheritDoc /> */
        protected override IComputeFunc<object, object> Func(bool err)
        {
            return new BinarizableFunc(err);
        }

        /** <inheritDoc /> */
        protected override void CheckResult(object res)
        {
            Assert.IsTrue(res != null);

            var res0 = res as BinarizableResult;

            Assert.IsNotNull(res0);
            Assert.AreEqual(1, res0.Res);
        }

        /** <inheritDoc /> */
        protected override void CheckError(Exception err)
        {
            Assert.IsTrue(err != null);

            err = err.InnerException;
            Assert.IsNotNull(err);

            var err0 = err.InnerException as BinarizableException;

            Assert.IsNotNull(err0);
            Assert.AreEqual(ErrMsg, err0.Msg);
        }

        /// <summary>
        /// 
        /// </summary>
        private class BinarizableOutFunc : IComputeFunc<object>
        {
            /** Error. */
            private readonly bool _err;

            /// <summary>
            /// 
            /// </summary>
            /// <param name="err"></param>
            public BinarizableOutFunc(bool err)
            {
                _err = err;
            }
            
            /** <inheritDoc /> */
            public object Invoke()
            {
                if (_err)
                    throw new BinarizableException(ErrMsg);
                return new BinarizableResult(1);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        private class BinarizableFunc : IComputeFunc<object, object>
        {
            /** Error. */
            private readonly bool _err;

            /// <summary>
            /// 
            /// </summary>
            /// <param name="err"></param>
            public BinarizableFunc(bool err)
            {
                _err = err;
            }
            
            /** <inheritDoc /> */
            public object Invoke(object arg)
            {
                if (_err)
                    throw new BinarizableException(ErrMsg);
                return new BinarizableResult(1);
            }
        }

        /// <summary>
        /// 
        /// </summary>
        private class BinarizableException : Exception, IBinarizable
        {
            /** */
            public string Msg;

            /// <summary>
            /// 
            /// </summary>
            /// <param name="msg"></param>
            public BinarizableException(string msg)
            {
                Msg = msg;
            }

            /** <inheritDoc /> */
            public void WriteBinary(IBinaryWriter writer)
            {
                writer.GetRawWriter().WriteString(Msg);
            }

            /** <inheritDoc /> */
            public void ReadBinary(IBinaryReader reader)
            {
                Msg = reader.GetRawReader().ReadString();
            }
        }

        /// <summary>
        /// 
        /// </summary>
        private class BinarizableResult
        {
            /** */
            public readonly int Res;

            /// <summary>
            /// 
            /// </summary>
            /// <param name="res"></param>
            public BinarizableResult(int res)
            {
                Res = res;
            }
        }
    }
}
