/*
 * @(#)MessageEvent.java	1.1 5-December-2003  Nigel Whitley
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
public class MessageEvent extends PuzzleEvent
{
    static protected int messageEventID = RESERVED_ID_MAX+3;

    public MessageEvent(Object source, String message) {
        super(source, messageEventID, "Message");
        setMessage (message);
    }

    public MessageEvent(Object source, String message, int eventID) {
        super(source, eventID, "Message");
        setMessage (message);
    }

    public MessageEvent(Object source, int eventID) {
        super(source, eventID, "Message");
    }

    public MessageEvent(Object source, int eventID, String command) {
        super(source, eventID, command);
    }

    public MessageEvent(Object source, String message, String command) {
        super(source, messageEventID, command);
        setMessage (message);
    }

    public String getMessage() {
        return ((String) getEventInfo());
    }

    public void setMessage (String message) {
        setEventInfo( message );
    }

}
