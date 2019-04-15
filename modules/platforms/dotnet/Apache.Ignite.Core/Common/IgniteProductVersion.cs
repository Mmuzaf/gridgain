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

namespace Apache.Ignite.Core.Common
{
    using System;
    using System.Diagnostics.CodeAnalysis;
    using Apache.Ignite.Core.Binary;
    using Apache.Ignite.Core.Impl.Binary;
    using Apache.Ignite.Core.Impl.Common;

    /// <summary>
    /// Represents node version.
    /// </summary>
    [SuppressMessage("Microsoft.Design", "CA1036:OverrideMethodsOnComparableTypes")]
    public class IgniteProductVersion : IEquatable<IgniteProductVersion>, IComparable<IgniteProductVersion>
    {
        /** Major version number. */
        private readonly byte _major;

        /** Minor version number. */
        private readonly byte _minor;

        /** Maintenance version number. */
        private readonly byte _maintenance;

        /** Stage of development. */
        private readonly string _stage;

        /** Release date. */
        private readonly DateTime _releaseDate;

        /** Revision hash. */
        private readonly byte[] _revHash;

        /// <summary>
        /// Constructor.
        /// </summary>
        /// <param name="major">Major version number.</param>
        /// <param name="minor">Minor version number.</param>
        /// <param name="maintenance">Maintenance version number.</param>
        /// <param name="stage">Stage of development.</param>
        /// <param name="releaseDate">Revision date.</param>
        /// <param name="revHash">Revision hash.</param>
        public IgniteProductVersion(byte major, byte minor, byte maintenance, string stage, DateTime releaseDate, byte[] revHash)
        {
            if (revHash != null && revHash.Length != 20)
            {
                throw new ArgumentException("Invalid length for SHA1 hash (must be 20): " + revHash.Length);
            }

            _major = major;
            _minor = minor;
            _maintenance = maintenance;
            _stage = stage;
            _releaseDate = releaseDate;
            _revHash = revHash;
        }

        /// <summary>
        /// Constructor.
        /// </summary>
        /// <param name="reader"><see cref="IBinaryRawReader"/></param>
        internal IgniteProductVersion(IBinaryRawReader reader)
        {
            IgniteArgumentCheck.NotNull(reader, "reader");

            _major = reader.ReadByte();
            _minor = reader.ReadByte();
            _maintenance = reader.ReadByte();
            _stage = reader.ReadString();
            _releaseDate = BinaryUtils.JavaTicksToDateTime(reader.ReadLong());
            _revHash = reader.ReadByteArray();
        }

        /// <summary>
        /// Gets the major version number.
        /// </summary>
        public byte Major
        {
            get { return _major; }
        }

        /// <summary>
        /// Gets the minor version number.
        /// </summary>
        public byte Minor
        {
            get { return _minor; }
        }

        /// <summary>
        /// Gets the maintenance version number.
        /// </summary>
        public byte Maintenance
        {
            get { return _maintenance; }
        }

        /// <summary>
        /// Gets the stage of development.
        /// </summary>
        public string Stage
        {
            get { return _stage; }
        }

        /// <summary>
        /// Gets the release date.
        /// </summary>
        public DateTime ReleaseDate
        {
            get { return _releaseDate; }
        }

        /// <summary>
        /// Gets the revision hash
        /// </summary>
        [SuppressMessage("Microsoft.Performance", "CA1819:PropertiesShouldNotReturnArrays")]
        public byte[] RevisionHash
        {
            get { return _revHash; }
        }

        /** <inheritDoc /> */
        public override int GetHashCode()
        {
            int res = Major;

            res = 31 * res + Minor;
            res = 31 * res + Maintenance;
            res = 31 * res + ReleaseDate.GetHashCode();

            return res;
        }

        /** <inheritDoc /> */
        [SuppressMessage("Microsoft.Globalization", "CA1308:NormalizeStringsToUppercase")]
        public override string ToString()
        {
            string hash = null;
            if (RevisionHash != null)
            {
                hash = BitConverter.ToString(RevisionHash).Replace("-", "")
                    .ToLowerInvariant()
                    .Substring(0, Math.Min(RevisionHash.Length, 8));
            }

            return string.Format("{0}.{1}.{2}#{3:yyyyMMdd}-sha1:{4}", Major, Minor, Maintenance, ReleaseDate, hash);
        }

        /** <inheritDoc /> */
        public bool Equals(IgniteProductVersion other)
        {
            if (other == null)
                return false;

            return ReleaseDate == other.ReleaseDate
                   && Maintenance == other.Maintenance
                   && Minor == other.Minor
                   && Major == other.Major;
        }

        /** <inheritDoc /> */
        public int CompareTo(IgniteProductVersion other)
        {
            IgniteArgumentCheck.NotNull(other, "other");

            // NOTE: Unknown version is less than any other version.
            int res = Major.CompareTo(other.Major);

            if (res != 0)
                return res;

            res = Minor.CompareTo(other.Minor);

            if (res != 0)
                return res;

            res = Maintenance.CompareTo(other.Maintenance);

            if (res != 0)
                return res;

            return ReleaseDate.CompareTo(other.ReleaseDate);
        }
    }
}
