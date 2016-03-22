package scribbles.gui;

import org.jetbrains.annotations.Nullable;
import scribbles.dom.Note;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Holds onto the active note edit pane
 */
public class ActiveNoteContainer extends JPanel
{
	private final JEditorPane editPane = new JEditorPane();
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
			editPane.setDocument(note);
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
