package scribbles;

import scribbles.gui.ScribbleFrame;
import scribbles.notebook.Notebook;

import javax.swing.*;

public class Scribbles
{
	public static void main(String[] args)
	{
		//TODO Will this compile on non-mac systems?
		if( Utils.getOS() == Utils.OS.MAC )
			MacInit.initMacStuff();

		//TODO accept a notebook file to open on the command line

		ScribbleFrame win = new ScribbleFrame( Notebook.DEFAULT_NOTEBOOK_FILE ); //open the first frame
		win.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}
