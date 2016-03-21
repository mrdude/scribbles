package scribbles.dom;

import java.io.IOException;

public class InvalidNotebookFormatException extends IOException
{
	public InvalidNotebookFormatException()
	{
	}

	public InvalidNotebookFormatException(Throwable t)
	{
		super(t);
	}
}
