package scribbles.swing_extensions;

import org.jetbrains.annotations.Nullable;
import scribbles.Utils;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by warrens on 3/23/16.
 */
public class KeystrokeTextfield extends JPlaceholderTextField
{
	private
	@Nullable
	KeyStroke keystroke;

	public KeystrokeTextfield()
	{
		setEditable(false);
		setPlaceholderText("Set your shortcut here");

		addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if( e.getKeyCode() == KeyEvent.VK_ESCAPE )
					keystroke(null);
				else if( e.getKeyCode() != KeyEvent.VK_UNDEFINED )
					keystroke(KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers()));
			}
		});

		setHorizontalAlignment(SwingConstants.CENTER);
	}

	public void keystroke(@Nullable KeyStroke ks)
	{
		this.keystroke = ks;
		if( keystroke == null )
			setText("");
		else
			setText(Utils.keyStrokeToString(keystroke));
	}

	public
	@Nullable
	KeyStroke keystroke()
	{
		return keystroke;
	}
}
