/*
 * @(#)PuzzleData.java	1.1 5-December-2003  Nigel Whitley
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
 * This class holds the logical representation of the puzzle. It records the position of each tile in a rectangular grid.
 */

public class PuzzleData {
	// Swapping values and swapping position do the same thing conceptually but define
	// a routine for each - could have an overloaded function swapTiles instead.
	int _rows;	// Number of rows in the puzzle
	int _columns;	// Number of columns in the puzzle
	int _tileValues [][];	// A map of the values in their current grid position : allows user to take a position and get the value there.
	PuzzlePosition _tilePositions [];	// A map of the positions of the current values : allows user to take a value and get its position
	Random1 shuffleRandom;	// Used for generating random moves when "shuffling" the puzzle.

	ArrayList<PuzzleListener> _puzzleListeners = new ArrayList<PuzzleListener>();		// Keep track of components interested in the puzzle changes

//	Add this listener into our list of interested parties : don't bother if they're known to us
	public void addPuzzleListener (PuzzleListener listener) {
		if (! _puzzleListeners.contains(listener)) {
			_puzzleListeners.add(listener);
		}
	}

//	Remove this listener from our list of interested parties
	public void removePuzzleListener (PuzzleListener listener) {
		_puzzleListeners.remove(listener);
	}

//	Tell all parties interested in this puzzle event that it has taken place
	public void notifyListeners (PuzzleEvent puzzleEvent) {
		ArrayList<PuzzleListener> copyOfListeners = new ArrayList<PuzzleListener> ();
		copyOfListeners.addAll(_puzzleListeners);

		for (PuzzleListener listener: copyOfListeners) {
			listener.puzzleChange(puzzleEvent);
		}
	}

//	Construct a puzzle data structure of the suggested size
	PuzzleData (Dimension puzzleSize) {
		buildPuzzleData( puzzleSize);
	}

//	Ensure constructor builds a minimal data structure
	PuzzleData () {
		this( new Dimension(2,2) );
	}

	// Wrapper methods for setting the size of the puzzle and getting the information back
	void setRows( int rows ) { _rows = rows; }
	void setColumns( int columns ) { _columns = columns; }

	int minRow() { return 0; }
	int minColumn() { return 0; }
	int maxRow() { return (getRows() - 1); }
	int maxColumn() { return (getColumns() - 1); }

	int getRows() { return _rows; }
	int getColumns() { return _columns; }

	// Set the two maps to have a consistent picture of the value at a position.
	void setTileValue( int tileRow, int tileColumn, int tileValue ) {
		_tileValues[tileRow][tileColumn] = tileValue;
		_tilePositions[tileValue - 1].setPuzzlePosition( tileRow, tileColumn );
	}
	void setTileValue( PuzzlePosition tilePosition, int tileValue ) {
		setTileValue( tilePosition.getRow(), tilePosition.getColumn(), tileValue);
	}

	// Wrapper methods for reading from the tile value and tile position maps.
	int getTileValue( int tileRow, int tileColumn ) { return _tileValues[tileRow][tileColumn]; }
	int getTileValue( PuzzlePosition tilePosition ) { return getTileValue(tilePosition.getRow(), tilePosition.getColumn() ); }
	PuzzlePosition getPuzzlePosition( int tileValue ) { return new PuzzlePosition ( _tilePositions[tileValue - 1] ); }

	// The target position for a value (tile) is useful for solving the puzzle and for knowing when it has been solved
	// The target position is the position a value had when the puzzle was first created i.e. before any shuffling.
	PuzzlePosition getTargetPositionForValue( int tileValue ) {
		int row = ( tileValue - 1 ) / getColumns();
		int column = ( tileValue - 1 ) - ( getColumns() * row );
		return new PuzzlePosition ( row, column );
	}

