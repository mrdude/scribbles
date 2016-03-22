package scribbles.gui;

import javax.swing.*;
import java.awt.*;

class JPlaceholderTextField extends JTextField
{
	private Font font, blankFont;
	private String placeholderText;
	private Color placeholderForeground = Color.gray;

	public JPlaceholderTextField()
	{
		font = getFont();
		blankFont = getFont().deriveFont(Font.ITALIC);
	}

	public void setFont(Font font)
	{
		super.setFont(font);
		this.font = font;
		this.blankFont = font.deriveFont( Font.ITALIC );
	}

	public void setPlaceholderText(String placeholderText)
	{
		this.placeholderText = placeholderText;
	}

	public String getPlaceholderText()
	{
		return placeholderText;
	}

	public void setPlaceholderForeground(Color clr)
	{
		placeholderForeground = clr;
	}

	public Color getPlaceholderForeground()
	{
		return placeholderForeground;
	}

	public void paintComponent(Graphics graphics)
	{
		Graphics2D g = (Graphics2D) graphics.create();

		if( getText().isEmpty() )
			g.setFont(blankFont);
		else
			g.setFont(font);

		final String oldText = getText();
		final Color oldFg = getForeground();

		if( oldText.isEmpty() && !hasFocus() )
		{
			setText(placeholderText);
			setForeground(placeholderForeground);
		}

		getUI().update(g, this);

		if( oldText.isEmpty() )
		{
			setText("");
			setForeground(oldFg);
		}

		g.dispose();
	}
}
