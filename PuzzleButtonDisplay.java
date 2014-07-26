/*
 * @(#)PuzzleButtonDisplay.java	1.1 5-December-2003  Nigel Whitley
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
 * This component displays the puzzle with a button for each tile.
 */
public class PuzzleButtonDisplay extends AutoDisplay implements ActionListener {

//	Have an alternate background colour to brighten the display
	Color _backgroundColor2;

//	The default constructor for the display can't display anything until it gets its data reference
	PuzzleButtonDisplay( ) {
		super();
	}

//	The constructor for the display stores the data reference and can then use it to build the puzzle display
	PuzzleButtonDisplay( PuzzleData tileData ) {
		super();
		setBackgroundColor2(Color.yellow);
		setData (tileData);
		buildDisplayFromData();
	}

//	When the tile is clicked, we need to know where the tile is in the puzzle.
//	This method searches the set of components to find this button.
//	The position in the list (which this method returns) can later be converted to a tile position.
	public int findButtonIndex( Button buttonToFind ) {
		for (int componentIndex=0; componentIndex<getComponentCount(); componentIndex++) {
			Button checkButton = (Button) getComponent(componentIndex);
			if (checkButton == buttonToFind) {
				return (componentIndex);
			}
		}
//		Could not find the button in the puzzle display, -1 is not a valid position in the list so return that.
		return -1;
	}

//	This method is called when the tile is clicked (button is pressed).
//	The position of this tile in the puzzle is calculated and passed to the tile listeners.
	public void actionPerformed( ActionEvent event ) {
		//System.out.println("Tile clicked");
		// Get the index of this button - a return value of -1 indicates that it couldn't be found
		int _buttonIndex = findButtonIndex((Button) event.getSource());

		if ( _buttonIndex >= 0 )
		{
			PuzzlePosition clickedTile = getPositionForOrdinal ( _buttonIndex );
			requestPuzzleMove ( clickedTile );
		}
	}


	// Convert a tile position to the ordinal value within the panel
	int getOrdinalForPosition( PuzzlePosition tilePosition ) {
		return ( ( ( tilePosition.getRow() ) * getData().getColumns() ) + tilePosition.getColumn() );
	}

	// Convert to a tile position from the ordinal value within the panel
	PuzzlePosition getPositionForOrdinal( int tileOrdinal ) {
		int row = ( ( tileOrdinal ) / getData().getColumns() );
		int column = ( ( tileOrdinal ) - ( getData().getColumns() * row ) );
		return new PuzzlePosition ( row, column );
	}


	// Get a reference to the button which represents the tile
	Button getPanelTileAt( PuzzlePosition tilePosition ) { return getPanelTileAt ( getOrdinalForPosition ( tilePosition ) ); }
	Button getPanelTileAt( int tileRow, int tileColumn ) {
		return getPanelTileAt ( getOrdinalForPosition ( new PuzzlePosition ( tileRow, tileColumn ) ) );
	}
	Button getPanelTileAt( int tileOrdinal ) {
		return (Button) getComponent ( tileOrdinal );
	}

	// Draw the tiles themselves
	void buildDisplayFromData() {
		// Draw the tiles
//		System.out.println ("Removing all" );
		clear( null );
		setLayout(new GridLayout( getData().getRows(), getData().getColumns() ));

		for (int tile_row = getData().minRow(); tile_row <= getData().maxRow(); tile_row++)
		{
			for (int tile_column = getData().minColumn(); tile_column <= getData().maxColumn(); tile_column++)
			{
				Button tileButton = new Button( Integer.toString ( getData().getTileValue ( tile_row, tile_column ) ) );
				tileButton.addActionListener(this);
				add( tileButton );
			}
		}
		doLayout();
		repaint();
	}

	void setBackgroundColor2( Color backgroundColor2 ) { _backgroundColor2 = backgroundColor2; }
	Color getBackgroundColor2() { return _backgroundColor2; }

