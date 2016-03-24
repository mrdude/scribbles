package scribbles.gui;

import scribbles.Utils;
import scribbles.swing_extensions.AbstractTreeModel;
import scribbles.swing_extensions.KeystrokeTextfield;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

public class PreferencesDialog
{
	public static void show()
	{
		final String title = (Utils.getOS() == Utils.OS.MAC
				? "Preferences"
				: "Settings");

		/* init GUI */
		//TODO the longer I look at this GUI init code, the more my eyes bleed -- change this to a declarative format later

		//create the frame
		final JFrame prefs = new JFrame(title);
		prefs.setPreferredSize( new Dimension(650, 400) );
		prefs.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		prefs.setLayout( new BorderLayout() );

		//create the main JComponents
		final KeystrokeTextfield keystrokeField = new KeystrokeTextfield();
		final JTextArea shortcutDesc = createShortcutDescTextArea(prefs);
		final JTree tree = new JTree( new KeyboardShortcutTreeModel() );

		final JButton closeButton = createCloseButton(prefs);
		final JButton applyButton = createApplyButton(tree, keystrokeField);
		final JButton okButton = createOKButton(closeButton, applyButton);

		//create the "grouping" components
		final JPanel centerPanel = createCenterPanel(keystrokeField, shortcutDesc);
		final JPanel buttonPanel = createButtonPanel(okButton, applyButton, closeButton);

		//add the grouping components to the frame
		prefs.add( centerPanel, BorderLayout.CENTER );
		prefs.add( tree, BorderLayout.WEST );
		prefs.add( buttonPanel, BorderLayout.SOUTH );

		//add the tree listener
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

		//if the user presses Escape or the Close Window key, close the window
		final InputMap inputMap = prefs.getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		inputMap.put( KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeWindow" );
		inputMap.put( KeyboardShortcuts.closeWindow.keystroke(), "closeWindow" );
		KeyboardShortcuts.closeWindow.addChangeListener( (oldKs, newKs) -> {
			inputMap.remove( oldKs );
			inputMap.put( newKs, "closeWindow" );
		} );

		prefs.getRootPane().getActionMap().put("closeWindow", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				prefs.dispatchEvent( new WindowEvent(prefs, WindowEvent.WINDOW_CLOSING) );
			}
		});

		//pack and display the window
		prefs.pack();
		prefs.setMinimumSize( prefs.getSize() );
		prefs.setVisible(true);
	}

	//gui helpers
	private static JPanel createButtonPanel(JButton okButton, JButton applyButton, JButton closeButton)
	{
		final JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout( new FlowLayout( FlowLayout.TRAILING ) );
		buttonPanel.setBorder( BorderFactory.createEmptyBorder(2, 0, 0, 0) );

		//the ordering of buttons is OS-specific
		if( Utils.getOS() == Utils.OS.MAC )
		{
			buttonPanel.add(closeButton);
			buttonPanel.add(applyButton);
			buttonPanel.add(okButton);
		}
		else
		{
			buttonPanel.add(okButton);
			buttonPanel.add(applyButton);
			buttonPanel.add(closeButton);
		}

		return buttonPanel;
	}

	private static JPanel createCenterPanel(KeystrokeTextfield keystrokeField, JTextArea shortcutDesc)
	{
		final JPanel centerPanel = new JPanel();

		centerPanel.setLayout( new GridLayout(0, 1) );
		centerPanel.setBorder( BorderFactory.createRaisedBevelBorder() );
		centerPanel.add( keystrokeField );
		centerPanel.add( new JLabel("Press Esc to clear") );
		centerPanel.add( shortcutDesc );

		return centerPanel;
	}

	private static JTextArea createShortcutDescTextArea(JFrame prefsWindow)
	{
		final JTextArea shortcutDesc = new JTextArea();

		shortcutDesc.setEditable( false );
		shortcutDesc.setBackground( prefsWindow.getBackground() );
		shortcutDesc.setBorder( BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Description") );

		return shortcutDesc;
	}

	private static JButton createCloseButton(JFrame prefsWindow)
	{
		final JButton closeButton = new JButton("Close");

		closeButton.addActionListener( (e) -> {
			prefsWindow.dispatchEvent( new WindowEvent(prefsWindow, WindowEvent.WINDOW_CLOSING) );
		} );

		return closeButton;
	}

	private static JButton createApplyButton(JTree tree, KeystrokeTextfield keystrokeField)
	{
		final JButton applyButton = new JButton("Apply");

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

		return applyButton;
	}

	private static JButton createOKButton(JButton closeButton, JButton applyButton)
	{
		final JButton okButton = new JButton("OK");

		okButton.addActionListener( (e) -> {
			applyButton.doClick();
			closeButton.doClick();
		} );

		return okButton;
	}

	//tree model
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
