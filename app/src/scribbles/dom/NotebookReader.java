package scribbles.dom;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class NotebookReader
{
	public static NotebookReader load(File file) throws IOException, InvalidNotebookFormatException
	{
		try( FileInputStream fis = new FileInputStream(file);
		     BufferedInputStream bis = new BufferedInputStream(fis);
		     InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8);
		     JsonReader reader = new JsonReader(isr) )
		{
			final NotebookReader r = new NotebookReader();
			r.readNotebookFile(reader);
			return r;
		}
		catch(MalformedJsonException malformed)
		{
			throw new InvalidNotebookFormatException(malformed);
		}
	}

	public final List<String> noteList = new ArrayList<>();

	private NotebookReader() {}

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