	public void clear( Graphics g ) {
	        removeAll();
	}

//	This method verifies whether a puzzle of the suggested size would fit in the display. If the size is invalid, it returns false.
	public boolean isValidPuzzleSize( Dimension suggestedSize ) {

//		Check width and height against the suggested number of tiles. Assume max dimensions of 8x8
		if ( ( suggestedSize.width > 8 ) || ( suggestedSize.height > 8 ) )
		{
//			Return false to indicate that the size is invalid
			return false;
		}
		else
		{
//			Return true to indicate that the size is invalid
			return true;
		}
	}

	// Draw the blank (missing) tile by making the button the same colour as the frame.
	void drawBlankTile( Graphics g, int tileRow, int tileColumn ) {
		Button tileButton = getPanelTileAt( tileRow, tileColumn );
		tileButton.setBackground( getFrameColor() );
		tileButton.setForeground( getFrameColor() );
		String stringValue = Integer.toString(getData().getTileValue(tileRow, tileColumn) );
		tileButton.setLabel( "" );
	}

	// Draw the tile which is currently the target of the automated solution. For a button, just make the foreground the solving colour.
	void drawSolvingTile( Graphics g, int tileRow, int tileColumn ) {
		Button tileButton = getPanelTileAt( tileRow, tileColumn );
		tileButton.setForeground( getSolvingColor() );
		String stringValue = Integer.toString(getData().getTileValue(tileRow, tileColumn) );
		tileButton.setLabel( Integer.toString(getData().getTileValue(tileRow, tileColumn) ) );
	}

	// Draw a tile which the user can move. For a button, just make the foreground the movable colour.
	void drawMovableTile( Graphics g, int tileRow, int tileColumn ) {
		Button tileButton = getPanelTileAt( tileRow, tileColumn );
		tileButton.setForeground( getMovableColor() );
		String stringValue = Integer.toString(getData().getTileValue(tileRow, tileColumn) );
		tileButton.setLabel( Integer.toString(getData().getTileValue(tileRow, tileColumn) ) );
	}

	// Draw a tile which is not blank, movable or solving. For a button, just make the foreground the normal colour.
	void drawNormalTile( Graphics g, int tileRow, int tileColumn ) {
		Button tileButton = getPanelTileAt( tileRow, tileColumn );
		tileButton.setForeground( getNormalColor() );
		String stringValue = Integer.toString(getData().getTileValue(tileRow, tileColumn) );
		tileButton.setLabel( Integer.toString(getData().getTileValue(tileRow, tileColumn) ) );
	}

	// Draw the background for the tile. For a button, just make the background the background colour.
	void drawTileBackground( Graphics g, int tileRow, int tileColumn ) {
		Button tileButton = getPanelTileAt( tileRow, tileColumn );
		int tileValue = getData().getTileValue(tileRow, tileColumn);
		if ( ( tileValue % 2) == 0)
		{
			tileButton.setBackground( getBackgroundColor() );
		}
		else
		{
			tileButton.setBackground( getBackgroundColor2() );
		}
		String stringValue = Integer.toString(getData().getTileValue(tileRow, tileColumn) );
		tileButton.setLabel( Integer.toString(getData().getTileValue(tileRow, tileColumn) ) );
	}

	// Draw the tile frame i.e. the lines forming a grid. This is just a dummy.
	void drawTileFrame( Graphics g, int tileRow, int tileColumn ) {
	}

	// Draw the tiles themselves
	void drawTiles( Graphics g ) {
		// Cycle through all the rows and columns and build them from the back forwards.
		for (int tile_row = getData().minRow(); tile_row <= getData().maxRow(); tile_row++)
		{
			for (int tile_column = getData().minColumn(); tile_column <= getData().maxColumn(); tile_column++)
			{
				drawTile(g, tile_row, tile_column);
			}
		}
	}

    }
