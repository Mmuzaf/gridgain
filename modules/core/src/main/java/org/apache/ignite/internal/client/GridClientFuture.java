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

package org.apache.ignite.internal.client;

import java.util.concurrent.TimeUnit;

/**
 * Future for asynchronous operations.
 */
public interface GridClientFuture<R> {
    /**
     * Synchronously waits for completion and returns result.
     *
     * @return Completed future result.
     * @throws GridClientException In case of error.
     */
    public R get() throws GridClientException;

    /**
     * Synchronously waits for completion and returns result.
     *
     * @param timeout Timeout interval to wait future completes.
     * @param unit Timeout interval unit to wait future completes.
     * @return Completed future result.
     * @throws GridClientException In case of error.
     * @throws GridClientFutureTimeoutException If timed out before future finishes.
     */
    public R get(long timeout, TimeUnit unit) throws GridClientException;

    /**
     * Checks if future is done.
     *
     * @return Whether future is done.
     */
    public boolean isDone();

    /**
     * Register new listeners for notification when future completes.
     *
     * Note that current implementations are calling listeners in
     * the completing thread.
     *
     * @param lsnrs Listeners to be registered.
     */
    public void listen(GridClientFutureListener<R>... lsnrs);
}