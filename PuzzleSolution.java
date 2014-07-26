/*
 * @(#)PuzzleSolution.java	1.1 5-December-2003  Nigel Whitley
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
 * An automated solution will clearly consist of many tile moves.
 * These classes build on the classes in PuzzleMove to permit a complete solution.
 * Later classes can build on previous ones to make succesively more complex moves.
 * It must be possible to perform the moves a tile at a time by repeatedly obtaining the next tile to move.
 */

// Start with the most basic functions for tile moves.
// The general idea is that we move a tile of a certain value to a new position.
// Of course, the move also needs a reference to the data for the puzzle.
// The complete move may consist of more than one tile move.
// Consequently, we also need to be able to check when the move is complete i.e. if the tile has arrived at its final position.
class PuzzleSolution extends PuzzleMove {

	PuzzleSolution( int targetValue, PuzzlePosition targetPosition, PuzzleData tileData ) {
		super( targetValue, targetPosition, tileData );
	}

	private ArrayList<SolutionListener> _solutionListeners = new ArrayList<SolutionListener>();

	public void addSolutionListener (SolutionListener listener) {
		if (! _solutionListeners.contains(listener)) {
			_solutionListeners.add(listener);
		}
	}

	public void removeSolutionListener (SolutionListener listener) {
		_solutionListeners.remove(listener);
	}

	public void notifyListeners (SolutionEvent solutionEvent) {
		ArrayList<SolutionListener> copyOfListeners =  new ArrayList<SolutionListener>();
		copyOfListeners.addAll(_solutionListeners);

		for ( SolutionListener listener : _solutionListeners ) {
			listener.solutionChanged(solutionEvent);
		}
	}

    }


