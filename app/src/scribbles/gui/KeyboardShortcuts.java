package scribbles.gui;

import com.google.common.collect.ImmutableList;
import scribbles.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

public class KeyboardShortcuts
{
	private static final int shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(); //returns Ctrl on Windows/Linux, Cmd on Mac

	public static final KeyboardShortcuts newNotebook = new KeyboardShortcuts(
			"New Notebook",
			"Creates a new notebook",
			KeyStroke.getKeyStroke('N', shortcutKey | InputEvent.SHIFT_DOWN_MASK )
	);
	public static final KeyboardShortcuts openNotebook = new KeyboardShortcuts(
			"Open Notebook",
			"Opens a notebook",
			KeyStroke.getKeyStroke('O', shortcutKey | InputEvent.SHIFT_DOWN_MASK )
	);
	public static final KeyboardShortcuts saveNotebookAs = new KeyboardShortcuts(
			"Save Notebook As",
			"Saves the current notebook to a new location",
			KeyStroke.getKeyStroke('S', shortcutKey | InputEvent.SHIFT_DOWN_MASK )
	);
	public static final KeyboardShortcuts saveNotebook = new KeyboardShortcuts(
			"Save Notebook",
			"Saves the current notebook to disk",
			KeyStroke.getKeyStroke('S', shortcutKey)
	);
	public static final KeyboardShortcuts focusOnEdit = new KeyboardShortcuts(
			"Focus on Edit Window",
			"Places cursor focus on the edit window",
			KeyStroke.getKeyStroke('E', shortcutKey)
	);
	public static final KeyboardShortcuts minimizeCurrentWindow = new KeyboardShortcuts(
			"Minimize",
			"Minimizes the current window",
			KeyStroke.getKeyStroke('M', shortcutKey)
	);

	//notes
	public static final KeyboardShortcuts newNote = new KeyboardShortcuts(
			"New Note",
			"Creates a new note in the current notebook",
			KeyStroke.getKeyStroke('N', shortcutKey)
	);
	public static final KeyboardShortcuts duplicateActiveNote = new KeyboardShortcuts(
			"Duplicate",
			"Makes a copy of the active note",
			KeyStroke.getKeyStroke('D', shortcutKey)
	);

	//copy / paste
	public static final KeyboardShortcuts cut = new KeyboardShortcuts(
			"Cut",
			"Cuts the current selection to the keyboard",
			KeyStroke.getKeyStroke('X', shortcutKey)
	);
	public static final KeyboardShortcuts copy = new KeyboardShortcuts(
			"Copy",
			"Copies the current selection to the keyboard",
			KeyStroke.getKeyStroke('C', shortcutKey)
	);
	public static final KeyboardShortcuts paste = new KeyboardShortcuts(
			"Paste",
			"Pastes the contents of the clipboard",
			KeyStroke.getKeyStroke('V', shortcutKey)
	);

	//undo / redo
	public static final KeyboardShortcuts undo = new KeyboardShortcuts(
			"Undo",
			"Undos the last action",
			KeyStroke.getKeyStroke('Z', shortcutKey )
	);
	public static final KeyboardShortcuts redo = new KeyboardShortcuts(
			"Redo",
			"Redos the last undo",
			KeyStroke.getKeyStroke('Z', shortcutKey | InputEvent.SHIFT_DOWN_MASK)
	);

	//list
	private static List<KeyboardShortcuts> mutableShortcuts;
	public static final ImmutableList<KeyboardShortcuts> shortcuts;
	static {
		shortcuts = ImmutableList.copyOf(mutableShortcuts);
		mutableShortcuts = null;
	}

	//
	private final String name, desc;
	private KeyStroke keystroke;
	private final List<Runnable> changeListenerList = new ArrayList<>();
	private final boolean changeable;

	private KeyboardShortcuts(KeyStroke keystroke)
	{
		this("Not yet", "Not Yet", keystroke, true);
	}

	private KeyboardShortcuts(String name, String desc, KeyStroke keystroke)
	{
		this(name, desc, keystroke, true);
	}

	/**
	 * @param name the user displayable name of the shortcut
	 * @param desc a short description of the shortcut
	 * @param keystroke the initial value of the keystroke
	 * @param changeable if true, the user cannot change this shortcut
	 */
	private KeyboardShortcuts(String name, String desc, KeyStroke keystroke, boolean changeable)
	{
		if( mutableShortcuts == null )
			mutableShortcuts = new ArrayList<>();
		mutableShortcuts.add( this );

		this.name = name;
		this.desc = desc;
		this.keystroke = keystroke;
		this.changeable = changeable;
	}

	public String name()
	{
		return name;
	}

	public String desc()
	{
		return desc;
	}

	public KeyStroke keystroke()
	{
		return keystroke;
	}

	public String toUserDisplayableString()
	{
		return Utils.keyStrokeToString(keystroke);
	}

	public void keystroke(KeyStroke keystroke)
	{
		if( changeable )
		{
			this.keystroke = keystroke;
			fireChangeEvent();
		}
	}

	/** This is so that the preference window's JTree displays the correct text */
	public String toString()
	{
		return name;
	}

	public void addChangeListener(Runnable changeListener)
	{
		synchronized( changeListenerList )
		{
			changeListenerList.add( changeListener );
		}
	}

	public void removeChangeListener(Runnable changeListener)
	{
		synchronized( changeListenerList )
		{
			changeListenerList.remove( changeListener );
		}
	}

	private void fireChangeEvent()
	{
		final List<Runnable> listenerArray;

		synchronized( changeListenerList )
		{
			listenerArray = new ArrayList<>( changeListenerList );
		}

		for( Runnable p : listenerArray )
			p.run();
	}
}
