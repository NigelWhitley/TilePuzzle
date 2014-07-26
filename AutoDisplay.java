/*
 * @(#)AutoDisplay.java	1.1 5-December-2003  Nigel Whitley
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
abstract public class AutoDisplay extends PuzzleDisplay {

//	The default constructor for the display sets safe initial values for internal variables.
	AutoDisplay() {
		super();
		setSolvingColor( Color.red );
		setMakeNormal( false );
	}

	boolean _makeNormal;	// Used for redisplaying non-normal tiles before moving a tile (which would produce a new set of non-normal tiles).

	Color _solvingColor;
	void setSolvingColor( Color solvingColor ) { _solvingColor = solvingColor; }
	Color getSolvingColor() { return _solvingColor; }

	public void setMakeNormal( boolean makeNormal ) { _makeNormal = makeNormal; }
	public boolean isMakingNormal( ) { return _makeNormal; }

	// Override this in derived classes to draw the tile which is being "solved".
	abstract void drawSolvingTile( Graphics g, int tileRow, int tileColumn );

//	As in the base class (PuzzleDisplay), this method doesn't do any drawing itself.
//	It calls the appropriate underlying method depending on the type of tile to be drawn.
//	This derived version has additioinal processing to handle the tile being "solved".
//	Note that the movable tiles and solving tiles will not be displayed together with this approach.
	void drawTileValue( Graphics g, int tileRow, int tileColumn ) {
		if (getData().isTileBlank( tileRow, tileColumn ))
		 {
		  drawBlankTile( g, tileRow, tileColumn );
		 }
		else
		 {
		  if ( ( getData().isTileMovable( tileRow, tileColumn ) ) && ( ! ( (AutoData) getData() ).isAutoSolving() ) && ( ! isMakingNormal() ) )
		   {
		    drawMovableTile( g, tileRow, tileColumn );
		   }
		  else
		   {
		    if ( ( ( (AutoData) getData() ).isAutoSolving() ) && ( getData().getTileValue ( tileRow, tileColumn ) == ( (AutoData) getData()).getSolvingValue() ) )
		     {
		      drawSolvingTile( g, tileRow, tileColumn );
		     }
		    else
		     {
		      drawNormalTile( g, tileRow, tileColumn );
		     }
		   }
		 }
		}

    }
