package scribbles.notebook;

import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

class NotebookWriter
{
	public static void save(File file, List<Note> noteList) throws IOException
	{
		final File tmpFile = Notebook.getTempFile(file);
		Files.move( file.toPath(), tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING );

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
		catch(IOException e) //if we encounter an error while saving the file, attempt to bail out before rethrowing the exception
		{
			try
			{
				file.delete();
				Files.move(tmpFile.toPath(), file.toPath());
			}
			catch(IOException e2) {} //there's nothing we can do about this exception

			throw e;
		}
	}
}
