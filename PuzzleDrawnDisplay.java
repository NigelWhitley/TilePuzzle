/*
 * @(#)PuzzleDrawnDisplay.java	1.1 5-December-2003  Nigel Whitley
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
 * This component displays the puzzle by using the assigned colours to draw each tile as a rectangle with the value as a number.
 */

public class PuzzleDrawnDisplay extends AutoDisplay {

	private static final long serialVersionUID = 1L;
//	Have an alternate background colour to brighten the display
	Color _backgroundColor2;
	Dimension _savedSize;

//	The default constructor for the display can't display anything until it gets its data reference.
//	This component must define a mouse listener to detect tile clicks.
	PuzzleDrawnDisplay( ) {
		super();
		setBackgroundColor2(Color.green);
		setBackgroundColor(Color.yellow);
		setInnerMargin(1);
		addMouseListener(new PuzzleDrawnAdapter(this));
		setSavedSize(new Dimension(0,0));
	}

//	The constructor for the display stores the data reference and can then build the puzzle display
	PuzzleDrawnDisplay( PuzzleData data) {
		this();
		setData(data);
	}

	void setSavedSize (Dimension size) {
		_savedSize = size;
	}

	boolean hasPuzzleSizeChanged( ) {
		Dimension newSize = new Dimension (this.getSize().width, this.getSize().height );
		return ( ! newSize.equals(_savedSize) );
	}

	void adjustPuzzleSize( ) {
		if ( hasPuzzleSizeChanged() ) {
//			We want to position the puzzle in the centre of the display. Set the origin accordingly.
			Dimension newSize = new Dimension (this.getSize().width, this.getSize().height );
			setSavedSize (newSize);
			setPuzzleOrigin( ( newSize.width - ( getTileWidth() * getData().getColumns() ) ) / 2, ( newSize.height - ( getTileHeight() * getData().getRows() ) ) / 2 );
		}
	}

//	Clear the display ready for drawing
	public void clear( Graphics g ) {
		if ( g == null ) {
			g = getGraphics();
		}

		if ( g != null ) {
			Color saved_color = g.getColor();
			g.fillRect( 0,
				0,
				this.getSize().width,
				this.getSize().height );
			g.setColor(saved_color);
		}
	}

	void setBackgroundColor2( Color backgroundColor2 ) { _backgroundColor2 = backgroundColor2; }
	Color getBackgroundColor2() { return _backgroundColor2; }

//	The data reference gives us the number of rows and columns, so we can define some internal values for the display.
	public void buildDisplayFromData() {
		clear (null) ;

		int combinedMargin = getInnerMargin() * 2;
		int maxWidth = ( this.getSize().width / getData().getColumns() ) - combinedMargin;
		int maxHeight = ( this.getSize().height / getData().getRows() ) - combinedMargin;
		setTileWidth ( maxWidth );
		setTileHeight ( maxHeight );

//		We want to position the puzzle in the centre of the display. Set the origin accordingly.
		setPuzzleOrigin( ( this.getSize().width - ( getTileWidth() * getData().getColumns() ) ) / 2, ( this.getSize().height - ( getTileHeight() * getData().getRows() ) ) / 2 );
	}

