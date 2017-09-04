package com.polar.browser.download_refactor;

/**
 * Created by chenliang on 14-12-11.
 */
public class ArrayUtil {
    
    /*
     * write int value to array, big-endian. same order as RandomAccessFile::readInt()
     */
    static void arrayAssignFromInt(byte[] array, int arrayPos, int value) throws ParamException {
        if (array == null || arrayPos < 0 || arrayPos + 4 > array.length) {
            throw new ParamException("arrayPos = " + arrayPos);
        }

        for (int i=0; i<4; i++) {
            array[arrayPos + 3 - i] = (byte) (value >> (i*8));
        }
    }

    /*
     * write long value to array, big-endian. same order as RandomAccessFile::readLong()
     */
    static void arrayAssignFromLong(byte[] array, int arrayPos, long value) throws ParamException {
        if (array == null || arrayPos < 0 || arrayPos + 8 > array.length) {
            throw new ParamException("arrayPos = " + arrayPos);
        }

        for (int i=0; i<8; i++) {
            array[arrayPos + 7 - i] = (byte) (value >> (i*8));
        }
    }
    
    /*
     * read int value from array, big-endian. same order as RandomAccessFile::readInt()
     */
    static int readIntFromArray(byte[] array, int arrayPos) throws ParamException {
        if (array == null || arrayPos < 0 || arrayPos + 4 > array.length) {
            throw new ParamException("arrayPos = " + arrayPos);
        }

        int value = 0;
        for (int i=0; i<4; i++) {
            value |= ((int)(array[arrayPos + 3 - i] & 0xFF)) << (i*8);
        }
        return value;
    }

    /*
     * read long value from array, big-endian. same order as RandomAccessFile::readLong()
     */
    static long readLongFromArray(byte[] array, int arrayPos) throws ParamException {
        if (array == null || arrayPos < 0 || arrayPos + 8 > array.length) {
            throw new ParamException("arrayPos = " + arrayPos);
        }

        long value = 0;
        for (int i=0; i<8; i++) {
            value |= ((long)(array[arrayPos + 7 - i] & 0xFF)) << (i*8);
        }
        return value;
    }
}
