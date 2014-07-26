/*
 * @(#)AutoData.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2001-2003 Nigel Whitley. All Rights Reserved.
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
 * This component is the base for displaying the puzzle when it can be automatically solved.
 * It adds variables and methods for tracking the automatic solution.
 */
public class AutoData extends PuzzleData {

	PuzzleSolution _autoSolution = null;	// Holds the current solution when solving automatically
	boolean _autoSolving;	// Indicates whether the solution method is manual(false) or automatic (true)
	int _solvingValue;	// Holds the value of the tile which is being "solved" i.e. moved to its final position.


//	The default constructor for the display sets safe initial values for internal variables.
	AutoData() {
		super();
		clearAutoSolution();
		setSolvingValue( 0 );
		setAutoSolving( false );
	}

//	The default constructor for the display sets safe initial values for internal variables.
	AutoData( Dimension puzzleSize) {
		super(puzzleSize);
		clearAutoSolution();
		setSolvingValue( 0 );
		setAutoSolving( false );
	}

//	Other components may be interested in solution events.
	public void addSolutionListener (SolutionListener listener) {
		_autoSolution.addSolutionListener(listener);
	}

	public void removeSolutionListener (SolutionListener listener) {
		_autoSolution.removeSolutionListener(listener);
	}

	public void notifyListeners (SolutionEvent solutionEvent) {
		_autoSolution.notifyListeners (solutionEvent);
	}

	void setSolvingValue( int solvingValue ) { _solvingValue = solvingValue; }
	int getSolvingValue() { return _solvingValue; }

	public void setAutoSolving( boolean autoSolving ) { _autoSolving = autoSolving; }
	public boolean isAutoSolving( ) { return _autoSolving; }
	public boolean isSolved( ) {
		return ( getLowestUnsolvedValue() == 0 );
	}

	public void setAutoSolution ( PuzzleSolution autoSolution ) { _autoSolution = autoSolution; }
	public void clearAutoSolution ( ) { _autoSolution = null; }
	public PuzzleSolution getAutoSolution () { return _autoSolution; }

	// The lowest unsolved value will normally be the next one to be solved.
	// All tiles with lower values are already in their target positions.
	int getLowestUnsolvedValue () {
		int lowestUnsolvedValue = 0;
		int tileValue = 1;

		// Find the next unsolved tile. The last two rows require special treatment : above them,
		// simply scan each row from top to bottom, left to right. In practice, this means counting
		// up from 1.
		while ( ( lowestUnsolvedValue == 0 ) && ( tileValue <= ( ( getRows() - 2 ) * getColumns() ) ) )
		{
			if ( ! ( getPuzzlePosition( tileValue ).equals( getTargetPositionForValue( tileValue ) ) ) )
			{
				lowestUnsolvedValue = tileValue;
			}
			tileValue++;
		}

                // The last two rows require special treatment : move column by column from the left, checking
                // the penultimate row, then the last row. Don't need to check the last column.
		while ( ( lowestUnsolvedValue == 0 ) && ( tileValue < ( ( getRows() - 1 ) * getColumns() ) ) )
		{
			if ( ! ( getPuzzlePosition( tileValue ).equals( getTargetPositionForValue( tileValue ) ) ) )
			{
				lowestUnsolvedValue = tileValue;
			}
			else if ( ! ( getPuzzlePosition( tileValue + getColumns() ).equals( getTargetPositionForValue( tileValue + getColumns()) ) ) )
			{
				lowestUnsolvedValue = tileValue + getColumns();
			}
			tileValue++;
		}

		if ( ( lowestUnsolvedValue == 0 ) && ( ! getPuzzlePosition( ( getRows() - 1 ) * getColumns() ).equals( getTargetPositionForValue( ( getRows() - 1 ) * getColumns() ) ) ) )
		{
			lowestUnsolvedValue = ( getRows() - 1 ) * getColumns();
		}

                return lowestUnsolvedValue;
	}

	// Initialise the data to be in "solved" order.
	public void buildPuzzleData( ) {
		setAutoSolving(false);
		clearAutoSolution();
		super.buildPuzzleData();
	}

	// Set the size of the puzzle then initialise it.
	public void buildPuzzleData( int rows, int columns ) {
		setAutoSolving(false);
		clearAutoSolution();
		super.buildPuzzleData( rows, columns );
	}

    }
