package scribbles.dom;

import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

class NotebookWriter
{
	public static void save(File file, List<Note> noteList) throws IOException
	{
		try( FileOutputStream fos = new FileOutputStream(file);
		     BufferedOutputStream bos = new BufferedOutputStream(fos);
		     OutputStreamWriter osr = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
		     JsonWriter writer = new JsonWriter(osr) )
		{
			writer.setIndent("\t");

			writer.beginObject();

			writer.name("notes");
			writer.beginArray();
			for( Note n : noteList )
				writer.value( n.getDocumentText() );
			writer.endArray();

			writer.endObject();
		}
	}
}
