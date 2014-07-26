/*
 * @(#)InformationPanel.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2003 Nigel Whitley. All Rights Reserved.
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
class InformationPanel extends Panel implements PuzzleListener
{
	AutoData		_data;

//	This display component will not be owned by this component, but it's easier to allocate it from here.
	TextField		_infoText;				// A status field

//	Used when puzzle is drawn with an image
	Image			_baseImage = null;
	ThumbnailImage		_thumbnailImage = null;

	InformationPanel ( AutoData data ) {
		super();

//		Store the reference to the puzzle data
		setData( data );

		getData().addPuzzleListener( this );

//		Build the controls
		buildControls();

//		Show the controls based on the data state
		showControls();
	}

//	Record the underlying data organisation for the puzzle
	public void setData( AutoData data ) { _data = data; }

//	Return (a pointer to) the underlying data organisation for the puzzle
	public AutoData getData( ) { return _data; }

//	Record the image to be used in the puzzle when it's displayed as a TileImagePuzzle
	public void setImage( Image baseImage ) { _baseImage = baseImage; }

//	Return the image to be used in the puzzle when it's displayed as a TileImagePuzzle
	public Image getImage( ) { return _baseImage; }

//	When the puzzle display is of an image, this method displays the thumbnail of the image in the information panel.
	public void addThumbnailImage( Image image ) {
		if (  ! ( image.equals(_baseImage) ) )
		{
			setImage( image );
		}
		addThumbnailImage( new ThumbnailImage( image ) );
	}

//	When the puzzle display is of an image, this method displays the thumbnail of the image in the information panel.
	public void addThumbnailImage( ThumbnailImage image ) {
//		If a thumbnail image has been built, we must remove it from the display and dispose of it.
		removeThumbnailImage( );
		_thumbnailImage = image;
		add ( "East", image );
		image.buildDisplayFromData();
		doLayout();
		repaint();
	}

//	When the puzzle display is no longer of an image, the thumbnail of the image is removed from the information panel.
	public void removeThumbnailImage( ) {
		if ( _thumbnailImage != null )
		{
			removeThumbnailImage( _thumbnailImage );
		}
	}

//	When the puzzle display is no longer of an image, the thumbnail of the image is removed from the information panel.
	public void removeThumbnailImage( ThumbnailImage image ) {
		remove ( image );
		_thumbnailImage = null;
		doLayout();
	}

//	Display the text in the information panel.
	public void setInformation( String information ) {
		_infoText.setText ( information );
	}

//	The Resize controls display the current settings for the number of rows and columns
//	and permit the user to change them. They are used in conjunction with the Change controls.
//	This method builds them but does not display them.
	public void buildControls( ) {
		setLayout(new BorderLayout());				// Split the controls into two rows
		add( "Center", _infoText = new TextField (35) );
		_infoText.setEditable ( false );
	}

//	This method displays the Resize controls. The values in the text fields are set from the stored values.
	public void showControls( ) {
		doLayout();
		repaint();
		validate();
	}

//	This method removes the Resize controls from the display.
	public void removeControls( ) {
		remove(_infoText);
	}

//	This is the method from the PuzzleListener interface. When the puzzle needs to change, this method should be told about it (i.e called)
	public void puzzleChange(PuzzleEvent puzzleEvent) {
//		The associated event passes the position where the puzzle may change : extract it for later use.
		String puzzleCommand = puzzleEvent.getActionCommand();

//		Display the resize controls
		if ( puzzleCommand.equals("Message") )
		{
			String infoMessage = ((MessageEvent) puzzleEvent).getMessage();
			setInformation( infoMessage );

		}
		//else if ( puzzleCommand.equals("AddThumbnail") )
		//{
		//	String infoMessage = ((MessageEvent) puzzleEvent).getMessage();
		//	setInformation( infoMessage );
		//}
	}

}
