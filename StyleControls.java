/*
 * @(#)StyleControls.java	1.1 5-December-2003  Nigel Whitley
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

// A component which allows the user to choose the style for displaying the puzzle
class StyleControls extends Panel implements ActionListener
{
	AutoData		_data;
	private static final long serialVersionUID = 1L;

	Panel			_changeControls;
	Panel			_styleControls;

//	Define buttons to control changes to the puzzle display.
	Button			_ok;
	Button			_cancel;
	Button			_imageChoose;
//	Define a check box to control the display style
	CheckboxGroup		_puzzleTypeGroup;
	Checkbox		_buttonPuzzle;
	Checkbox		_drawnPuzzle;
	Checkbox		_imagePuzzle;
	JFileChooser		_chooser;
	FilePreviewer		_previewer;

	boolean			_imageDefined = false;			// Default to thinking there is no image for display

	StyleControls ( AutoData data, boolean imageDefined ) {
		super();

//		Store the reference to the puzzle data
		setData( data );

//		Note whether an image is defined
		setImageDefined (imageDefined);

//		Build the controls
		buildControls();

//		Show the controls based on the data state
		showControls();

	}

//	Record the underlying data organisation for the puzzle
	public void setData( AutoData data ) { _data = data; }

//	Return (a pointer to) the underlying data organisation for the puzzle
	public AutoData getData( ) { return _data; }

//	The Style controls allow the users to choose between the styles of display.
//	They are used in conjunctioin with the Change controls.
//	This method builds them but does not display them.
	public void buildControls( ) {
		setLayout(new BorderLayout());				// Split the controls into two rows
		_changeControls = new Panel();
		_styleControls = new Panel();
		add("North", _changeControls);
		add("South", _styleControls);
		_puzzleTypeGroup = new CheckboxGroup();
		_buttonPuzzle = new Checkbox ( "Button", _puzzleTypeGroup, true );
		_drawnPuzzle = new Checkbox ( "Drawn", _puzzleTypeGroup, false );
		_styleControls.add(_buttonPuzzle);
		_styleControls.add(_drawnPuzzle);
		if ( isImageDefined() )
		{
			_imagePuzzle = new Checkbox ( "Image", _puzzleTypeGroup, false );
			_styleControls.add(_imagePuzzle);
		}
		else
		{
			_chooser = new JFileChooser();
			_previewer = new FilePreviewer(_chooser);
			_chooser.setAccessory(_previewer);
		}
		rebuildAlternatives();
	}

//	This method displays the Resize controls. The values in the text fields are set from the stored values.
	public void showControls( ) {
		_styleControls.doLayout();
		_styleControls.repaint();
		_changeControls.doLayout();
		doLayout();
		repaint();
	}

//	This method rebuilds the controls which will switch from this set of controls to another.
	public void rebuildAlternatives( ) {
		if ( _imageChoose != null )
		{
			_changeControls.remove( _imageChoose );
		}
		if ( _ok != null )
		{
			_changeControls.remove( _ok );
		}
		if ( _cancel != null )
		{
			_changeControls.remove( _cancel );
		}
		if ( ! isImageDefined() )
		{
			_imageChoose = new Button("Choose Image");
			_imageChoose.addActionListener(this);
			_changeControls.add(_imageChoose);
		}
		_ok = new Button("OK");
		_ok.addActionListener(this);
		_cancel = new Button("Cancel");
		_cancel.addActionListener(this);
		_changeControls.add(_ok);
		_changeControls.add(_cancel);
		_changeControls.validate();
	}

//	This method returns the display style as a text string.
	public String getStyle( ) {
//		Check what kind of puzzle should be created.
		if ( _buttonPuzzle.getState() )
		{
			return "ButtonStyle";
		}
		else if ( _drawnPuzzle.getState() )
		{
			return "DrawnStyle";
		}
		else
		{
			return "ImageStyle";
		}
	}

//	This method sets the display style as indicated by the text string passed as a parameter.
	public void setStyle( String displayStyle ) {
//		Check what kind of puzzle should be created. If _buttonPuzzle is true, build a display with buttons for tiles.
		if ( displayStyle != null )
		{
			if ( displayStyle.equals("ButtonStyle") )
			{
				_buttonPuzzle.setState( true);
			}
			else if ( displayStyle.equals("DrawnStyle") )
			{
				_drawnPuzzle.setState( true);
			}
			else if ( ( displayStyle.equals("ImageStyle") ) && ( isImageDefined() ) )
			{
				_imagePuzzle.setState( true);
			}
		}
	}

//	This method responds to button clicks. All of the buttons in this control are processed here.
	public void actionPerformed(ActionEvent ev) {
		String actionCommand = ev.getActionCommand();

//		Shuffle, recalculate the automatic solution and display the new arrangement
		if ( ev.getSource().equals(_ok) ) {
//			Send a message to the puzzle that it should be shuffled
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "StyleOK" );
			getData().notifyListeners(puzzleEvent);
//		Discard the changes from the Style or Resize controls and replace them with the Regular controls
		} else if ( ev.getSource().equals(_cancel) ) {
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "StyleCancel" );
			getData().notifyListeners(puzzleEvent);
//		Discard the changes from the Style or Resize controls and replace them with the Regular controls
		} else if ( ev.getSource().equals(_imageChoose) ) {
			int retval = _chooser.showDialog(this, null);

			if ( retval == JFileChooser.APPROVE_OPTION) {
				File theFile = _chooser.getSelectedFile();
				if ( theFile != null) {
					File [] files = _chooser.getSelectedFiles();
					if (_chooser.isMultiSelectionEnabled() && files != null && files.length > 1) {
						String filenames = "";
						for(int i = 0; i < files.length; i++) {
							filenames = filenames + "\n" + files[i].getPath();
						}
						//System.out.println( "You chose these files: \n" + filenames );
					} else if(theFile.isDirectory()) {
						//System.out.println( "You chose this directory: " + _chooser.getSelectedFile().getPath() );
					} else {
						//System.out.println( "You chose this file: " + _chooser.getSelectedFile().getPath() );
						DisplayEvent puzzleEvent = new DisplayEvent ( this, _chooser.getSelectedFile().getPath(), "UseImage" );
						getData().notifyListeners(puzzleEvent);
					}
				}
			} else if ( retval == JFileChooser.CANCEL_OPTION) {
				//System.out.println( "User cancelled operation. No file was chosen." );
			} else if ( retval == JFileChooser.ERROR_OPTION) {
				//System.out.println( "An error occured. No file was chosen." );
			} else {
				//System.out.println( "Unknown operation occured." );
			}
		}
	}

//	This method is a fiddle for keeping track of whether the puzzle is running as an applet - the image style is only available for an applet
	public void setImageDefined( boolean imageDefined ) {
		_imageDefined = imageDefined;
	}

//	This method is a fiddle for keeping track of whether the puzzle is running as an applet - the image style is only available for an applet
	public boolean isImageDefined( ) {
		return _imageDefined;
	}

}