	// Override the base class values to include the border
	// Note that this means that setTileHeight (getTileHeight ) will increase the height of the tiles (unless we also redefine setHeight/setWidth).
	int getTileHeight() { return _tileHeight + ( getInnerMargin()*2 ); }
	int getTileWidth() { return _tileWidth + ( getInnerMargin()*2 ); }

//	This method verifies whether a puzzle of the suggested size would fit in the display. If the size is invalid, it returns false.
	public boolean isValidPuzzleSize( Dimension suggestedSize ) {

//		The width and height of the tiles are based on the font dimensions
//		The widest numeral is typically "8" so base the tile size on that numeral
		String all8s = new String ("8888");			// Expect the number of tiles to be fewer than 10,000
		String maxValueAsString = Integer.toString( ( getData().getColumns() * getData().getRows() ) - 1 );
		String maxValueWidth = all8s.substring(0, maxValueAsString.length() );	// Use the number of digits from the largets value

//		The width and height of the tiles are based on the font dimensions
		int minWidth = getFontMetrics( getFont() ).stringWidth(maxValueWidth) + ( getInnerMargin() * 2 );
		int minHeight = getFontMetrics( getFont() ).getAscent() + ( getInnerMargin() * 2 );
		//setTileWidth ( minWidth );
		//setTileHeight ( minHeight );

//		Check width and height against the suggested number of tiles
		if ( ( this.getSize().width < ( minWidth * suggestedSize.width ) ) ||( this.getSize().height < ( minHeight * suggestedSize.height ) ) )
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

	// Draw the tile value
	// Note to self: when writing text the text origin is the bottom left
	void drawTileValueOnly( Graphics g, int tileRow, int tileColumn ) {
		// Convert the numerical tile value to a string
		String valueAsString = Integer.toString(getData().getTileValue(tileRow, tileColumn) );
		// We want to centre the string in the tile, so first calculate the display width of the string
		FontMetrics fontMetrics = g.getFontMetrics();
		int widthOffset = ( ( getTileWidth() - fontMetrics.stringWidth( valueAsString ) ) / 2 ) + getInnerMargin();	// Calculate the starting offset for the string within the tile
		int heightOffset = ( ( getTileHeight() + fontMetrics.getAscent() ) / 2 ) + getInnerMargin();	// Calculate the starting offset for the string within the tile
		//int heightOffset = ( getTileHeight() / 2 ) + getInnerMargin();	// Calculate the starting offset for the string within the tile
		Point tileOrigin = getTileOrigin( tileRow, tileColumn );	// Calculate the display origin of the tile
		// We know what to display and where, so do it.
		g.drawString( valueAsString,
				tileOrigin.x + widthOffset,			// 
				tileOrigin.y + heightOffset );	// 
	}

	// Draw the blank (missing) tile as a rectangle of the Frame color
	void drawBlankTile( Graphics g, int tileRow, int tileColumn ) {
		if ( g == null ) {
			g = getGraphics();
		}

		if ( g != null ) {
			Color saved_color = g.getColor();
			g.setColor(getFrameColor());
			Point tileOrigin = getTileOrigin( tileRow, tileColumn );
			g.fillRect( (tileOrigin.x),
				(tileOrigin.y),
				(getTileWidth()),
				(getTileHeight()) );
			g.setColor(saved_color);
		}
	}

	// Draw the tile which is currently the target of the automated solution.
	// Set the foreground color to the Solving colour, then draw the value
	void drawSolvingTile( Graphics g, int tileRow, int tileColumn ) {
		if ( g == null ) {
			g = getGraphics();
		}

		if ( g != null ) {
			Color saved_color = g.getColor();
			g.setColor( getSolvingColor() );

			drawTileValueOnly ( g, tileRow, tileColumn );
			g.setColor(saved_color);
		}
	}

	// Draw a tile which the user can move by setting the foreground color to the Movable colour, then drawing the value
	void drawMovableTile( Graphics g, int tileRow, int tileColumn ) {
		if ( g == null ) {
			g = getGraphics();
		}

		if ( g != null ) {
			Color saved_color = g.getColor();
			g.setColor(getMovableColor());
			drawTileValueOnly ( g, tileRow, tileColumn );
			g.setColor(saved_color);
		}
	}

	// Draw a tile which is not blank, movable or solving.
	// Set the foreground color to the Normal colour, then draw the value
	void drawNormalTile( Graphics g, int tileRow, int tileColumn ) {
		if ( g == null ) {
			g = getGraphics();
		}

		if ( g != null ) {
			Color saved_color = g.getColor();
			g.setColor(getNormalColor());
			drawTileValueOnly ( g, tileRow, tileColumn );
			g.setColor(saved_color);
		}
	}

	// Draw the tile background as a rectangle of the Background color
	void drawTileBackground( Graphics g, int tileRow, int tileColumn ) {
		if ( g == null ) {
			g = getGraphics();
		}

		if ( g != null ) {
			Color saved_color = g.getColor();
			int tileValue = getData().getTileValue(tileRow, tileColumn);
			if ( ( tileValue % 2) == 0)
			{
				g.setColor( getBackgroundColor() );
			}
			else
			{
				g.setColor( getBackgroundColor2() );
			}
//			g.setColor( getBackgroundColor() );

			Point tileOrigin = getTileOrigin( tileRow, tileColumn );
			g.fillRect( (tileOrigin.x),
				(tileOrigin.y),
				(getTileWidth()),
				(getTileHeight()) );
			g.setColor(saved_color);
		}
	}

	// Draw the tile frame i.e. the lines forming a grid
	void drawTileFrame( Graphics g, int tileRow, int tileColumn ) {
		if ( g == null ) {
			g = getGraphics();
		}

		if ( g != null ) {
			Color saved_color = g.getColor();
			g.setColor(getFrameColor());
			Point tileOrigin = getTileOrigin( tileRow, tileColumn );
			g.drawRect( tileOrigin.x, tileOrigin.y, getTileWidth(), getTileHeight() );
			g.setColor(saved_color);
		}
	}

    }
