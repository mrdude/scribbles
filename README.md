# What's this?
A simple note-taking application written in Java.

# How's it work?
When the application starts, it loads the notebook file located at
~/default_notebook.json (%USERPROFILE%\default_notebook.json on Windows systems).

Start typing in the right-hand panel to add content to the current note.
The first line of a note is used as it's title, which is used to identify it in the left-hand panel.

# How'd you build this?
* Google's Gson library is used to read/write the .json notebook files
* Google Guava is used in various places (those immutable collections really come in handy!)
* Intellij IDEA was used as the IDE; this repo is actually an Intellij project