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

package org.apache.ignite.internal.util.nio;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteException;
import org.apache.ignite.lang.IgniteInClosure;

/**
 * This interface defines the general element in transformation chain between the nio server and
 * application.
 */
public interface GridNioFilter {
    /**
     * Beginning of a filter lifecycle, invoked on server start. It is guaranteed that this method will be invoked
     * prior to any of the event-related methods.
     */
    public void start();

    /**
     * End of a filter lifecycle, invoked on server stop. It is guaranteed that this method will be invoked after all
     * events are processed, no more event-related methods will be invoked after this method called.
     */
    public void stop();

    /**
     * Gets next filter in filter chain.
     *
     * @return Next filter (in order from application to network layer).
     */
    public GridNioFilter nextFilter();

    /**
     * Gets previous filter in filter chain.
     *
     * @return Previous filter (in order from application to network layer).
     */
    public GridNioFilter previousFilter();

    /**
     * Sets next filter in filter chain. In filter chain next filter would be more close
     * to the network layer.
     *
     * @param filter Next filter in filter chain.
     */
    public void nextFilter(GridNioFilter filter);

    /**
     * Sets previous filter in filter chain. In filter chain previous filter would be more close
     * to application layer.
     *
     * @param filter Previous filter in filter chain.
     */
    public void previousFilter(GridNioFilter filter);

    /**
     * Forwards session opened event to the next logical filter in filter chain.
     *
     * @param ses Opened session.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public void proceedSessionOpened(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Forwards session closed event to the next logical filter in filter chain.
     *
     * @param ses Closed session.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public void proceedSessionClosed(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Forwards GridNioException event to the next logical filter in filter chain.
     *
     * @param ses Session instance.
     * @param e GridNioException instance.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public void proceedExceptionCaught(GridNioSession ses, IgniteCheckedException e) throws IgniteCheckedException;

    /**
     * Forwards received message to the next logical filter in filter chain.
     *
     * @param ses Session instance.
     * @param msg Received message.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public void proceedMessageReceived(GridNioSession ses, Object msg) throws IgniteCheckedException;

    /**
     * Forwards write request to the next logical filter in filter chain.
     *
     * @param ses Session instance.
     * @param msg Message to send.
     * @param fut {@code True} if write future should be created.
     * @param ackC Closure invoked when message ACK is received.
     * @return Write future or {@code null}.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public GridNioFuture<?> proceedSessionWrite(
        GridNioSession ses,
        Object msg,
        boolean fut,
        IgniteInClosure<IgniteException> ackC
    ) throws IgniteCheckedException;

    /**
     * Forwards session close request to the next logical filter in filter chain.
     *
     * @param ses Session instance.
     * @return Close future.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public GridNioFuture<Boolean> proceedSessionClose(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Invoked when a new session was created.
     *
     * @param ses Opened session.
     * @throws IgniteCheckedException If GridNioException occurred while handling event.
     */
    public void onSessionOpened(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Invoked after session get closed.
     *
     * @param ses Closed session.
     * @throws IgniteCheckedException If GridNioException occurred while handling event.
     */
    public void onSessionClosed(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Invoked when exception is caught in filter processing.
     *
     * @param ses Session that caused IgniteCheckedException.
     * @param ex GridNioException instance.
     * @throws IgniteCheckedException If IgniteCheckedException occurred while handling event.
     */
    public void onExceptionCaught(GridNioSession ses, IgniteCheckedException ex) throws IgniteCheckedException;

    /**
     * Invoked when a write request is performed on a session.
     *
     * @param ses Session on which message should be written.
     * @param msg Message being written.
     * @param fut {@code True} if write future should be created.
     * @param ackC Closure invoked when message ACK is received.
     * @return Write future or {@code null}.
     * @throws GridNioException If GridNioException occurred while handling event.
     */
    public GridNioFuture<?> onSessionWrite(GridNioSession ses,
        Object msg,
        boolean fut,
        IgniteInClosure<IgniteException> ackC) throws IgniteCheckedException;

    /**
     * Invoked when a new messages received.
     *
     * @param ses Session on which message was received.
     * @param msg Received message.
     * @throws IgniteCheckedException If IgniteCheckedException occurred while handling event.
     */
    public void onMessageReceived(GridNioSession ses, Object msg) throws IgniteCheckedException;

    /**
     * Invoked when a session close request is performed on session.
     *
     * @param ses Session to close.
     * @return Close future.
     * @throws IgniteCheckedException If IgniteCheckedException occurred while handling event.
     */
    public GridNioFuture<Boolean> onSessionClose(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Called when session is idle for longer time that is
     * allowed by NIO server.
     *
     * @param ses Session that is idle.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public void onSessionIdleTimeout(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Forwards session idle notification to the next logical filter in filter chain.
     *
     * @param ses Session instance.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public void proceedSessionIdleTimeout(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Called when session has not empty write buffer that has not been fully
     * flushed during max timeout allowed by NIO server.
     *
     * @param ses Session that has timed out writes.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public void onSessionWriteTimeout(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Forwards session write timeout notification to the next logical filter in filter chain.
     *
     * @param ses Session instance.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public void proceedSessionWriteTimeout(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Pauses reads for session.
     *
     * @param ses Session.
     * @return Future for operation.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public GridNioFuture<?> proceedPauseReads(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Pauses reads for session.
     *
     * @param ses Session.
     * @return Future for operation.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public GridNioFuture<?> onPauseReads(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Resumes reads for session.
     *
     * @param ses Session.
     * @return Future for operation.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public GridNioFuture<?> proceedResumeReads(GridNioSession ses) throws IgniteCheckedException;

    /**
     * Resumes reads for session.
     *
     * @param ses Session.
     * @return Future for operation.
     * @throws IgniteCheckedException If filter is not in chain or GridNioException occurred in the underlying filter.
     */
    public GridNioFuture<?> onResumeReads(GridNioSession ses) throws IgniteCheckedException;
}
