package scribbles.gui;

interface ScribbleWindowListener
{
	void windowCreated(ScribbleFrame win);
	void windowFocused(ScribbleFrame win, int windowIndex);
	void windowDestroyed(ScribbleFrame win, int windowIndex);
}
