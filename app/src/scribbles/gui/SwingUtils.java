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

	void setSize(int wid, int hei);
	int getWidth();
	int getHeight();

	default void setWidth(double wid)
	{
		setSize( (int)wid, getHeight() );
	}

	default void setHeight(double hei)
	{
		setSize( getWidth(), (int)hei );
	}
}
