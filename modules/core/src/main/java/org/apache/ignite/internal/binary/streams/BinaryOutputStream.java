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

package org.apache.ignite.internal.binary.streams;

/**
 * Binary output stream.
 */
public interface BinaryOutputStream extends BinaryStream, AutoCloseable {
    /**
     * Write byte value.
     *
     * @param val Byte value.
     */
    public void writeByte(byte val);

    /**
     * Write byte array.
     *
     * @param val Byte array.
     */
    public void writeByteArray(byte[] val);

    /**
     * Write boolean value.
     *
     * @param val Boolean value.
     */
    public void writeBoolean(boolean val);

    /**
     * Write boolean array.
     *
     * @param val Boolean array.
     */
    public void writeBooleanArray(boolean[] val);

    /**
     * Write short value.
     *
     * @param val Short value.
     */
    public void writeShort(short val);

    /**
     * Write short array.
     *
     * @param val Short array.
     */
    public void writeShortArray(short[] val);

    /**
     * Write char value.
     *
     * @param val Char value.
     */
    public void writeChar(char val);

    /**
     * Write char array.
     *
     * @param val Char array.
     */
    public void writeCharArray(char[] val);

    /**
     * Write int value.
     *
     * @param val Int value.
     */
    public void writeInt(int val);

    /**
     * Write short value at the given position.
     *
     * @param pos Position.
     * @param val Value.
     */
    public void writeShort(int pos, short val);

    /**
     * Write int value to the given position.
     *
     * @param pos Position.
     * @param val Value.
     */
    public void writeInt(int pos, int val);

    /**
     * Write int array.
     *
     * @param val Int array.
     */
    public void writeIntArray(int[] val);

    /**
     * Write float value.
     *
     * @param val Float value.
     */
    public void writeFloat(float val);

    /**
     * Write float array.
     *
     * @param val Float array.
     */
    public void writeFloatArray(float[] val);

    /**
     * Write long value.
     *
     * @param val Long value.
     */
    public void writeLong(long val);

    /**
     * Write long array.
     *
     * @param val Long array.
     */
    public void writeLongArray(long[] val);

    /**
     * Write double value.
     *
     * @param val Double value.
     */
    public void writeDouble(double val);

    /**
     * Write double array.
     *
     * @param val Double array.
     */
    public void writeDoubleArray(double[] val);

    /**
     * Write byte array.
     *
     * @param arr Array.
     * @param off Offset.
     * @param len Length.
     */
    public void write(byte[] arr, int off, int len);

    /**
     * Write data from unmanaged memory.
     *
     * @param addr Address.
     * @param cnt Count.
     */
    public void write(long addr, int cnt);

    /**
     * Close the stream releasing resources.
     */
    @Override public void close();

    /**
     * Set position in unsafe mode.
     *
     * @param pos Position.
     */
    public void unsafePosition(int pos);

    /**
     * Ensure capacity for unsafe writes.
     *
     * @param cap Capacity.
     */
    public void unsafeEnsure(int cap);

    /**
     * Write byte in unsafe mode.
     *
     * @param val Value.
     */
    public void unsafeWriteByte(byte val);

    /**
     * Write boolean in unsafe mode.
     *
     * @param val Value.
     */
    public void unsafeWriteBoolean(boolean val);

    /**
     * Write short in unsafe mode.
     *
     * @param val Value.
     */
    public void unsafeWriteShort(short val);

    /**
     * Write short in unsafe mode.
     *
     * @param pos Position.
     * @param val Value.
     */
    public void unsafeWriteShort(int pos, short val);

    /**
     * Write char in unsafe mode.
     *
     * @param val Value.
     */
    public void unsafeWriteChar(char val);

    /**
     * Write int in unsafe mode.
     *
     * @param val Value.
     */
    public void unsafeWriteInt(int val);

    /**
     * Write int in unsafe mode.
     *
     * @param pos Position.
     * @param val Value.
     */
    public void unsafeWriteInt(int pos, int val);

    /**
     * Write long in unsafe mode.
     *
     * @param val Value.
     */
    public void unsafeWriteLong(long val);

    /**
     * Write float in unsafe mode.
     *
     * @param val Value.
     */
    public void unsafeWriteFloat(float val);

    /**
     * Write double in unsafe mode.
     *
     * @param val Value.
     */
    public void unsafeWriteDouble(double val);
}
