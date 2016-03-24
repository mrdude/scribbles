package scribbles.gui;

import scribbles.Utils;
import scribbles.listeners.NotebookListener;
import scribbles.listeners.ScribbleWindowListener;
import scribbles.notebook.InvalidNotebookFormatException;
import scribbles.notebook.Note;
import scribbles.notebook.Notebook;
import scribbles.swing_extensions.JComponentHelpers;
import scribbles.swing_extensions.JMenuBuilder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/** A top level window. One window corresponds to one notebook. */
public class ScribbleFrame extends JFrame implements JComponentHelpers, ScribbleWindowListener
{
	private final Notebook notebook;

	private final ActiveNoteContainer activeNoteContainer; //holds onto the JEditorPane for the currently edited note
	private final NoteListPanel noteList;
	private final StatusBar statusBar;

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

		activeNoteContainer = new ActiveNoteContainer(notebook);
		statusBar = new StatusBar(this, activeNoteContainer.getEditPane());
		noteList = new NoteListPanel(this);

		//Add a document listener to all notes in the list so that we can update the Undo/Redo menu items when notes are modified
		notebook.getDocumentEventMulticaster().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { noteModified( (Note)e.getDocument() ); }
			@Override public void removeUpdate(DocumentEvent e) { noteModified( (Note)e.getDocument() ); }
			@Override public void changedUpdate(DocumentEvent e) { noteModified( (Note)e.getDocument() ); }
		});

		//listen for failed notebook saves
		notebook.addNotebookListener(new NotebookListener.Adapter() {
			@Override
			public void failedSave(IOException e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(ScribbleFrame.this, "Encountered an IO error while saving the file");
			}
		});

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

		setJMenuBar( createMenuBar() );

		pack();
		setVisible(true);

		splitPane.setDividerLocation( 0.25 );

		//updateSearch key bindings
		final InputMap inputMap = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		inputMap.put( KeyboardShortcuts.noteSearch.keystroke(), "updateSearch" );
		KeyboardShortcuts.noteSearch.addChangeListener( (oldKs, newKs) -> {
			inputMap.remove( oldKs );
			inputMap.put( newKs, "updateSearch" );
		} );

		getRootPane().getActionMap().put("updateSearch", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final JTextField field = statusBar.getSearchTextField();
				field.requestFocusInWindow();
				field.setSelectionStart(0);
				field.setSelectionEnd( field.getText().length() );
			}
		} );

		//updateSearch text field listeners
		final JTextField searchTextField = statusBar.getSearchTextField();
		searchTextField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) { updateSearch( searchTextField.getText() ); }
			@Override public void keyPressed(KeyEvent e) { updateSearch( searchTextField.getText() ); }
			@Override public void keyReleased(KeyEvent e) { updateSearch( searchTextField.getText() ); }
		});

		searchTextField.addFocusListener(new FocusAdapter() {
			@Override public void focusGained(FocusEvent e) { updateSearch( searchTextField.getText() ); }
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
					.menuItem("Save notebook", (e) -> notebook.save(), KeyboardShortcuts.saveNotebook, Utils.OS.all() )
					.seperator()
					.menuItem("New note", (e) -> notebook.setActiveNote( notebook.createNote() ), KeyboardShortcuts.newNote, Utils.OS.all() )
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
					.menuItem("About Scribbles", (e) -> AboutDialog.show(), Utils.OS.allExcept(Utils.OS.MAC) )
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
	}

	private void updateSearch(String searchString)
	{
		int searchResultCount = notebook.updateSearch(searchString);
		if( searchResultCount > 0 )
			activeNoteContainer.repaint();

		if( searchString.isEmpty() )
			statusBar.onSearch(-1);
		else
			statusBar.onSearch( searchResultCount );
	}

	Notebook getNotebook()
	{
		return notebook;
	}

	private void duplicateActiveNote()
	{
		final Note n = notebook.getActiveNote();
		if( n != null )
			notebook.setActiveNote( notebook.duplicateNote(n) );
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
}
