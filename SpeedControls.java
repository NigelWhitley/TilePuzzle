/*
 * @(#)SpeedControls.java	1.1 5-December-2003  Nigel Whitley
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

// A component which allows the user to control the rate at which the automatic solution moves tile
class SpeedControls extends Panel implements ActionListener
{
	AutoData		_data;
	private static final long serialVersionUID = 1L;

	Panel			_changeControls;
	Panel			_speedControls;

//	Define buttons to control changes to the puzzle display.
	Button			_ok;
	Button			_cancel;
//	Define a check box to control the display style
	Choice			_puzzleSpeedChoice;

	boolean			_imageDefined = false;			// Default to thinking there is no image for display

	SpeedControls ( AutoData data ) {
		super();

//		Store the reference to the puzzle data
		setData( data );

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
		_speedControls = new Panel();
		add("North", _changeControls);
		add("South", _speedControls);
		_puzzleSpeedChoice = new Choice();
		_puzzleSpeedChoice.add( "Slowest" );
		_puzzleSpeedChoice.add( "Slower" );
		_puzzleSpeedChoice.add( "Normal" );
		_puzzleSpeedChoice.add( "Faster" );
		_puzzleSpeedChoice.add( "Fastest" );
		_speedControls.add( _puzzleSpeedChoice );
		rebuildAlternatives();
	}

//	This method displays the Resize controls. The values in the text fields are set from the stored values.
	public void showControls( ) {
		if ( getFont() != null )
		{
			_speedControls.setFont( getFont() );
			_puzzleSpeedChoice.setFont( getFont() );
		}
		_speedControls.doLayout();
		_speedControls.repaint();
		_changeControls.doLayout();
		doLayout();
		repaint();
	}

//	This method rebuilds the controls which will switch from this set of controls to another.
	public void rebuildAlternatives( ) {
		if ( _ok != null )
		{
			_changeControls.remove( _ok );
		}
		if ( _cancel != null )
		{
			_changeControls.remove( _cancel );
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
	public String getSpeed( ) {
//		Check what speed of solution should be used.
		return _puzzleSpeedChoice.getSelectedItem();
	}

//	This method sets the display style as indicated by the text string passed as a parameter.
	public void setSpeed( String puzzleSpeed ) {
//		Check what kind of puzzle should be created. If _buttonPuzzle is true, build a display with buttons for tiles.
		if ( puzzleSpeed != null )
		{
			_puzzleSpeedChoice.select(puzzleSpeed);
		}
	}

//	This method responds to button clicks. All of the buttons in this control are processed here.
	public void actionPerformed(ActionEvent ev) {
		String actionCommand = ev.getActionCommand();

//		Shuffle, recalculate the automatic solution and display the new arrangement
		if ( ev.getSource().equals(_ok) ) {
//			Send a message to the puzzle that it should be shuffled
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "SpeedOK" );
			getData().notifyListeners(puzzleEvent);
//		Discard the changes from the Style or Resize controls and replace them with the Regular controls
		} else if ( ev.getSource().equals(_cancel) ) {
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "SpeedCancel" );
			getData().notifyListeners(puzzleEvent);
		}
	}

}
