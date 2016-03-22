package scribbles.gui;

import scribbles.Utils;
import scribbles.dom.InvalidNotebookFormatException;
import scribbles.dom.Note;
import scribbles.dom.Notebook;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A top level window. One window corresponds to one notebook. */
public class ScribbleFrame extends JFrame implements SwingUtils, ScribbleWindowListener
{
	private final Notebook notebook;

	private final ActiveNoteContainer activeNoteContainer; //holds onto the JEditorPane for the currently edited note
	private final NoteListPanel noteList;
	private final StatusBar statusBar;
	private final SearchPopup searchPopup;

	private JMenuItem undoMenuItem, redoMenuItem, duplicateMenuItem;
	private JMenuItem windowListMenuItem;

	public ScribbleFrame(File notebookFile)
	{
		ScribbleApplication.registerScribbleFrame(this);

		notebook = new Notebook(notebookFile);
		if( notebook.getIOException() != null ) //TODO storing the exception instead of saving it looks like a code smell -- is there a better way to do this?
		{
			notebook.getIOException().printStackTrace();

			if( notebook.getIOException() instanceof InvalidNotebookFormatException )
				JOptionPane.showMessageDialog(this, "The provided notebook is not in a valid format. Are you sure this is a Scribble json file?");
			else
				JOptionPane.showMessageDialog(this, "Encountered an IO error while reading the file");
		}

		activeNoteContainer = new ActiveNoteContainer();
		statusBar = new StatusBar(this, activeNoteContainer.getEditPane());
		noteList = new NoteListPanel(this);
		searchPopup = new SearchPopup();

		//Add a document listener to all notes in the list so that we can update the NoteList when titles of notes change
		for( Note n : notebook.getNoteList() )
		{
			n.addDocumentListener(new DocumentListener() {
				@Override public void insertUpdate(DocumentEvent e) { noteModified(n); }
				@Override public void removeUpdate(DocumentEvent e) { noteModified(n); }
				@Override public void changedUpdate(DocumentEvent e) { noteModified(n); }
			});
		}

		//init the GUI
		setLayout( new BorderLayout() );
		setPreferredSize( 750, 1000 );
		setTitle("Scribbles - " +notebook.getFile().getName());
		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				requestClose();
			}
		});

		JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(noteList), activeNoteContainer );
		this.add( splitPane, BorderLayout.CENTER );
		this.add( statusBar, BorderLayout.SOUTH );

		this.getLayeredPane().add( searchPopup );

		setJMenuBar( createMenuBar() );

		pack();
		setVisible(true);

		splitPane.setDividerLocation( 0.25 );

		//search key bindings
		final InputMap inputMap = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		inputMap.put( KeyboardShortcuts.noteSearch.keystroke(), "search" );
		KeyboardShortcuts.noteSearch.addChangeListener( () -> {
			inputMap.clear();
			inputMap.put( KeyboardShortcuts.noteSearch.keystroke(), "search" );
		} );

		getRootPane().getActionMap().put("search", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final JTextField field = statusBar.getSearchTextField();
				field.requestFocusInWindow();
				field.setSelectionStart(0);
				field.setSelectionEnd( field.getText().length() );
			}
		} );

		//search text field listeners
		final JTextField searchTextField = statusBar.getSearchTextField();
		searchTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e)
			{
				if( !searchPopup.isVisible() )
				{
					searchPopup.setVisible(true);
					searchTextField.requestFocusInWindow();
				}

				searchPopup.updateResults();

				ScribbleFrame.this.repaint();
			}
		});

		searchTextField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e)
			{
				if( !searchPopup.hasFocus() )
					searchPopup.setVisible(false);
			}
		});
	}

	private JMenuBar createMenuBar()
	{
		AtomicReference<JMenuItem> undoRef = new AtomicReference<>(),
				redoRef = new AtomicReference<>(),
				duplicateRef = new AtomicReference<>();

		AtomicReference<JMenu> windowRef = new AtomicReference<>();

		JMenuBar menuBar = JMenuBuilder.createMenuBar(
				new JMenuBuilder("File")
					.menuItem("New notebook", (e) -> newNotebookModalWindow(), KeyboardShortcuts.newNotebook, Utils.OS.all() )
					.menuItem("Open notebook", (e) -> openNotebookModalWindow(), KeyboardShortcuts.openNotebook, Utils.OS.all() )
					.menuItem("Save notebook As")//, (e) -> saveAsNotebookModalWindow(), KeyboardShortcuts.saveNotebookAs, Utils.OS.all() )
					.menuItem("Save notebook", (e) -> saveNotebook(), KeyboardShortcuts.saveNotebook, Utils.OS.all() )
					.seperator()
					.menuItem("New note", (e) -> setActiveNote( createNote() ), KeyboardShortcuts.newNote, Utils.OS.all() )
					.menuItem("Duplicate active note", duplicateRef, (e) -> duplicateActiveNote(), KeyboardShortcuts.duplicateActiveNote, Utils.OS.all() )
					.seperator()
					.menuItem("Close window", (e) -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)), KeyboardShortcuts.closeWindow, Utils.OS.all() ),
				new JMenuBuilder("Edit")
					.menuItem("Undo", undoRef, (e) -> undo(), KeyboardShortcuts.undo, Utils.OS.all() )
					.menuItem("Redo", redoRef, (e) -> redo(), KeyboardShortcuts.redo, Utils.OS.all() )
					.seperator()
					.menuItem("Cut", (e) -> new DefaultEditorKit.CutAction(), KeyboardShortcuts.cut, Utils.OS.all() )
					.menuItem("Copy", (e) -> new DefaultEditorKit.CopyAction(), KeyboardShortcuts.copy, Utils.OS.all() )
					.menuItem("Paste", (e) -> new DefaultEditorKit.PasteAction(), KeyboardShortcuts.paste, Utils.OS.all() )
					.seperator()
					.menuItem("Focus on Note Edit Window", (e) -> activeNoteContainer.requestFocusInWindow(), KeyboardShortcuts.focusOnEdit, Utils.OS.all() )
					.menuItem("Focus on Note List Window", (e) -> noteList.requestFocusInWindow(), KeyboardShortcuts.focusOnList, Utils.OS.all() )
					.menuItem("Settings", (e) -> PreferencesDialog.show(), Utils.OS.allExcept(Utils.OS.MAC) ),
				new JMenuBuilder("Window")
					.menuItem("Minimize", (e) -> setState(ICONIFIED), KeyboardShortcuts.minimizeCurrentWindow, Utils.OS.all() )
					.seperator()
					.nestedMenu("Windows", windowRef, Utils.OS.all() )
						.endNestedMenu(),
				new JMenuBuilder("Help")
		);

		undoMenuItem = undoRef.get();
		redoMenuItem = redoRef.get();
		duplicateMenuItem = duplicateRef.get();
		windowListMenuItem = windowRef.get();

		undoMenuItem.setEnabled(false);
		redoMenuItem.setEnabled(false);
		duplicateMenuItem.setEnabled(false);
		ScribbleApplication.addScribbleWindowListener(this);

		//update the window list
		ScribbleFrame[] windows = ScribbleApplication.getOpenWindows(null);
		for( ScribbleFrame w : windows )
		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem( w.getTitle() );
			item.setState( w.hasFocus() );
			windowListMenuItem.add(item);
		}

		return menuBar;
	}

	private void newNotebookModalWindow()
	{
		final JFileChooser fs = new JFileChooser();
		fs.setFileSelectionMode( JFileChooser.FILES_ONLY );
		fs.setFileFilter(new javax.swing.filechooser.FileFilter() {
			@Override
			public boolean accept(File f)
			{
				return Utils.ext(f).equalsIgnoreCase("json");
			}

			@Override
			public String getDescription()
			{
				return "JSON Scribble notebooks (.json files)";
			}
		});

		if( fs.showSaveDialog(this) == JFileChooser.APPROVE_OPTION )
			new ScribbleFrame( fs.getSelectedFile() );
	}

	private void openNotebookModalWindow()
	{
		final JFileChooser fs = new JFileChooser();
		fs.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
		fs.setFileFilter(new javax.swing.filechooser.FileFilter() {
			@Override
			public boolean accept(File f)
			{
				return Utils.ext(f).equalsIgnoreCase("json");
			}

			@Override
			public String getDescription()
			{
				return "JSON Scribble notebooks (.json files)";
			}
		});

		if( fs.showOpenDialog(this) == JFileChooser.APPROVE_OPTION )
			new ScribbleFrame( fs.getSelectedFile() );
	}

	private void saveAsNotebookModalWindow()
	{
		//TODO
	}

	private void saveNotebook()
	{
		try
		{
			notebook.save();
			//TODO add "Last Saved" to the status bar
		}
		catch( IOException e )
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Encountered an IO error while saving the file");
		}
	}

	/** This is called when the user requests that a window be closed */
	private void requestClose()
	{
		ScribbleApplication.removeScribbleWindowListener(this);
		ScribbleApplication.deregisterScribbleFrame( this );
		dispose();
	}

	/** This is called when a note is modified */
	private void noteModified(Note n)
	{
		noteList.onNoteModified(n);

		//update redo/undo menu items
		if( n.canUndo() )
		{
			undoMenuItem.setEnabled( true );
			undoMenuItem.setText(n.getUndoPresentationName());
		}
		else
		{
			undoMenuItem.setEnabled( false );
			undoMenuItem.setText("Undo");
		}

		if( n.canRedo() )
		{
			redoMenuItem.setEnabled( true );
			redoMenuItem.setText(n.getRedoPresentationName());
		}
		else
		{
			redoMenuItem.setEnabled( false );
			redoMenuItem.setText("Redo");
		}

		ScribbleFrame.this.repaint();
	}

	Notebook getNotebook()
	{
		return notebook;
	}

	Note createNote()
	{
		final Note n = notebook.createNote();

		//Add a document listener so that we can update the NoteList when titles of notes change
		n.addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { noteModified(n); }
			@Override public void removeUpdate(DocumentEvent e) { noteModified(n); }
			@Override public void changedUpdate(DocumentEvent e) { noteModified(n); }
		});

		noteList.onNewNote(n);

		noteList.revalidate();
		noteList.repaint();

		return n;
	}

	Note duplicateNote(Note n)
	{
		final Note copy = notebook.duplicateNote(n);

		//Add a document listener so that we can update the NoteList when titles of notes change
		copy.addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { noteModified(copy); }
			@Override public void removeUpdate(DocumentEvent e) { noteModified(copy); }
			@Override public void changedUpdate(DocumentEvent e) { noteModified(copy); }
		});

		noteList.onNewNote(copy);

		noteList.revalidate();
		noteList.repaint();

		return copy;
	}

	private void duplicateActiveNote()
	{
		final Note n = notebook.getActiveNote();
		if( n != null )
			setActiveNote( duplicateNote(n) );
	}

	void setActiveNote(Note n)
	{
		notebook.setActiveNote(n);
		activeNoteContainer.setActiveNote(n);
		noteList.onActiveNoteChange(n);

		duplicateMenuItem.setEnabled(true);

		activeNoteContainer.repaint();
		activeNoteContainer.revalidate();
		noteList.repaint();
	}

	private void undo()
	{
		final Note n = notebook.getActiveNote();
		if( n != null && n.canUndo() )
			n.undo();
	}

	private void redo()
	{
		final Note n = notebook.getActiveNote();
		if( n != null && n.canRedo() )
			n.redo();
	}

	//window listener
	public void windowCreated(ScribbleFrame win)
	{
		JCheckBoxMenuItem item = new JCheckBoxMenuItem( win.getTitle() );
		item.addActionListener( (e) -> win.requestFocusInWindow() );
		windowListMenuItem.add(item);
	}

	public void windowFocused(ScribbleFrame win, int windowIndex)
	{
		if( windowIndex > windowListMenuItem.getComponentCount()-1 )
			return;

		for( Component c : windowListMenuItem.getComponents() )
			((JCheckBoxMenuItem)c).setState(false);

		JCheckBoxMenuItem item = (JCheckBoxMenuItem)windowListMenuItem.getComponent( windowIndex );
		item.setState(true);
	}

	public void windowDestroyed(ScribbleFrame win, int windowIndex)
	{
		windowListMenuItem.remove(windowIndex);
	}

	//search popup
	private class SearchPopup extends JPanel implements SwingUtils
	{
		public SearchPopup()
		{
			final Border padding = BorderFactory.createLineBorder( Color.black, 1, true );
			final Border border = BorderFactory.createTitledBorder(padding, "Search Results");
			setBorder( border );

			setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );

			setVisible(true);
		}

		void updateResults()
		{
			removeAll();
			runSearch();
			if( getComponentCount() == 0 )
				addSearchResult("No search results");

			pack();
			repaint();
		}

		private void runSearch()
		{
			final String searchString = statusBar.getSearchTextField().getText();
			if( searchString.isEmpty() )
				return;

			for( Note n : notebook.getNoteList() )
			{
				final String docText = n.getDocumentText();
				for( int x = docText.indexOf(searchString); x != -1 && x < docText.length() - searchString.length(); x = docText.indexOf(searchString, x+1) )
				{
					int row = 0;
					int col = 0;

					for( int y = 0; y < x; y++ )
					{
						switch( docText.charAt(y) )
						{
							case '\n':
								row++;
								col = 0;
							default:
								col++;
								break;
						}
					}

					addSearchResult( String.format("Result in '%s' @ %d:%d", n.getTitle(), row+1, col+1) );
				}
			}
		}

		public void pack()
		{
			Dimension dim = getPreferredSize();
			dim.width = getWidth();
			setSize( dim );
			validate();
		}

		private void addSearchResult(String res)
		{
			JLabel item = new JLabel(res);
			add(item);
		}

		public int getX()
		{
			return statusBar.getSearchTextField().getX();
		}

		public int getY()
		{
			//find the y coordinate of the search field, relative to the SearchPopup's parent
			int y = 0;
			Container cmp = statusBar.getSearchTextField();
			while( cmp != null && cmp != getParent() )
			{
				y += cmp.getY();
				cmp = cmp.getParent();
			}

			//set our y coordinate
			return y - getHeight();
		}

		public int getWidth()
		{
			return statusBar.getSearchTextField().getWidth();
		}
	}
}
