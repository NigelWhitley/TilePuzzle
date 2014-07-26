/*
 * @(#)SolutionEvent.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2001-2003 Nigel Whitley. All Rights Reserved.
 *
 * This software is released under the terms of the GPL. For details, see license.txt.
 *
 */


import java.awt.*;
import java.awt.event.*;

/**
 * Notify the world of a tile change, indicate by a tile position
 */
public class SolutionEvent extends PuzzleEvent
{
    static private int solutionEventID = RESERVED_ID_MAX+4;

    private int _tileValue;

    public SolutionEvent(Object source, int tileValue) {
        super(source, solutionEventID, "Solving" );
        setTileValue (tileValue);
    }

    public SolutionEvent(Object source, int tileValue, int eventID) {
        super(source, eventID, "Solving");
        setTileValue (tileValue);
    }

    public int getTileValue() {
        return _tileValue;
    }

    public void setTileValue (int tileValue) {
        _tileValue = tileValue;
    }

}
