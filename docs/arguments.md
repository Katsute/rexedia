# Required Arguments

In order to format files Rexedia requires at least one **input** and a **format** or **preset**.

```
rexedia -i video.mp4 -m "title" "(.+)" "$1"
```
```
rexedia -i video.mp4 -p "preset.yml"
```

Note that formats and presets can not be used at the same time. If a preset is specified then the format and output parameters will be ignored.

# Arguments

Regular expression will test against the file name **without the extension**.

|Flag|Type|Description|
|---|---|---|
|<kbd>i</kbd> <kbd>input</kbd>|*`file`*|The file or directory path to format. Can be used multiple times.|
|<kbd>w</kbd> <kbd>walk</kbd>|*`boolean`*|If true, subdirectories will also be formatted (only for directory input).|
|<kbd>b</kbd> <kbd>backup</kbd>|*`boolean`*|If true, input files will be backed up.|
|<kbd>l</kbd> <kbd>logging</kbd>|*`boolean`*|If true, log files will be generated.|
|<kbd>d</kbd> <kbd>debug</kbd>|*`boolean`*|If true, debug logs will be generated.|
|<kbd>pc</kbd> <kbd>preserveCover</kbd>|*`boolean`*|If true, files with existing cover art will not get erased unless a new one is specified.|
|<kbd>pm</kbd> <kbd>preserveMeta</kbd>|*`boolean`*|If true, files will preserve any existing metadata in the final output.|
|<kbd>p</kbd> <kbd>preset</kbd>|*`file`*|The presets(/rexedia/presets) file path. Overrides cover, metadata, and output flags. Can only be used once.|
|<kbd>c</kbd> <kbd>cover</kbd>|*`regex string`*| The regular expression and back reference to use for the cover art. Can only be used once.|
|<kbd>m</kbd> <kbd>metadata</kbd>|*`string regex string`*|The metadata tag name, regular expression, and back reference string to use for metadata. Can be used multiple times.|
|<kbd>o</kbd> <kbd>output</kbd>|*`regex string`*| The regular expression and back reference string to use for output file. Uses the extension from the final string or the extension of the input file if none is specified. Can only be used once.|

# Example Usage

**Example:** Assign video a cover art image with the same name.
```
┬ video.mp4
└ video.png
```
```
rexedia -i video.mp4 -c "(.+)" "$1.png"
```

**Example:** Assign video a cover art image with the same name and save to new file.
```
┬ video.mp4 → video.avi
└ video.png
```
```
rexedia -i video.mp4 -c "(.+)" "$1.png" -o "(.+)" "$1.avi"
```

**Example:** Assign video metadata based on the name.
```
─ [S01 E02] video.mp4 → ┌ season_number = 1
                        ├ episode_sort = 2
                        └ show = video
```
```
rexedia -i video.mp4 -m "season_number" "\[S0*(\d*) E0*\d*\]" "$1" -m "episode_sort" "\[S0*\d* E0*(\d*)\]" "$1" -m "show" "\[S0*\d* E0*\d*\] (.+)" "$1"
```