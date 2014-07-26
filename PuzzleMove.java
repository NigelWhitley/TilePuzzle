/*
 * @(#)PuzzleMove.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2001-2003 Nigel Whitley. All Rights Reserved.
 *
 * This software is released under the terms of the GPL. For details, see license.txt.
 *
 */

import java.awt.*;
import java.awt.event.*;
import java.applet.*;

/**
 * An automated solution will clearly consist of many tile moves.
 * To simplify the coding of the solution, we will create aggregate moves.
 * Later classes can build on previous ones to make succesively more complex moves.
 * It must be possible to perform the moves a tile at a time by repeatedly obtaining the next tile to move.
 */

// Start with the most basic functions for tile moves.
// The general idea is that we move a tile of a certain value to a new position.
// Of course, the move also needs a reference to the data for the puzzle.
// The complete move may consist of more than one tile move.
// Consequently, we also need to be able to check when the move is complete i.e. if the tile has arrived at its final position.
// It also has a note of how far through the move we are (stage), a subordinate Move for the stage and a reference position to modify the submove.
class PuzzleMove{
	int		_targetValue;
	PuzzleData	_tileData;
	PuzzlePosition	_targetPosition;

	PuzzlePosition		_referencePosition;
	PuzzleMove	_subMove;
	int			_stage;
	PuzzlePosition            _blankPuzzlePosition;

	PuzzleMove( int targetValue, PuzzlePosition targetPosition, PuzzleData tileData ) {
		setTargetPosition ( targetPosition );
		setTargetValue ( targetValue );
		setPuzzleData ( tileData );
	}
	void setPuzzleData( PuzzleData tileData ) { _tileData = tileData; }
	void setTargetPosition ( PuzzlePosition targetPosition ) { _targetPosition = new PuzzlePosition(targetPosition); }
	void setTargetValue ( int targetValue ) { _targetValue = targetValue; }
	PuzzlePosition getTargetPosition ( ) { return new PuzzlePosition( _targetPosition ); }
	int getTargetValue ( ) { return _targetValue; }
	PuzzleData getPuzzleData() { return _tileData; }

	void setReferencePosition ( PuzzlePosition referencePosition ) { _referencePosition = new PuzzlePosition(referencePosition); }
	PuzzlePosition getReferencePosition ( ) { return new PuzzlePosition( _referencePosition ); }

	void setStage ( int stage ) { _stage = stage; }
	int getStage ( ) { return _stage; }
	void nextStage ( ) { _stage++; }

	boolean isComplete () {
		if ( _subMove == null )
		 {
		  return true;
		 }
		else if ( _subMove.isComplete() )
		 {
		  calculateNextMove ();
		  if ( _subMove == null )
		   {
		    return true;
		   }
		  else
		   {
		    return _subMove.isComplete();
		   }
		 }
		else
		 {
		  return false;
		 }
	}

	PuzzlePosition getTileToMove () {
		if ( _subMove == null )
		 {
		  return invalidPuzzlePosition();
		 }
		else
		 {
		  return _subMove.getTileToMove();
		 }
	}

	void calculateNextMove () { ; } // just a dummy for now

	// These are defined here as a convenience to avoid having to explicitly use getPuzzleData() to use the functions
	// The code is less efficient but is easier to read.
	int blankTileValue() { return getPuzzleData().blankTileValue ( ); }
	boolean isTileBlank( PuzzlePosition tilePosition ) { return ( getPuzzleData().isTileBlank( tilePosition ) ); }
	PuzzlePosition invalidPuzzlePosition ( ) { return getPuzzleData().invalidPuzzlePosition ( ); }
	boolean isValidPuzzlePosition ( PuzzlePosition tilePosition ) { return getPuzzleData().isValidPuzzlePosition ( tilePosition ); }
	boolean isTileMovable( PuzzlePosition tilePosition ) { return ( getPuzzleData().isTileMovable( tilePosition ) ); }
	PuzzlePosition getPuzzlePosition ( int tileValue ) { return getPuzzleData().getPuzzlePosition ( tileValue ); }
	int getTileValue ( PuzzlePosition tilePosition ) { return getPuzzleData().getTileValue ( tilePosition ); }
	PuzzlePosition getPositionToLeft ( PuzzlePosition tilePosition ) { return getPuzzleData().getPositionToLeft ( tilePosition ); }
	PuzzlePosition getPositionToRight ( PuzzlePosition tilePosition ) { return getPuzzleData().getPositionToRight ( tilePosition ); }
	PuzzlePosition getPositionAbove ( PuzzlePosition tilePosition ) { return getPuzzleData().getPositionAbove ( tilePosition ); }
	PuzzlePosition getPositionBelow ( PuzzlePosition tilePosition ) { return getPuzzleData().getPositionBelow ( tilePosition ); }
    }


