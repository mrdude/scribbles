package scribbles.dom;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

public class Note extends DefaultStyledDocument implements Document
{
	private final UndoManager undoManager = new UndoManager();

	public Note()
	{
		this("");
	}

	public Note(Note n)
	{
		this( n.getDocumentText() );
	}

	public Note(String initialText)
	{
		try
		{
			insertString(0, initialText, null);
			initUndoManager();
		}
		catch( BadLocationException e )
		{
			//This should NEVER be thrown
			e.printStackTrace();
		}
	}

	private void initUndoManager()
	{
		undoManager.setLimit(-1); //unlimited undos
		addUndoableEditListener( undoManager::undoableEditHappened );
	}

	public boolean canUndo()
	{
		return undoManager.canUndo();
	}

	public boolean canRedo()
	{
		return undoManager.canRedo();
	}

	public void undo()
	{
		undoManager.undo();
	}

	public void redo()
	{
		undoManager.redo();
	}

	public String getDocumentText()
	{
		return getDocumentText(0, getLength());
	}

	public String getDocumentText(int offset, int len)
	{
		try
		{
			return getText(offset, len);
		}
		catch( BadLocationException e )
		{
			throw new RuntimeException("This should never be thrown!", e);
		}
	}

	/** Returns the title of the document */
	public String getTitle()
	{
		int firstNewline = getDocumentText().indexOf('\n');
		if( firstNewline == -1 )
			firstNewline = getLength();

		String title = getDocumentText(0, firstNewline);

		if( title.isEmpty() )
			title = "<untitled note>";

		return title;
	}

	/** This returns the title of the note -- it is used by NoteListPanel's JTree */
	public String toString()
	{
		return getTitle();
	}
}
