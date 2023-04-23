# Local Installation

The latest stable release can be found in releases:
[![releases](https://img.shields.io/github/v/release/Ktt-Development/rexedia)](https://github.com/Ktt-Development/rexedia/releases)

Simply run the installer or extract the zip file into the desired directory.

# Running Rexedia

When first running Rexedia you may notice that Windows does not recognize the command.

```
C:\Users\Katsute> rexedia
'rexedia' is not recognized as an internal or external command,
operable program or batch file.
```

This behavior is expected, if the application is not added to the system path it can only be accessed by using the full installation path.

**Example:** Rexedia installed in the downloads folder can be accessed using:
```
C:\Users\Katsute> C:\Users\Katsute\Downloads\rexedia\rexedia.exe
usage: rexedia
 -b,--backup <arg>           Should a backup file be kept of the original
 -c,--cover <arg>            The cover format to use
 -d,--debug <arg>            Run logging in debug mode and create a debug
                             file
```

Note that paths with spaces in them may require quotations `"` around the path.

# Adding Rexedia to the Path

In order to get to the path variables you have to open the system environment variables. This can be achieved by searching for **environment variables** in the search bar or in the control panel search bar.

![search 'environment variables'](https://raw.githubusercontent.com/Ktt-Development/rexedia/main/docs/setup_1.png)

Next click on **Environment Variables**, this will open the user and system variables.

![click 'Environment Variables'](https://raw.githubusercontent.com/Ktt-Development/rexedia/main/docs/setup_2.png)

Select the **Path** variable then **Edit** to edit the variables.

![select path & click 'Edit...'](https://raw.githubusercontent.com/Ktt-Development/rexedia/main/docs/setup_3.png)

Press **New** and add the path to the rexedia installation. The path should be the folder containing the executable, not the exe file itself.

**Example:** `C:\Program Files\rexedia`

![add new with rexedia path](https://raw.githubusercontent.com/Ktt-Development/rexedia/main/docs/setup_4.png)

After this is completed Rexedia should be accessible from the command prompt without needing the full path.

```
C:\Users\Katsute> rexedia
usage: rexedia
 -b,--backup <arg>           Should a backup file be kept of the original
 -c,--cover <arg>            The cover format to use
 -d,--debug <arg>            Run logging in debug mode and create a debug
                             file
```