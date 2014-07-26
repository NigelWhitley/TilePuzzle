/*
 * @(#)PuzzlePosition.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2001-2003 Nigel Whitley. All Rights Reserved.
 *
 * This software is released under the terms of the GPL. For details, see license.txt.
 *
 */

import java.awt.*;

public class PuzzlePosition {
        int _row;
        int _column;

	PuzzlePosition( int row, int column ) { setPuzzlePosition ( row, column ); }
	PuzzlePosition( PuzzlePosition tilePosition ) { setPuzzlePosition ( tilePosition ); }
	void setPuzzlePosition( int row, int column ) { _row = row; _column = column; }
	void setPuzzlePosition( PuzzlePosition tilePosition ) { _row = tilePosition._row; _column = tilePosition._column; }
	PuzzlePosition getPuzzlePosition() { return new PuzzlePosition( _row, _column ); }
	int getRow() { return _row; }
	int getColumn() { return _column; }
	boolean equals (PuzzlePosition otherTile) { 
		if ( ( getRow() == otherTile.getRow() ) &&
		     ( getColumn() == otherTile.getColumn() ) )
		 {
		  return true;
		 }
		else
		 {
		  return false;
		 }
	}

	boolean isBelow( PuzzlePosition tilePosition ) { return ( getRow() > tilePosition.getRow() ); }
	boolean isAbove( PuzzlePosition tilePosition ) { return ( getRow() < tilePosition.getRow() ); }
	boolean isToLeftOf( PuzzlePosition tilePosition ) { return ( getColumn() < tilePosition.getColumn() ); }
	boolean isToRightOf( PuzzlePosition tilePosition ) { return ( getColumn() > tilePosition.getColumn() ); }
	boolean isSameRow( PuzzlePosition tilePosition ) { return ( getRow() == tilePosition.getRow() ); }
	boolean isSameColumn( PuzzlePosition tilePosition ) { return ( getColumn() == tilePosition.getColumn() ); }

    }