// This is the simplest possible move. It simply models a single tile move.
class MoveTile extends PuzzleMove {

	MoveTile( PuzzlePosition tilePosition, PuzzleData tileData ) {
		super( tileData.getTileValue( tilePosition ), tileData.getPuzzlePosition( tileData.blankTileValue() ), tileData );
	}

	MoveTile( int tileValue, PuzzleData tileData ) {
		super( tileValue, tileData.getPuzzlePosition( tileData.blankTileValue() ), tileData );
	}

	boolean isComplete () {
		if ( getPuzzlePosition( getTargetValue() ).equals( getTargetPosition() ) )
		 {
		  return true;
		 }
		else
		 {
		  return false;
		 }
	}

	PuzzlePosition getTileToMove () {
		return getPuzzlePosition ( getTargetValue () );
	}

    }


// In order to move a tile, it must be next to a blank tile. So it would be handy to have aggregate moves to reposition the blank tile.
// This is a base class for moving the blank tile.
class MoveBlankTo extends PuzzleMove {

	MoveBlankTo( PuzzlePosition targetPosition, PuzzleData tileData ) {
		super( tileData.blankTileValue(), targetPosition, tileData );
	}

	boolean isComplete () {
		if ( getPuzzlePosition( blankTileValue()).equals(_targetPosition) )
		 {
		  return true;
		 }
		else
		 {
		  return false;
		 }
	}

    }


// This will reposition the blank tile along a row to the right.
class MoveBlankRightTo extends MoveBlankTo {

	MoveBlankRightTo( PuzzlePosition targetPosition, PuzzleData tileData ) {
		super( targetPosition, tileData );
	}

	PuzzlePosition getTileToMove () {
		return getPositionToRight ( getPuzzlePosition ( blankTileValue () ) );
	}

	boolean isComplete () {
		return (! getPuzzlePosition( blankTileValue()).isToLeftOf(_targetPosition) );
	}
    }


// This will reposition the blank tile along a row to the left.
class MoveBlankLeftTo extends MoveBlankTo {

	MoveBlankLeftTo( PuzzlePosition targetPosition, PuzzleData tileData ) {
		super( targetPosition, tileData );
	}

	PuzzlePosition getTileToMove () {
		return getPositionToLeft ( getPuzzlePosition ( blankTileValue () ) );
	}

	boolean isComplete () {
		return (! getPuzzlePosition( blankTileValue()).isToRightOf(_targetPosition) );
	}
    }


// This will reposition the blank tile up along a column.
class MoveBlankUpTo extends MoveBlankTo {

	MoveBlankUpTo( PuzzlePosition targetPosition, PuzzleData tileData ) {
		super( targetPosition, tileData );
	}

	PuzzlePosition getTileToMove () {
		return getPositionAbove ( getPuzzlePosition ( blankTileValue () ) );
	}

	boolean isComplete () {
		return (! getPuzzlePosition( blankTileValue()).isBelow(_targetPosition) );
	}
    }


// This will reposition the blank tile down along a column.
class MoveBlankDownTo extends MoveBlankTo {

	MoveBlankDownTo( PuzzlePosition targetPosition, PuzzleData tileData ) {
		super( targetPosition, tileData );
	}

	PuzzlePosition getTileToMove () {
		return getPositionBelow ( getPuzzlePosition ( blankTileValue () ) );
	}

