package scribbles.gui;

import javax.swing.*;

public class AboutDialog
{
	public static void show(JComponent parent)
	{
		JOptionPane.showMessageDialog(parent, "Scribbles is a note-taking program", "About Scribbles", JOptionPane.INFORMATION_MESSAGE);
	}
}
