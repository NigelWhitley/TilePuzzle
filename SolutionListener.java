/*
 * @(#)SolutionListener.java	1.1 5-December  Nigel Whitley
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
 * Notify the world of a tile solution change, indicate by a tile value
 */
public interface SolutionListener extends java.util.EventListener
{
    public void solutionChanged(SolutionEvent solutionEvent);
//    public void tileMoved(TileEvent te);

}
