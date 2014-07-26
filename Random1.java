/*
 * @(#)Random1.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2001-2003 Nigel Whitley. All Rights Reserved.
 *
 * This software is released under the terms of the GPL. For details, see license.txt.
 *
 */

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;

/**
 * Unfortunately, the Java 1.1 version of random doesn't fully support getting a random integer from an integer range (as supported in 1.2)
 * So I've created a simple extension to the standard Random class to support that facility.
 */
class Random1 extends Random
                  {

    public int _bitRange;
    public int _bitRanges[] = {1,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192, 16384, 32768};

    Random1() {
        super();
    }

    public int nextIntFromRange( int intRange ) {
        int bitRange = 0;

        // Calculate the smallest power of 2 which is not less than the intRange;
        for ( int bitRangeIndex = 0; bitRangeIndex < 16; bitRangeIndex++ )
        {
                if ( intRange <= _bitRanges[bitRangeIndex] ) { bitRange = _bitRanges[bitRangeIndex]; break; }
        }

        // Get an integer from the bitRange (which may be bigger than the intRange)
        int intValue = next(bitRange);
        // Keep getting values from the bitRange until we get one within the intRange
        while ( intValue >= intRange )
        {
                intValue = next(bitRange);
        }
        // The resulting integer value is returned as our "random" number
        return intValue;

    }

}
