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

package org.apache.ignite.spark.impl

import org.apache.ignite.spark.impl
import org.apache.ignite.spark.impl.optimization.accumulator.{JoinSQLAccumulator, QueryAccumulator}
import org.apache.ignite.spark.impl.optimization.isSimpleTableAcc
import org.apache.spark.Partition
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, TableScan}
import org.apache.spark.sql.types.{Metadata, StructField, StructType}

/**
  * Relation to query data from query generated by <code>QueryAccumulator</code>.
  * <code>QueryAccumulator</code> is generated by <code>IgniteOptimization</code>.
  *
  * @see IgniteOptimization
  */
class IgniteSQLAccumulatorRelation[K, V](val acc: QueryAccumulator)
    (@transient val sqlContext: SQLContext) extends BaseRelation with TableScan {

    /** @inheritdoc */
    override def schema: StructType =
        StructType(acc.output.map { c ⇒
            StructField(
                name = c.name,
                dataType = c.dataType,
                nullable = c.nullable,
                metadata = Metadata.empty)
        })

    /** @inheritdoc */
    override def buildScan(): RDD[Row] =
        IgniteSQLDataFrameRDD[K, V](
            acc.igniteQueryContext.igniteContext,
            acc.igniteQueryContext.cacheName,
            schema,
            acc.compileQuery(),
            List.empty,
            calcPartitions,
            isDistributeJoin(acc)
        )

    /** @inheritdoc */
    override def toString: String =
        s"IgniteSQLAccumulatorRelation(columns=[${acc.output.map(_.name).mkString(", ")}], qry=${acc.compileQuery()})"

    /**
      * @return Collection of spark partition.
      */
    private def calcPartitions: Array[Partition] =
        //If accumulator stores some complex query(join, aggregation, limit, order, etc.).
        //we has to load data from Ignite as a single Spark partition.
        if (!isSimpleTableAcc(acc)){
            val aff = acc.igniteQueryContext.igniteContext.ignite().affinity(acc.igniteQueryContext.cacheName)

            val parts = aff.partitions()

            Array(IgniteDataFramePartition(0, primary = null, igniteParts = (0 until parts).toList))
        }
        else
            impl.calcPartitions(acc.igniteQueryContext.igniteContext, acc.igniteQueryContext.cacheName)

    /**
      * @param acc Plan.
      * @return True if plan of one or its children are `JoinSQLAccumulator`, false otherwise.
      */
    private def isDistributeJoin(acc: LogicalPlan): Boolean =
        acc match {
            case _: JoinSQLAccumulator ⇒
                true

            case _ ⇒
                acc.children.exists(isDistributeJoin)
        }
}

object IgniteSQLAccumulatorRelation {
    def apply[K, V](acc: QueryAccumulator): IgniteSQLAccumulatorRelation[K, V] =
        new IgniteSQLAccumulatorRelation[K, V](acc)(acc.igniteQueryContext.sqlContext)
}
