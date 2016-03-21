package scribbles.gui;

import org.jetbrains.annotations.Nullable;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;

/** Keeps track of all application windows */
public class ScribbleApplication
{
	private static final List<ScribbleFrame> openWindows = new ArrayList<>();
	private static final List<ScribbleWindowListener> listeners = new ArrayList<>();

	static void registerScribbleFrame(ScribbleFrame win)
	{
		synchronized( openWindows )
		{
			openWindows.add(win);
		}

		win.addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowGainedFocus(WindowEvent e)
			{
				final int index;

				synchronized( openWindows )
				{
					index = openWindows.indexOf(win);
				}

				fireWindowFocusedEvent(win, index);
			}

			@Override
			public void windowLostFocus(WindowEvent e)
			{

			}
		});

		fireWindowCreatedEvent(win);
	}

	static void deregisterScribbleFrame(ScribbleFrame win)
	{
		final int index;

		synchronized( openWindows )
		{
			index = openWindows.indexOf(win);
			openWindows.remove(win);
		}

		fireWindowDestroyedEvent(win, index);
	}

	static void addScribbleWindowListener(ScribbleWindowListener listener)
	{
		synchronized( listeners )
		{
			listeners.add( listener );
		}
	}

	static void removeScribbleWindowListener(ScribbleWindowListener listener)
	{
		synchronized( listeners )
		{
			listeners.remove( listener );
		}
	}

	/** Returns the number of open windows */
	public static int countOpenWindows()
	{
		synchronized( openWindows )
		{
			return openWindows.size();
		}
	}

	/**
	 * @param array if this array is of length >= countOpenWindows(), it will be filled with the list of open windows
	 * @return an array of all open windows
	 */
	public static ScribbleFrame[] getOpenWindows(@Nullable ScribbleFrame[] array)
	{
		synchronized( openWindows )
		{
			if( array == null )
				array = new ScribbleFrame[openWindows.size()];

			return openWindows.toArray(array);
		}
	}

	/** Sends a WINDOW_CLOSING event to all open windows */
	public static void requestCloseOnAllWindows()
	{
		ScribbleFrame[] windows = getOpenWindows(null);
		for( ScribbleFrame win : windows )
			win.dispatchEvent( new WindowEvent(win, WindowEvent.WINDOW_CLOSING) );
	}

	//listener helper functions
	private static void fireWindowCreatedEvent(ScribbleFrame win)
	{
		for( ScribbleWindowListener l : getListenerArray() )
			l.windowCreated(win);
	}

	private static void fireWindowFocusedEvent(ScribbleFrame win, int windowIndex)
	{
		for( ScribbleWindowListener l : getListenerArray() )
			l.windowFocused(win, windowIndex);
	}

	private static void fireWindowDestroyedEvent(ScribbleFrame win, int windowIndex)
	{
		for( ScribbleWindowListener l : getListenerArray() )
			l.windowDestroyed(win, windowIndex);
	}

	private static ScribbleWindowListener[] getListenerArray()
	{
		synchronized( listeners )
		{
			final int sz = listeners.size();
			return listeners.toArray(new ScribbleWindowListener[sz]);
		}
	}
}
