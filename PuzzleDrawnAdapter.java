/*
 * @(#)PuzzleDrawnAdapter.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2003 Nigel Whitley. All Rights Reserved.
 *
 * This software is released under the terms of the GPL. For details, see license.txt.
 *
 */

import java.awt.*;
import java.util.*;
import java.text.*;
import java.applet.Applet;
import java.awt.event.*;
import java.io.*;

/**
 * This component defines a mouse response for PuzzleDrawnDisplay and its descendents
 */

public class PuzzleDrawnAdapter extends MouseAdapter {
	PuzzleDrawnDisplay	_puzzleDrawnDisplay;

	PuzzleDrawnAdapter( PuzzleDrawnDisplay puzzleDrawnDisplay ) {
		super();
		_puzzleDrawnDisplay = puzzleDrawnDisplay;
	}

	public PuzzleDrawnDisplay getDisplay() {
		return _puzzleDrawnDisplay;
	}

	public void mouseClicked(MouseEvent e)
	{
//		Find out which tile has been clicked, then request a move for it
		PuzzlePosition clickedTile = getDisplay().getTileFromPoint ( new Point(e.getX(), e.getY()) );
		getDisplay().requestPuzzleMove( clickedTile );
	}
}
