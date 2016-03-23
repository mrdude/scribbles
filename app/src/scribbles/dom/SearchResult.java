package scribbles.dom;

public class SearchResult
{
	public final Note n;
	public final int pos;
	public final int row, col;

	SearchResult(Note n, int pos, int row, int col)
	{
		this.n = n;
		this.pos = pos;
		this.row = row;
		this.col = col;
	}

	public String toString()
	{
		return String.format("Result in '%s' @ %d:%d", n.getTitle(), row + 1, col + 1);
	}
}