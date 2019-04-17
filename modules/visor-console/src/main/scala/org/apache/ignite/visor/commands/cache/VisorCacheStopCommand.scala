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

package org.apache.ignite.visor.commands.cache

import org.apache.ignite.cluster.{ClusterGroupEmptyException, ClusterNode}
import org.apache.ignite.visor.visor._
import org.apache.ignite.internal.visor.cache.{VisorCacheStopTask, VisorCacheStopTaskArg}
import org.apache.ignite.internal.visor.util.VisorTaskUtils._

/**
 * ==Overview==
 * Visor 'stop' command implementation.
 *
 * ====Specification====
 * {{{
 *     cache -c=<cache name> -stop
 * }}}
 *
 * ====Arguments====
 * {{{
 *     <cache-name>
 *         Name of the cache.
 * }}}
 *
 * ====Examples====
 * {{{
 *    cache -c=@c0 -stop
 *        Stops cache with name taken from 'c0' memory variable.
 * }}}
 */
class VisorCacheStopCommand {
    /**
     * Prints error message and advise.
     *
     * @param errMsgs Error messages.
     */
    private def scold(errMsgs: Any*) {
        assert(errMsgs != null)

        warn(errMsgs: _*)
        warn("Type 'help cache' to see how to use this command.")
    }

    private def error(e: Exception) {
        var cause: Throwable = e

        while (cause.getCause != null)
            cause = cause.getCause

        scold(cause.getMessage)
    }

    /**
     * ===Command===
     * Stop cache with specified name.
     *
     * ===Examples===
     * <ex>cache -c=cache -stop</ex>
     *     Stop cache with name 'cache'.
     *
     * @param argLst Command arguments.
     */
    def stop(argLst: ArgList, node: Option[ClusterNode]) {
        val cacheArg = argValue("c", argLst)

        val cacheName = cacheArg match {
            case None => null // default cache.

            case Some(s) if s.startsWith("@") =>
                warn("Can't find cache variable with specified name: " + s,
                    "Type 'cache' to see available cache variables."
                )

                return

            case Some(name) => name
        }

        val grp = try {
            groupForDataNode(node, cacheName)
        }
        catch {
            case _: ClusterGroupEmptyException =>
                scold(messageNodeNotFound(node, cacheName))

                return
        }

        val dflt = if (batchMode) "y" else "n"

        ask(s"Are you sure you want to stop cache: ${escapeName(cacheName)}? (y/n) [$dflt]: ", dflt) match {
            case "y" | "Y" =>
                try {
                    executeRandom(grp, classOf[VisorCacheStopTask], new VisorCacheStopTaskArg(cacheName))

                    println("Visor successfully stop cache: " + escapeName(cacheName))
                }
                catch {
                    case _: ClusterGroupEmptyException => scold(messageNodeNotFound(node, cacheName))
                    case e: Exception => error(e)
                }

            case "n" | "N" =>

            case x =>
                nl()

                warn("Invalid answer: " + x)
        }
    }
}

object VisorCacheStopCommand {
    /** Singleton command. */
    private val cmd = new VisorCacheStopCommand

    /**
     * Singleton.
     */
    def apply() = cmd
}
