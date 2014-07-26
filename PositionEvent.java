/*
 * @(#)PositionEvent.java	1.0 10-May-2001  Nigel Whitley
 *
 * Copyright (c) 2001 Liftdigit Limited. All Rights Reserved.
 *
 * This software is released under the terms of the GPL. For details, see license.txt.
 *
 */


import java.awt.*;
import java.awt.event.*;

/**
 * Notify the world of a tile change, indicate by a tile position
 */
public class PositionEvent extends PuzzleEvent
{
    static protected int positionEventID = RESERVED_ID_MAX+2;

    public PositionEvent(Object source, PuzzlePosition tilePosition) {
        super(source, positionEventID, "Position");
        setPuzzlePosition (tilePosition);
    }

    public PositionEvent(Object source, PuzzlePosition tilePosition, int eventID) {
        super(source, eventID, "Position");
        setPuzzlePosition (tilePosition);
    }

    public PositionEvent(Object source, int eventID) {
        super(source, eventID, "Position");
    }

    public PositionEvent(Object source, int eventID, String command) {
        super(source, eventID, command);
    }

    public PositionEvent(Object source, PuzzlePosition tilePosition, String command) {
        super(source, positionEventID, command);
        setPuzzlePosition (tilePosition);
    }

    public PuzzlePosition getPuzzlePosition() {
        return ((PuzzlePosition) getEventInfo());
    }

    public void setPuzzlePosition (PuzzlePosition tilePosition) {
        setEventInfo( tilePosition );
    }

}
