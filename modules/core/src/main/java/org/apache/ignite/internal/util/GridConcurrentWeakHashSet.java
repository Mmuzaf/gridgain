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

package org.apache.ignite.internal.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.ignite.internal.util.tostring.GridToStringExclude;
import org.apache.ignite.internal.util.tostring.GridToStringInclude;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.internal.A;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.lang.IgniteClosure;
import org.jetbrains.annotations.Nullable;

/**
 * Concurrent weak hash set implementation.
 */
public class GridConcurrentWeakHashSet<E> implements Set<E> {
    /** Empty array. */
    private static final Object[] EMPTY_ARR = new Object[0];

    /** Reference store. */
    @GridToStringInclude
    private GridConcurrentHashSet<WeakReferenceElement<E>> store;

    /** Reference queue. */
    @GridToStringExclude
    private final ReferenceQueue<E> gcQ = new ReferenceQueue<>();

    /** Reference factory. */
    private final IgniteClosure<E, WeakReferenceElement<E>> fact = new IgniteClosure<E, WeakReferenceElement<E>>() {
        @Override public WeakReferenceElement<E> apply(E e) {
            assert e != null;

            return new WeakReferenceElement<>(e, gcQ);
        }
    };

    /**
     * Creates a new, empty set with a default initial capacity,
     * load factor, and concurrencyLevel.
     */
    public GridConcurrentWeakHashSet() {
        store = new GridConcurrentHashSet<>();
    }

    /**
     * Creates a new, empty set with the specified initial
     * capacity, and with default load factor and concurrencyLevel.
     *
     * @param initCap The initial capacity. The implementation
     *      performs internal sizing to accommodate this many elements.
     * @throws IllegalArgumentException if the initial capacity of
     *      elements is negative.
     */
    public GridConcurrentWeakHashSet(int initCap) {
        store = new GridConcurrentHashSet<>(initCap);
    }

    /**
     * Creates a new, empty set with the specified initial
     * capacity, load factor, and concurrency level.
     *
     * @param initCap The initial capacity. The implementation
     *      performs internal sizing to accommodate this many elements.
     * @param loadFactor The load factor threshold, used to control resizing.
     *      Resizing may be performed when the average number of elements per
     *      bin exceeds this threshold.
     * @param conLevel The estimated number of concurrently
     *      updating threads. The implementation performs internal sizing
     *      to try to accommodate this many threads.
     * @throws IllegalArgumentException if the initial capacity is
     *      negative or the load factor or concurrency level are
     *      non-positive.
     */
    public GridConcurrentWeakHashSet(int initCap, float loadFactor, int conLevel) {
        store = new GridConcurrentHashSet<>(initCap, loadFactor, conLevel);
    }

