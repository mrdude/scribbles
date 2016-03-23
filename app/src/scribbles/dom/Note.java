package scribbles.dom;

import org.jetbrains.annotations.Nullable;

import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Note extends DefaultStyledDocument implements StyledDocument
{
	private final UndoManager undoManager = new UndoManager();
	private String lastSavedText = "";

	public Note()
	{
		this("");
	}

	public Note(Note n)
	{
		this( n.getDocumentText() );
	}

	public Note(String initialText, boolean dirtyFlag)
	{
		this(initialText);

		if( !dirtyFlag )
			lastSavedText = initialText;
	}

	public Note(String initialText)
	{
		try
		{
			addStyle("text", getStyle(StyleContext.DEFAULT_STYLE));
			addStyle("searchResult", getStyle(StyleContext.DEFAULT_STYLE));
			addStyle("title", getStyle("text"));
			addStyle("note", getStyle("text"));

			getStyle("title").addAttribute( StyleConstants.FontSize, 24 );
			getStyle("title").addAttribute( StyleConstants.FontConstants.Underline, true );
			getStyle("searchResult").addAttribute( StyleConstants.Background, Color.yellow );
			getStyle("note").addAttribute( StyleConstants.FontConstants.Bold, true );

			insertString(0, initialText, null);

			undoManager.setLimit(-1); //unlimited undos
			addUndoableEditListener( undoManager::undoableEditHappened );
		}
		catch( BadLocationException e )
		{
			//This should NEVER be thrown
			e.printStackTrace();
		}
	}

	public boolean isDirty()
	{
		return !getDocumentText().equals( lastSavedText );
	}

	public void resetDirtyFlag()
	{
		lastSavedText = getDocumentText();
	}

	public void insertString(int offs, String str, @Nullable AttributeSet a) throws BadLocationException
	{
		super.insertString(offs, str, a);
		styleText();
	}

	public void remove(int offs, int len) throws BadLocationException
	{
		super.remove(offs, len);
		styleText();
	}

	private void styleText()
	{
		final Style textAttr = getStyle("text");
		final Style titleAttr = getStyle("title");
		final Style noteAttr = getStyle("note");

		//style the text
		final String text = getDocumentText();
		int type = 0;
		final int TYPE_TITLE = 0;
		final int TYPE_TEXT = 1;
		final int TYPE_NOTE = 2;
		for( int x = 0; x < text.length(); )
		{
			switch( type )
			{
				case TYPE_TITLE:
					{
						final int index = text.indexOf("\n", x);
						if( index == -1 )
						{
							setCharacterAttributes(x, text.length(), titleAttr, true);
							x = text.length();
						}
						else
						{
							setCharacterAttributes(x, index, titleAttr, true);
							x = index+1;
							type = TYPE_TEXT;
						}
					}
					break;
				case TYPE_TEXT:
					{
						final int index = text.indexOf("[", x);
						if( index == -1 )
						{
							setCharacterAttributes(x, text.length(), textAttr, true);
							x = text.length();
						}
						else
						{
							setCharacterAttributes(x, index, textAttr, true);
							x = index+1;
							type = TYPE_NOTE;
						}
					}
					break;
				case TYPE_NOTE:
					{
						final int index = text.indexOf("]", x);
						if( index == -1 )
						{
							setCharacterAttributes(x, text.length(), noteAttr, true);
							x = text.length();
						}
						else
						{
							setCharacterAttributes(x, index-1, noteAttr, true);
							x = index;
							type = TYPE_TEXT;
						}
					}
					break;
			}
		}

		//add search highlights
		for( SearchResult h : searchHighlights )
			setCharacterAttributes(h.pos, h.len, getStyle("searchResult"), false);
	}

	private final List<SearchResult> searchHighlights = new ArrayList<>();

	void clearSearchHighlights()
	{
		searchHighlights.clear();
	}

	void addSearchHighlight(SearchResult res)
	{
		searchHighlights.add( res );
		styleText();
	}

	public boolean canUndo()
	{
		return undoManager.canUndo();
	}

	public boolean canRedo()
	{
		return undoManager.canRedo();
	}

	public String getUndoPresentationName()
	{
		return undoManager.getUndoPresentationName();
	}

	public String getRedoPresentationName()
	{
		return undoManager.getRedoPresentationName();
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
