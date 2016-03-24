package scribbles.listeners;

import scribbles.gui.ScribbleFrame;

public interface ScribbleWindowListener
{
	void windowCreated(ScribbleFrame win);
	void windowFocused(ScribbleFrame win, int windowIndex);
	void windowDestroyed(ScribbleFrame win, int windowIndex);
}
