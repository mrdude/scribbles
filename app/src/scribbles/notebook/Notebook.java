package scribbles.notebook;

import org.jetbrains.annotations.Nullable;
import scribbles.listeners.NotebookListener;
import scribbles.swing_extensions.DocumentEventMulticaster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Notebook
{
	public static final File DEFAULT_NOTEBOOK_FILE = new File( System.getProperty("user.home"), "default_scribble.json" );
	private final File file; //the file that this notebook is saved to
	private final List<Note> noteList = new ArrayList<>();
	private Note activeNote = null;
	private final DocumentEventMulticaster eventMulticaster = new DocumentEventMulticaster();
	private final CopyOnWriteArrayList<NotebookListener> listenerList = new CopyOnWriteArrayList<>();

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

	/**
	 * Returns the IOException that occurred during the last load operation (if any)
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

	public void setActiveNote(@Nullable Note n)
	{
		activeNote = n;
		fireActiveNoteChangedEvent(n);
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
	public void save()
	{
		try
		{
			//TODO if the save fails in the middle of writing, you will be left with a half-written file. Come up with an atomic save operation.
			NotebookWriter.save(file, noteList);

			for( Note n : noteList )
				n.resetDirtyFlag();

			fireNotebookSavedEvent();
		}
		catch(IOException e)
		{
			fireFailedNotebookSaveEvent(e);
		}
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
		int resultCount = 0;

		//do the search
		if( searchString.isEmpty() )
		{
			for( Note n : getNoteList() )
				n.clearSearchHighlights();
		}
		else
		{
			for( Note n : getNoteList() )
			{
				final List<SearchResult> resultsForNote = new ArrayList<>();

				final String docText = n.getDocumentText();
				for( int x = docText.indexOf(searchString); x != -1 && x < docText.length(); x = docText.indexOf(searchString, x + 1) )
					resultsForNote.add( new SearchResult(n, x, searchString.length()) );

				resultCount += resultsForNote.size();
				n.setSearchHighlights(resultsForNote);
			}
		}

		return resultCount;
	}

	//notebook listener methods
	public void addNotebookListener(NotebookListener l)
	{
		listenerList.add( l );
	}

	public void removeNotebookListener(NotebookListener l)
	{
		listenerList.remove( l );
	}

	private void fireNewNoteEvent(Note newNote)
	{
		for( NotebookListener l : listenerList )
			l.noteCreated(newNote);
	}

	private void fireNotebookSavedEvent()
	{
		for( NotebookListener l : listenerList )
			l.notebookSaved();
	}

	private void fireFailedNotebookSaveEvent(IOException e)
	{
		for( NotebookListener l : listenerList )
			l.failedSave(e);
	}

	private void fireActiveNoteChangedEvent(final Note newActiveNote)
	{
		for( NotebookListener l : listenerList )
			l.activeNoteChanged(newActiveNote);
	}
}
