package scribbles.swing_extensions;

import java.awt.*;

public interface JComponentHelpers
{
	void setPreferredSize(Dimension dim);

	default void setPreferredSize(double wid, double hei)
	{
		final Dimension dim = new Dimension( (int)wid, (int)hei );
		setPreferredSize( dim );
	}

	Dimension getPreferredSize();
	default int getPreferredWidth()
	{
		return getPreferredSize().width;
	}

	default int getPreferredHeight()
	{
		return getPreferredSize().height;
	}

	default void setPreferredWidth(double width)
	{
		final Dimension dim = new Dimension( getPreferredSize() );
		dim.width = (int)width;
		setPreferredSize(dim);
	}

	default void setPreferredHeight(double height)
	{
		final Dimension dim = new Dimension( getPreferredSize() );
		dim.height = (int)height;
		setPreferredSize(dim);
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
