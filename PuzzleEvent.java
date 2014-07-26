/*
 * @(#)PuzzleEvent.java	1.1 5-Decmebr-2003  Nigel Whitley
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
public class PuzzleEvent extends ActionEvent
{
    private Object _eventInfo;
    private static final long serialVersionUID = 1L;
    static protected int puzzleEventID = RESERVED_ID_MAX+1;

    public PuzzleEvent(Object source, Object eventInfo) {
        super(source, puzzleEventID, "Puzzle");
        setEventInfo (eventInfo);
    }

    public PuzzleEvent(Object source, String command, Object eventInfo) {
        super(source, puzzleEventID, command);
        setEventInfo (eventInfo);
    }

    public PuzzleEvent(Object source, int eventID, String command) {
        super(source, eventID, command);
    }

    public PuzzleEvent(Object source, String command) {
        super(source, puzzleEventID, command);
    }

    public PuzzleEvent(Object source, int eventID) {
        super(source, eventID, "Puzzle");
    }

    public PuzzleEvent(Object source, int eventID, Object eventInfo) {
        super(source, eventID, "Puzzle");
        setEventInfo (eventInfo);
    }

    public Object getEventInfo() {
        return _eventInfo;
    }

    public void setEventInfo (Object eventInfo) {
        _eventInfo = eventInfo;
    }

}