	boolean isComplete () {
		return (! getPuzzlePosition( blankTileValue()).isAbove(_targetPosition) );
	}
    }


// The next set of moves use the idea of "rotating" the blank tile around a hub (reference) to a target position.
// This is necessary when there may be a tile in an intermediate position which should not be moved.
// This moves the blank tile to one of the eight tiles around the target tile position.
// From there the rotate classes can operate normally
class MakeTileRotatable extends PuzzleMove {
	PuzzlePosition blankPuzzlePosition;
	PuzzlePosition targetBlankPuzzlePosition;

	MakeTileRotatable ( int tileValue, PuzzleData tileData ) {
		super( tileValue, tileData.getPuzzlePosition ( tileValue ), tileData ); //note that you can't use the local getPuzzlePosition
		calculateNextMove ();
	}

//	The general idea is to move the blank tile until it is adjacent to the target tile.
//	The order in which this happens is not too important, but we need to choose a priority.
//	Therefore, we start by moving it up or down until it is one of the target rows (the same row as the target tile and those immediately above and below). order in which this happens is not too important, but we need to be consistent.
//	Next, we move it left or right until it is one of the target columns (the same column as the target tile and those immediately above and below). order in which this happens is not too important, but we need to be consistent.
//	Notes :	The subMove is recalculated on each iteration. When there is nothing to do, the subMove is null and the move is complete.
//		We don't use the isComplete method of the subMove to terminate the subMove - it is used only once.
	void calculateNextMove () {
		_subMove = null;
		blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );

		if ( isValidPuzzlePosition ( getPositionAbove ( getTargetPosition () ) ) && ( blankPuzzlePosition.isAbove ( getPositionAbove ( getTargetPosition () ) ) ) )
		{
//			Blank is more than one row above the target tile - move the blank down
			targetBlankPuzzlePosition = getPositionAbove ( getTargetPosition ()  );
			_subMove = new MoveBlankDownTo ( targetBlankPuzzlePosition, getPuzzleData() );
		}
		else if ( isValidPuzzlePosition ( getPositionBelow ( getTargetPosition () ) ) && ( blankPuzzlePosition.isBelow ( getPositionBelow ( getTargetPosition () ) ) ) )
		{
//			Blank is more than one row below the target tile - move the blank up
			targetBlankPuzzlePosition = getPositionBelow ( getTargetPosition ()  );
			_subMove = new MoveBlankUpTo ( targetBlankPuzzlePosition, getPuzzleData() );
		}
		else if ( isValidPuzzlePosition ( getPositionToLeft ( getTargetPosition () ) ) && ( blankPuzzlePosition.isToLeftOf ( getTargetPosition () ) ) )
		{
//			Blank is to left of the target tile - move the blank right
			targetBlankPuzzlePosition = getPositionToLeft ( getTargetPosition ()  );
			_subMove = new MoveBlankRightTo ( targetBlankPuzzlePosition, getPuzzleData() );
		}
		else if ( isValidPuzzlePosition ( getPositionToRight ( getTargetPosition () ) ) &&  ( blankPuzzlePosition.isToRightOf ( getTargetPosition () ) ) )
		{
//			Blank is to right of the target tile - move the blank left
			targetBlankPuzzlePosition = getPositionToRight ( getTargetPosition ()  );
			_subMove = new MoveBlankLeftTo ( targetBlankPuzzlePosition, getPuzzleData() );
		}
	}

    }


// This moves the blank tile in a clockwise direction around a reference position to the target position.
// It is used when it may not be safe to move the blank directly to the target position.
// The reference position is used as the hub around which the blank rotates (but may not enter)
// Note: Some of the eight tiles may be off the puzzle - it doesn't matter as long as they are not being moved to or from.
class RotateBlankTo extends PuzzleMove {

	RotateBlankTo ( PuzzlePosition tilePosition, PuzzleData tileData ) {
		super( tileData.getTileValue( tilePosition ), tilePosition, tileData ); //note that you can't use the local getTileValue
		calculateReferencePosition ();
	}

