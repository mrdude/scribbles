package scribbles.dom;

import org.jetbrains.annotations.Nullable;
import scribbles.DocumentEventMulticaster;
import scribbles.gui.NotebookListener;

import javax.swing.event.EventListenerList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Notebook
{
	public static final File DEFAULT_NOTEBOOK_FILE = new File( System.getProperty("user.home"), "default_scribble.json" );
	private final File file; //the file that this notebook is saved to
	private final List<Note> noteList = new ArrayList<>();
	private Note activeNote = null;
	private final DocumentEventMulticaster eventMulticaster = new DocumentEventMulticaster();
	private final EventListenerList listenerList = new EventListenerList();

	private IOException ioException = null;

	public Notebook(File file)
	{
		this.file = file;

		if( file.exists() )
		{
			try
			{
				ioException = null;
				load();
			}
			catch(IOException e)
			{
				//store the exception so that the ScribbleFrame knows what happened
				ioException = e;
			}
		}
	}

	/**
	 * Returns the event multicaster for this notebook.
	 * Listeners attached to this multicaster are notified when changes happen to any note in the notebook.
	 */
	public DocumentEventMulticaster getDocumentEventMulticaster()
	{
		return eventMulticaster;
	}

	public void addNotebookListener(NotebookListener l)
	{
		listenerList.add( NotebookListener.class, l );
	}

	public void removeNotebookListener(NotebookListener l)
	{
		listenerList.remove( NotebookListener.class, l );
	}

	private void fireNewNoteEvent(Note newNote)
	{
		final Object[] obj = listenerList.getListenerList();
		for( int x = 0; x < obj.length; x += 2 )
		{
			final NotebookListener l = (NotebookListener)obj[x+1];
			l.noteCreated(newNote);
		}
	}

	private void fireNotebookSavedEvent()
	{
		final Object[] obj = listenerList.getListenerList();
		for( int x = 0; x < obj.length; x += 2 )
		{
			final NotebookListener l = (NotebookListener)obj[x+1];
			l.notebookSaved();
		}
	}

	/**
	 * Returns the IOException that occured during the last IO operation (if any)
	 */
	public IOException getIOException()
	{
		return ioException;
	}

	/** Returns the file that this notebook represents */
	public File getFile()
	{
		return file;
	}

	public @Nullable Note getActiveNote()
	{
		return activeNote;
	}

	public void setActiveNote(Note n)
	{
		activeNote = n;
	}

	public Note createNote()
	{
		Note n = new Note();
		n.addDocumentListener( eventMulticaster.getSourceListener() );
		noteList.add( n );
		fireNewNoteEvent( n );
		return n;
	}

	public Note duplicateNote(Note n)
	{
		Note copy = new Note(n);
		copy.addDocumentListener( eventMulticaster.getSourceListener() );
		noteList.add( noteList.indexOf(n)+1, copy );
		fireNewNoteEvent( copy );
		return copy;
	}

	public List<Note> getNoteList()
	{
		return noteList;
	}

	/** Saves the notebook to it's file */
	public void save() throws IOException
	{
		//TODO if the save fails in the middle of writing, you will be left with a half-written file. Come up with an atomic save operation.
		NotebookWriter.save(file, noteList);

		for( Note n : noteList )
			n.resetDirtyFlag();

		fireNotebookSavedEvent();
	}

	/** Loads the notebook from it's file */
	private void load() throws IOException, InvalidNotebookFormatException
	{
		final NotebookReader r = NotebookReader.load(file);
		for( String str : r.noteList )
		{
			final Note n = new Note(str, false);
			n.addDocumentListener( eventMulticaster.getSourceListener() );
			noteList.add( n );
		}
	}

	/**
	 * Searches the notebook
	 * @return the number of search results
	 */
	public int updateSearch(String searchString)
	{
		//clear all search results
		int resultCount = 0;

		for( Note n : getNoteList() )
			n.clearSearchHighlights();

		//do the search
		if( !searchString.isEmpty() )
		{
			for( Note n : getNoteList() )
			{
				final String docText = n.getDocumentText();
				for( int x = docText.indexOf(searchString); x != -1 && x < docText.length() - searchString.length(); x = docText.indexOf(searchString, x + 1) )
				{
					int row = 0;
					int col = 0;

					for( int y = 0; y < x; y++ )
					{
						switch( docText.charAt(y) )
						{
							case '\n':
								row++;
								col = 0;
							default:
								col++;
								break;
						}
					}

					final SearchResult res = new SearchResult(n, x, searchString.length(), row, col);
					n.addSearchHighlight(res);
					resultCount++;
				}
			}
		}

		return resultCount;
	}
}
