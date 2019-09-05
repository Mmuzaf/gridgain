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

package org.apache.ignite.internal.processors.cache.persistence.pagemem;

import java.util.Collection;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteSystemProperties;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.pagemem.FullPageId;

import static org.apache.ignite.IgniteSystemProperties.IGNITE_PAGE_REPLACER_AWAIT_CHECKPOINT_TIMEOUT;

/**
 * View of pages which should be stored during current checkpoint.
 */
class CheckpointPages {
    /** **/
    private static final long CHECKPOINT_WAIT_TIMEOUT = IgniteSystemProperties.getLong(IGNITE_PAGE_REPLACER_AWAIT_CHECKPOINT_TIMEOUT, 20_000);
    /** */
    private volatile Collection<FullPageId> segCheckpointPages;

    /** The sign which allows to evict pages from a checkpoint by page replacer. */
    private final IgniteInternalFuture allowToEvict;

    /**
     * @param pages Pages which would be stored to disk in current checkpoint.
     * @param evict The sign which allows to evict pages from a checkpoint by page replacer.
     */
    CheckpointPages(Collection<FullPageId> pages, IgniteInternalFuture evict) {
        segCheckpointPages = pages;
        allowToEvict = evict;
    }

    /**
     * @param fullPageId Page id for checking.
     * @return {@code true} If fullPageId is allowable to store to disk.
     */
    public boolean allowToSave(FullPageId fullPageId) throws IgniteCheckedException {
        Collection<FullPageId> checkpointPages = segCheckpointPages;

        if (checkpointPages == null || allowToEvict == null)
            return false;

        allowToEvict.get(CHECKPOINT_WAIT_TIMEOUT);

        return checkpointPages.contains(fullPageId);
    }

    /**
     * @param fullPageId Page id for checking.
     * @return {@code true} If fullPageId is candidate to stored to disk by current checkpoint.
     */
    public boolean contains(FullPageId fullPageId) {
        Collection<FullPageId> checkpointPages = segCheckpointPages;

        return checkpointPages != null && checkpointPages.contains(fullPageId);
    }

    /**
     * @param fullPageId Page id which should be marked as saved to disk.
     * @return {@code true} if is marking was successful.
     */
    public boolean markAsSaved(FullPageId fullPageId) {
        Collection<FullPageId> checkpointPages = segCheckpointPages;

        return checkpointPages != null && checkpointPages.remove(fullPageId);
    }

    /**
     * @return Size of all pages in current checkpoint.
     */
    public int size() {
        Collection<FullPageId> checkpointPages = segCheckpointPages;

        return checkpointPages == null ? 0 : checkpointPages.size();
    }
}
