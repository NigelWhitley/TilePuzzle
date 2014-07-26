/*
 * @(#)ThumbNailImage.java	1.1 5-December-2003  Nigel Whitley
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
import java.net.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.File;
import java.beans.*;

/**
 * An interactive simulation of the sliding tile puzzle where one tile
 * is removed to allow the others to be re-arranged.
 * Can be run either as a standalone application by
 * typing "java TilePuzzle" or as an applet in a suitable browser.
 */

// A component which displays a thumbnail of an image scaled to fit the component's container
class ThumbnailImage extends Canvas
{
	int calculatedWidth = 0;
	int calculatedHeight = 0;
	Image			_baseImage = null;
	private static final long serialVersionUID = 1L;

	ThumbnailImage ( Image baseImage ) {
		super();

//		Keep a local record of the image we use as a thumbnail
		setImage( baseImage );

//		Build the scaled image of the data
		buildDisplayFromData();
	}

	public void setImage( Image baseImage ) { _baseImage = baseImage; }

	public Image getImage( ) { return _baseImage; }

//	Build the scaled image of the data
	public void buildDisplayFromData()
	{
//		Only build a thumbnail if we have an image
		if ( getImage() != null )
		{
//			Keep track of the size of the thumbnail, to act if it changes
			int oldWidth = calculatedWidth;
			int oldHeight = calculatedHeight;

//			Get the size of the container (if the component has been placed in one)
			if ( getParent() != null )
			{
//				Reduce the component dimensions without allowing for the border
				calculatedWidth = getParent().getSize().width;
				calculatedHeight = getParent().getSize().height;

			}

//			Get the size of the original image. If the image size is not yet known,
//			we'll give this component as the ImageObserver, so imageUpdate will be called when the size is available.
			int imageWidth = getImage().getWidth(this);
			int imageHeight = getImage().getHeight(this);

//			In order to scale the image correctly, we need to figure out which dimenson to
//			use as a base for scaling the image.
//			Basically, if the image shape is "taller" than the container shape, then we use the height, otherwise the width.
			if ( ( imageHeight * calculatedWidth ) >= ( imageWidth * calculatedHeight ) )
			{
//				Use the scaled height as a basis. If the image is smaller than the container, use the image height
				if ( imageHeight < calculatedHeight )
				{
					calculatedHeight = imageHeight;
				}
//				Calculate the scaled width of the thumbnail using the scaled height as a basis.
				calculatedWidth = ( ( calculatedHeight * imageWidth ) /  imageHeight );
			}
			else
			{
//				Use the scaled width as a basis. If the image is smaller than the container, use the image width
				if ( imageWidth < calculatedWidth )
				{
					calculatedWidth = imageWidth;
				}
//				Calculate the scaled height of the thumbnail using the scaled width as a basis.
				calculatedHeight = ( ( calculatedWidth * imageHeight ) /  imageWidth );
			}

//			If the thumbnail needs to be resized, resize it and recalculate the layout of the components in the container
			if ( ( oldWidth != calculatedWidth ) || (oldHeight != calculatedHeight) )
			{
				setSize ( calculatedWidth, calculatedHeight );
				if ( getParent() != null )
				{
					getParent().doLayout();
				}
			}
		}
	}

//	This will get called when the size becomes known (if it wasn't available when the size was asked for)
	public boolean imageUpdate(Image img,int flags,int x,int y,int w,int h){
		if ((flags & ALLBITS) != 0)
		{

//			Build the scaled image now that the size is available.
			buildDisplayFromData();
			repaint();
		}

		if ((flags & ERROR) != 0)
		{
//			getTilePuzzle().getAppletContext().showStatus("Error Loading Image ");
			System.out.println("Error Loading Image ");
		}

		return (flags & (ALLBITS|ERROR)) == 0;
	}

//	Override paint() to draw the scaled image
	public void paint(Graphics g){
		super.paint(g);
		buildDisplayFromData();
		g.drawImage( getImage(),
				0,
				0,
				calculatedWidth,
				calculatedHeight,
				this );
	}

}