	RotateBlankTo ( PuzzlePosition tilePosition, PuzzleData tileData, PuzzlePosition referencePosition) {
		super( tileData.getTileValue( tilePosition ), tilePosition, tileData ); //note that you can't use the local getTileValue
		setReferencePosition( referencePosition );
	}

	// The area around the reference tile is divided into four quadrants.
	// The direction to move the blank tile depends on which quadrant it is in.
	PuzzlePosition getTileToMove () {
		_blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );

		if ( _blankPuzzlePosition.isToLeftOf ( getReferencePosition () ) )
		{
			if ( _blankPuzzlePosition.isAbove ( getReferencePosition () ) )
			{
				return getPositionToRight ( _blankPuzzlePosition );
			}
			else
			{
				return getPositionAbove ( _blankPuzzlePosition );
			}
		}
		else if ( _blankPuzzlePosition.isBelow ( getReferencePosition () ) )
		{
			return getPositionToLeft ( _blankPuzzlePosition );
		}
		else if ( _blankPuzzlePosition.isToRightOf ( getReferencePosition () ) )
		{
			return getPositionBelow ( _blankPuzzlePosition );
		}
		else
		{
			return getPositionToRight ( _blankPuzzlePosition );
		}
	}


	// The reference position can be set directly, to cope with special circumstances, but this routine caters for the "normal" cases.
	// The reference position set here can be overridden by a direct call to setReferencePosition after the move has been constructed.
	// Note: The calculated reference position will never be the target position, but must be adjacent to it.
	void calculateReferencePosition () {
		_blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );

		// The calculation needs to be viewed in conjunction with the getTileToMove function.
		// The reference position is the position adjacent to the target, but one tile closer along the direction in which the blank will first move.
		// For example, if the blank must move 4 to the left and 2 up, the reference position is 3 to the left and 2 up.
		// When calculating the reference position, it is normally "between" the blank tile and the target.
		// However, if the blank is already adjacent to the target, the reference position is chosen so that the next move will be to the target.
		if ( _blankPuzzlePosition.isToLeftOf ( getTargetPosition () ) )
		{
			if ( _blankPuzzlePosition.isBelow ( getTargetPosition () ) )
			{
				setReferencePosition( getPositionBelow ( getTargetPosition() ) );
			}
			else if ( isTileBlank( getPositionToLeft ( getTargetPosition () ) ) )
			{
				setReferencePosition( getPositionBelow ( getTargetPosition() ) );
			}
			else
			{
				setReferencePosition( getPositionToLeft ( getTargetPosition() ) );
			}
		}
		else if ( _blankPuzzlePosition.isAbove ( getTargetPosition () ) )
		{
			if ( isTileBlank( getPositionAbove ( getTargetPosition () ) ) )
			{
				setReferencePosition( getPositionToLeft ( getTargetPosition() ) );
			}
			else
			{
				setReferencePosition( getPositionAbove ( getTargetPosition() ) );
			}
		}
		else if ( _blankPuzzlePosition.isToRightOf ( getTargetPosition () ) )
		{
			if ( isTileBlank( getPositionToRight ( getTargetPosition () ) ) )
			{
				setReferencePosition( getPositionAbove ( getTargetPosition() ) );
			}
			else
			{
				setReferencePosition( getPositionToRight ( getTargetPosition() ) );
			}
		}
		else
		{
			if ( isTileBlank( getPositionBelow ( getTargetPosition () ) ) )
			{
				setReferencePosition( getPositionToRight ( getTargetPosition() ) );
			}
			else
			{
				setReferencePosition( getPositionBelow ( getTargetPosition() ) );
			}
		}

	}


	// The getTileToMove method does all the work so this routine is a dummy.
	void calculateNextMove () {
	}

	// The move is complete when the blank tile reaches the target position
	boolean isComplete () {
		return ( getPuzzlePosition( blankTileValue()).equals( getTargetPosition() ) );
	}

    }


// This moves the blank tile around the eight tiles surrounding the reference tile position in an anti-clockwise direction.
// Note: Some of the eight tiles may be off the puzzle - it doesn't matter as long as they are not being moved to or from.
class AntiRotateBlankTo extends PuzzleMove {

