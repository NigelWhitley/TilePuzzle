/*
 * @(#)TilePuzzle.java	1.1 5-December-2003  Nigel Whitley
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
public class TilePuzzle extends Applet implements Runnable, PuzzleListener, SolutionListener
{
	boolean			_runAsApplet = true;			// Default to thinking this is run as an applet
	private static final long serialVersionUID = 1L;
	AutoData		_data;

//	These display components will be owned by this component.
	PuzzleDisplay		_puzzleDisplay;				// The puzzle of tiles seen by the user

	RegularControls		_regularControls;			// The controls for solving the puzzle
	ResizeControls		_resizeControls;			// The controls for chinging the number of rows and columns in the puzzle
	StyleControls		_styleControls;				// The controls for style used to display the puzzle
	SpeedControls		_speedControls;				// The controls for the speed of automatic solution
	InformationPanel	_informationPanel;			// Displays a status field and (when appropriate) a thumbnail image

	Dimension		_defaultPuzzleSize = new Dimension(4,4);	// Default rows and columns in the puzzle
	Dimension		_puzzleSize;				// Number of rows and columns in the puzzle
	String			_puzzleSpeed = new String("Normal");
	String			_puzzleStyle;

//	This display component will not be owned by this component, but it's easier to allocate it from here.
	TextField		_infoText;				// A status field

//	Used when puzzle is drawn with an image
	String			_imageName = null;
	Image			_baseImage = null;
	ThumbnailImage		_thumbnailImage = null;

	Thread			_puzzleThread = null;			// The thread that displays images
	int			_moveDelay = 300;			// The delay time between automatic moves
	int			_fontSize;				// The default font size for the puzzle
	PuzzleMove _autoSolution = null;
	int			_previousChangedTile = 0;		// Used to control display of "movable" tiles

	int			_totalMoves = 0;			// The total number of tile moves since the last shuffle or reset


	public String getAppletInfo() {
		return "An interactive simulation of a sliding tile puzzle.\n Can be run either as a standalone application by typing 'java TilePuzzle' \nor as an applet in a suitable browser.";
	}

	public void init() {
		_puzzleDisplay = null;

//		Set the display style using the applet parameter or default to buttons if it's not defined.
		if (isRunAsApplet() ) {
			_puzzleStyle = new String( getParam("style", "ButtonStyle") );

//			Record the name of the image to use.
			setImageName ( getParam("img", "puzzle.jpg") );
		}
		else {
			_puzzleStyle = new String( "ButtonStyle" );
		}

		_puzzleSize = new Dimension ( _defaultPuzzleSize );
		setData ( new AutoData( _puzzleSize ) );
		getData().addPuzzleListener( this );
		setLayout(new BorderLayout());
		_informationPanel = new InformationPanel( getData() );
		add("South", _informationPanel);
		_styleControls = new StyleControls( getData(), isRunAsApplet() );
		_resizeControls = new ResizeControls( getData() );
		_speedControls = new SpeedControls( getData() );
		_regularControls = new RegularControls( getData() );
		add( "North", _regularControls );

//		We define a specific font for the displays - you can use the default font by commenting out these lines
		_fontSize = 14;
		setFont(new Font("TimesRoman", Font.PLAIN, _fontSize ) );
		_regularControls.setFont( getFont() );
		_resizeControls.setFont( getFont() );
		_styleControls.setFont( getFont() );
		_speedControls.setFont( getFont() );
		_informationPanel.setFont( getFont() );

		_styleControls.setStyle( _puzzleStyle );

		_speedControls.setSpeed( _puzzleSpeed );
		setSpeed( _puzzleSpeed );

		setInformationPanel( _informationPanel );

		rebuildPuzzle( );
	}

	public void destroy() {
		remove(_speedControls);
		remove(_styleControls);
		remove(_resizeControls);
		remove(_regularControls);
		remove(_puzzleDisplay);
		remove(_informationPanel);
	}

	public void start() {
		_puzzleThread = new Thread(this);
		_puzzleThread.start();
		repaint();
	}

	public void stop() {
		_puzzleThread = null;
	}

	public void run() {
		Thread me = Thread.currentThread();
		while (_puzzleThread == me) {
			try {
				Thread.sleep( _moveDelay );
				nextAutoMove();
			}
			catch (InterruptedException e) {
			}
		}
	}

	public void processEvent(AWTEvent e) {
		if (e.getID() == Event.WINDOW_DESTROY) {
			System.exit(0);
		}
	}

	public static void main(String args[]) {
		Frame f = new Frame("puzzleTest");
		TilePuzzle tilePuzzle = new TilePuzzle();

		tilePuzzle.setRunAsApplet( false );

		tilePuzzle.init();

		f.add( "Center", tilePuzzle );

		f.setSize(400, 300);

		f.setVisible(true);

		tilePuzzle.validate();

		f.addWindowListener(new WindowCloseAdapter());
		tilePuzzle.start();
	}

//	This method is a fiddle for keeping track of whether the puzzle is running as an applet - the image style is only available for an applet
	public void setRunAsApplet( boolean runAsApplet ) {
		_runAsApplet = runAsApplet;
	}

//	This method is a fiddle for keeping track of whether the puzzle is running as an applet - the image style is only available for an applet
	public boolean isRunAsApplet( ) {
		return _runAsApplet;
	}

//	Record the underlying data organisation for the puzzle
	public void setData( AutoData data ) { _data = data; }

//	Return (a pointer to) the underlying data organisation for the puzzle
	public AutoData getData( ) { return _data; }

//	This is the method from the PuzzleListener interface. When the puzzle needs to change, this method should be told about it (i.e called)
	public void puzzleChange(PuzzleEvent puzzleEvent) {
//		The associated event passes the position where the puzzle may change : extract it for later use.
		String puzzleCommand = puzzleEvent.getActionCommand();

//		Move a tile
		if ( puzzleCommand.equals("Click") )
		{
			PuzzlePosition affectedTile = ((PositionEvent) puzzleEvent).getPuzzlePosition();
//			A user event (tile click), will switch solving to manual from automatic.
			if ( getData().isValidPuzzlePosition ( affectedTile ) )
			{
				boolean wasAutoSolving = getData().isAutoSolving();
				setAutoSolving( false );
				getData().clearAutoSolution( );
				if ( wasAutoSolving )
				{
					_puzzleDisplay.repaint();
				}
			}

//			Only interested if the affected tile is movable. (We won't move tiles that can't be moved!)
			if ( getData().isTileMovable ( affectedTile ) )
			{
//				System.out.println("Moving tile" + clickedTile.getRow() + "," + clickedTile.getColumn());
//				Move the affected tile, then redraw it and the blank tile (with which it has been interchanged).
				moveTileManual ( affectedTile );
				if ( getData().isSolved() )
				{
					setInformation("Puzzle solved - " + Integer.toString( _totalMoves ) + " moves" );
					_totalMoves = 0;
				}
				else
				{
					setInformation("Click a tile - " + Integer.toString( _totalMoves ) + " moves" );
				}
			}

			_regularControls.showControls( );
		}
		else if ( puzzleCommand.equals("NextSolve") )
		{

			PuzzlePosition affectedTile = ((PositionEvent) puzzleEvent).getPuzzlePosition();

//			Move the affected tile
			moveTile ( affectedTile );

//			If the puzzle is not already solved, make the next move.
			if ( ! ( getData().getAutoSolution().isComplete() ) )
			{
				setInformation("Solving " + Integer.toString( _previousChangedTile ) + " - " + Integer.toString( _totalMoves ) + " moves");
			}
			else
			{
				setInformation("Puzzle solved ! - " + Integer.toString( _totalMoves ) + " moves");
//				_totalMoves = 0;
			}

			_regularControls.showControls( );
		}
//		Shuffle, recalculate the automatic solution and display the new arrangement
		else if ( puzzleCommand.equals("Shuffle") ) {
			boolean wasAutoSolving = getData().isAutoSolving();
			setAutoSolving( false );
			getData().clearAutoSolution( );
			getData().setSolvingValue( 0 );
			setInformation("Shuffling puzzle..." );
			getData().shufflePuzzle();
			if ( wasAutoSolving ) {
				setAutoSolution( new SolveTilePuzzle ( getData() ) );
				_previousChangedTile = getData().getLowestUnsolvedValue();
				getData().setSolvingValue( getData().getLowestUnsolvedValue() );
				_puzzleDisplay.drawTile(getData().getPuzzlePosition( _previousChangedTile ) );
				setAutoSolving( true );
				setInformation("Solving " + Integer.toString( _previousChangedTile ) );
			}
			else {
				setInformation("Click a tile" );
			}
			_regularControls.showControls( );
			_puzzleDisplay.repaint();
			_totalMoves = 0;			// No moves since the last shuffle.
		}
//		Switch between automatic and manual solving
		else if ( puzzleCommand.equals("Solve") )
		{
//			Is the computer doing the hard work ?
			if ( getData().isAutoSolving() )
			{
//				Clear the automatic solution and make the users think for themselves.
				setAutoSolving( false );
				getData().clearAutoSolution( );
				getData().setSolvingValue( 0 );
			}
//				If the puzzle is already complete, clear out the solution and switch to manual
			else if ( getData().isSolved() )
			{
				setAutoSolving( false );
				getData().clearAutoSolution();
				setInformation("Puzzle already solved - shuffle or click a tile" );
			}
			else
			{
//				Calculate the automatic solution.
				setAutoSolution( new SolveTilePuzzle ( getData() ) );

//				Switch to automatic, and redraw the target tile.
				_previousChangedTile = getData().getLowestUnsolvedValue();
				_puzzleDisplay.drawTile(getData().getPuzzlePosition(_previousChangedTile) );
				setAutoSolving( true );
			}
			_regularControls.showControls( );
			_puzzleDisplay.repaint();
		}
//		Have the computer make a single move from the automatic solution
		else if ( puzzleCommand.equals("NextMove") )
		{
//			If the puzzle is solving automatically, switch to manual.
			if ( getData().isAutoSolving() )
			{
				setAutoSolving( false );
			}
			else
			{
//				If there is no automatic solution assigned, create one.
				if ( getData().getAutoSolution() == null)
				{
					setAutoSolution( new SolveTilePuzzle ( getData() ) );
				}
			}

//			If the puzzle is not already solved, make the next move.
			if ( ! ( getData().getAutoSolution().isComplete() ) )
			{
				moveTileManual ( getData().getAutoSolution().getTileToMove() );
				setInformation("Solving " + Integer.toString( _previousChangedTile ) + " - " + Integer.toString( _totalMoves ) + " moves");
			}
			else
			{

//				Puzzle solved, so tell the user and clear the automatic solution.
				getData().clearAutoSolution ();
				setInformation("Puzzle solved ! - " + Integer.toString( _totalMoves ) + " moves");
				_totalMoves = 0;
				( (AutoDisplay) _puzzleDisplay).repaint();
			}
			_regularControls.showControls( );

		}
		else if ( puzzleCommand.equals("Resize") )
		{
			remove(_regularControls);
			doLayout();
			repaint();
			add("North", _resizeControls );
			_resizeControls.rebuildAlternatives();
			_resizeControls.showControls();
			_resizeControls.doLayout();
			doLayout();
			repaint();
			validate();
			_totalMoves = 0;			// No moves since the last shuffle.
		}
//		Display the style controls
		else if ( puzzleCommand.equals("Style") )
		{
			remove(_regularControls);
			_styleControls.setStyle( _puzzleStyle );
			add("North", _styleControls );
			_styleControls.rebuildAlternatives();
			doLayout();
			validate();
		}
//		Display the style controls
		else if ( puzzleCommand.equals("Speed") )
		{
			remove(_regularControls);
			_speedControls.setSpeed( _puzzleSpeed );
			add("North", _speedControls );
			_speedControls.rebuildAlternatives();
			_speedControls.showControls();
			doLayout();
			validate();
		}
		else if ( puzzleCommand.equals("UseImage") )
		{
			_puzzleStyle = "ImageStyle";
			setImageName( ( (DisplayEvent) puzzleEvent).getImageName() );
			ImageIcon tmpIcon = new ImageIcon(getImageName());
			setImage ( tmpIcon.getImage() );

			remove(_styleControls);
			add("North", _regularControls );
			_regularControls.rebuildAlternatives();
			addThumbnailImage( getImage() );
			doLayout();
			validate();
			rebuildPuzzleDisplay();
		}
//		Accept the changes from the Style or Resize controls
		else if ( puzzleCommand.equals("StyleOK") )
		{
//			If the Style controls are on display, replace them with the Regular controls
//			and rebuild the puzzle display in the new style.
			_puzzleStyle = _styleControls.getStyle( );
			remove(_styleControls);
			add("North", _regularControls );
			_regularControls.rebuildAlternatives();
			doLayout();
			validate();
			rebuildPuzzleDisplay();

			Dimension puzzleSize = _resizeControls.getPuzzleSize();
			if ( puzzleSize != null )
			{
//				The change in style may mean that the size is now invalid.
//				Check the size and reset it to the default if necessary.
				if ( ! _puzzleDisplay.isValidPuzzleSize( puzzleSize ) )
				{
					setAutoSolving( false );
					getData().clearAutoSolution( );
					getData().buildPuzzleData( _defaultPuzzleSize.height, _defaultPuzzleSize.width );
					rebuildPuzzleDisplay();
					_regularControls.showControls( );
					doLayout();
					validate();
					setInformation("Puzzle resize forced due to style change");
					_totalMoves = 0;			// No moves since the last shuffle.
				}
			}
//			setInformation("Style changed");
		}
//		Discard the changes from the Style or Resize controls and replace them with the Regular controls
		else if ( puzzleCommand.equals("StyleCancel") )
		{
			remove(_styleControls);
			add("North", _regularControls );
			_regularControls.rebuildAlternatives();
			doLayout();
			validate();
		}
//		Accept the changes from the Style or Resize controls
		else if ( puzzleCommand.equals("ResizeOK") )
		{
			Dimension puzzleSize = _resizeControls.getPuzzleSize();
			if ( puzzleSize != null )
			{
//				Replace the Resize controls on display with the Regular controls, rebuild the puzzle in the new size.
//				recreate the puzzle in the new size and remove any existing automatic solution.
				if ( _puzzleDisplay.isValidPuzzleSize( puzzleSize ) )
				{
					setAutoSolving( false );
					getData().clearAutoSolution( );
					getData().buildPuzzleData( puzzleSize.height, puzzleSize.width );
					remove(_resizeControls);
					add("North", _regularControls );
					_regularControls.rebuildAlternatives();
					_regularControls.showControls( );
					doLayout();
					validate();
					rebuildPuzzleDisplay();
					setInformation("Puzzle resized");
				}
				else
				{
					setInformation("Invalid puzzle size - use smaller values");
				}
			}
		}
//		Accept the changes from the Style or Resize controls
		else if ( puzzleCommand.equals("SpeedOK") )
		{
//			If the Style controls are on display, replace them with the Regular controls
//			and rebuild the puzzle display in the new style.
			_puzzleSpeed = _speedControls.getSpeed( );
			setSpeed( _puzzleSpeed );
			remove(_speedControls);
			add("North", _regularControls );
			_regularControls.rebuildAlternatives();
			doLayout();
			validate();
			rebuildPuzzleDisplay();
		}
//		Discard the changes from the Style or Resize controls and replace them with the Regular controls
		else if ( puzzleCommand.equals("SpeedCancel") )
		{
			remove(_speedControls);
			add("North", _regularControls );
			_regularControls.rebuildAlternatives();
			doLayout();
			validate();
		}
//		Discard the changes from the Style or Resize controls and replace them with the Regular controls
		else if ( puzzleCommand.equals("ResizeCancel") )
		{
			remove(_resizeControls);
			add("North", _regularControls );
			_regularControls.rebuildAlternatives();
			doLayout();
			validate();
		}
//		Accept the changes from the Style or Resize controls
		else if ( puzzleCommand.equals("ResetPuzzle") )
		{
//			Replace the Resize controls on display with the Regular controls, rebuild the puzzle in the new size.
//			recreate the puzzle in the new size and remove any existing automatic solution.
			setAutoSolving( false );
			getData().clearAutoSolution( );
			getData().buildPuzzleData();
			_regularControls.rebuildAlternatives();
			_regularControls.showControls( );
			doLayout();
			validate();
			rebuildPuzzleDisplay();
			_totalMoves = 0;			// No moves since the last shuffle.
			setInformation("Puzzle reset");
		}
//		Display a thumbnail image in the information panel
		else if ( puzzleCommand.equals("AddThumbnail") )
		{
//			After the base image has been loaded, a thumbnail image must be displayed
			addThumbnailImage( getImage() );
		}
	}

//	Record the image name to be used in the puzzle when it's displayed as a PuzzleImageDisplay
	public void setImageName( String imageName ) { _imageName = imageName; }

//	Return the image name to be used in the puzzle when it's displayed as a PuzzleImageDisplay
	public String getImageName( ) { return _imageName; }

//	Record the image to be used in the puzzle when it's displayed as a PuzzleImageDisplay
	public void setImage( Image baseImage ) {
		_baseImage = baseImage;
	}

//	Return the image to be used in the puzzle when it's displayed as a PuzzleImageDisplay
	public Image getImage( ) { return _baseImage; }

//	Return the image to be used in the puzzle when it's displayed as a PuzzleImageDisplay
	public String getStyle( ) { return _puzzleStyle; }

	public void rebuildPuzzle( ) {
		rebuildPuzzleData();
		rebuildPuzzleDisplay();
	}

//	Build the data describing the tile layout in the puzzle, then use it for the display
	public void rebuildPuzzleData( ) {
		getData().buildPuzzleData( _puzzleSize );
		if ( _puzzleDisplay != null )
		{
			_puzzleDisplay.setData( getData() );
		}
	}

//	Build the display after disposing of the old one
	public void rebuildPuzzleDisplay( ) {

//		If there is already a puzzle display, remove it from the applet display
		if ( _puzzleDisplay != null )
		{
			remove(_puzzleDisplay);
			_puzzleDisplay = null;
		}

//		Check what kind of puzzle should be created. If _buttonPuzzle is true, build a display with buttons for tiles.
//		if ( _puzzleStyle.equals( "ButtonStyle" ) )
		if ( getStyle().equals( "ButtonStyle" ) )
		{
//			If a thumbnail image has been built, we must remove it from the display and dispose of it.
			getInformationPanel().removeThumbnailImage( );

//			Build a display with buttons for tiles.
			_puzzleDisplay = new PuzzleButtonDisplay( );
		}
		else if ( getStyle().equals( "DrawnStyle" ) )
		{
//			If a thumbnail image has been built, we must remove it from the display and dispose of it.
			getInformationPanel().removeThumbnailImage( );

//			Build a display by drawing each tile as a number on a plain background
			_puzzleDisplay = new PuzzleDrawnDisplay( );
		}
		else
		{
			if ( isRunAsApplet() )
			{
//				Build a display by drawing sections of an image as the tiles
//				For an applet, build a display based on an image.
				setImage ( getImage( getDocumentBase(), getImageName() ) );
			}
			else
			{
				// Added for loading a new image
				ImageIcon tmpIcon = new ImageIcon(getImageName());
				setImage ( tmpIcon.getImage() );
			}



//			Display the puzzle using the image
			_puzzleDisplay = new PuzzleImageDisplay( getData(), getImage() );

//			Build the new thumbnail image and display it
			//getInformationPanel().addThumbnailImage( getImage() );
		}

//		If the puzzle display has been built, display it and keep track of events in it
		if ( _puzzleDisplay != null )
		{
			add("Center", _puzzleDisplay);			// Display the puzzle in the centre of the applet
			_puzzleDisplay.setFont( getFont() );
			_puzzleDisplay.repaint();
			doLayout();
		}

//		The display size may have changed, so sort out the display layout
		_puzzleDisplay.setData( getData() );
		_puzzleDisplay.doLayout();
		_puzzleDisplay.repaint();
		doLayout();
	}

//	The information panel has a read-only text field and (when appropriate) a thumbnail image
	public void setInformationPanel( InformationPanel informationPanel ) {
		_informationPanel = informationPanel;
	}

//	Return a reference to the information panel.
	public InformationPanel getInformationPanel( ) {
		return _informationPanel;
	}

//	When the puzzle display is of an image, this method displays the thumbnail of the image in the information panel.
	public void addThumbnailImage( Image image ) {
		getInformationPanel().addThumbnailImage ( image );
	}

//	When the puzzle display is of an image, this method displays the thumbnail of the image in the information panel.
	public void addThumbnailImage( ThumbnailImage image ) {
		getInformationPanel().addThumbnailImage ( image );
	}

//	When the puzzle display is no longer of an image, the thumbnail of the image is removed from the information panel.
	public void removeThumbnailImage( ThumbnailImage image ) {
		getInformationPanel().removeThumbnailImage ( image );
	}

//	Display the text in the information panel.
	public void setInformation( String information ) {
		getData().sendMessage(information);
	}

//	When the puzzle is to be switched between manual and automatic solving of the puzzle, this method records the new state
//	and displays a message. If the puzzle is switching to automatic solving, it will note the tile with which to start its solution.
	public void setAutoSolving( boolean autoSolving ) {
		getData().setAutoSolving( autoSolving );
		if ( autoSolving ) {
			getData().setSolvingValue( getData().getLowestUnsolvedValue() );
			setInformation("Solving " + Integer.toString( getData().getLowestUnsolvedValue() ) + " - " + Integer.toString( _totalMoves ) + " moves" );
		}
		else {
			setInformation("Click a tile - " + Integer.toString( _totalMoves ) + " moves" );
		}
	}

//	Move a tile.
	public void moveTile( PuzzlePosition affectedTile ) {
//		Move the affected tile, then redraw it and the blank tile (with which it has been interchanged).
		PuzzlePosition blankTile = getData().getPuzzlePosition(getData().blankTileValue());
		getData().moveTile ( affectedTile );
		_puzzleDisplay.drawTile(affectedTile);
		_puzzleDisplay.drawTile(blankTile);
		_totalMoves++;			// Increment the move total to reflect this move.

	}

//	Move a tile when the solving is manual (so we display movable tiles specially).
	public void moveTileManual( PuzzlePosition affectedTile ) {
//		For manual solving, display all of the movable tiles with the "normal" style, instead of the "movable" one
		if ( ! getData().isAutoSolving() )
		{
			ArrayList<PuzzlePosition> movableTiles = getData().getMovablePuzzlePositions();
			boolean oldMakingNormal = ( (AutoDisplay) _puzzleDisplay ).isMakingNormal();
			( (AutoDisplay) _puzzleDisplay ).setMakeNormal( true );
			for ( PuzzlePosition movableTile: movableTiles ) {
				_puzzleDisplay.drawTile(movableTile);
			}
			( (AutoDisplay) _puzzleDisplay ).setMakeNormal( oldMakingNormal );
		}

//		Move the affected tile, then redraw it and the blank tile (with which it has been interchanged).
		moveTile ( affectedTile );

//		For manual solving, display all of the movable tiles with the "movable" style, instead of the "normal" one
		if ( ! getData().isAutoSolving() )
		{
			ArrayList<PuzzlePosition> movableTiles = getData().getMovablePuzzlePositions();
			for ( PuzzlePosition movableTile: movableTiles ) {
				_puzzleDisplay.drawTile(movableTile);
			}
		}

	}

//	This method performs the next "automatic" tile move. When not solving the puzzle automatically, this method does nothing.
//	If the puzzle is solving automatically, it will only move a tile if a solution exists and is not complete.
	public void nextAutoMove () {
		if ( ( _puzzleDisplay != null ) && getData().isAutoSolving() )
		{

			if ( ! ( getData().getAutoSolution() == null ) )
			{
				if ( ! ( getData().getAutoSolution().isComplete() ) )
				{
					PuzzleEvent puzzleEvent = new PositionEvent ( this, getData().getAutoSolution().getTileToMove(), "NextSolve" );
					getData().notifyListeners(puzzleEvent);
				}
				else
				{
					setAutoSolving ( false );
					getData().setAutoSolving ( false );
					setInformation ( "Puzzle solved ! - " + Integer.toString( _totalMoves ) + " moves" );
					_regularControls.showControls( );
					_puzzleDisplay.repaint();
				}
			}
		}
	}

//	This method connects the solution to the Puzzle data.
//	It also adds a listener for the solution (so we can know when things change), and forgets the previous changed tile.
	public void setSpeed( String puzzleSpeed ) {
		if ( puzzleSpeed.equals( "Slowest") )
		{
			_moveDelay = 2000;			// The delay time between automatic moves
			_puzzleSpeed = "Slowest";
		}
		else if ( puzzleSpeed.equals( "Slower") )
		{
			_moveDelay = 1000;			// The delay time between automatic moves
			_puzzleSpeed = "Slower";
		}
		else if ( puzzleSpeed.equals( "Faster") )
		{
			_moveDelay = 250;			// The delay time between automatic moves
			_puzzleSpeed = "Faster";
		}
		else if ( puzzleSpeed.equals( "Fastest") )
		{
			_moveDelay = 100;			// The delay time between automatic moves
			_puzzleSpeed = "Fastest";
		}
		else
		{
			_moveDelay = 500;			// The delay time between automatic moves
			_puzzleSpeed = "Average";
		}
	}

//	This method connects the solution to the Puzzle data.
//	It also adds a listener for the solution (so we can know when things change), and forgets the previous changed tile.
	public void setAutoSolution( SolveTilePuzzle autoSolution ) {
		getData().setAutoSolution ( autoSolution );
		getData().addSolutionListener ( this );
		_previousChangedTile = 0;
	}

//	This is the method from the SolutionListener interface. When the solution operates on a different tile, this method should be told about it (i.e called)
	public void solutionChanged(SolutionEvent solutionEvent) {
		int tileValue = solutionEvent.getTileValue();
//		Ensure the display draws the new solving tile correctly
		getData().setSolvingValue( tileValue );
		if ( _previousChangedTile != 0 )
		{
//			Draw the old solving tile, so that it gets drawn normally.
			_puzzleDisplay.drawTile(getData().getPuzzlePosition(_previousChangedTile) );
		}
		_previousChangedTile = tileValue;

//		Display a message about the progress of the automatic solution
		if ( tileValue == 0 )
		{
			setInformation("Solved - " + Integer.toString( _totalMoves ) + " moves" );
		}
		else
		{
			_puzzleDisplay.drawTile(getData().getPuzzlePosition(tileValue) );
			setInformation("Solving " + Integer.toString( tileValue ) + " - " + Integer.toString( _totalMoves ) + " moves" );
		}
	}

//	Just a wrapper method for getting the named parameter from the main applet.
	public String getParam(String p,String d)
	{
		String s = getParameter(p);
		return s == null ? d : s;
	}

}