    /**
     * Constructs a new set containing the elements in the specified
     * collection, with default load factor and an initial
     * capacity sufficient to contain the elements in the specified collection.
     *
     * @param c Collection to add.
     */
    public GridConcurrentWeakHashSet(Collection<E> c) {
        this(c.size());

        addAll(c);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"SimplifiableIfStatement"})
    @Override public boolean add(E e) {
        A.notNull(e, "e");

        removeStale();

        if (!contains(e))
            return store.add(fact.apply(e));

        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean addAll(@Nullable Collection<? extends E> c) {
        boolean res = false;

        if (!F.isEmpty(c)) {
            assert c != null;

            for (E e : c) {
                res |= add(e);
            }
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override public boolean retainAll(@Nullable Collection<?> c) {
        removeStale();

        boolean res = false;

        if (!F.isEmpty(c)) {
            assert c != null;

            Iterator<WeakReferenceElement<E>> iter = store.iterator();

            while (iter.hasNext()) {
                if (!c.contains(iter.next().get())) {
                    iter.remove();

                    res = true;
                }
            }
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override public int size() {
        removeStale();

        return store.size();
    }

    /** {@inheritDoc} */
    @Override public boolean isEmpty() {
        removeStale();

        return store.isEmpty();
    }

    /** {@inheritDoc} */
    @Override public boolean contains(@Nullable Object o) {
        removeStale();

        if (!store.isEmpty() && o != null) {
            for (WeakReferenceElement ref : store) {
                Object reft = ref.get();

                if (reft != null && reft.equals(o))
                    return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean containsAll(@Nullable Collection<?> c) {
        if (F.isEmpty(c))
            return false;

        assert c != null;

        for (Object o : c) {
            if (!contains(o))
                return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
    @Override public Object[] toArray() {
        return toArray(EMPTY_ARR);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"SuspiciousToArrayCall"})
    @Override public <T> T[] toArray(T[] a) {
        removeStale();

        Collection<E> elems = new LinkedList<>();

        for (WeakReferenceElement<E> ref : store) {
            E e = ref.get();

            if (e != null)
                elems.add(e);
        }

        return elems.toArray(a);
    }

    /** {@inheritDoc} */
    @Override public Iterator<E> iterator() {
        removeStale();

        return new Iterator<E>() {
            /** Storage iterator. */
            private Iterator<WeakReferenceElement<E>> iter = store.iterator();

            /** Current element. */
            private E elem;

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                if (elem == null) {
                    while (iter.hasNext()) {
                        WeakReferenceElement<E> ref = iter.next();

                        E e;

                        if (ref != null && (e = ref.get()) != null) {
                            elem = e;

                            break;
                        }
                        else
                            removeStale();
                    }
                }

                return elem != null;
            }

            /** {@inheritDoc} */
            @SuppressWarnings({"IteratorNextCanNotThrowNoSuchElementException"})
            @Override public E next() {
                if (elem == null) {
                    if (!hasNext())
                        throw new NoSuchElementException();
                }

                E res = elem;

                elem = null;

                return res;
            }

            /** {@inheritDoc} */
            @Override public void remove() {
                iter.remove();
            }
        };
    }

    /** {@inheritDoc} */
    @Override public void clear() {
        store.clear();
    }

    /** {@inheritDoc} */
    @Override public boolean remove(@Nullable Object o) {
        removeStale();

        if (o != null) {
            for (Iterator<WeakReferenceElement<E>> iter = store.iterator(); iter.hasNext();) {
                Object reft = iter.next().get();

                if (reft != null && reft.equals(o)) {
                    iter.remove();

                    return true;
                }
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean removeAll(@Nullable Collection<?> c) {
        boolean res = false;

        if (!F.isEmpty(c)) {
            assert c != null;

            for (Object o : c) {
                res |= remove(o);
            }
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;

        if (!(o instanceof GridConcurrentWeakHashSet))
            return false;

        GridConcurrentWeakHashSet that = (GridConcurrentWeakHashSet)o;

        return store.equals(that.store);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return store.hashCode();
    }

    /**
     * Removes stale references.
     */
    private void removeStale() {
        WeakReferenceElement<E> ref;

        while ((ref = (WeakReferenceElement<E>) gcQ.poll()) != null) {
            store.remove(ref);

            onGc(ref.get());
        }
    }

    /**
     * This method is called on every element when it gets GC-ed.
     *
     * @param e Element that is about to get GC-ed.
     */
    protected void onGc(E e) {
        // No-op.
    }

    /**
     * Weak reference implementation for this set.
     */
    private static class WeakReferenceElement<E> extends WeakReference<E> {
        /** Element hash code. */
        private int hashCode;

        /**
         * Creates weak reference element.
         *
         * @param reft Referent.
         * @param queue Reference queue.
         */
        private WeakReferenceElement(E reft, ReferenceQueue<? super E> queue) {
            super(reft, queue);

            hashCode = reft != null ? reft.hashCode() : 0;
        }

        /** {@inheritDoc} */
        @Override public boolean equals(Object o) {
            if (this == o)
                return true;

            if (!(o instanceof WeakReferenceElement))
                return false;

            E thisReft = get();

            Object thatReft = ((Reference)o).get();

            return thisReft != null ? thisReft.equals(thatReft) : thatReft == null;
        }

        /** {@inheritDoc} */
        @Override public int hashCode() {
            return hashCode;
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridConcurrentWeakHashSet.class, this);
    }
}