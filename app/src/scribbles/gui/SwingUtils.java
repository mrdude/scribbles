package scribbles.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

interface SwingUtils
{
	void setPreferredSize(Dimension dim);

	default void setPreferredSize(double wid, double hei)
	{
		final Dimension dim = new Dimension( (int)wid, (int)hei );
		setPreferredSize( dim );
	}
}
