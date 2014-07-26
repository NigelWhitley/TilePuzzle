/*
 * @(#)PuzzleDisplay.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2001-2003 Nigel Whitley. All Rights Reserved.
 *
 * This software is released under the terms of the GPL. For details, see license.txt.
 *
 */

import java.awt.*;
import java.util.*;
import java.text.*;
import java.awt.event.*;
import java.io.*;

/**
 * The puzzle is logically split into form and function.
 * The base element for the "form" is this display component.
 * In other words, any display of the puzzle will be derived from this component.
 */
abstract class PuzzleDisplay extends Panel{

//	Keep track of components intersted in the puzzle changes
	ArrayList<PuzzleListener> _puzzleListeners = new ArrayList<PuzzleListener>();

//	The default constructor for the display sets safe initial values for internal variables.
	PuzzleDisplay() {
		super();
		setFrameColor(Color.black);
		setNormalColor(Color.blue);
//		setMovableColor(Color.magenta);
		setMovableColor( getNormalColor() );
		setBackgroundColor(Color.white);
//		The puzzle origin is the offset from the top left of the panel at which the puzzle is displayed
		setPuzzleOrigin(0,0);
//		The inner margin is the gap between the edge of the tile and the area where the tile value is displayed.
		setInnerMargin(1);
//		Default the tile size;
		setTileHeight(16);
		setTileWidth(16);
	}

//	Override the default paint to make sure it paints the puzzle in the right order : background, then the tiles
	public void paint(Graphics g) {
		super.paint(g);
		drawTiles(g);
	}

//	This is effectively a wrapper for the notifyListeners method, but takes a position as input rather than an event.
//	It will be called from the descendants of this object
	public void requestPuzzleMove (PuzzlePosition tileToMove) {
//		Notify the requested tile move and that it has originated from the display (and hence a tile click)
//		PuzzleEvent puzzleEvent = new PuzzleEvent ( this, tileToMove, "Click" );
		PuzzleEvent puzzleEvent = new PositionEvent ( this, tileToMove, "Click" );
		getData().notifyListeners(puzzleEvent);
	}

//	Define general display variables and methods to read from and write to them
	int _fontPoint;
	int _innerMargin;
	int _tileHeight;
	int _tileWidth;

	void setInnerMargin( int innerMargin ) { _innerMargin = innerMargin; }
	void setTileHeight( int height ) { _tileHeight = height; }
	void setTileWidth( int width ) { _tileWidth = width; }

	int getInnerMargin() { return _innerMargin; }
	int getTileHeight() { return _tileHeight; }
	int getTileWidth() { return _tileWidth; }

	Color _frameColor;
	Color _normalColor;
	Color _movableColor;
	Color _backgroundColor;

	void setFrameColor( Color frameColor ) { _frameColor = frameColor; }
	void setNormalColor( Color normalColor ) { _normalColor = normalColor; }
	void setMovableColor( Color movableColor ) { _movableColor = movableColor; }
	void setBackgroundColor( Color backgroundColor ) { _backgroundColor = backgroundColor; }
	Color getFrameColor() { return _frameColor; }
	Color getNormalColor() { return _normalColor; }
	Color getMovableColor() { return _movableColor; }
	Color getBackgroundColor() { return _backgroundColor; }

