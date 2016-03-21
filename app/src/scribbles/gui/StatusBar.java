package scribbles.gui;

import scribbles.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class StatusBar extends JPanel implements SwingUtils
{
	private final ScribbleFrame win;

	public StatusBar(ScribbleFrame win)
	{
		this.win = win;
		setLayout( new BoxLayout(this, BoxLayout.X_AXIS) );

		setPreferredSize( 0, getFont().getSize() * 1.5 );
		add( new StatusLabel( () -> Utils.prettifyFilePath(win.getNotebook().getFile()) ) );
	}

	private class StatusLabel extends JPanel
	{
		private final Supplier<String> textSupplier;

		public StatusLabel(Supplier<String> textSupplier)
		{
			this.textSupplier = textSupplier;
		}

		public void paint(Graphics g)
		{
			g.drawString( textSupplier.get(), 0, g.getFont().getSize() );
		}
	}
}
