package scribbles.gui;

import org.jetbrains.annotations.NotNull;
import scribbles.dom.Note;
import scribbles.dom.Notebook;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists all notes in the current notebook
 */
public class NoteListPanel extends JTree
{
	private final Notebook notebook;

	public NoteListPanel(ScribbleFrame win)
	{
		this.notebook = win.getNotebook();

		setModel( new CustomTreeModel() );

		setBackground( Color.white );
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );

		//when the user clicks on a note, set that note as the active note
		addTreeSelectionListener( (e) -> {
			if( e.getPath().getLastPathComponent() instanceof Note )
			{
				final Note n = (Note) e.getPath().getLastPathComponent();
				win.setActiveNote(n);
			}
		});
	}

	public CustomTreeModel getModel()
	{
		return (CustomTreeModel)super.getModel();
	}

	public void paint(Graphics graphics)
	{
		super.paint(graphics);

		if( !hasFocus() )
		{
			Graphics g = graphics.create();
			g.setColor( new Color(200, 200, 200, 64) );
			g.fillRect( 0, 0, getWidth(), getHeight() );
			g.dispose();
		}
	}

	/** This is called by ScribbleFrame when a new note is created */
	void onNewNote(final Note n)
	{
		getModel().noteCreated(n);
	}

	/** This is called by Swing when a note is modified */
	void onNoteModified(final Note n)
	{
		getModel().valueForPathChanged(null, n);
	}

	/** This is called by ScribbleFrame when the active note is changed */
	void onActiveNoteChange(@NotNull Note newActiveNote)
	{
		final TreePath path = new TreePath( new Object[] { getModel().rootObject, newActiveNote } );
		setSelectionPath( path );
	}

	public boolean requestFocusInWindow()
	{
		if( super.requestFocusInWindow() )
		{
			if( getSelectionPath() == null )
			{
				if( notebook.getActiveNote() != null )
					setSelectionPath(new TreePath(new Object[]{getModel().rootObject, notebook.getActiveNote()}));
				else
					setSelectionPath(new TreePath(new Object[]{getModel().rootObject}));
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	private class CustomTreeModel extends AbstractTreeModel implements TreeModel
	{
		private final Object rootObject = new Object() {
			public String toString()
			{
				return "Notes";
			}
		};

		void noteCreated(Note n)
		{
			fireInsertEvent(n);
		}

		@Override
		public Object getRoot()
		{
			return rootObject;
		}

		@Override
		public Object getChild(Object parent, int index)
		{
			assert parent == rootObject;
			return notebook.getNoteList().get(index);
		}

		@Override
		public int getChildCount(Object parent)
		{
			if( parent == rootObject )
				return notebook.getNoteList().size();
			else
				return 0;
		}

		@Override
		public boolean isLeaf(Object node)
		{
			return node != rootObject;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue)
		{
			fireChangeEvent( (Note)newValue );
		}

		@Override
		public int getIndexOfChild(Object parent, Object child)
		{
			assert parent == rootObject;
			return notebook.getNoteList().indexOf( (Note)child );
		}

		private void fireChangeEvent(Note n)
		{
			final TreeModelEvent evt = new TreeModelEvent( NoteListPanel.this,
					new Object[] { rootObject, n }
			);

			fireChangeEvent(evt);
		}

		private void fireInsertEvent(Note n)
		{
			final TreeModelEvent evt = new TreeModelEvent( NoteListPanel.this,
					new Object[] { rootObject },
					new int[] { notebook.getNoteList().indexOf(n) },
					new Object[] { n } );

			fireInsertEvent(evt);
		}
	}
}
