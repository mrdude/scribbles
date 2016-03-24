package scribbles.gui;

import scribbles.Utils;
import scribbles.swing_extensions.AbstractTreeModel;
import scribbles.swing_extensions.KeystrokeTextfield;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.WindowEvent;

public class PreferencesDialog
{
	public static void show()
	{
		final String title = (Utils.getOS() == Utils.OS.MAC
				? "Preferences"
				: "Settings");

		//init GUI
		//TODO the longer I look at this GUI init code, the more my eyes bleed -- change this to a declarative format later
		final JFrame prefs = new JFrame(title);
		prefs.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		prefs.setLayout( new BorderLayout() );

		final JPanel centerPanel = new JPanel();
		final KeystrokeTextfield keystrokeField = new KeystrokeTextfield();
		final JTextArea shortcutDesc = new JTextArea();
		shortcutDesc.setEditable( false );
		shortcutDesc.setBorder( BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Description") );

		centerPanel.setLayout( new GridLayout(0, 1) );
		centerPanel.setBorder( BorderFactory.createRaisedBevelBorder() );
		centerPanel.add( keystrokeField );
		centerPanel.add( new JLabel("Press Esc to clear") );
		centerPanel.add( shortcutDesc );
		prefs.add( centerPanel, BorderLayout.CENTER );

		final JPanel treePanel = new JPanel();
		treePanel.setBorder( BorderFactory.createEmptyBorder(0, 0, 0, 2) );
		treePanel.setLayout( new FlowLayout() );

		final JTree tree = new JTree( new KeyboardShortcutTreeModel() );
		treePanel.add( new JScrollPane(tree) );

		prefs.add( treePanel, BorderLayout.WEST );

		final JButton closeButton = new JButton("Close");
		final JButton applyButton = new JButton("Apply");
		final JButton okButton = new JButton("OK");

		tree.addTreeSelectionListener( (e) -> {
			applyButton.setEnabled( e.getPath() != null );
			if( e.getPath() != null )
			{
				final KeyboardShortcuts shortcut = (KeyboardShortcuts) e.getPath().getLastPathComponent();
				shortcutDesc.setText( shortcut.desc() );
				keystrokeField.keystroke( shortcut.keystroke() );
				keystrokeField.requestFocusInWindow();
			}
		} );

		closeButton.addActionListener( (e) -> {
			prefs.dispatchEvent( new WindowEvent(prefs, WindowEvent.WINDOW_CLOSING) );
		} );

		applyButton.addActionListener( (e) -> {
			final TreePath path = tree.getSelectionPath();
			if( path != null )
			{
				final KeyStroke keystroke = keystrokeField.keystroke();
				if( keystroke != null )
					((KeyboardShortcuts)path.getLastPathComponent()).keystroke( keystroke );

				applyButton.setEnabled(false);
			}
		} );

		okButton.addActionListener( (e) -> {
			applyButton.doClick();
			closeButton.doClick();
		} );

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout( new FlowLayout( FlowLayout.TRAILING ) );
		buttonPanel.setBorder( BorderFactory.createEmptyBorder(2, 0, 0, 0) );
		buttonPanel.add( okButton );
		buttonPanel.add( applyButton );
		buttonPanel.add( closeButton );
		prefs.add( buttonPanel, BorderLayout.SOUTH );

		prefs.pack();
		prefs.setVisible(true);
	}

	private static class KeyboardShortcutTreeModel extends AbstractTreeModel implements TreeModel
	{
		private final Object rootObject = new Object() {
			public String toString()
			{
				return "Modifiable shortcuts";
			}
		};

		@Override
		public Object getRoot()
		{
			return rootObject;
		}

		@Override
		public Object getChild(Object parent, int index)
		{
			return KeyboardShortcuts.shortcuts.get(index);
		}

		@Override
		public int getChildCount(Object parent)
		{
			return KeyboardShortcuts.shortcuts.size();
		}

		@Override
		public boolean isLeaf(Object node)
		{
			return node != rootObject;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {}

		@Override
		public int getIndexOfChild(Object parent, Object child)
		{
			assert parent == rootObject;
			return KeyboardShortcuts.shortcuts.indexOf( (KeyboardShortcuts)child);
		}
	}
}
