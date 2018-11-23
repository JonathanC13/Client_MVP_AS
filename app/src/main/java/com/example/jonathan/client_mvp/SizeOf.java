package com.example.jonathan.client_mvp;

public class SizeOf {

    public SizeOf(){

    }

    /*
     * this method take advantage of SIZE constant from wrapper class to get the size of primitive data types.

     */

    public int get_sizeOf(Class dataType) {

        if (dataType == null) {

            throw new NullPointerException();

        }

        if (dataType == byte.class || dataType == Byte.class) {

            return Byte.SIZE;

        }

        if (dataType == short.class || dataType == Short.class) {

            return Short.SIZE;

        }

        if (dataType == char.class || dataType == Character.class) {

            return Character.SIZE;

        }

        if (dataType == int.class || dataType == Integer.class) {

            return Integer.SIZE;

        }

        if (dataType == long.class || dataType == Long.class) {

            return Long.SIZE;

        }

        if (dataType == float.class || dataType == Float.class) {

            return Float.SIZE;

        }

        if (dataType == double.class || dataType == Double.class) {

            return Double.SIZE;

        }

        return 4; // default for 32-bit memory pointer

    }
}
