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

namespace Apache.Ignite.Core.Impl.Binary
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using Apache.Ignite.Core.Log;

    /// <summary>
    /// Provides mapping between Java and .NET basic types.
    /// </summary>
    internal static class JavaTypes
    {
        /** */
        private static readonly Dictionary<Type, string> NetToJava = new Dictionary<Type, string>
        {
            {typeof (bool), "java.lang.Boolean"},
            {typeof (byte), "java.lang.Byte"},
            {typeof (sbyte), "java.lang.Byte"},
            {typeof (short), "java.lang.Short"},
            {typeof (ushort), "java.lang.Short"},
            {typeof (char), "java.lang.Character"},
            {typeof (int), "java.lang.Integer"},
            {typeof (uint), "java.lang.Integer"},
            {typeof (long), "java.lang.Long"},
            {typeof (ulong), "java.lang.Long"},
            {typeof (float), "java.lang.Float"},
            {typeof (double), "java.lang.Double"},
            {typeof (string), "java.lang.String"},
            {typeof (decimal), "java.math.BigDecimal"},
            {typeof (Guid), "java.util.UUID"},
            {typeof (DateTime), "java.sql.Timestamp"}
        };

        /** */
        private static readonly Dictionary<Type, Type> IndirectMappingTypes = new Dictionary<Type, Type>
        {
            {typeof (sbyte), typeof (byte)},
            {typeof (ushort), typeof (short)},
            {typeof (uint), typeof (int)},
            {typeof (ulong), typeof (long)}
        };

        /** */
        private static readonly Dictionary<string, Type> JavaToNet =
            NetToJava.GroupBy(x => x.Value).ToDictionary(g => g.Key, g => g.First().Key);

        /** */
        private static readonly Dictionary<string, string> JavaPrimitiveToType = new Dictionary<string, string>
        {
            {"boolean", "java.lang.Boolean"},
            {"byte", "java.lang.Byte"},
            {"short", "java.lang.Short"},
            {"char", "java.lang.Character"},
            {"int", "java.lang.Integer"},
            {"long", "java.lang.Long"},
            {"float", "java.lang.Float"},
            {"double", "java.lang.Double"},
        };

        /// <summary>
        /// Gets the corresponding Java type name.
        /// </summary>
        public static string GetJavaTypeName(Type type)
        {
            if (type == null)
                return null;

            // Unwrap nullable.
            type = Nullable.GetUnderlyingType(type) ?? type;

            string res;

            return NetToJava.TryGetValue(type, out res) ? res : null;
        }

        /// <summary>
        /// Logs a warning for indirectly mapped types.
        /// </summary>
        public static void LogIndirectMappingWarning(Type type, ILogger log, string logInfo)
        {
            if (type == null)
                return;

            var directType = GetDirectlyMappedType(type);

            if (directType == type)
                return;

            log.Warn("{0}: Type '{1}' maps to Java type '{2}' using unchecked conversion. " +
                     "This may cause issues in SQL queries. " +
                     "You can use '{3}' instead to achieve direct mapping.",
                logInfo, type, NetToJava[type], directType);
        }

        /// <summary>
        /// Gets the compatible type that maps directly to Java.
        /// </summary>
        public static Type GetDirectlyMappedType(Type type)
        {
            // Unwrap nullable.
            var unwrapType = Nullable.GetUnderlyingType(type) ?? type;

            Type directType;

            return IndirectMappingTypes.TryGetValue(unwrapType, out directType) ? directType : type;
        }

        /// <summary>
        /// Gets .NET type that corresponds to specified Java type name.
        /// </summary>
        /// <param name="javaTypeName">Name of the java type.</param>
        /// <returns></returns>
        public static Type GetDotNetType(string javaTypeName)
        {
            if (string.IsNullOrEmpty(javaTypeName))
                return null;

            string fullJavaTypeName;

            JavaPrimitiveToType.TryGetValue(javaTypeName, out fullJavaTypeName);

            Type res;

            return JavaToNet.TryGetValue(fullJavaTypeName ?? javaTypeName, out res) ? res : null;
        }
    }
}
