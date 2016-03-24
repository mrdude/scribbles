package scribbles.swing_extensions;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.List;

public class DocumentEventMulticaster
{
	private final List<DocumentListener> listenerList = new ArrayList<>();
	private final DocumentListener listener;

	public DocumentEventMulticaster()
	{
		listener = new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { dispatchInsertEvent(e); }
			@Override public void removeUpdate(DocumentEvent e) { dispatchRemoveEvent(e); }
			@Override public void changedUpdate(DocumentEvent e) { dispatchChangeEvent(e); }
		};
	}

	public DocumentListener getSourceListener()
	{
		return listener;
	}

	private void dispatchInsertEvent(DocumentEvent e)
	{
		for( DocumentListener l : getListenerArray() )
			l.insertUpdate(e);
	}

	private void dispatchRemoveEvent(DocumentEvent e)
	{
		for( DocumentListener l : getListenerArray() )
			l.removeUpdate(e);
	}

	private void dispatchChangeEvent(DocumentEvent e)
	{
		for( DocumentListener l : getListenerArray() )
			l.changedUpdate(e);
	}

	public void addDocumentListener(DocumentListener l)
	{
		synchronized( listenerList )
		{
			listenerList.add( l );
		}
	}

	public void removeDocumentListener(DocumentListener l)
	{
		synchronized( listenerList )
		{
			listenerList.remove( l );
		}
	}

	private DocumentListener[] getListenerArray()
	{
		synchronized( listenerList )
		{
			final int sz = listenerList.size();
			return listenerList.toArray( new DocumentListener[sz] );
		}
	}
}
