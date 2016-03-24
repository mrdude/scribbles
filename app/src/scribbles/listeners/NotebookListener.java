package scribbles.listeners;

import scribbles.dom.Note;

import java.io.IOException;

public interface NotebookListener
{
	/** This is called after a note is created */
	void noteCreated(final Note newNote);

	/** This is called after the notebook is successfully saved */
	void notebookSaved();

	/** This is called when the active note is set */
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
