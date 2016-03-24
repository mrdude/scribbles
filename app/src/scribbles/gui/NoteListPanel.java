package scribbles.gui;

import org.jetbrains.annotations.NotNull;
import scribbles.dom.Note;
import scribbles.dom.Notebook;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

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

		//only allow one node to be selected at once
		getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );

		setCellRenderer(new DefaultTreeCellRenderer() {
			private final Icon defaultLeafIcon = getLeafIcon();
			private final Icon dirtyLeafIcon = new CustomDirtyLeafIcon(getLeafIcon());

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value,
			                                              boolean selected,
			                                              boolean expanded,
			                                              boolean leaf,
			                                              int row,
			                                              boolean hasFocus)
			{
				setLeafIcon( leaf && ((Note)value).isDirty()
					? dirtyLeafIcon
					: defaultLeafIcon );

				return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			}
		});

		//Add a document listener to all notes in the list so that we can update the NoteList when titles of notes change
		notebook.getDocumentEventMulticaster().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { handle( (Note)e.getDocument() ); }
			@Override public void removeUpdate(DocumentEvent e) { handle( (Note)e.getDocument() ); }
			@Override public void changedUpdate(DocumentEvent e) { handle( (Note)e.getDocument() ); }

			private void handle(Note n)
			{
				getModel().valueForPathChanged(null, n);
			}
		});

		//listen for changes to the notebook
		notebook.addNotebookListener(new NotebookListener.Adapter() {
			@Override public void noteCreated(Note newNote) { getModel().noteCreated( newNote ); }
			@Override public void notebookSaved() { getModel().valueForRootChanged(); }
			@Override public void activeNoteChanged(Note newActiveNote) { onActiveNoteChange(newActiveNote); }

		});

		//when the user clicks on a note, set that note as the active note
		addTreeSelectionListener( (e) -> {
			final Object selectedNode = e.getPath().getLastPathComponent();
			if( selectedNode == null )
				notebook.setActiveNote(null);
			else if( selectedNode instanceof Note )
				notebook.setActiveNote( (Note)selectedNode );
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

	/** This is called by ScribbleFrame when the active note is changed */
	private void onActiveNoteChange(@NotNull Note newActiveNote)
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

		public void valueForRootChanged()
		{
			final TreeModelEvent evt = new TreeModelEvent( NoteListPanel.this,
					new Object[] { rootObject }
			);

			fireChangeEvent(evt);
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

	private class CustomDirtyLeafIcon implements Icon
	{
		private final Icon base;

		public CustomDirtyLeafIcon(Icon base)
		{
			this.base = base;
		}

		@Override
		public void paintIcon(Component c, Graphics graphics, int x, int y)
		{
			base.paintIcon(c, graphics, x, y);

			Graphics2D g = (Graphics2D)graphics.create();
			g.setClip(0, 0, getIconWidth(), getIconHeight());
			g.setColor( Color.red );
			g.setStroke( new BasicStroke(4) );

			g.drawLine( 0, 0, getIconHeight(), getIconHeight() );
			g.drawLine( 0, getIconHeight(), getIconWidth(), 0 );
		}

		@Override
		public int getIconWidth()
		{
			return base.getIconWidth();
		}

		@Override
		public int getIconHeight()
		{
			return base.getIconHeight();
		}
	}
}
