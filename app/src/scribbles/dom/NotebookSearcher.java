package scribbles.dom;

import com.google.common.collect.AbstractIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class NotebookSearcher implements Iterable<SearchResult>
{
	private final List<SearchResult> results = new ArrayList<>();
	private final Notebook notebook;

	NotebookSearcher(Notebook notebook)
	{
		this.notebook = notebook;
	}

	/**
	 * Updates search results
	 * @return true if the notes need to have their SearchHighlights updated
	 */
	public boolean updateSearch(String searchString)
	{
		results.clear();

		if( !searchString.isEmpty() )
		{
			for( Note n : notebook.getNoteList() )
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

					results.add( new SearchResult(n, x, row, col) );
				}
			}
		}

		return true;
	}

	public Iterator<SearchResult> iterator()
	{
		return new AbstractIterator<SearchResult>() {
			private final Iterator<SearchResult> it = results.iterator();

			@Override
			protected SearchResult computeNext()
			{
				return it.hasNext()
					? it.next()
					: endOfData();
			}
		};
	}

	public int getSearchResultCount()
	{
		return results.size();
	}
}
