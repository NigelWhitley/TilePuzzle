/*
 * @(#) FilePreviewer.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2003 Nigel Whitley. All Rights Reserved.
 *
 * This software is released under the terms o_fileHandle the GPL. For details, see license.txt.
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
 * An interactive simulation o_fileHandle the sliding tile puzzle where one tile
 * is removed to allow the others to be re-arranged.
 * Can be run either as a standalone application by
 * typing "java TilePuzzle" or as an applet in a suitable browser.
 */

 // Based on the Java example code, this component allows the user to choose an image file to use in the puzzle.
class FilePreviewer extends JComponent implements PropertyChangeListener {
	ImageIcon _thumbnail = null;
	File _fileHandle = null;

	public FilePreviewer(JFileChooser fileChooser) {
		setPreferredSize(new Dimension(100, 50));
		fileChooser.addPropertyChangeListener(this);
	}

	public void loadImage() {
		if ( _fileHandle != null ) {
			ImageIcon tmpIcon = new ImageIcon(_fileHandle.getPath());
			if ( tmpIcon.getIconWidth() > 90 ) {
			    _thumbnail = new ImageIcon(
				tmpIcon.getImage().getScaledInstance(90, -1, Image.SCALE_DEFAULT));
			} else {
			    _thumbnail = tmpIcon;
			}
		}
	}

	public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
		String prop = propertyChangeEvent.getPropertyName();
		if ( prop == JFileChooser.SELECTED_FILE_CHANGED_PROPERTY ) {
			_fileHandle = (File) propertyChangeEvent.getNewValue();
			if ( isShowing() ) {
				loadImage();
				repaint();
			}
		}
	}

	public void paint(Graphics g) {
		if ( _thumbnail == null ) {
			loadImage();
		}

		if ( _thumbnail != null ) {
			int x = getWidth()/2 - _thumbnail.getIconWidth()/2;
			int y = getHeight()/2 - _thumbnail.getIconHeight()/2;
			if (y < 0) {
				y = 0;
			}

			if (x < 5) {
				x = 5;
			}
			_thumbnail.paintIcon(this, g, x, y);
		}
	}
}
