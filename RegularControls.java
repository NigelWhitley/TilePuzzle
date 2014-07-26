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

// A component which displays the normal controls for the puzzle
class RegularControls extends Panel implements ActionListener
{
	AutoData		_data;
	private static final long serialVersionUID = 1L;

//	Define panels to group the button controls
	Panel			_solutionControls;
	Panel			_auxiliaryControls;

//	Define buttons to control the puzzle. These are the ones normally shown.
	Button			_nextMove;
	Button			_shuffle;
	Button			_solve;
	Button			_reset;
	Button			_resize;
	Button			_style;
	Button			_speed;

	RegularControls ( AutoData data ) {
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

//	The controls allow the user to shuffle the puzzle, ask for it to be solved by the computer
//	or not. This method builds them but does not display them.
	public void buildControls( ) {
		setLayout(new BorderLayout());				// Split the controls into two rows
		_solutionControls = new Panel();
		_auxiliaryControls = new Panel();
		add("North", _solutionControls);
		add("South", _auxiliaryControls);
		_shuffle = new Button("Shuffle");
		_shuffle.addActionListener(this);
		_solutionControls.add(_shuffle);
		_reset = new Button("Reset");
		_reset.addActionListener(this);
		_solutionControls.add(_reset);
		_solve = new Button("Stop Solving");
		_solve.addActionListener(this);
		_solutionControls.add(_solve);
		_nextMove = new Button("Move");
		_nextMove.addActionListener(this);
		_solutionControls.add(_nextMove);
		rebuildAlternatives();
	}

//	This method displays the solution controls depending on the current status of the puzzle and user inputs.
	public void showControls( ) {
//		Is the the puzzle already solved ?
		if ( getData().isSolved() )
		{
//			Disable the controls because no solution is needed
			_solve.setLabel( "Solve it" );
			_solve.invalidate();
			_solve.setEnabled( false );
			_nextMove.setLabel("Move");
			_nextMove.invalidate();
			_nextMove.setEnabled( false );
			_reset.setLabel("Reset");
			_reset.invalidate();
			_reset.setEnabled( false );
		}
//		Is the computer doing the hard work ?
		else if ( getData().isAutoSolving() )
		{
//			Calculate the automatic solution.
			_nextMove.setLabel("Move");
			_nextMove.invalidate();
			_nextMove.setEnabled( false );
			_solve.setLabel( "Stop solving" );
			_solve.invalidate();
			_solve.setEnabled( true );
			_reset.setLabel("Reset");
			_reset.invalidate();
			_reset.setEnabled( true );
		}
//		The user is solving the puzzle - offer to do it for them
		else
		{
//			Enable the controls because a solution is needed
			_solve.setLabel( "Solve it" );
			_solve.invalidate();
			_solve.setEnabled( true );
			_nextMove.setLabel("Move");
			_nextMove.invalidate();
			_nextMove.setEnabled( true );
			_reset.setLabel("Reset");
			_reset.invalidate();
			_reset.setEnabled( true );
		}
		validate();
	}

//	This method rebuilds the controls which will switch from this set of controls to another.
	public void rebuildAlternatives( ) {
		if ( _resize != null )
		{
			_auxiliaryControls.remove( _resize );
		}
		if ( _style != null )
		{
			_auxiliaryControls.remove( _style );
		}
		if ( _speed != null )
		{
			_auxiliaryControls.remove( _speed );
		}
		_resize = new Button("Resize");
		_resize.addActionListener(this);
		_style = new Button("Style");
		_style.addActionListener(this);
		_speed = new Button("Speed");
		_speed.addActionListener(this);
		_auxiliaryControls.add(_resize);
		_auxiliaryControls.add(_style);
		_auxiliaryControls.add(_speed);
		_auxiliaryControls.validate();
	}

//	This method responds to button clicks. All of the buttons in this control are processed here.
	public void actionPerformed(ActionEvent ev) {
		String label = ev.getActionCommand();

//		Shuffle, recalculate the automatic solution and display the new arrangement
		if ( ev.getSource().equals(_shuffle) ) {
//			Send a message to the puzzle that it should be shuffled
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "Shuffle" );
			getData().notifyListeners(puzzleEvent);
			showControls( );
		}
//		Switch between automatic and manual solving
		else if ( ev.getSource().equals(_solve) )
		{
//			Send a message to the puzzle that the solution mode must be toggled
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "Solve" );
			getData().notifyListeners(puzzleEvent);
			showControls( );
		}
//		Have the computer make a single move from the automatic solution
		else if ( ev.getSource().equals(_nextMove) )
		{
//			Send a message to the puzzle that a single move is to be performed
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "NextMove" );
			getData().notifyListeners(puzzleEvent);
			showControls( );
		}
//		Reset the tile puzzle to the solved state
		else if ( ev.getSource().equals(_reset) )
		{
//			Send a message to the puzzle that a single move is to be performed
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "ResetPuzzle" );
			getData().notifyListeners(puzzleEvent);
			showControls( );
		}
//		Have the computer make a single move from the automatic solution
		else if ( ev.getSource().equals(_resize) )
		{
//			Send a message to the puzzle that a single move is to be performed
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "Resize" );
			getData().notifyListeners(puzzleEvent);
			showControls( );
		}
//		Have the computer make a single move from the automatic solution
		else if ( ev.getSource().equals(_style) )
		{
//			Send a message to the puzzle that a single move is to be performed
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "Style" );
			getData().notifyListeners(puzzleEvent);
			showControls( );
		}
//		Have the computer make a single move from the automatic solution
		else if ( ev.getSource().equals(_speed) )
		{
//			Send a message to the puzzle that a single move is to be performed
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "Speed" );
			getData().notifyListeners(puzzleEvent);
			showControls( );
		}
	}

}
