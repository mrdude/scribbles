package scribbles.dom;

public class SearchResult
{
	private final Note n;
	private final int row, col;

	SearchResult(Note n, int row, int col)
	{
		this.n = n;
		this.row = row;
		this.col = col;
	}

	public String toString()
	{
		return String.format("Result in '%s' @ %d:%d", n.getTitle(), row + 1, col + 1);
	}
}