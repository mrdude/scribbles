package scribbles.gui;

import scribbles.dom.Note;

import java.util.EventListener;

public interface NotebookListener extends EventListener
{
	void noteCreated(final Note newNote);
	void notebookSaved();
}
