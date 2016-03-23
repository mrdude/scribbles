package scribbles.gui;

import scribbles.Utils;

import javax.swing.*;
import javax.swing.text.BadLocationException;

public class StatusBar extends JPanel implements SwingUtils
{
	private final ScribbleFrame win;
	private final JLabel searchResultLabel = new JLabel();
	private final JPlaceholderTextField searchTextField;

	public StatusBar(ScribbleFrame win, JEditorPane editPane)
	{
		this.win = win;

		final JLabel notebookFilenameLabel = new JLabel();
		notebookFilenameLabel.setText( Utils.prettifyFilePath(win.getNotebook().getFile()) );

		searchTextField = new JPlaceholderTextField();
		searchTextField.setPlaceholderText("Search (" +KeyboardShortcuts.noteSearch.toUserDisplayableString()+ ")");
		KeyboardShortcuts.noteSearch.addChangeListener( () -> searchTextField.setPlaceholderText("Search (" +KeyboardShortcuts.noteSearch.toUserDisplayableString()+ ")") );

		final JLabel caretPositionLabel = new JLabel();
		caretPositionLabel.setText("0:0");

		editPane.addCaretListener( (e) -> {
			try
			{
				final String text = editPane.getText(0, e.getDot());
				int row = 0, col = 0;
				for( int y = 0; y < e.getDot(); y++ )
				{
					if( text.charAt(y) == '\n' )
					{
						row++;
						col = 0;
					}
					else
					{
						col++;
					}
				}

				caretPositionLabel.setText((row+1)+ ":" +(col+1));
			}
			catch( BadLocationException e1 )
			{
				//This should never be thrown!
				e1.printStackTrace();
			}
		} );

		setLayout( new BoxLayout(this, BoxLayout.X_AXIS) );

		setPreferredSize( 0, getFont().getSize() * 1.5 );
		add( notebookFilenameLabel );
		add( new JSeparator(SwingConstants.VERTICAL) );
		add( searchTextField );
		add( searchResultLabel );
		add( new JSeparator(SwingConstants.VERTICAL) );
		add( caretPositionLabel );
	}

	public JTextField getSearchTextField()
	{
		return searchTextField;
	}

	void onSearch(int searchResultCount)
	{
		if( searchResultCount == -1 )
			searchResultLabel.setText("");
		else
			searchResultLabel.setText("Found " +searchResultCount+ " result(s)");
	}
}