	AntiRotateBlankTo ( PuzzlePosition tilePosition, PuzzleData tileData ) {
		super( tileData.getTileValue( tilePosition ), tilePosition, tileData ); //note that you can't use the local getTileValue
		calculateReferencePosition ();
	}



	AntiRotateBlankTo ( PuzzlePosition tilePosition, PuzzleData tileData, PuzzlePosition referencePosition) {
		super( tileData.getTileValue( tilePosition ), tilePosition, tileData ); //note that you can't use the local getTileValue
		setReferencePosition( referencePosition );
	}


	// The area around the reference tile is divided into four quadrants.
	// The direction to move the blank tile depends on which quadrant it is in.
	PuzzlePosition getTileToMove () {
		_blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );

		if ( _blankPuzzlePosition.isToLeftOf ( getReferencePosition () ) )
		{
			if ( _blankPuzzlePosition.isBelow ( getReferencePosition () ) )
			{
				return getPositionToRight ( _blankPuzzlePosition );
			}
			else
			{
				return getPositionBelow ( _blankPuzzlePosition );
			}
		}
		else if ( _blankPuzzlePosition.isAbove ( getReferencePosition () ) )
		{
			return getPositionToLeft ( _blankPuzzlePosition );
		}
		else if ( _blankPuzzlePosition.isToRightOf ( getReferencePosition () ) )
		{
			return getPositionAbove ( _blankPuzzlePosition );
		}
		else
		{
			return getPositionToRight ( _blankPuzzlePosition );
		}
	}


	// The reference position can be set directly, to cope with special circumstances, but this routine caters for the "normal" cases.
	// The reference position set here can be overridden by a direct call to setReferencePosition after the move has been constructed.
	// Note: The calculated reference position will never be the target position, but must be adjacent to it.
	void calculateReferencePosition () {
		_blankPuzzlePosition = getPuzzlePosition ( blankTileValue () );

		// The calculation needs to be viewed in conjunction with the getTileToMove function.
		// The reference position is the position adjacent to the target, but one tile closer along the direction in which the blank will first move.
		// For example, if the blank must move 4 to the right and 2 up, the reference position is 3 to the right and 2 up.
		// When calculating the reference position, it is normally "between" the blank tile and the target.
		// However, if the blank is already adjacent to the target, the reference position is chosen so that the next move will be to the target.
		if ( _blankPuzzlePosition.isToRightOf ( getTargetPosition () ) )
		{
			if ( _blankPuzzlePosition.isBelow ( getTargetPosition () ) )
			{
				setReferencePosition( getPositionBelow ( getTargetPosition() ) );
			}
			else if ( isTileBlank( getPositionToRight ( getTargetPosition () ) ) )
			{
				setReferencePosition( getPositionBelow ( getTargetPosition() ) );
			}
			else
			{
				setReferencePosition( getPositionToRight ( getTargetPosition() ) );
			}
		}
		else if ( _blankPuzzlePosition.isAbove ( getTargetPosition () ) )
		{
			if ( isTileBlank( getPositionAbove ( getTargetPosition () ) ) )
			{
				setReferencePosition( getPositionToRight ( getTargetPosition() ) );
			}
			else
			{
				setReferencePosition( getPositionAbove ( getTargetPosition() ) );
			}
		}
		else if ( _blankPuzzlePosition.isToLeftOf ( getTargetPosition () ) )
		{
			if ( isTileBlank( getPositionToLeft ( getTargetPosition () ) ) )
			{
				setReferencePosition( getPositionAbove ( getTargetPosition() ) );
			}
			else
			{
				setReferencePosition( getPositionToLeft ( getTargetPosition() ) );
			}
		}
		else
		{
			if ( isTileBlank( getPositionBelow ( getTargetPosition () ) ) )
			{
				setReferencePosition( getPositionToLeft ( getTargetPosition() ) );
			}
			else
			{
				setReferencePosition( getPositionBelow ( getTargetPosition() ) );
			}
		}
	}


	void calculateNextMove () {
	}


	boolean isComplete () {
		return ( getPuzzlePosition( blankTileValue()).equals( getTargetPosition() ) );
	}

    }


