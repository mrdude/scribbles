package scribbles.listeners;

import javax.swing.*;

public interface ShortcutChangeListener
{
	void shortcutChanged(KeyStroke oldKeystroke, KeyStroke newKeyStroke);
}
