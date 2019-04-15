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

package org.apache.ignite.testsuites;

import org.apache.ignite.internal.binary.BinaryArrayIdentityResolverSelfTest;
import org.apache.ignite.internal.binary.BinaryBasicIdMapperSelfTest;
import org.apache.ignite.internal.binary.BinaryBasicNameMapperSelfTest;
import org.apache.ignite.internal.binary.BinaryConfigurationConsistencySelfTest;
import org.apache.ignite.internal.binary.BinaryConfigurationCustomSerializerSelfTest;
import org.apache.ignite.internal.binary.BinaryEnumsSelfTest;
import org.apache.ignite.internal.binary.BinaryFieldsHeapSelfTest;
import org.apache.ignite.internal.binary.BinaryFieldsOffheapSelfTest;
import org.apache.ignite.internal.binary.BinaryFooterOffsetsHeapSelfTest;
import org.apache.ignite.internal.binary.BinaryFooterOffsetsOffheapSelfTest;
import org.apache.ignite.internal.binary.BinaryMarshallerSelfTest;
import org.apache.ignite.internal.binary.BinaryObjectBuilderAdditionalSelfTest;
import org.apache.ignite.internal.binary.BinaryObjectBuilderDefaultMappersSelfTest;
import org.apache.ignite.internal.binary.BinaryObjectBuilderSimpleNameLowerCaseMappersSelfTest;
import org.apache.ignite.internal.binary.BinaryObjectExceptionSelfTest;
import org.apache.ignite.internal.binary.BinaryObjectToStringSelfTest;
import org.apache.ignite.internal.binary.BinaryObjectTypeCompatibilityTest;
import org.apache.ignite.internal.binary.BinarySerialiedFieldComparatorSelfTest;
import org.apache.ignite.internal.binary.BinarySimpleNameTestPropertySelfTest;
import org.apache.ignite.internal.binary.BinaryTreeSelfTest;
import org.apache.ignite.internal.binary.GridBinaryAffinityKeySelfTest;
import org.apache.ignite.internal.binary.GridBinaryMarshallerCtxDisabledSelfTest;
import org.apache.ignite.internal.binary.GridBinaryWildcardsSelfTest;
import org.apache.ignite.internal.binary.GridDefaultBinaryMappersBinaryMetaDataSelfTest;
import org.apache.ignite.internal.binary.GridSimpleLowerCaseBinaryMappersBinaryMetaDataSelfTest;
import org.apache.ignite.internal.binary.noncompact.BinaryFieldsHeapNonCompactSelfTest;
import org.apache.ignite.internal.binary.noncompact.BinaryFieldsOffheapNonCompactSelfTest;
import org.apache.ignite.internal.binary.noncompact.BinaryFooterOffsetsHeapNonCompactSelfTest;
import org.apache.ignite.internal.binary.noncompact.BinaryFooterOffsetsOffheapNonCompactSelfTest;
import org.apache.ignite.internal.binary.noncompact.BinaryMarshallerNonCompactSelfTest;
import org.apache.ignite.internal.binary.noncompact.BinaryObjectBuilderAdditionalNonCompactSelfTest;
import org.apache.ignite.internal.binary.noncompact.BinaryObjectBuilderNonCompactDefaultMappersSelfTest;
import org.apache.ignite.internal.binary.noncompact.BinaryObjectBuilderNonCompactSimpleNameLowerCaseMappersSelfTest;
import org.apache.ignite.internal.binary.streams.BinaryAbstractOutputStreamTest;
import org.apache.ignite.internal.binary.streams.BinaryHeapStreamByteOrderSelfTest;
import org.apache.ignite.internal.binary.streams.BinaryOffheapStreamByteOrderSelfTest;
import org.apache.ignite.internal.processors.cache.binary.BinaryAtomicCacheLocalEntriesSelfTest;
import org.apache.ignite.internal.processors.cache.binary.BinaryMetadataUpdatesFlowTest;
import org.apache.ignite.internal.processors.cache.binary.BinaryTxCacheLocalEntriesSelfTest;
import org.apache.ignite.internal.processors.cache.binary.GridCacheBinaryObjectMetadataExchangeMultinodeTest;
import org.apache.ignite.internal.processors.cache.binary.GridCacheBinaryObjectUserClassloaderSelfTest;
import org.apache.ignite.internal.processors.cache.binary.GridCacheBinaryStoreBinariesDefaultMappersSelfTest;
import org.apache.ignite.internal.processors.cache.binary.GridCacheBinaryStoreBinariesSimpleNameMappersSelfTest;
import org.apache.ignite.internal.processors.cache.binary.GridCacheBinaryStoreObjectsSelfTest;
import org.apache.ignite.internal.processors.cache.binary.GridCacheClientNodeBinaryObjectMetadataMultinodeTest;
import org.apache.ignite.internal.processors.cache.binary.GridCacheClientNodeBinaryObjectMetadataTest;
import org.apache.ignite.internal.processors.cache.binary.distributed.dht.GridCacheBinaryObjectsAtomicNearDisabledSelfTest;
import org.apache.ignite.internal.processors.cache.binary.distributed.dht.GridCacheBinaryObjectsAtomicSelfTest;
import org.apache.ignite.internal.processors.cache.binary.distributed.dht.GridCacheBinaryObjectsPartitionedNearDisabledSelfTest;
import org.apache.ignite.internal.processors.cache.binary.distributed.dht.GridCacheBinaryObjectsPartitionedSelfTest;
import org.apache.ignite.internal.processors.cache.binary.distributed.replicated.GridCacheBinaryObjectsReplicatedSelfTest;
import org.apache.ignite.internal.processors.cache.binary.local.GridCacheBinaryObjectsAtomicLocalSelfTest;
import org.apache.ignite.internal.processors.cache.binary.local.GridCacheBinaryObjectsLocalSelfTest;
import org.apache.ignite.internal.processors.cache.distributed.IgniteBinaryMetadataUpdateChangingTopologySelfTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test for binary objects stored in cache.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BinarySimpleNameTestPropertySelfTest.class,

    BinaryBasicIdMapperSelfTest.class,
    BinaryBasicNameMapperSelfTest.class,

    BinaryTreeSelfTest.class,
    BinaryMarshallerSelfTest.class,
    BinaryObjectExceptionSelfTest.class,

    BinarySerialiedFieldComparatorSelfTest.class,
    BinaryArrayIdentityResolverSelfTest.class,

    BinaryConfigurationConsistencySelfTest.class,
    BinaryConfigurationCustomSerializerSelfTest.class,
    GridBinaryMarshallerCtxDisabledSelfTest.class,
    BinaryObjectBuilderDefaultMappersSelfTest.class,
    BinaryObjectBuilderSimpleNameLowerCaseMappersSelfTest.class,
    BinaryObjectBuilderAdditionalSelfTest.class,
    //BinaryFieldExtractionSelfTest.class,
    BinaryFieldsHeapSelfTest.class,
    BinaryFieldsOffheapSelfTest.class,
    BinaryFooterOffsetsHeapSelfTest.class,
    BinaryFooterOffsetsOffheapSelfTest.class,
    BinaryEnumsSelfTest.class,
    GridDefaultBinaryMappersBinaryMetaDataSelfTest.class,
    GridSimpleLowerCaseBinaryMappersBinaryMetaDataSelfTest.class,
    GridBinaryAffinityKeySelfTest.class,
    GridBinaryWildcardsSelfTest.class,
    BinaryObjectToStringSelfTest.class,
    BinaryObjectTypeCompatibilityTest.class,

    // Tests for objects with non-compact footers.
    BinaryMarshallerNonCompactSelfTest.class,
    BinaryObjectBuilderNonCompactDefaultMappersSelfTest.class,
    BinaryObjectBuilderNonCompactSimpleNameLowerCaseMappersSelfTest.class,
    BinaryObjectBuilderAdditionalNonCompactSelfTest.class,
    BinaryFieldsHeapNonCompactSelfTest.class,
    BinaryFieldsOffheapNonCompactSelfTest.class,
    BinaryFooterOffsetsHeapNonCompactSelfTest.class,
    BinaryFooterOffsetsOffheapNonCompactSelfTest.class,

    GridCacheBinaryObjectsLocalSelfTest.class,
    //GridCacheBinaryObjectsLocalOnheapSelfTest.class,
    GridCacheBinaryObjectsAtomicLocalSelfTest.class,
    GridCacheBinaryObjectsReplicatedSelfTest.class,
    GridCacheBinaryObjectsPartitionedSelfTest.class,
    GridCacheBinaryObjectsPartitionedNearDisabledSelfTest.class,
    //GridCacheBinaryObjectsPartitionedNearDisabledOnheapSelfTest.class,
    //GridCacheBinaryObjectsPartitionedOnheapSelfTest.class,
    GridCacheBinaryObjectsAtomicSelfTest.class,
    //GridCacheBinaryObjectsAtomicOnheapSelfTest.class,
    GridCacheBinaryObjectsAtomicNearDisabledSelfTest.class,
    //GridCacheBinaryObjectsAtomicNearDisabledOnheapSelfTest.class,

    GridCacheBinaryStoreObjectsSelfTest.class,
    GridCacheBinaryStoreBinariesDefaultMappersSelfTest.class,
    GridCacheBinaryStoreBinariesSimpleNameMappersSelfTest.class,

    GridCacheClientNodeBinaryObjectMetadataTest.class,
    GridCacheBinaryObjectMetadataExchangeMultinodeTest.class,
    BinaryMetadataUpdatesFlowTest.class,
    GridCacheClientNodeBinaryObjectMetadataMultinodeTest.class,
    IgniteBinaryMetadataUpdateChangingTopologySelfTest.class,

    BinaryTxCacheLocalEntriesSelfTest.class,
    BinaryAtomicCacheLocalEntriesSelfTest.class,

    // Byte order
    BinaryHeapStreamByteOrderSelfTest.class,
    BinaryAbstractOutputStreamTest.class,
    BinaryOffheapStreamByteOrderSelfTest.class,

    GridCacheBinaryObjectUserClassloaderSelfTest.class
})
public class IgniteBinaryObjectsTestSuite {
}
