package scribbles.gui;

import org.jetbrains.annotations.Nullable;
import scribbles.dom.Note;

import javax.swing.*;
import java.awt.*;

/**
 * Holds onto the active note edit pane
 */
public class ActiveNoteContainer extends JPanel
{
	private final JTextPane editPane = new JTextPane();
	private final JScrollPane scrollPane = new JScrollPane(editPane);

	public ActiveNoteContainer()
	{
		setLayout( new BorderLayout() );
		add( scrollPane, BorderLayout.CENTER );
		scrollPane.setVisible(false);
	}

	protected void paintComponent(Graphics graphics)
	{
		super.paintComponent(graphics);

		final Graphics2D g = (Graphics2D)graphics;
		g.setColor( Color.black );
		drawCenteredString(g, KeyboardShortcuts.newNote.toUserDisplayableString()+ " to create a new note", getWidth()/2, getHeight()/2, true, true);
	}

	private void drawCenteredString(Graphics2D g, String str, int x, int y, boolean center_x, boolean center_y)
	{
		if( center_x ) x -= g.getFontMetrics().stringWidth(str)/2;
		if( center_y ) y -= g.getFont().getSize()/2;

		g.drawString( str, x, y );
	}

	public void setActiveNote(@Nullable Note note)
	{
		scrollPane.setVisible( note != null );

		if( note != null )
			editPane.setStyledDocument(note);
	}

	public JEditorPane getEditPane()
	{
		return editPane;
	}

	public boolean requestFocusInWindow()
	{
		if( scrollPane.isVisible() )
			return editPane.requestFocusInWindow();
		else
			return super.requestFocusInWindow();
	}
}
