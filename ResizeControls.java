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

// A component which allows the user to choose the size of the puzzle
class ResizeControls extends Panel implements ActionListener
{
	AutoData		_data;
	private static final long serialVersionUID = 1L;

	Panel			_changeControls;
	Panel			_sizeControls;

//	Define buttons to control changes to the puzzle display.
	Button			_ok;
	Button			_cancel;
//	Define text input fields to change the size of the puzzle.
	Label			_rowLabel;
	TextField		_rows;
	Label			_columnLabel;
	TextField		_columns;

	int			_puzzleRows;			// Number of rows in the puzzle
	int			_puzzleColumns;			// Number of columns in the puzzle

	ResizeControls ( AutoData data ) {
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

//	The Resize controls display the current settings for the number of rows and columns
//	and permit the user to change them. They are used in conjunction with the Change controls.
//	This method builds them but does not display them.
	public void buildControls( ) {
		setLayout(new BorderLayout());				// Split the controls into two rows
		_changeControls = new Panel();
		_sizeControls = new Panel();
		add("North", _changeControls);
		add("South", _sizeControls);
		_rowLabel = new Label("Rows");
		_columnLabel = new Label("Columns");
		_rows = new TextField(Integer.toString(_puzzleRows), 2);
		_columns = new TextField(Integer.toString(_puzzleColumns), 2);
		rebuildAlternatives();
		_sizeControls.add(_rowLabel);
		_sizeControls.add(_rows);
		_sizeControls.add(_columns);
		_sizeControls.add(_columnLabel);
	}

//	This method displays the Resize controls. The values in the text fields are set from the stored values.
	public void showControls( ) {
		_puzzleRows = getData().getRows();
		_puzzleColumns = getData().getColumns();
		_rows.setText( Integer.toString(_puzzleRows) );
		_columns.setText( Integer.toString(_puzzleColumns) );
		_changeControls.doLayout();
		_changeControls.repaint();
		_sizeControls.doLayout();
		_sizeControls.repaint();
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

//	This method responds to button clicks. All of the buttons in this control are processed here.
	public void actionPerformed(ActionEvent ev) {
		String actionCommand = ev.getActionCommand();

//		Accept the changes from the Resize controls
		if ( ev.getSource().equals(_ok) ) {
			Dimension puzzleSize = getPuzzleSize();
//			If the new values for the puzzle size are invalid, tell the user.
			if ( puzzleSize == null ) {
//				Send a message to the puzzle that it the input is invalid
				getData().sendMessage( "Invalid Puzzle Size" );
			}
			else
			{
				PuzzleEvent puzzleEvent;

//				Only send OK if the puzzle size has changed.
				if ( ( _puzzleRows != puzzleSize.height ) || ( _puzzleColumns != puzzleSize.width ) ) {
					_puzzleRows = puzzleSize.height;
					_puzzleColumns = puzzleSize.width;
					puzzleEvent = new PuzzleEvent ( this, "ResizeOK", puzzleSize );
				}
				else
				{
					puzzleEvent = new PuzzleEvent ( this, "ResizeCancel" );
				}
				getData().notifyListeners(puzzleEvent);
			}
//		Discard the changes from the Style or Resize controls and replace them with the Regular controls
		} else if ( ev.getSource().equals(_cancel) ) {
			PuzzleEvent puzzleEvent = new PuzzleEvent ( this, "ResizeCancel" );
			getData().notifyListeners(puzzleEvent);
		}
	}

//	This method returns the size indicated in the controls. If the size is invalid, it returns null.
	public Dimension getPuzzleSize() {

		Dimension	_puzzleSize;

//		Accept the changes from the Resize controls
//		Replace the Resize controls on display with the Regular controls, rebuild the puzzle in the new size.
//		recreate the puzzle in the new size and remove any existing automatic solution.
		_puzzleSize = new Dimension();
		try
		{
			_puzzleSize.height = Integer.parseInt(_rows.getText());
			_puzzleSize.width = Integer.parseInt(_columns.getText());
			return _puzzleSize;
		}
//		If the new values for the puzzle size are invalid, tell the user.
		catch (NumberFormatException e)
		{
//			Return null to indicate that the size is invalid
			return null;
		}
	}

}
