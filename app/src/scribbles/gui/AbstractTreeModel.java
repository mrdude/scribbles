package scribbles.gui;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractTreeModel implements TreeModel
{
	private final List<TreeModelListener> listeners = new ArrayList<>();

	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		synchronized( listeners )
		{
			listeners.add( l );
		}
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		synchronized( listeners )
		{
			listeners.remove( l );
		}
	}

	protected void fireChangeEvent(final TreeModelEvent evt)
	{
		for( TreeModelListener l : getListenerArray() )
			l.treeNodesChanged(evt);
	}

	protected void fireRemoveEvent(final TreeModelEvent evt)
	{
		for( TreeModelListener l : getListenerArray() )
			l.treeNodesRemoved(evt);
	}

	protected void fireInsertEvent(final TreeModelEvent evt)
	{
		for( TreeModelListener l : getListenerArray() )
			l.treeNodesInserted(evt);
	}

	protected void fireStructureEvent(final TreeModelEvent evt)
	{
		for( TreeModelListener l : getListenerArray() )
			l.treeStructureChanged(evt);
	}

	private TreeModelListener[] getListenerArray()
	{
		synchronized( listeners )
		{
			final int sz = listeners.size();
			return listeners.toArray(new TreeModelListener[sz]);
		}
	}
}
