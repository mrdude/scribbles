package scribbles.swing_extensions;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractTreeModel implements TreeModel
{
	private final CopyOnWriteArrayList<TreeModelListener> listeners = new CopyOnWriteArrayList<>();

	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		listeners.add( l );
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		listeners.remove( l );
	}

	protected void fireChangeEvent(final TreeModelEvent evt)
	{
		for( TreeModelListener l : listeners )
			l.treeNodesChanged(evt);
	}

	protected void fireRemoveEvent(final TreeModelEvent evt)
	{
		for( TreeModelListener l : listeners )
			l.treeNodesRemoved(evt);
	}

	protected void fireInsertEvent(final TreeModelEvent evt)
	{
		for( TreeModelListener l : listeners )
			l.treeNodesInserted(evt);
	}

	protected void fireStructureEvent(final TreeModelEvent evt)
	{
		for( TreeModelListener l : listeners )
			l.treeStructureChanged(evt);
	}
}
