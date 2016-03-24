package scribbles.gui;

import javax.swing.*;

public class AboutDialog
{
	private static final String aboutText = "Scribbles is a note-taking program";

	public static void show()
	{
		JOptionPane.showMessageDialog(null, "Scribbles is a note-taking program written in Java", "About Scribbles", JOptionPane.INFORMATION_MESSAGE);
	}
}
