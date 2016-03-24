package scribbles.dom;

public class SearchResult
{
	public final Note n;
	public final int pos, len;
	public final int row, col;

	SearchResult(Note n, int pos, int len)
	{
		this.n = n;
		this.pos = pos;
		this.len = len;

		//find the row and column of the text
		final String docText = n.getDocumentText();
		int r = 0;
		int c = 0;

		for( int y = 0; y < pos; y++ )
		{
			switch( docText.charAt(y) )
			{
				case '\n':
					r++;
					c = 0;
				default:
					c++;
					break;
			}
		}

		this.row = r;
		this.col = c;
	}

	public String toString()
	{
		return String.format("Result in '%s' @ %d:%d", n.getTitle(), row + 1, col + 1);
	}
}