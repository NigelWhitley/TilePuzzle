/*
 * @(#)SolveTilePuzzle.java	1.1 5-December-2003  Nigel Whitley
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


// Having laid the groundwork, we can develop a strategy to move the tile to their target positions (and the classes to support that strategy).
// The overall approach is to solve the tile from left to right and from top to bottom.
// So the first tile solved will be at the top left, then the one immediately to the right of it.
// When all the tiles in the row have been solved, the next row down will be solved starting with the one on the left.
// The exceptions to this approach are the last two rows : they will be solved a column at a time.
// Therefore the leftmost tile in the bottom row will be solved after the leftmost tile in the penultimate row and so on.

// This is the class to move the target tile to its target position in most cases.
// The class will not be called if any of the following "special" conditions hold true
// a) the target tile column is less than the column of its target position.
// b) the target position is in the maximum column.
// c) the target position is in the maximum row.
// If none of the special conditions apply, repeatedly move the blank to immediately above or below the target tile and then move it.
class SolveNormal extends PuzzleSolution {

	// The first task is to move the the blank next to the target tile (make the target rotatable) but, if that has already been done,
	// the next part of the move will be calculated.
	SolveNormal ( int targetValue, PuzzleData tileData ) {
		super( targetValue, tileData.getTargetPositionForValue (targetValue), tileData );
		_subMove = new MakeTileRotatable( getTargetValue(), getPuzzleData() );
		if ( _subMove.isComplete() )
		{
			calculateNextMove();
		}
	}

	// All remaining moves of the target tile must be either up or to the left.
	// Therefore,  the next move (to be created as the subMove) is either
	// a) to do nothing (because the tile is in the target position and the move is therefore complete)
	// b) to move the target tile up
	// c) to move the target tile to the left
	// d) to move the blank to immediately above the target tile
	// e) to move the blank to immediately to the left of the target tile
	void calculateNextMove () {
		_subMove = null;
		_blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );
		PuzzlePosition sourcePosition = getPuzzlePosition ( getTargetValue () );	// The source position is the current position of the tile value.

		// First, if there is nothing to do, do nothing !
		if ( sourcePosition.equals( getTargetPosition () ) )
		{
			_subMove = null;
		}
		// Is the tile immediately below the target position ?
		else if ( getPositionAbove( sourcePosition ).equals ( getTargetPosition () ) )
		{
			// Is the target position blank ?
			if ( isTileBlank ( getPositionAbove( sourcePosition ) ) )
			{
				// Is the target tile currently not in the bottom row ?
				if ( isValidPuzzlePosition ( getPositionBelow( sourcePosition ) ) )
				{
					// Is the next target tile already in the solved position and is the one after that intended for the same row
					if ( ( getTargetPosition().isSameRow ( getPuzzleData().getTargetPositionForValue( getTargetValue() + 2 ) ) ) && ( getTileValue( getPositionAbove( getPositionToRight( sourcePosition ) ) ) == ( getTargetValue() + 1 ) ) )
					{
						if ( ( isValidPuzzlePosition( getPositionToRight( sourcePosition ) ) ) && ( getTileValue( getPositionToRight( sourcePosition ) ) == ( getTargetValue()  + 2 ) ) )
						//if ( getTileValue( getPositionToRight( sourcePosition ) ) == ( getTargetValue()  + 2 ) )
						{
							// Target position is blank, we can just move the tile into place.
							_subMove = new SolveFromLeft( getTargetValue() + 2 , getPuzzleData(), 2 );
							//System.out.println("setting from left stage 2");
						}
						else if ( ( isValidPuzzlePosition ( getPositionToRight( getPositionToRight( sourcePosition ) ) ) ) && ( getTileValue( getPositionToRight( getPositionToRight( sourcePosition ) ) ) == ( getTargetValue () + 2 ) ) )
						{
							// Target position is blank, we can just move the tile into place.
							_subMove = new SolveLastColumn( getTargetValue() + 2 , getPuzzleData(), 3 );
							//System.out.println("setting last column stage 3");
						}
						else
						{
							// Target position is blank, we can just move the tile into place.
							_subMove = new MoveTile( getTargetValue() , getPuzzleData() );
						}
					}
					else
					{
						// Target position is blank, we can just move the tile into place.
						_subMove = new MoveTile( getTargetValue() , getPuzzleData() );
					}
				}
				else
				{
					// Is the next target tile ready to start the special move for the last row ?
					if ( ( isValidPuzzlePosition ( getPositionToRight( getPositionToRight( sourcePosition ) ) ) ) && ( getTileValue( getPositionToRight( getPositionToRight( sourcePosition ) ) ) == ( getTargetValue() + getPuzzleData().getColumns() ) ) )
					{
						// Target position is blank, we can just move the tile into place.
						_subMove = new SolveLastRow( getTargetValue() + getPuzzleData().getColumns(), getPuzzleData(), 3 );
					}
					else
					{
						// Target position is blank, we can just move the tile into place.
						_subMove = new MoveTile( getTargetValue() , getPuzzleData() );
					}
				}
			}
			else
			{
				// Move blank to target position by Anti Rotating around the current position of the tile value
				// Need to override the default reference position to ensure the target tile isn't moved.
				_subMove = new AntiRotateBlankTo( getPositionAbove ( sourcePosition ) , getPuzzleData(), sourcePosition );
			}
		}
		// Is the blank tile to the right of the target tile ?
		else if ( _blankPuzzlePosition.isToRightOf( sourcePosition ) )
		{
			// Are the rows above the target tile already solved ?
			if ( getTargetPosition().isSameRow( sourcePosition ) )
			{
				// Make sure that moving the blank will not move the already solved tiles - rotate it under the target tile
				// Need to override the default reference position to ensure the target tile isn't moved.
				_subMove = new RotateBlankTo( getPositionToLeft ( sourcePosition ) , getPuzzleData(), sourcePosition );
			}
			else
			{
				// Don't need to worry that moving the blank will move the already solved tiles - anti rotate it to the target position
				// Need to override the default reference position to ensure the target tile isn't moved.
				_subMove = new AntiRotateBlankTo( getPositionAbove ( sourcePosition ) , getPuzzleData(), sourcePosition );
			}
		}
		// Is the blank tile below the target tile ?
		else if ( _blankPuzzlePosition.isBelow( sourcePosition ) )
		{
			// Will the target tile move left ?
			if ( sourcePosition.isToRightOf ( getTargetPosition() ) )
			{
				// Rotate the blank to let the target tile move left
				_subMove = new RotateBlankTo( getPositionToLeft ( sourcePosition ) , getPuzzleData() );
			}
			// If the blank is to the left of the source, we can safely use rotate to move it above the target tile
			else if ( _blankPuzzlePosition.isToLeftOf ( sourcePosition ) )
			{
				// Rotate the blank to let the target tile move up
				_subMove = new RotateBlankTo( getPositionAbove ( sourcePosition ) , getPuzzleData() );
			}
			else
			{
				// The target may be in the leftmost column so use anti-rotate to move the blank above the target tile.
				_subMove = new AntiRotateBlankTo( getPositionAbove ( sourcePosition ) , getPuzzleData() );
			}
		}
		// Is the blank tile to the left of the target tile (but obviously not below) ?
		else if ( _blankPuzzlePosition.isToLeftOf ( sourcePosition ) )
		{
			// Will the target tile move left ?
			if ( sourcePosition.isToRightOf ( getTargetPosition() ) )
			{
				// If the blank is above the target tile, we need to move it the long way around to the left of the target tile
				if ( _blankPuzzlePosition.isAbove ( sourcePosition ) )
				{
					// Antirotate the blank the long way around to the left of the target tile
					_subMove = new AntiRotateBlankTo( getPositionToLeft ( sourcePosition ) , getPuzzleData() );
				}
				else
				{
					// Nice and simple, just move the blank one to the right.
					_subMove = new MoveTile( getPositionToRight ( _blankPuzzlePosition ) , getPuzzleData() );
				}
			}
			else
			{
				// Target tile can't move left, so rotate the blank to above the target tile
				_subMove = new RotateBlankTo( getPositionAbove ( sourcePosition ) , getPuzzleData(), sourcePosition );
			}
		}
		else
		{
			// Just move the blank down (which may move the target tile up)
			_subMove = new MoveTile( getTileValue( getPositionBelow( _blankPuzzlePosition ) ) , getPuzzleData() );
		}
	}

    };


// This is the class to move the target tile to its target position when the target tile column is less than the column of its target position.
// This class will not be used in these "special" conditions
// a) the target position is in the maximum column.
// b) the target position is in the maximum row.
// If none of the special conditions apply, move the target tile right or up until it can be solved with SolveNormal.
class SolveFromLeft extends PuzzleSolution {
	PuzzlePosition sourcePosition;

	// In the constructor, we must calculate the next move.
	SolveFromLeft ( int targetValue, PuzzleData tileData, int initStage ) {
		super( targetValue, tileData.getTargetPositionForValue (targetValue), tileData );
		setStage ( initStage );
		calculateNextMove();
	}

	// In the constructor, we must calculate the next move.
	SolveFromLeft ( int targetValue, PuzzleData tileData ) {
		this(targetValue, tileData, 0);
	}

	// All moves of the target tile (within this move) must be either up or to the right.
	// Therefore,  the next move (to be created as the subMove) is either
	// a) a SolveNormal (because the tile is no longer to the left of its target position and the move is therefore complete)
	// b) to move the target tile up
	// c) to move the target tile to the right
	// d) to move the blank to immediately above the target tile
	// e) to move the blank to immediately to the right of the target tile
	void calculateNextMove () {
		_subMove = null;
		_blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );
		sourcePosition = getPuzzlePosition ( getTargetValue () );
		nextStage();

		if ( getStage() == 1 )
		{
			// We stop when no longer solving from the left i.e. the target tile is NOT to the left of its target position
			if ( ! sourcePosition.isToLeftOf( getTargetPosition () ) )
			{
				// No longer solving from left - do nothing
				_subMove = null;
			}
			// If the blank is to the left of the target tile, it needs to be moved to either above or to the right of the target tile.
			else if ( _blankPuzzlePosition.isToLeftOf ( sourcePosition ) )
			{
				// Is the target tile only one row below its target position ?
				if ( getPositionAbove( sourcePosition ).isSameRow ( getTargetPosition() ) )
				{
					// Is the target tile only one column to the left of its target position ?
					if ( getPositionToRight( sourcePosition ).isSameColumn ( getTargetPosition() ) )
					{
						// Is the blank tile immediately to the left of the target tile ?
						if ( getPositionToLeft( sourcePosition ).equals ( _blankPuzzlePosition ) )
						{
							// We can start a special move which rotates the target tile and its two predecessors
							// then slides all three into place in reverse order
							nextStage();
						}
						else
						{
							// Move the blank to its starting position (immediately to the left of the target tile)
							_subMove = new RotateBlankTo( getPositionToLeft ( sourcePosition ), getPuzzleData() );
						}
					}
					else
					{
						// Must avoid disturbing the already "solved" tiles, so anti-rotate the blank under the target tile to the right of it
						_subMove = new AntiRotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData() );
					}
				}
				else
				{
					// The already "solved" tiles are out of the way, so rotate the blank to above the target tile
					// If the blank is above the target tile, we can move it more directly
					if ( _blankPuzzlePosition.isAbove ( sourcePosition ) )
					{
						_subMove = new RotateBlankTo( getPositionAbove ( sourcePosition ), getPuzzleData(), getPositionToLeft (sourcePosition ) );
					}
					else
					// If the blank is not above the target tile, make sure it doesn't move through it
					{
						_subMove = new RotateBlankTo( getPositionAbove ( sourcePosition ), getPuzzleData(), sourcePosition );
					}
				}
			}
			// If the blank is below the target tile, it should be moved to the right of the target tile.
			else if ( _blankPuzzlePosition.isBelow( sourcePosition ) )
			{
				// Anti-rotate the blank to the right of the target tile
				_subMove = new AntiRotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData() );
			}
			// Is the blank is to the right of the target tile ?
			else if ( _blankPuzzlePosition.isToRightOf( sourcePosition ) )
			{
			// If the target tile is on the same row as the blank tile, just move the tile to left of the blank (which will move the blank left)
				if ( _blankPuzzlePosition.isSameRow( sourcePosition ) )
				{
					// Just move the tile to left of the blank (which will move the blank left)
					_subMove = new MoveTile( getPositionToLeft ( _blankPuzzlePosition ), getPuzzleData() );
				}
				// If the blank is one column to the right of the target tile, move the blank down the column until the target tile is movable
				else if ( _blankPuzzlePosition.isSameColumn( getPositionToRight ( sourcePosition ) ) )
				{
					// Move the blank down the column until the target tile is movable
					_subMove = new RotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData(), sourcePosition );
				}
				else
				{
					// Rotate the blank to the right of the target tile
					_subMove = new RotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData() );
				}
			}
			else
			{
				// Move the blank tile down (which may move the target tile up)
				_subMove = new MoveTile( getPositionBelow ( _blankPuzzlePosition ), getPuzzleData() );
			}
		}

		if ( getStage() == 1 )
		{
			setStage ( 0 );     // Use this to avoid having an additional Move object
		}

		if ( getStage() == 2 )
		{
			// Move the first solved tile out of the way
			_subMove = new MoveTile( getPositionToLeft ( getPositionAbove ( sourcePosition ) ), getPuzzleData() );
			//System.out.println("stage 2");
		}

		if ( getStage() == 3 )
		{
			// Move the second solved tile out of the way
			_subMove = new MoveTile( getPositionAbove ( sourcePosition ), getPuzzleData() );
			//System.out.println("stage 3");
		}

		if ( getStage() == 4 )
		{
			// Move the target tile up into the target row then move the blank to the target column
			_subMove = new AntiRotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData(), getPositionToRight ( getPositionAbove ( sourcePosition ) ) );
			//System.out.println("stage 4");
		}

		if ( getStage() == 5 )
		{
			// Move the target tile into place then move the previously solved tiles back into position.
			_subMove = new AntiRotateBlankTo( getPositionBelow ( getPositionToLeft ( sourcePosition ) ), getPuzzleData(), getPositionBelow ( sourcePosition ) );
		}
	}
    };


// This is the class to move the target tile to its target position when the target position is in the rightmost column.
// This class will not be used if the target position is in the maximum row.
// This is the first complex move which needs to be moved in stages i.e. it is not possible to calculate the next move merely by looking at the current position of tiles.
// After some initial positioning, whenver calculateNextMove is called it will move on to the next stage and use that to determine the next move.
class SolveLastColumn extends PuzzleSolution {
	PuzzlePosition sourcePosition;

	// In the constructor, we must initialise the stage counter and calculate the next move.
	SolveLastColumn ( int targetValue, PuzzleData tileData ) {
		this(targetValue, tileData, 0);
	}

	// In the constructor, we must initialise the stage counter and calculate the next move.
	SolveLastColumn ( int targetValue, PuzzleData tileData, int initStage ) {
		super( targetValue, tileData.getTargetPositionForValue (targetValue), tileData );
		setStage ( initStage );
		calculateNextMove();
	}

	// After some initial positioning, whenver calculateNextMove is called it will move on to the next stage and use that to determine the next move.
	void calculateNextMove () {
		nextStage();
		_subMove = null;
		_blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );
		sourcePosition = getPuzzlePosition ( getTargetValue () );

		// The first stage is to move the target tile to immediately below the target position (or terminate the move because there's nothing to do).
		if ( ( getStage() == 1 ) && ( ! sourcePosition.equals ( getTargetPosition() ) ) )
		{
			// If the blank tile is immediately above the target tile, just move the target tile up (which may complete the move)
			if ( isTileBlank ( getPositionAbove( sourcePosition ) ) )
			{
				// Move the target tile up (which may complete the move) but stay at stage 1 by re-initialising the stage.
				_subMove = new MoveTile( getTargetValue(), getPuzzleData() );
				setStage ( 0 );     // Use this to avoid having an additional Move object
			}
			// If the target position is immediately above the target tile, just move to the next stage and get the blank in the right place
			else if ( getPositionAbove ( sourcePosition ).equals ( getTargetPosition() ) )
			{
				// Still need to get the blank in the right starting position - move to stage 2 to start our choreography
				nextStage();
			}
			// If the blank tile is in the rows above the target tile, move the blank to immediately above the target tile
			else if ( _blankPuzzlePosition.isAbove ( sourcePosition ) )
			{
				// Rotate the blank to above the target tile but stay at stage 1 by re-initialising the stage.
				// By using the tile to the left of the target tile as the reference position, we can cater for blanks to the right of target tile
				_subMove = new RotateBlankTo( getPositionAbove ( sourcePosition ), getPuzzleData(), getPositionToLeft ( sourcePosition ) );
				setStage ( 0 );     // Use this to avoid having an additional Move object
			}
			else
			{
				// Rotate the blank to above the target tile but stay at stage 1 by re-initialising the stage.
				// Using the target tile as the reference position, we cater for blanks in the same row but to the right
				_subMove = new RotateBlankTo( getPositionAbove ( sourcePosition ), getPuzzleData(), sourcePosition );
				setStage ( 0 );     // Use this to avoid having an additional Move object
			}
		}

		// In the second stage we have a few possible variations for getting the blank into the right starting position
		if ( getStage() == 2 )
		{
			// If the blank tile is immediately to the left of the target tile, its in a good position so just move to the next stage
			if ( isTileBlank ( getPositionToLeft( sourcePosition ) ) )
			{
				// The blank tile is in a valid start position so just move to the next stage
				_subMove = new MoveBlankLeftTo( getPositionToLeft( getPositionToLeft( sourcePosition ) ), getPuzzleData() );
			}
			// If the blank tile is two columns to the left of the target tile, its in a good position so just move to the next stage
			else if ( isTileBlank ( getPositionToLeft( getPositionToLeft( sourcePosition ) ) ) )
			{
				// The blank tile is in a good position so just move to the next stage
				nextStage();
			}
			// If the blank tile is below the target tile, we can rotate the blank into place to the left of the target tile
			else if ( _blankPuzzlePosition.isBelow ( sourcePosition ) )
			{
				// Rotate the blank into place to the left of the target tile
				_subMove = new RotateBlankTo( getPositionToLeft ( getPositionToLeft ( sourcePosition ) ), getPuzzleData() );
			}
			else
			{
				// Keep the solved tiles safe by anti-rotating the blank into place above the target tile
				_subMove = new AntiRotateBlankTo( getPositionToLeft ( getPositionToLeft ( sourcePosition ) ), getPuzzleData(), getPositionAbove (sourcePosition ) );
			}
		}

		// Having moved the target tile and blank into position, the remaining moves are set
		if ( getStage() == 3 )
		{
			// Move the solved tile down which is two columns to the left and one up
			_subMove = new RotateBlankTo( getPositionAbove ( getPositionToLeft ( getPositionToLeft ( sourcePosition ) ) ), getPuzzleData(), getPositionToLeft ( sourcePosition ) );
		}

		// Move to the left the solved tile which is one column up and one row to the left of the target tile
		// Then make room for the target tile by moving the incumbent tile out of the target position.
		// Finally, move the target tile to the target position.
		if ( getStage() == 4 )
		{
			// Move the target tile into position, after disturbing another solved tile.
			_subMove = new RotateBlankTo( sourcePosition, getPuzzleData(), getPositionToLeft ( sourcePosition ) );
		}

		// Having moved the target tile into position, we must restore the two solved tiles which have been disturbed.
		// First, remove from the target row the tile which had been in the target position.
		if ( getStage() == 5 )
		{
			// This will move the blank into position to restore the previously solved tiles
			_subMove = new RotateBlankTo( getPositionToLeft ( getTargetPosition() ), getPuzzleData() );
		}

		// By moving the blank to the left and then down, we will restore the two solved tiles which have been disturbed.
		if ( getStage() == 6 )
		{
			// This will complete the move
			_subMove = new AntiRotateBlankTo( getPositionBelow ( getPositionToLeft ( getPositionToLeft ( getTargetPosition() ) ) ), getPuzzleData() );
		}
	}

    };


// This is the class to move the target tile to its target position when the target position is in the right column of two.
// This class is essentially the same as SolveLastRow but reflected along the diagonal
class SolveRightColumn extends PuzzleSolution {
	PuzzlePosition sourcePosition;

	// In the constructor, we must initialise the stage counter and calculate the next move.
	SolveRightColumn ( int targetValue, PuzzleData tileData ) {
		this(targetValue, tileData, 0);
	}

	// In the constructor, we must initialise the stage counter and calculate the next move.
	SolveRightColumn ( int targetValue, PuzzleData tileData, int initStage ) {
		super( targetValue, tileData.getTargetPositionForValue (targetValue), tileData );
		setStage ( initStage );
		calculateNextMove();
	}

	// After some initial positioning, whenver calculateNextMove is called it will move on to the next stage and use that to determine the next move.
	void calculateNextMove () {
		nextStage();
		_subMove = null;
		_blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );
		sourcePosition = getPuzzlePosition ( getTargetValue () );

		// The first stage is to move the target tile to immediately below the target position (or terminate the move because there's nothing to do).
		if ( ( getStage() == 1 ) && ( ! sourcePosition.equals ( getTargetPosition() ) ) )
		{

			// Is the target tile in the row immediately below the target position ?
			if ( getPositionAbove ( sourcePosition ).isSameRow ( getTargetPosition() ) )
			{
				// If the target tile is immediately below the target position and the target position is blank, we can move it in right away
				if ( getPositionAbove ( sourcePosition ).equals ( getTargetPosition() ) )
				{
					// If the target tile is immediately to the right of the target position and the target position is blank, we can move it in right away
					if ( isTileBlank ( getPositionAbove( sourcePosition ) ) )
					{
						// The blank is between the target tile and the target position and on the same row, so just move to the stage 3
						_subMove = new MoveTile( getTargetValue(), getPuzzleData() );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
					else
					{
						// The target tile is in a situation which needs this move - proceed to the next stage
						nextStage();
					}
				}
				// The target tile is immediately to the left and down from the target position
				else
				{
					// If the blank is in the target position or under the target tile, we can move it to the target tile by anti-rotating
					if ( ! _blankPuzzlePosition.isBelow ( sourcePosition ) )
					{
						// Can safely move the blank to the target tile - this will move the target tile right one columnw
						_subMove = new RotateBlankTo( sourcePosition, getPuzzleData() );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
					// If the blank tile is on the same column as the target position, rotate it to the target tile
					else if ( _blankPuzzlePosition.isToRightOf ( sourcePosition ) )
					{
						// Can safely move the blank to the target tile - this will move the target tile down one row
						_subMove = new AntiRotateBlankTo( sourcePosition, getPuzzleData() );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
					else
					{
						// Move the blank to the target tile - this will move the target tile right one column
						_subMove = new RotateBlankTo( sourcePosition, getPuzzleData() );
						_subMove.setReferencePosition ( getPositionBelow ( sourcePosition ) );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
				}
			}
			// If the target tile is two rows below the target position, we must position the blank
			else if ( getPositionAbove ( sourcePosition ).isSameRow ( getPositionBelow ( getTargetPosition() ) ) )
			{
				// Is the target tile on the row above the target position ?
				if ( sourcePosition.isAbove ( getTargetPosition() ) )
				{
					// If the tile below the target tile is blank, we can move the target tile down so that the staged move can continue
					if ( isTileBlank ( getPositionBelow( sourcePosition ) ) )
					{
						// Move the target tile down to the target row
						_subMove = new MoveTile( getTargetValue(), getPuzzleData() );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
					// If the blank is above the target tile, we can antirotate the blank to the right of the target tile
					else if ( _blankPuzzlePosition.isAbove ( sourcePosition ) )
					{
						// Move the blank to the target tile - this will move the target tile right one column
						_subMove = new RotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData(), sourcePosition );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
					// The blank is to the right of the target tile, so we can rotate the blank to below the target tile
					else
					{
						// Move the blank to the target tile - this will move the target tile right one column
						_subMove = new AntiRotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData(), sourcePosition );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
				}
				// The target tile on the same row as the target position
				else
				{
					// If the blank is between the target tile and the its target position, skip a stage
					if ( isTileBlank ( getPositionAbove( sourcePosition ) ) )
					{
						// The blank is between the target tile and the target position and on the same row, so just move to the stage 3
						setStage ( 3 );     // The tiles are positioned part way through stage 3 - moving to it will just continue the process
					}
					// If the blank is the target position, skip a stage
					else if ( isTileBlank ( getTargetPosition( ) ) )
					{
						// The blank is between the target tile and the target position and on the same row, so just move to the stage 3
						setStage ( 3 );     // The tiles are positioned part way through stage 3 - moving to it will just continue the process
					}
					else
					{
						// Move the blank between the target tile and the target position without moving the target tile
						_subMove = new RotateBlankTo( getPositionAbove ( sourcePosition ), getPuzzleData() );
//						_subMove.setReferencePosition ( sourcePosition );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
				}
			}
			// If the blank is immediately to the left of the target tile, move the target tile left but don't move to the next stage
			else if ( isTileBlank ( getPositionAbove( sourcePosition ) ) )
			{
				// Move the target tile left but don't move to the next stage
				_subMove = new MoveTile( getTargetValue(), getPuzzleData() );
				setStage ( 0 );     // Use this to avoid having an additional Move object
			}
			// If the blank is to the left of the target tile, make sure you don't move the solved tiles when moving the blank.
			else if ( _blankPuzzlePosition.isAbove ( sourcePosition ) )
			{
				// If the target tile is (in the row) above the target position we need to move it down, so move the blank to below the target tile
				if ( sourcePosition.isToLeftOf ( getTargetPosition() ) )
				{
					// Use anti-rotate to move the blank so that we don't disturb the target tile
					_subMove = new RotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData(), sourcePosition );
					setStage ( 0 );     // Use this to avoid having an additional Move object
				}
				// If the blank is (in the row) above the target tile, move it down to the immediate left of the target tile
				else if ( _blankPuzzlePosition.isToLeftOf ( sourcePosition ) )
				{
					// Move it down to the immediate left of the target tile by anti-rotating (to avoid moving the target tile)
					_subMove = new RotateBlankTo( getPositionAbove ( sourcePosition ), getPuzzleData() );
					setStage ( 0 );     // Use this to avoid having an additional Move object
				}
				else
				{
					// The blank is between the target tile and the target position and on the same row, so just move the blank right
					_subMove = new MoveTile( getPositionBelow ( _blankPuzzlePosition ), getPuzzleData() );
					setStage ( 0 );     // Use this to avoid having an additional Move object
				}
			}
			// If the blank is to the right of the target tile, be careful with the target tile when moving the blank
			else if ( _blankPuzzlePosition.isBelow ( sourcePosition ) )
			{
				// If the target tile is (in the row) above the target position we need to move it down, so move the blank to below the target tile
				if ( sourcePosition.isToLeftOf ( getTargetPosition() ) )
				{
					// Use rotate to move the blank so that we don't disturb the target tile
					_subMove = new AntiRotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData(), sourcePosition );
					setStage ( 0 );     // Use this to avoid having an additional Move object
				}
				else
				{
					// Avoid moving the target tile by using anti-rotate to move the blank to the immediate left of the target tile
					_subMove = new RotateBlankTo( getPositionAbove ( sourcePosition ), getPuzzleData(), sourcePosition );
					setStage ( 0 );     // Use this to avoid having an additional Move object
				}
			}
			// The blank is neither to the right nor the left, so it must be above or below the target tile.
			// If the target tile isn't in the bottom row, the blank is below it and the tile can be moved down
			else if ( isValidPuzzlePosition ( getPositionToRight( sourcePosition ) ) )
			{
				// Move the target tile down to the target row
				_subMove = new MoveTile( getTargetValue(), getPuzzleData() );
				setStage ( 0 );     // Use this to avoid having an additional Move object
			}
			else
			{
				// Rotate the blank from above the target tile to the immediate left of it.
				_subMove = new RotateBlankTo( getPositionAbove ( sourcePosition ), getPuzzleData(), sourcePosition );
				setStage ( 0 );     // Use this to avoid having an additional Move object
			}
		}

		// In the second stage we have a few possible variations for getting the blank into the right starting position
		if ( getStage() == 2 )
		{
			// If the blank tile is immediately to the right of the target tile, its in a good position so just move to the next stage
			if ( isTileBlank ( getPositionBelow( sourcePosition ) ) )
			{
				// The blank tile is in a valid start position so just move to the next stage
				nextStage();
			}
			// If the blank tile is immediately above the target tile, its in a good position so just move to the next stage
			else if ( isTileBlank ( getPositionToLeft( sourcePosition ) ) )
			{
				// Rotate the blank into place to the right of the target tile
				_subMove = new AntiRotateBlankTo( getPositionBelow ( sourcePosition ), getPuzzleData() );
			}
			// If the blank tile is above the target tile, we can rotate the blank into place to the right of the target tile
			else if ( _blankPuzzlePosition.isToLeftOf ( sourcePosition ) )
			{
				// Rotate the blank into place to the right of the target tile
				// Set the reference position to keep the target tile safe
				_subMove = new AntiRotateBlankTo( getPositionBelow ( sourcePosition ), getPuzzleData(), getPositionToLeft ( sourcePosition ) );
			}
			else
			{
				// Keep the target tile safe by rotating the blank into place to the right of the target tile
				// Set the reference position to keep the target tile safe
				_subMove = new AntiRotateBlankTo( getPositionBelow ( sourcePosition ), getPuzzleData(), getPositionToLeft (sourcePosition ) );
			}
		}

		// +++ This needs to be improved +++
		// Having moved the target tile into position, the remaining moves are set
		if ( getStage() == 3 )
		{
//			System.out.println( "Rotating to Above Left of Source" );
			_subMove = new AntiRotateBlankTo( getPositionToLeft ( getTargetPosition() ), getPuzzleData() );
//			_subMove.setReferencePosition ( sourcePosition );
		}

		if ( getStage() == 4 )
		{
//			System.out.println( "Rotating to Left Source around Left Left (Source)" );
			_subMove = new AntiRotateBlankTo( getPositionAbove ( sourcePosition ), getPuzzleData() );
//			_subMove.setReferencePosition ( getPositionToLeft( getPositionToLeft ( sourcePosition ) ) );
		}

		if ( getStage() == 5 )
		{
//			System.out.println( "Move the blank to the right" );
			_subMove = new MoveTile ( sourcePosition, getPuzzleData() );
		}

                if ( getStage() == 6 )
		{
//			System.out.println( "AntiRotating to Above Left Source around Source" );
			_subMove = new RotateBlankTo( getPositionAbove ( getPositionToLeft ( sourcePosition ) ), getPuzzleData() );
//			_subMove.setReferencePosition ( sourcePosition );
		}

                if ( getStage() == 7 )
		{
//			System.out.println( "Rotating to Right Right of Target around Right(Source)" );
			_subMove = new RotateBlankTo( sourcePosition, getPuzzleData() );
//			_subMove.setReferencePosition ( getPositionToRight ( sourcePosition ) );
		}

	}

    };


// This is the class to move the target tile to its target position when the target position is in the bottom row.
// The last two rows are solved a column at a time, whereas the previous rows are solved a row at a time.
// This means that for each column (from the left), the tile in the penultimate row will be solved, then the tile in the last row.
// Although the penultimate row can be solved using SolveNormal, the final row requires special consideration and (usually) a staged move.
class SolveLastRow extends PuzzleSolution {
	PuzzlePosition sourcePosition;

	// In the constructor, we must initialise the stage counter and calculate the next move.
	SolveLastRow ( int targetValue, PuzzleData tileData ) {
		this(targetValue, tileData, 0);
	}

	// In the constructor, we must initialise the stage counter and calculate the next move.
	SolveLastRow ( int targetValue, PuzzleData tileData, int initStage ) {
		super( targetValue, tileData.getTargetPositionForValue (targetValue), tileData );
		setStage ( initStage );
		calculateNextMove();
	}

	// After some initial positioning, whenver calculateNextMove is called it will move on to the next stage and use that to determine the next move.
	void calculateNextMove () {
		nextStage();
		_subMove = null;
		_blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );
		sourcePosition = getPuzzlePosition ( getTargetValue () );

		// The first stage is to move the target tile to immediately below the target position (or terminate the move because there's nothing to do).
		if ( ( getStage() == 1 ) && ( ! sourcePosition.equals ( getTargetPosition() ) ) )
		{

			// Is the target tile in the column immediately to the right of the target position ?
			if ( getPositionToLeft ( sourcePosition ).isSameColumn ( getTargetPosition() ) )
			{
				// If the target tile is immediately to the right of the target position and the target position is blank, we can move it in right away
				if ( getPositionToLeft ( sourcePosition ).equals ( getTargetPosition() ) )
				{
					// If the target tile is immediately to the right of the target position and the target position is blank, we can move it in right away
					if ( isTileBlank ( getPositionToLeft( sourcePosition ) ) )
					{
						// The blank is between the target tile and the target position and on the same row, so just move to the stage 3
						_subMove = new MoveTile( getTargetValue(), getPuzzleData() );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
					else
					{
						// The target tile is in a situation which needs this move - proceed to the next stage
					nextStage();
					}
				}
				// The target tile is immediately to the right and up from the target position
				else
				{
					// If the blank is in the target position or under the target tile, we can move it to the target tile by anti-rotating
					if ( ! _blankPuzzlePosition.isToRightOf ( sourcePosition ) )
					{
						// Can safely move the blank to the target tile - this will move the target tile down one row
						_subMove = new AntiRotateBlankTo( sourcePosition, getPuzzleData() );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
					// If the blank tile in on the same row as the target position, rotate it to the target tile
					else if ( _blankPuzzlePosition.isBelow ( sourcePosition ) )
					{
						// Can safely move the blank to the target tile - this will move the target tile down one row
						_subMove = new RotateBlankTo( sourcePosition, getPuzzleData() );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
					else
					{
						// Move the blank to the target tile - this will move the target tile right one column
						_subMove = new AntiRotateBlankTo( sourcePosition, getPuzzleData(), getPositionBelow ( sourcePosition ) );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
				}
			}
			// If the target tile is two columns to the right of the target position, we must position the blank
			else if ( getPositionToLeft ( sourcePosition ).isSameColumn ( getPositionToRight ( getTargetPosition() ) ) )
			{
				// Is the target tile on the row above the target position ?
				if ( sourcePosition.isAbove ( getTargetPosition() ) )
				{
					// If the tile below the target tile is blank, we can move the target tile down so that the staged move can continue
					if ( isTileBlank ( getPositionBelow( sourcePosition ) ) )
					{
						// Move the target tile down to the target row
						_subMove = new MoveTile( getTargetValue(), getPuzzleData() );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
					// If the blank is to the left of the target tile, we can antirotate the blank to below the target tile
					else if ( _blankPuzzlePosition.isToLeftOf ( sourcePosition ) )
					{
						// Move the blank to the target tile - this will move the target tile right one column
						_subMove = new AntiRotateBlankTo( getPositionBelow ( sourcePosition ), getPuzzleData(), sourcePosition );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
					// The blank is to the right of the target tile, so we can rotate the blank to below the target tile
					else
					{
						// Move the blank to the target tile - this will move the target tile right one column
						_subMove = new RotateBlankTo( getPositionBelow ( sourcePosition ), getPuzzleData(), sourcePosition );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
				}
				// The target tile on the same row as the target position
				else
				{
					// If the blank is between the target tile and the its target position, skip a stage
					if ( isTileBlank ( getPositionToLeft( sourcePosition ) ) )
					{
						// The blank is between the target tile and the target position and on the same row, so just move to the stage 3
						setStage ( 3 );     // The tiles are positioned part way through stage 3 - moving to it will just continue the process
					}
					// If the blank is the target position, skip a stage
					else if ( isTileBlank ( getTargetPosition( ) ) )
					{
						// The blank is between the target tile and the target position and on the same row, so just move to the stage 3
						setStage ( 3 );     // The tiles are positioned part way through stage 3 - moving to it will just continue the process
					}
					else
					{
						// Move the blank between the target tile and the target position without moving the target tile
						_subMove = new AntiRotateBlankTo( getPositionToLeft ( sourcePosition ), getPuzzleData() );
//						_subMove.setReferencePosition ( sourcePosition );
						setStage ( 0 );     // Use this to avoid having an additional Move object
					}
				}
			}
			// If the blank is immediately to the left of the target tile, move the target tile left but don't move to the next stage
			else if ( isTileBlank ( getPositionToLeft( sourcePosition ) ) )
			{
				// Move the target tile left but don't move to the next stage
				_subMove = new MoveTile( getTargetValue(), getPuzzleData() );
				setStage ( 0 );     // Use this to avoid having an additional Move object
			}
			// If the blank is to the left of the target tile, make sure you don't move the solved tiles when moving the blank.
			else if ( _blankPuzzlePosition.isToLeftOf ( sourcePosition ) )
			{
				// If the target tile is (in the row) above the target position we need to move it down, so move the blank to below the target tile
				if ( sourcePosition.isAbove ( getTargetPosition() ) )
				{
					// Use anti-rotate to move the blank so that we don't disturb the target tile
					_subMove = new AntiRotateBlankTo( getPositionBelow ( sourcePosition ), getPuzzleData(), sourcePosition );
					setStage ( 0 );     // Use this to avoid having an additional Move object
				}
				// If the blank is (in the row) above the target tile, move it down to the immediate left of the target tile
				else if ( _blankPuzzlePosition.isAbove ( sourcePosition ) )
				{
					// Move it down to the immediate left of the target tile by anti-rotating (to avoid moving the target tile)
					_subMove = new AntiRotateBlankTo( getPositionToLeft ( sourcePosition ), getPuzzleData() );
					setStage ( 0 );     // Use this to avoid having an additional Move object
				}
				else
				{
					// The blank is between the target tile and the target position and on the same row, so just move the blank right
					_subMove = new MoveTile( getPositionToRight ( _blankPuzzlePosition ), getPuzzleData() );
					setStage ( 0 );     // Use this to avoid having an additional Move object
				}
			}
			// If the blank is to the right of the target tile, be careful with the target tile when moving the blank
			else if ( _blankPuzzlePosition.isToRightOf ( sourcePosition ) )
			{
				// If the target tile is (in the row) above the target position we need to move it down, so move the blank to below the target tile
				if ( sourcePosition.isAbove ( getTargetPosition() ) )
				{
					// Use rotate to move the blank so that we don't disturb the target tile
					_subMove = new RotateBlankTo( getPositionBelow ( sourcePosition ), getPuzzleData(), sourcePosition );
					setStage ( 0 );     // Use this to avoid having an additional Move object
				}
				else
				{
					// Avoid moving the target tile by using anti-rotate to move the blank to the immediate left of the target tile
					_subMove = new AntiRotateBlankTo( getPositionToLeft ( sourcePosition ), getPuzzleData(), sourcePosition );
					setStage ( 0 );     // Use this to avoid having an additional Move object
				}
			}
			// The blank is neither to the right nor the left, so it must be above or below the target tile.
			// If the target tile isn't in the bottom row, the blank is below it and the tile can be moved down
			else if ( isValidPuzzlePosition ( getPositionBelow( sourcePosition ) ) )
			{
				// Move the target tile down to the target row
				_subMove = new MoveTile( getTargetValue(), getPuzzleData() );
				setStage ( 0 );     // Use this to avoid having an additional Move object
			}
			else
			{
				// Rotate the blank from above the target tile to the immediate left of it.
				_subMove = new AntiRotateBlankTo( getPositionToLeft ( sourcePosition ), getPuzzleData(), sourcePosition );
				setStage ( 0 );     // Use this to avoid having an additional Move object
			}
		}

		// In the second stage we have a few possible variations for getting the blank into the right starting position
		if ( getStage() == 2 )
		{
			// If the blank tile is immediately to the right of the target tile, its in a good position so just move to the next stage
			if ( isTileBlank ( getPositionToRight( sourcePosition ) ) )
			{
				// The blank tile is in a valid start position so just move to the next stage
				nextStage();
			}
			// If the blank tile is immediately above the target tile, its in a good position so just move to the next stage
			else if ( isTileBlank ( getPositionAbove( sourcePosition ) ) )
			{
				// Rotate the blank into place to the right of the target tile
				_subMove = new RotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData() );
			}
			// If the blank tile is above the target tile, we can rotate the blank into place to the right of the target tile
			else if ( _blankPuzzlePosition.isAbove ( sourcePosition ) )
			{
				// Rotate the blank into place to the right of the target tile
				// Set the reference position to keep the target tile safe
				_subMove = new RotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData(), getPositionAbove ( sourcePosition ) );
			}
			else
			{
				// Keep the target tile safe by rotating the blank into place to the right of the target tile
				_subMove = new RotateBlankTo( getPositionToRight ( sourcePosition ), getPuzzleData(), getPositionAbove (sourcePosition ) );
			}
		}

		// +++ This needs to be improved +++
		// Having moved the target tile into position, the remaining moves are set
		if ( getStage() == 3 )
		{
//			System.out.println( "Rotating to Above Left of Source" );
			_subMove = new RotateBlankTo( getPositionAbove ( getTargetPosition() ), getPuzzleData() );
//			_subMove.setReferencePosition ( sourcePosition );
		}

		if ( getStage() == 4 )
		{
//			System.out.println( "Rotating to Left Source around Left Left (Source)" );
			_subMove = new RotateBlankTo( getPositionToLeft ( sourcePosition ), getPuzzleData() );
//			_subMove.setReferencePosition ( getPositionToLeft( getPositionToLeft ( sourcePosition ) ) );
		}

		if ( getStage() == 5 )
		{
//			System.out.println( "Move the blank to the right" );
			_subMove = new MoveTile ( sourcePosition, getPuzzleData() );
		}

                if ( getStage() == 6 )
		{
//			System.out.println( "AntiRotating to Above Left Source around Source" );
			_subMove = new AntiRotateBlankTo( getPositionAbove ( getPositionToLeft ( sourcePosition ) ), getPuzzleData() );
//			_subMove.setReferencePosition ( sourcePosition );
		}

                if ( getStage() == 7 )
		{
//			System.out.println( "Rotating to Right Right of Target around Right(Source)" );
			_subMove = new AntiRotateBlankTo( sourcePosition, getPuzzleData() );
//			_subMove.setReferencePosition ( getPositionToRight ( sourcePosition ) );
		}

	}

    };


// This is the class to move the target tile to its target position.
// It chooses the appropriate submove for solving the tile based on its target position.
// The last two rows are solved a column at a time, whereas the previous rows are solved a row at a time.
// This means that for each column (from the left), the tile in the penultimate row will be solved, then the tile in the last row.
// Although the penultimate row can be solved using SolveNormal, the final row requires special consideration and (usually) a staged move.
class SolveTileValue extends PuzzleSolution {
	PuzzlePosition sourcePosition;

	// In the constructor, we must calculate the appropriate class to solve this tile.
	SolveTileValue ( int targetValue, PuzzleData tileData ) {
		super( targetValue, tileData.getTargetPositionForValue (targetValue), tileData );
		calculateNextMove();
	}

	void calculateNextMove () {
		_subMove = null;
		_blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );
		sourcePosition = getPuzzlePosition ( getTargetValue () );

		// If the target tile is in its final position, do nothing
		if ( sourcePosition.equals( getTargetPosition () ) )
		{
			_subMove = null;
		}
		// If the target position is in the last row, call the special class for the last row.
		else if ( ! isValidPuzzlePosition( getPositionBelow( getTargetPosition() ) ) )
		{
			_subMove = new SolveLastRow( getTargetValue(), getPuzzleData() );
		}
		// If the target tile is to the left of the target position, call the special class for the last row (to keep the previously solved tiles safe).
		else if ( sourcePosition.isToLeftOf( getTargetPosition () ) )
		{
			_subMove = new SolveFromLeft( getTargetValue(), getPuzzleData() );
		}
		// If the target position is in the rightmost column, call the special class for the last column (to move and restore the previously solved tiles if necessary).
		else if ( ! isValidPuzzlePosition( getPositionToRight( getTargetPosition() ) ) )
		{
			// If the target position is in the rightmost column, call the special class for the last column (to move and restore the previously solved tiles if necessary).
			if ( getPuzzleData().getColumns() == 2 )
			{
				_subMove = new SolveRightColumn( getTargetValue(), getPuzzleData() );
			}
			else
			{
				_subMove = new SolveLastColumn( getTargetValue(), getPuzzleData() );
			}
		}
		else
		{
			// Nothing special needed so use the "normal" class
			_subMove = new SolveNormal ( getTargetValue() , getPuzzleData() );
		}
	}

    };


// This is the class to solve the whole puzzle
// It solves each tile in a sequence from the lowest to the highest.
// It also allows listeners which will be notified when a different tile is chosen for solution.
class SolveTilePuzzle extends PuzzleSolution {
	PuzzlePosition sourcePosition;

	// In the constructor, we must calculate the next tile to solve and the move to solve it
	SolveTilePuzzle ( PuzzleData data ) {
		super( 0, new PuzzlePosition ( 0,0 ), data );
		calculateNextMove();
	}

	void calculateNextMove () {
		_subMove = null;
		// Calculate the next tile to solve. The routine in PuzzleData needs to be developed in conjunction with the solution classes.
		int lowestUnsolvedValue = ( (AutoData) getPuzzleData()).getLowestUnsolvedValue();
		// The routine returns 0 if there are no tiles to solve
		if ( lowestUnsolvedValue != 0 )
		{
			// Create the move to solve this tile
			_subMove = new SolveTileValue ( lowestUnsolvedValue,
							getPuzzleData() );
		}

		// Tell anyone who's interested about the tile solution
		SolutionEvent solutionEvent = new SolutionEvent ( getPuzzleData(), lowestUnsolvedValue );
		notifyListeners(solutionEvent);

	}

    }


