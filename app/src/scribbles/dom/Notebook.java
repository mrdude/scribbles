package scribbles.dom;

import org.jetbrains.annotations.Nullable;

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
	private final NotebookSearcher searcher = new NotebookSearcher(this);

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
		noteList.add( n );
		return n;
	}

	public Note duplicateNote(Note n)
	{
		Note copy = new Note(n);
		noteList.add( noteList.indexOf(n)+1, copy );
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
	}

	/** Loads the notebook from it's file */
	private void load() throws IOException, InvalidNotebookFormatException
	{
		final NotebookReader r = NotebookReader.load(file);
		for( String str : r.noteList )
			noteList.add( new Note(str, false) );
	}

	/**
	 * Searches the notebook
	 * @return the number of search results
	 */
	public int updateSearch(String searchString)
	{
		boolean needsRestyle = searcher.updateSearch(searchString);

		if( needsRestyle )
		{
			for( Note n : getNoteList() )
				n.clearSearchHighlights();

			for( SearchResult res : searcher )
			{
				res.n.addSearchHighlight( res.pos, searchString.length() );
			}
		}

		return searcher.getSearchResultCount();
	}
}
