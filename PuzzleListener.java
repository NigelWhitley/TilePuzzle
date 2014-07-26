/*
 * @(#)PuzzleListener.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2001-2003 Nigel Whitley. All Rights Reserved.
 *
 * This software is released under the terms of the GPL. For details, see license.txt.
 *
 */

import java.awt.AWTEvent;
import java.awt.*;
import java.util.*;

/**
 * Notify the world of a required tile change, indicated by a tile position
 */
public interface PuzzleListener extends java.util.EventListener
{
    public void puzzleChange(PuzzleEvent puzzleEvent);
//    public void tileMoved(TileEvent te);

}