	// For a tile position to be valid, both the row and column ordinates must be valid
	boolean isValidPuzzlePosition( int tileRow, int tileColumn ) {
		// Is tile position within limits for row and column ?
		if ( ( tileRow < minRow() ) || ( tileRow > maxRow() ) ||
		     ( tileColumn < minColumn() ) || ( tileColumn > maxColumn() ) )
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	// Overload the method for a tile Position as well as individual row and column parameters
	boolean isValidPuzzlePosition( PuzzlePosition tilePosition ) { return isValidPuzzlePosition( tilePosition.getRow(), tilePosition.getColumn() ); }

	// The valid tile values are normally from 1 through the product of the number of rows and columns
	// e.g. a puzzle with 5 rows and 6 columns will have (valid) tile values of 1 through 30.
	// Note that the method uses the wrapper functions rather than hard coding 1.
	boolean validTileValue( int tileValue ) {
		// Is tile value within limits for row and column ?
		if ( ( tileValue > ( minRow() * minColumn() ) ) &&
		     ( tileValue <= ( maxRow() * maxColumn() ) ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// Define a "deliberately" invalid position.
	// This can be used when a fuction should return a position but no valid position can be returned.
	// For example, if getPositionAbove is called for a tile on the top row, no valid position can be returned.
	PuzzlePosition invalidPuzzlePosition( ) { return new PuzzlePosition( maxRow() + 1, maxColumn() + 1 ); }

	// Methods for getting positions relative to others.
	// These are useful, for example,  when identifying which positions can be moved
	PuzzlePosition getPositionAbove ( int row, int column ) {
		if ( ( ! isValidPuzzlePosition ( row, column) ) || ( row == minRow() ) )
		{
			return invalidPuzzlePosition();
		}
		else
		{
			return new PuzzlePosition( row - 1, column );
		}
	}

	PuzzlePosition getPositionBelow ( int row, int column ) {
		if ( ( ! isValidPuzzlePosition ( row, column) ) || ( row == maxRow() ) )
		{
			return invalidPuzzlePosition();
		}
		else
		{
			return new PuzzlePosition( row + 1, column );
		}
	}

	PuzzlePosition getPositionToLeft ( int row, int column ) {
		if ( ( ! isValidPuzzlePosition ( row, column) ) || ( column == minColumn() ) )
		{
			return invalidPuzzlePosition();
		}
		else
		{
			return new PuzzlePosition( row, column - 1 );
		}
	}

	PuzzlePosition getPositionToRight ( int row, int column ) {
		if ( ( ! isValidPuzzlePosition ( row, column) ) || ( column == maxColumn() ) )
		{
			return invalidPuzzlePosition();
		}
		else
		{
			return new PuzzlePosition( row, column + 1 );
		}
	}

	// Overload the methods for getting positions relative to others, so that puzzle positions can be passed in directly.
	PuzzlePosition getPositionAbove ( PuzzlePosition tilePosition ) { return getPositionAbove( tilePosition.getRow(), tilePosition.getColumn() ); }
	PuzzlePosition getPositionBelow ( PuzzlePosition tilePosition ) { return getPositionBelow( tilePosition.getRow(), tilePosition.getColumn() ); }
	PuzzlePosition getPositionToLeft ( PuzzlePosition tilePosition ) { return getPositionToLeft( tilePosition.getRow(), tilePosition.getColumn() ); }
	PuzzlePosition getPositionToRight ( PuzzlePosition tilePosition ) { return getPositionToRight( tilePosition.getRow(), tilePosition.getColumn() ); }

	// Swapping values and swapping position do the same thing conceptually but define
	// a routine for each - could have an overloaded function swapTiles instead.
	void swapTileValues( int tileValue1, int tileValue2 ) {
		if ( ( tileValue1 != tileValue2 ) &&
		       validTileValue ( tileValue1 ) &&
		       validTileValue ( tileValue2 ) )
		 {
		  // Get the current positions for the values
		  PuzzlePosition tilePosition1 = getPuzzlePosition( tileValue1 );
		  PuzzlePosition tilePosition2 = getPuzzlePosition( tileValue2 );

		  // Invert the values for the positions
		  setTileValue(tilePosition1, tileValue2 );
		  setTileValue(tilePosition2, tileValue1 );
		 }
	}
	void swapPuzzlePositions( PuzzlePosition tilePosition1, PuzzlePosition tilePosition2 ) {
		if ( ( ! tilePosition1.equals( tilePosition2 ) ) &&
		       isValidPuzzlePosition ( tilePosition1 ) &&
		       isValidPuzzlePosition ( tilePosition2 ) )
		 {
		  // Get the current values for the positions
		  int tileValue1 = getTileValue( tilePosition1 );
		  int tileValue2 = getTileValue( tilePosition2 );

		  // Invert the positions for the values
		  setTileValue(tilePosition2, tileValue1 );
		  setTileValue(tilePosition1, tileValue2 );

		 }
	}

	// The point of the puzzle is to be able to move the tiles around.
	// This method encapsulates that : obviously only a few "movable" tiles can be moved. These are next to the blank tile.
	public void moveTile( PuzzlePosition tilePosition ) {
		if ( isTileMovable( tilePosition ) )
		{
			// Get the current positions for the blank tile
			PuzzlePosition blankPuzzlePosition = getPuzzlePosition( blankTileValue() );

//			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, tilePosition, "PreMove" );
//			notifyListeners(puzzleEvent);

			// Invert the positions for the tiles
			swapPuzzlePositions( tilePosition, blankPuzzlePosition );

//			puzzleEvent = new PuzzleEvent ( this, blankPuzzlePosition );
//			notifyListeners(puzzleEvent);

		}


	}

	// The "blank" tile will have the largest "value" in the tile puzzle. In a rectangular puzzle this
	// must be the product of the number of rows and columns.
	int blankTileValue() { return (getRows() * getColumns()); }

	// Is the tile at the given row and column the "blank" tile ?
	boolean isTileBlank( int tileRow, int tileColumn ) { return ( getTileValue( tileRow, tileColumn ) == blankTileValue() ); }
	boolean isTileBlank( PuzzlePosition tilePosition ) { return ( isTileBlank( tilePosition.getRow(), tilePosition.getColumn() ) ); }

	// Is the tile at the given row and column movable (can it become the "blank" tile) ?
	boolean isTileMovable( int tileRow, int tileColumn ) {
		boolean movable;
		movable = false;
		// If this is the blank tile, it can't be a movable tile
		if ( isValidPuzzlePosition ( tileRow, tileColumn ) && ( ! isTileBlank ( tileRow, tileColumn ) ) )
		{
			// Is blank tile "above"
			if (tileRow > minRow())
			{
				if (isTileBlank ( getPositionAbove ( tileRow , tileColumn ) ) )
				{
					movable = true;
				}
			}

			// Is blank tile "below"
			if (tileRow < maxRow())
			{
				if (isTileBlank ( getPositionBelow ( tileRow , tileColumn ) ) )
				{
					movable = true;
				}
			}

			// Is blank tile to "left"
			if (tileColumn > minColumn())
			{
				if (isTileBlank ( getPositionToLeft ( tileRow , tileColumn ) ) )
				{
					movable = true;
				}
			}

			// Is blank tile to "right"
			if (tileColumn < maxColumn())
			{
				if (isTileBlank ( getPositionToRight ( tileRow , tileColumn ) ) )
				{
					movable = true;
				}
			}
		}
		return movable;
	}
	boolean isTileMovable( PuzzlePosition tilePosition ) { return ( isTileMovable( tilePosition.getRow(), tilePosition.getColumn() ) ); }

	// Build and return a vector (list) of the positions of tiles in the puzzle which can be moved
	// There may be two, tree or four of these.
	public ArrayList<PuzzlePosition> getMovablePuzzlePositions( )
	{
		ArrayList<PuzzlePosition> movableTiles = new ArrayList<PuzzlePosition>();
		PuzzlePosition blankPuzzlePosition = getPuzzlePosition (blankTileValue());
		PuzzlePosition tilePosition = getPositionToLeft ( blankPuzzlePosition );
		if ( isValidPuzzlePosition ( tilePosition ) )
		{
			movableTiles.add ( tilePosition );
		}
		tilePosition = getPositionToRight ( blankPuzzlePosition );
		if ( isValidPuzzlePosition ( tilePosition ) )
		{
			movableTiles.add ( tilePosition );
		}
		tilePosition = getPositionAbove ( blankPuzzlePosition );
		if ( isValidPuzzlePosition ( tilePosition ) )
		{
			movableTiles.add ( tilePosition );
		}
		tilePosition = getPositionBelow ( blankPuzzlePosition );
		if ( isValidPuzzlePosition ( tilePosition ) )
		{
			movableTiles.add ( tilePosition );
		}
		return movableTiles;
	}

	// Build and return a vector (list) of the values of tiles in the puzzle which can be moved
	// There may be two, tree or four of these.
	public ArrayList<Integer> getMovableTileValues( )
	{
		ArrayList<Integer> movableTiles = new ArrayList<Integer>();
		PuzzlePosition blankPuzzlePosition = getPuzzlePosition (blankTileValue());
		PuzzlePosition tilePosition = getPositionToLeft ( blankPuzzlePosition );
		if ( isValidPuzzlePosition ( tilePosition ) )
		{
			movableTiles.add ( new Integer ( getTileValue (tilePosition ) ) );
		}
		tilePosition = getPositionToRight ( blankPuzzlePosition );
		if ( isValidPuzzlePosition ( tilePosition ) )
		{
			movableTiles.add ( new Integer ( getTileValue (tilePosition ) ) );
		}
		tilePosition = getPositionAbove ( blankPuzzlePosition );
		if ( isValidPuzzlePosition ( tilePosition ) )
		{
			movableTiles.add ( new Integer ( getTileValue (tilePosition ) ) );
		}
		tilePosition = getPositionBelow ( blankPuzzlePosition );
		if ( isValidPuzzlePosition ( tilePosition ) )
		{
			movableTiles.add ( new Integer ( getTileValue (tilePosition ) ) );
		}
		return movableTiles;
	}

	// Initialise the data to be in "solved" order.
	void buildPuzzleData( ) {
		shuffleRandom = new Random1();
		_tilePositions = new PuzzlePosition[getRows()*getColumns()];
		_tileValues = new int[getRows()][getColumns()];

		for (int tileValue = 1; tileValue <= ( getRows()*getColumns() ); tileValue++)
		{
			PuzzlePosition tilePosition = getTargetPositionForValue( tileValue );
			_tilePositions[tileValue - 1] = tilePosition;
			setTileValue( tilePosition.getRow(), tilePosition.getColumn(), tileValue );
		}
	}

	// Set the size of the puzzle then initialise it.
	void buildPuzzleData( int rows, int columns ) {
		setRows ( rows );
		setColumns ( columns );
		buildPuzzleData();
	}

	// Set the size of the puzzle then initialise it.
	void buildPuzzleData( Dimension puzzleSize ) {
		setRows ( puzzleSize.height );
		setColumns ( puzzleSize.width );
		buildPuzzleData();
	}

	// Shuffle the puzzle by randomly moving a tile.
	// Just pick pick one of the movable tiles at random and move it.
	void shuffle() {
		ArrayList<PuzzlePosition> movablePuzzlePositions = getMovablePuzzlePositions();
		int shuffleChoice = shuffleRandom.nextIntFromRange(movablePuzzlePositions.size());
		moveTile( movablePuzzlePositions.get( shuffleChoice ) );
	}

	// Repeatedly shuffle the puzzle (to give the user something to do !)
	void shufflePuzzle() {
		int numberOfTiles = getRows() * getColumns();
		int numberOfShuffles = numberOfTiles * numberOfTiles;
		for ( int manyShuffles = 0; manyShuffles < numberOfShuffles; manyShuffles++ )
		{
			shuffle();
		}
	}

	// Repeatedly shuffle the puzzle (to give the user something to do !)
	public void sendMessage( String puzzleMessage) {
//	Display the text in the information panel.
		PuzzleEvent messageEvent = new MessageEvent ( this, puzzleMessage );
		notifyListeners(messageEvent);
	}

    }

