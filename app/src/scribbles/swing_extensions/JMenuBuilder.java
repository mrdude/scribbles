package scribbles.swing_extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scribbles.Utils;
import scribbles.gui.KeyboardShortcuts;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class JMenuBuilder
{
	private final @Nullable JMenuBuilder parent;
	private final @Nullable AbstractButton menu;

	public static JMenuBar createMenuBar(JMenuBuilder... builders)
	{
		JMenuBar menuBar = new JMenuBar();

		for( JMenuBuilder b : builders )
		{
			if( b.parent != null )
				throw new RuntimeException("Invalid menu");

			if( b.menu != null )
				menuBar.add( b.menu );
		}

		return menuBar;
	}

	public JMenuBuilder(@NotNull String text)
	{
		this(null, new JMenu(text));
	}

	private JMenuBuilder(@Nullable JMenuBuilder parent, @Nullable AbstractButton menu)
	{
		this.parent = parent;
		this.menu = menu;

		if( parent != null && parent.menu != null && menu != null )
			parent.menu.add( menu );
	}

	public JMenuBuilder nestedMenu(String text, Utils.OS... requiredOS)
	{
		return nestedMenu(text, null, requiredOS);
	}

	public JMenuBuilder nestedMenu(String text, @Nullable AtomicReference<JMenu> ref, Utils.OS... requiredOS)
	{
		if( Arrays.asList(requiredOS).contains( Utils.getOS() ) )
		{
			JMenu item = new JMenu(text);
			if( ref != null ) ref.set(item);
			return new JMenuBuilder(this, item);
		}
		else
		{
			if( ref != null ) ref.set(null);
			return new JMenuBuilder(this, null);
		}
	}

	public JMenuBuilder endNestedMenu()
	{
		if( parent == null )
			throw new RuntimeException("You probably didn't mean to call this here");

		return parent;
	}

	public JMenuBuilder menuItem(String text, Utils.OS... requiredOS)
	{
		return menuItem(text, null, null, null, requiredOS);
	}

	public JMenuBuilder menuItem(String text, @Nullable ActionListener actionListener, Utils.OS... requiredOS)
	{
		return menuItem(text, null, actionListener, null, requiredOS);
	}

	public JMenuBuilder menuItem(String text, @Nullable ActionListener actionListener, @Nullable KeyboardShortcuts shortcut, Utils.OS... requiredOS)
	{
		return menuItem(text, null, actionListener, shortcut, requiredOS);
	}

	public JMenuBuilder menuItem(String text, @Nullable AtomicReference<JMenuItem> ref, @Nullable ActionListener actionListener, final @Nullable KeyboardShortcuts shortcut, Utils.OS... requiredOS)
	{
		if( Arrays.asList(requiredOS).contains( Utils.getOS() ) )
		{
			final JMenuItem item = new JMenuItem();
			if( ref != null ) ref.set(item);

			item.setText(text);

			if( actionListener == null )
				item.setEnabled(false); //if it doesn't have an ActionListener, the user shouldn't be able to click it
			else
				item.addActionListener(actionListener);

			if( shortcut != null )
			{
				item.setAccelerator(shortcut.keystroke());

				//when this keyboard shortcut is changed, change the accelerator of the menu item
				shortcut.addChangeListener( (oldKs, newKs) -> item.setAccelerator(shortcut.keystroke()) );
			}

			menu.add(item);
		}

		return this;
	}

	public JMenuBuilder seperator()
	{
		((JMenu)menu).addSeparator();
		return this;
	}
}
