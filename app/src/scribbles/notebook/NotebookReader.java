package scribbles.notebook;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

class NotebookReader
{
	public static NotebookReader load(File file) throws IOException, InvalidNotebookFormatException
	{
		try
		{
			return read(file, false);
		}
		catch(IOException e)
		{
			//attempt to read the .swp file
			NotebookReader r = read( Notebook.getTempFile(file), true );

			//if reading from the swap file works, replace the file with the .swp
			try
			{
				file.delete();
				Files.move(Notebook.getTempFile(file).toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);
			}
			catch(IOException e2) {} //we really don't care if this exception gets thrown

			return r;
		}
	}

	private static NotebookReader read(File file, boolean isSwapFile) throws IOException, MalformedJsonException
	{
		try( FileInputStream fis = new FileInputStream(file);
		     BufferedInputStream bis = new BufferedInputStream(fis);
		     InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8);
		     JsonReader reader = new JsonReader(isr) )
		{
			final NotebookReader r = new NotebookReader(isSwapFile);
			r.readNotebookFile(reader);
			return r;
		}
	}

	public final List<String> noteList = new ArrayList<>();
	public final boolean isSwapFile;

	private NotebookReader(boolean isSwapFile)
	{
		this.isSwapFile = isSwapFile;
	}

	private void readNotebookFile(JsonReader reader) throws IOException, MalformedJsonException
	{
		for(;;)
		{
			switch( reader.peek() )
			{
				case BEGIN_OBJECT:
					reader.beginObject();
					readTopLevelObject(reader);
					break;
				case END_OBJECT:
					reader.endObject();
					break;
				case END_DOCUMENT:
					return;
				default: //we got an unexpected token
					throw new InvalidNotebookFormatException();
			}
		}
	}

	private void readTopLevelObject(JsonReader reader) throws IOException
	{
		for(;;)
		{
			switch( reader.peek() )
			{
				case NAME:
					switch( reader.nextName() )
					{
						case "notes":
							noteList.clear();
							readNotesArray(reader);
							break;
						default:
							//Default action is to ignore properties we don't understand
							break;
					}
					break;
				case END_OBJECT:
					return;
				default: //we got an unexpected token
					throw new InvalidNotebookFormatException();
			}
		}
	}

	private void readNotesArray(JsonReader reader) throws IOException
	{
		for(;;)
		{
			switch( reader.peek() )
			{
				case BEGIN_ARRAY:
					reader.beginArray();
					readNotes(reader);
					break;
				case END_ARRAY:
					reader.endArray();
					return;
				default: //we got an unexpected token
					throw new InvalidNotebookFormatException();
			}
		}
	}

	private void readNotes(JsonReader reader) throws IOException
	{
		for(;;)
		{
			switch( reader.peek() )
			{
				case STRING:
					String str = reader.nextString();
					noteList.add( str );
					break;
				case END_ARRAY:
					return;
				default: //we got an unexpected token
					throw new InvalidNotebookFormatException();
			}
		}
	}
}
