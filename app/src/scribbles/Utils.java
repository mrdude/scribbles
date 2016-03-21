package scribbles;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Some misc utility functions */
public class Utils
{
	public enum OS
	{
		WINDOWS,
		LINUX,
		MAC,
		UNKNOWN;

		public static OS[] all()
		{
			return values();
		}

		public static OS[] allExcept(OS... exceptOS)
		{
			Set<OS> set = new HashSet<>();
			set.addAll( Arrays.asList(values()) );
			for( OS os : exceptOS )
				set.remove(os);
			return set.toArray( new OS[set.size()] );
		}
	}

	/** Returns the OS that we are running on */
	public static OS getOS()
	{
		final String osName = System.getProperty("os.name");
		if( osName.contains("Windows") )
			return OS.WINDOWS;
		else if( osName.contains("Linux") || osName.contains("BSD") ) //for our purposes, we don't need to differentiate between the *BSD's and Linux
			return OS.LINUX;
		else if( osName.contains("Mac") )
			return OS.MAC;
		else
			return OS.UNKNOWN;
	}

	/**
	 * Returns file.getAbsolutePath(). Replaces the user's home directory with ~ on Linux and Mac.
	 */
	public static String prettifyFilePath(File file)
	{
		String path = file.getAbsolutePath();

		final OS os = getOS();
		if( os == OS.LINUX || os == OS.MAC )
		{
			final String homeDir = System.getProperty("user.home");
			if( path.startsWith(homeDir) )
				path = "~" + path.substring(homeDir.length());
		}

		return path;
	}

	/** Converts a KeyStroke to a user readable string */
	public static String keyStrokeToString(KeyStroke ks)
	{
		return KeyEvent.getKeyModifiersText(ks.getModifiers()) + KeyEvent.getKeyText( ks.getKeyCode() );
	}

	/** Returns a file's extension */
	public static String ext(File file) { return ext(file.getName()); }

	/** Returns a file's extension */
	public static String ext(String filename)
	{
		final int p = filename.lastIndexOf(".");
		if( p == -1 )
			return "";
		else if( p == filename.length()-1 )
			return "";
		else
			return filename.substring( p+1 );
	}
}
