package scribbles;

import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitResponse;
import scribbles.gui.AboutDialog;
import scribbles.gui.PreferencesDialog;
import scribbles.gui.ScribbleApplication;

class MacInit
{
	static void initMacStuff()
	{
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Scribbles"); //set the application title on Mac OSX
		System.setProperty("apple.laf.useScreenMenuBar", "true"); //use the menu on Mac OSX

		Application.getApplication().setAboutHandler( (aboutEvent) -> AboutDialog.show() );
		Application.getApplication().setPreferencesHandler( (preferencesEvent) -> PreferencesDialog.show() );
		Application.getApplication().setQuitHandler( (AppEvent.QuitEvent quitEvent, QuitResponse quitResponse) -> {
			//don't quit the application, but send a quit message to all open windows
			quitResponse.cancelQuit();
			ScribbleApplication.requestCloseOnAllWindows();
		});
	}
}
