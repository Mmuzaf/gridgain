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

#include <cassert>

#include <ignite/impl/binary/binary_utils.h>

#include "ignite/odbc/utility.h"
#include "ignite/odbc/system/odbc_constants.h"

namespace ignite
{
    namespace utility
    {
        size_t CopyStringToBuffer(const std::string& str, char* buf, size_t buflen)
        {
            if (!buf || !buflen)
                return 0;

            size_t bytesToCopy = std::min(str.size(), static_cast<size_t>(buflen - 1));

            memcpy(buf, str.data(), bytesToCopy);
            buf[bytesToCopy] = 0;

            return bytesToCopy;
        }

        void ReadString(ignite::impl::binary::BinaryReaderImpl& reader, std::string& str)
        {
            int32_t strLen = reader.ReadString(0, 0);

            if (strLen > 0)
            {
                str.resize(strLen);

                reader.ReadString(&str[0], static_cast<int32_t>(str.size()));
            }
            else
            {
                str.clear();

                if (strLen == 0)
                {
                    char dummy;

                    reader.ReadString(&dummy, sizeof(dummy));
                }
            }
        }

        void WriteString(ignite::impl::binary::BinaryWriterImpl& writer, const std::string & str)
        {
            writer.WriteString(str.data(), static_cast<int32_t>(str.size()));
        }

        void ReadDecimal(ignite::impl::binary::BinaryReaderImpl& reader, common::Decimal& decimal)
        {
            int8_t hdr = reader.ReadInt8();

            assert(hdr == ignite::impl::binary::IGNITE_TYPE_DECIMAL);

            int32_t scale = reader.ReadInt32();

            int32_t len = reader.ReadInt32();

            std::vector<int8_t> mag;

            mag.resize(len);

            impl::binary::BinaryUtils::ReadInt8Array(reader.GetStream(), mag.data(), static_cast<int32_t>(mag.size()));

            int32_t sign = 1;
            
            if (mag[0] < 0)
            {
                mag[0] &= 0x7F;

                sign = -1;
            }

            common::Decimal res(mag.data(), static_cast<int32_t>(mag.size()), scale, sign);

            decimal.Swap(res);
        }

        void WriteDecimal(ignite::impl::binary::BinaryWriterImpl& writer, const common::Decimal& decimal)
        {
            writer.WriteInt8(ignite::impl::binary::IGNITE_TYPE_DECIMAL);

            const common::BigInteger &unscaled = decimal.GetUnscaledValue();

            writer.WriteInt32(decimal.GetScale());

            common::FixedSizeArray<int8_t> magnitude;

            unscaled.MagnitudeToBytes(magnitude);

            if (unscaled.GetSign() == -1)
                magnitude[0] |= -0x80;

            writer.WriteInt32(magnitude.GetSize());

            impl::binary::BinaryUtils::WriteInt8Array(writer.GetStream(), magnitude.GetData(), magnitude.GetSize());
        }

        std::string SqlStringToString(const unsigned char* sqlStr, int32_t sqlStrLen)
        {
            std::string res;

            const char* sqlStrC = reinterpret_cast<const char*>(sqlStr);

            if (!sqlStr || !sqlStrLen)
                return res;

            if (sqlStrLen == SQL_NTS)
                res.assign(sqlStrC);
            else if (sqlStrLen > 0)
                res.assign(sqlStrC, sqlStrLen);

            return res;
        }

        void ReadByteArray(impl::binary::BinaryReaderImpl& reader, std::vector<int8_t>& res)
        {
            int32_t len = reader.ReadInt8Array(0, 0);

            if (len > 0)
            {
                res.resize(len);

                reader.ReadInt8Array(&res[0], static_cast<int32_t>(res.size()));
            }
            else
                res.clear();
        }

        std::string HexDump(const void* data, size_t count)
        {
            std::stringstream  dump;
            size_t cnt = 0;
            for(const uint8_t* p = (const uint8_t*)data, *e = (const uint8_t*)data + count; p != e; ++p)
            {
                if (cnt++ % 16 == 0)
                {
                    dump << std::endl;
                }
                dump << std::hex << std::setfill('0') << std::setw(2) << (int)*p << " ";
            }
            return dump.str();
        }
    }
}

