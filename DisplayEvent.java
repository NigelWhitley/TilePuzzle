/*
 * @(#)DisplayEvent.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2003 Liftdigit Limited. All Rights Reserved.
 *
 * This software is released under the terms of the GPL. For details, see license.txt.
 *
 */


import java.awt.*;
import java.awt.event.*;

/**
 * Notify the world of a tile change, indicate by a tile position
 */
public class DisplayEvent extends PuzzleEvent
{
    static protected int displayEventID = RESERVED_ID_MAX+6;

    private String _imageName;

    public DisplayEvent(Object source, String imageName) {
        super(source, displayEventID, "Display");
        setImageName (imageName);
    }

    public DisplayEvent(Object source, String imageName, int eventID) {
        super(source, eventID, "Display");
        setImageName (imageName);
    }

    public DisplayEvent(Object source, int eventID) {
        super(source, eventID, "Display");
    }

    public DisplayEvent(Object source, int eventID, String command) {
        super(source, eventID, command);
    }

    public DisplayEvent(Object source, String imageName, String command) {
        super(source, displayEventID, command);
        setImageName (imageName);
    }

    public String getImageName() {
        return (_imageName);
    }

    public void setImageName(String imageName) {
        _imageName = imageName ;
    }

}
