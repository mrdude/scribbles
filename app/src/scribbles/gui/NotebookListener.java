package scribbles.gui;

import scribbles.dom.Note;

import java.io.IOException;
import java.util.EventListener;

public interface NotebookListener extends EventListener
{
	void noteCreated(final Note newNote);
	void notebookSaved();
	void activeNoteChanged(final Note newActiveNote);

	/** This is called when a save fails */
	void failedSave(IOException e);

	class Adapter implements NotebookListener
	{
		@Override public void noteCreated(Note newNote) {}
		@Override public void notebookSaved() {}
		@Override public void activeNoteChanged(Note newActiveNote) {}
		@Override public void failedSave(IOException e) {}
	}
}
