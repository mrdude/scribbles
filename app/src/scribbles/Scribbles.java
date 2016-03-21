package scribbles;

import com.apple.eawt.*;
import scribbles.dom.Notebook;
import scribbles.gui.AboutDialog;
import scribbles.gui.PreferencesDialog;
import scribbles.gui.ScribbleApplication;
import scribbles.gui.ScribbleFrame;

import javax.swing.*;
import java.util.prefs.Preferences;

public class Scribbles
{
	public static void main(String[] args)
	{
		initMacStuff();

		//TODO accept a notebook file to open on the command line

		ScribbleFrame win = new ScribbleFrame( Notebook.DEFAULT_NOTEBOOK_FILE ); //open the first frame
		win.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	//TODO Figure out how to make sure this compiles on non-OSX systems
	private static void initMacStuff()
	{
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Scribbles"); //set the application title on Mac OSX
		System.setProperty("apple.laf.useScreenMenuBar", "true"); //use the menu on Mac OSX

		Application.getApplication().setAboutHandler(new AboutHandler() {
			public void handleAbout(AppEvent.AboutEvent aboutEvent)
			{
				AboutDialog.show(null);
			}
		});

		Application.getApplication().setPreferencesHandler(new PreferencesHandler() {
			public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent)
			{
				PreferencesDialog.show();
			}
		});

		Application.getApplication().setQuitHandler(new QuitHandler() {
			public void handleQuitRequestWith(AppEvent.QuitEvent quitEvent, QuitResponse quitResponse)
			{
				//don't quit the application, but send a quit message to all open windows
				quitResponse.cancelQuit();
				ScribbleApplication.requestCloseOnAllWindows();
			}
		});
	}
}