	Point _puzzleOrigin;
	void setPuzzleOrigin( int x, int y ) { _puzzleOrigin = new Point(x,y); }
	Point getPuzzleOrigin() { return _puzzleOrigin; }
	void adjustPuzzleSize( ) {  }

//	The relative origin of a tile is the offset of the top left corner of a tile from the top left tile of the puzzle.
	Point getTileRelativeOrigin( int tileRow, int tileColumn ) { return new Point ((getTileWidth() * tileColumn), (getTileHeight() * tileRow)); }

//	The origin of a tile is the offset of the top left corner of a tile from the top left corner of the display.
	Point getTileOrigin( int tileRow, int tileColumn ) {
		Point puzzleOrigin = getPuzzleOrigin();
		Point tileRelativeOrigin = getTileRelativeOrigin( tileRow, tileColumn );
		return new Point ((puzzleOrigin.x + tileRelativeOrigin.x), (puzzleOrigin.y + tileRelativeOrigin.y));
	}
	Point getTileOrigin( PuzzlePosition tilePosition ) { return getTileOrigin( tilePosition.getRow(), tilePosition.getColumn() ); }

//	This is intended for use in response to mouse actions.
//	It takes a point (x,y) relative to the puzzle display (Panel) and converts it to a tile Position (row, column)
//	If the point is not over any tile, the "invalid" position will be returned
	PuzzlePosition getTileFromPoint ( Point point ) {
		Point puzzleOrigin = getPuzzleOrigin();
		Point relativePoint = new Point ( point.x - puzzleOrigin.x, point.y - puzzleOrigin.y );

//	        System.out.println("Relative point" + relativePoint.x + "," + relativePoint.y);

		int column = relativePoint.x / getTileWidth();
		int row = relativePoint.y / getTileHeight();

//	        System.out.println("Tile row " + row + ", column" + column );

		if ( ( relativePoint.x >= 0 ) && ( relativePoint.y >= 0 ) && getData().isValidPuzzlePosition ( row, column ) )
		{
			return new PuzzlePosition ( row, column );
		}
		else
		{
			return new PuzzlePosition ( getData().invalidPuzzlePosition() );
		}
	}

//	This method verifies whether a puzzle of the suggested size would fit in the display. If the size is invalid, it returns false.
	public boolean isValidPuzzleSize( Dimension suggestedSize ) {

//		Default to allowing any size
		return true;
	}

//	The function part of the form and function is the PuzzleData object.
//	It holds the underlying organisation of the data and methods to interact with it.
//	This is a reference to the data for this display and methods to store and retrieve it.
	PuzzleData _data;
	void setData( PuzzleData data ) { _data = data; buildDisplayFromData (); }
	PuzzleData getData() { return _data; }

//	This method doesn't do any drawing itself.
//	It calls the appropriate underlying method depending on the type of tile to be drawn.
//	This gives greater flexibility when different display styles are needed.
//	The implementation of the specific methods is the responsibility of the derived class.
	void drawTileValue( Graphics g, int tileRow, int tileColumn ) {
		if (getData().isTileBlank( tileRow, tileColumn ))
		{
			drawBlankTile( g, tileRow, tileColumn );
		}
		else
		{
			if (getData().isTileMovable( tileRow, tileColumn ))
			{
				drawMovableTile( g, tileRow, tileColumn );
			}
			else
			{
				drawNormalTile( g, tileRow, tileColumn );
			}
		}
	}

	// Override this in derived classes to draw normal tiles i.e. those which are neither blank nor movable
	abstract void drawNormalTile( Graphics g, int tileRow, int tileColumn );

	// Override this in derived classes to draw movable tiles i.e. those which are to left, right, above and below the blank tile
	// Typically these will be displayed differently to normal tiles as a prompt to the user.
        abstract void drawMovableTile( Graphics g, int tileRow, int tileColumn );

	// Override this in derived classes to draw the blank (missing) tile.
	abstract void drawBlankTile( Graphics g, int tileRow, int tileColumn );

	// Override this in derived classes to draw the background for the tile.
	abstract void drawTileBackground( Graphics g, int tileRow, int tileColumn );

	// Draw the tile frame i.e. the lines forming a grid
	abstract void drawTileFrame( Graphics g, int tileRow, int tileColumn );

	// Draw all of the tiles by successively drawing each tile
	void drawTiles( Graphics g ) {
		adjustPuzzleSize();
		// Cycle through all the rows and columns and build them from the back forwards.
		for (int tile_row = getData().minRow(); tile_row <= getData().maxRow(); tile_row++)
		{
			for (int tile_column = getData().minColumn(); tile_column <= getData().maxColumn(); tile_column++)
			{
				drawTile(g, tile_row, tile_column);
			}
		}
	}

	// Draw a single tile
	void drawTile( PuzzlePosition tilePosition ) {
		// Draw the tiles
		Graphics g = null;
		int tile_row = tilePosition.getRow();
		int tile_column = tilePosition.getColumn();

		drawTile(g, tile_row, tile_column);
	}

	// Draw a tile drom the back forwards, so start with the background, then the frame and finally the value.
	void drawTile( Graphics g, PuzzlePosition tilePosition ) {
		// Draw the tiles
		int tile_row = tilePosition.getRow();
		int tile_column = tilePosition.getColumn();

		drawTile(g, tile_row, tile_column);
	}

	// Draw a tile drom the back forwards, so start with the background, then the frame and finally the value.
	void drawTile( Graphics g, int tileRow, int tileColumn ) {
		// Draw the tile from the back forwards.
		drawTileBackground(g, tileRow, tileColumn);
		drawTileFrame( g, tileRow, tileColumn);
		drawTileValue(g, tileRow, tileColumn);
	}

	// clear the panel, ready for display
	abstract public void clear(Graphics g);

	// rebuild the panel based on the data
	abstract void buildDisplayFromData( );

    }
