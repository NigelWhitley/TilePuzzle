/*
 * @(#)WindowCloseAdapter.java	1.1 5-December-2003  Nigel Whitley
 *
 * Copyright (c) 2003 Nigel Whitley. All Rights Reserved.
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
 * This component defines a mouse response for PuzzleDrawnDisplay and its descendents
 */

public class WindowCloseAdapter extends WindowAdapter {
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}
}
