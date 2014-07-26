/*
 * @(#)PuzzleImageDisplay.java	1.1 5-December-2003  Nigel Whitley
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
import java.awt.image.*;

/**
 * This component displays the puzzle using an image. Each tile shows the corresponding portion of the whole.
 */
public class PuzzleImageDisplay extends PuzzleDrawnDisplay implements ImageObserver {

	Image			_puzzleImage = null;	// Keep a reference to the image to be used for the display.


//	The default constructor for the display can't display anything until it gets its data reference and image.
//	This component must define a mouse listener to detect tile clicks.
	PuzzleImageDisplay( ) {
		super();
	}

//	The constructor for the display stores the data reference for later use in building the puzzle display
//	It can't display anything without the base image.
	PuzzleImageDisplay( PuzzleData data) {
		super(data);
		setBackgroundColor2(getFrameColor());
		setBackgroundColor(getFrameColor());
	}

//	The constructor for the display stores the data reference and the image and can then build the puzzle display
	PuzzleImageDisplay( PuzzleData data, Image puzzleImage ) {
		this(data);
		setImage(puzzleImage);
	}

//	The constructor for the display stores the base image for later use in building the puzzle display
//	It can't display anything without the data reference.
	PuzzleImageDisplay( Image puzzleImage ) {
		this();
		setImage(puzzleImage);
	}

//	Store the reference to the image used for the puzzle display, then try to build the display.
	public void setImage( Image puzzleImage ) {
		_puzzleImage = puzzleImage;
		buildDisplayFromData();
	}

//	Return the stored reference to the image used for the puzzle display.
	public Image getImage() { return _puzzleImage; }

	void adjustPuzzleSize( ) {
		if ( hasPuzzleSizeChanged() ) {
//			We want to position the puzzle in the centre of the display. Set the origin accordingly.
			Dimension newSize = new Dimension (getSize().width, getSize().height );
			setSavedSize (newSize);
			calculateImageSize();
		}
	}

	void calculateImageSize( ) {
		if ( getImage() != null )
		{
//			Assume there is no need for scaling, so the scaled size is the current size of the image.
			int imageWidth = getImage().getWidth(this);
			int imageHeight = getImage().getHeight(this);

//			Get the current size of the display.
			int displayWidth = getSize().width;
			int displayHeight = getSize().height;

//			Have we got valid sizes for the image and the display ?
			if ( ( displayHeight > 0 ) && ( imageHeight > 0 ) )
			{
//				We need to scale the image to preserve the relatively larger axis
//				For example, 					image         width 200 height 160
//										display       width 100 height 150
//				so the image will be scaled by width 		scaled image  width 100 height 80
//				Compare the relative shapes of the image and the display.
				if ( ( displayHeight * imageWidth ) <= ( displayWidth * imageHeight ) )
				{
//					Would scale the image by height. Do we need to scale the image ?
					if ( displayHeight < imageHeight )
					{
						imageWidth = ( ( displayHeight * imageWidth ) /  imageHeight );
						imageHeight = displayHeight;
					}
				}
				else
				{
//					Would scale the image by width. Do we need to scale the image ?
					if ( displayWidth < imageWidth )
					{
						imageHeight = ( ( displayWidth * imageHeight ) /  imageWidth );
						imageWidth = displayWidth;
					}
				}

//				Calculate and store the tile size
				int tmpHeight = (imageHeight) / getData().getRows();
				setTileHeight ( tmpHeight );
				int tmpWidth = (imageWidth) / getData().getColumns();
				setTileWidth ( tmpWidth );

//				Centre the (scaled) image in the display.
//				Use the tile height and width because they may have been rounded down by the previous calculation,
//				so the image Height and Width will not be an exact multiple of the tiles (which is what we display).
//				The distortions in size and aspect ratio should be small and (hopefully) not noticeable.
				setPuzzleOrigin( (displayWidth - imageWidth ) / 2, ( displayHeight - imageHeight ) / 2 );
			}
		}
	}

//	The image may need to be scaled for the puzzle display.
//	This method compares the image size to the display size : if the image is larger it is scaled down.
//	After the scaling has been calculate an origin is calculated to centre the puzzle image in the display.
	public void buildDisplayFromData() {
		clear(null);	// Make sure the puzzle display is clean, before we start doodling on it.

//		We can only do scaling if an image has been supplied...
		if ( getImage() != null )
		{
			calculateImageSize( );
		}

	}

//	Override the base class values to exclude the border
	int getTileHeight() { return _tileHeight ; }
	int getTileWidth() { return _tileWidth ; }

//	This method verifies whether a puzzle of the suggested size would fit in the display. If the size is invalid, it returns false.
	public boolean isValidPuzzleSize( Dimension suggestedSize ) {

		int minImagePerTile = 20;	// Assume that the user wants to be able to display some of the image in each tile !

//		Check width and height against the suggested number of tiles
		if ( ( getSize().width < ( minImagePerTile * suggestedSize.width ) ) ||( getSize().height < ( minImagePerTile * suggestedSize.height ) ) )
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

//	This will get called when the size becomes known (if it wasn't available when the size was asked for)
//	When the size becomes known, we should rebuild and repaint the display.
	public boolean imageUpdate(Image img,int flags,int x,int y,int w,int h){
		if ((flags & ALLBITS) != 0)
		{
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "AddThumbnail" );
			getData().notifyListeners(puzzleEvent);

			buildDisplayFromData();
			repaint();
		}

		if ((flags & ERROR) != 0)
		{
//			System.out.println("Error Loading Image ");
		}

		return (flags & (ALLBITS|ERROR)) == 0;
	}

	// Draw the tile value
	// For the image display, the "value" is the relevant portion of the base image
	void drawTileValueOnly( Graphics g, int tileRow, int tileColumn ) {

//		To get the relevant portion of the image, we must first identify the target position for the current tile.
		PuzzlePosition tilePosition = getData().getTargetPositionForValue( getData().getTileValue (tileRow, tileColumn ) );
//		To get the relevant portion of the base (source) image, we must map the size of the tiles onto the image
		int sourceWidth = getImage().getWidth(this) / getData().getColumns();
		int sourceHeight = getImage().getHeight(this) / getData().getRows();

//		We display the tile image at the tile origin, and with the stored width and height.
		Point tileOrigin = getTileOrigin( tileRow, tileColumn );

//		System.out.println("Image " + Integer.toString(tileColumn) + "  end x " + Integer.toString(tileOrigin.x + ( getWidth() * ( tileColumn + 1) ) - 1 ) );
//		Draw the scaled tile image from the portion of the image source
		g.drawImage( getImage(),
				tileOrigin.x,
				tileOrigin.y,
				tileOrigin.x + ( getTileWidth() - 1 ),
				tileOrigin.y + ( getTileHeight() - 1 ),
				sourceWidth * tilePosition.getColumn(),
				sourceHeight * tilePosition.getRow(),
				( sourceWidth * ( tilePosition.getColumn() + 1 ) ) - 1,
				( sourceHeight * ( tilePosition.getRow() + 1 ) ) - 1,
				null );
		}

    }
