<div align="center">
    <a href="https://github.com/Ktt-Development/rexedia">
        <img src="https://raw.githubusercontent.com/Ktt-Development/rexedia/main/icon.png" alt="Logo" width="100" height="100">
    </a>
    <h3 align="center">Rexedia</h3>
    <p align="center">
        Metadata editing (including cover art) for bulk files using regular expressions (regex) and back references.
        <br />
        <a href="https://github.com/Ktt-Development/rexedia/tree/main/docs">Documentation</a>
        •
        <a href="https://github.com/Ktt-Development/rexedia/issues">Issues</a>
    </p>
    <a href="https://github.com/Ktt-Development/rexedia/actions/workflows/java_ci.yml"><img src="https://github.com/Ktt-Development/rexedia/actions/workflows/java_ci.yml/badge.svg" alt="Java CI"></a>
    <a href="https://github.com/Ktt-Development/rexedia/actions/workflows/codeql.yml"><img src="https://github.com/Ktt-Development/rexedia/actions/workflows/codeql.yml/badge.svg" alt="CodeQL"></a>
    <a href="https://github.com/Ktt-Development/rexedia/actions/workflows/release.yml"><img src="https://github.com/Ktt-Development/rexedia/actions/workflows/release.yml/badge.svg" alt="Deploy"></a>
    <a href="https://github.com/Ktt-Development/rexedia/releases"><img src="https://img.shields.io/github/v/release/Ktt-Development/rexedia" alt="version"></a>
    <a href="https://github.com/Ktt-Development/rexedia/blob/main/LICENSE"><img src="https://img.shields.io/github/license/Ktt-Development/rexedia" alt="license"></a>
</div>

**Contents:**
- [Setup](#setup)
- [Features](#features)
- [Arguments](#arguments)
- [Presets](#presets)

# Setup

Compiled binaries can be found in releases.
[![releases](https://img.shields.io/github/v/release/Ktt-Development/rexedia)](https://github.com/Ktt-Development/rexedia/releases)

# Features

### 🎬 Wide Media Support

Rexedia supports all file extensions supported by [FFMPEG](https://ffmpeg.org/).

```shell
rexedia -i "file.mp4" -c "(.+)" "$1.png" -o "(.+)" "$1.avi"
```

### ⭐ Features

Complicated tasks simplified:
- Apply cover art and metadata.
- Customize output with regular expressions and back references.
- Support for preset files instead of command-line flags.
- Logging to track changes.

```shell
rexedia -i video.mp4 -p preset.yml
```
```yml
# preset.yml
metadata:
  - meta: 'title'
    regex: '(.+)'
    format: '$1'
```

### ✔ Safe Video Formatting

Rexedia preserves the integrity of the video file.
- Verify file integrity using the `-v` flag.
- Backup files will always be saved on format failure.
- Use the `-b` flag to keep backup files even on successful formats.

```shell
rexedia -i "file.mp4" -c "(.+)" "$1.png" -b -v 3
```

# Arguments
**Regular expression will test against the file name without the extension.**

**At least one input required.**

**A cover, metadata, or output; or preset must be specified. If a preset is specified then the cover, metadata, and output flags will be ignored.**

|Flag|Type|Description|
|---|---|---|
|<kbd>-﻿i</kbd> <kbd>-﻿input</kbd>|*`[file]`*|The file or directory path to format. Can be used multiple times.|
|<kbd>-﻿w</kbd> <kbd>-﻿walk</kbd>|*`[boolean]`*|If true, subdirectories will also be formatted (only for directory input).|
|<kbd>-﻿b</kbd> <kbd>-﻿backup</kbd>|*`[boolean]`*|If true, input files will be backed up.|
|<kbd>-﻿l</kbd> <kbd>-﻿logging</kbd>|*`[boolean]`*|If true, log files will be generated.|
|<kbd>-﻿d</kbd> <kbd>-﻿debug</kbd>|*`[boolean]`*|If true, debug logs will be generated.|
|<kbd>-﻿v</kbd> <kbd>-verify</kbd>|*`[int]`*|File validation level. 0 = off, 1 = frames within range (default), 2 = frames equal to or exceeding within range, 3 = exact frame count.|
|<kbd>-﻿vd</kbd> <kbd>-﻿verifyDiscrepancy</kbd>|*`[int]`*|Frame difference range (only for verify 1 or 2).|
|<kbd>-﻿pc</kbd> <kbd>-﻿preserveCover</kbd>|*`[boolean]`*|If true, files with existing cover art will not get erased unless a new one is specified.|
|<kbd>-﻿pm</kbd> <kbd>-﻿preserveMeta</kbd>|*`[boolean]`*|If true, files will preserve any existing metadata in the final output.|
|<kbd>-﻿p</kbd> <kbd>-﻿preset</kbd>|*`[file]`*|The [presets](#presets) file path. Overrides cover, metadata, and output flags. Can only be used once.|
|<kbd>-﻿c</kbd> <kbd>-﻿cover</kbd>|*`[regex] [string]`*| The regular expression and back reference to use for the cover art. Can only be used once.|
|<kbd>-﻿m</kbd> <kbd>-﻿metadata</kbd>|*`[string] [regex] [string]`*|The metadata tag name, regular expression, and back reference string to use for metadata. Can be used multiple times.|
|<kbd>-﻿o</kbd> <kbd>-﻿output</kbd>|*`[regex] [string]`*| The regular expression and back reference string to use for output file. Uses the extension from the final string or the extension of the input file if none is specified. Can only be used once.|

## Example Usage

**Assign video a cover art image with the same name**
```
┬ video.mp4
└ video.png
```
```sh
rexedia -i video.mp4 -c "(.+)" "$1.png"
```

**Assign video a cover art image with the same name and save to new file**
```
┬ video.mp4 → video.avi
└ video.png
```
```sh
rexedia -i video.mp4 -c "(.+)" "$1.png" -o "(.+)" "$1.avi"
```

**Assign video metadata based on the name**
```
─ [S01 E02] video.mp4 → ┌ season_number = 1
                        ├ episode_sort = 2
                        └ show = video
```
```sh
rexedia -i video.mp4 -m "season_number" "\[S0*(\d*) E0*\d*\]" "$1" -m "episode_sort" "\[S0*\d* E0*(\d*)\]" "$1" -m "show" "\[S0*\d* E0*\d*\] (.+)" "$1"
```

# Presets

```yml
cover:  # cover art
  regex: '(.+)'  # the regex capture string
  format: '$1'  # the final cover art name (regex backreferences supported)
metadata:  # list of metadata tags
  - meta: 'name'  # the metadata tag name
    regex: '(.+)'  # the regex capture string
    format: '$1'  # the final metadata value (regex backreferences supported)
output:  # output file
  regex: '(.+)'  # the regex capture string
  format: '$1'  # the final output name (regex backreferences supported)
```

## Example Usage

**Assign video metadata based on the name from preset**
```
┬ [S01 E02] video.mp4 → ┌ season_number = 1
└ preset.yml            ├ episode_sort = 2
                        └ show = video
```
```yml
# preset.yml
metadata:
  - meta: 'season_number'
    regex: '\[S0*(\d*) E0*\d*\\]'
    format: '$1'
  - meta: 'episode_sort'
    regex: '\[S0*\d* E0*(\d*)\]'
    format: '$1'
  - meta: 'show'
    regex: '\[S0*\d* E0*\d*\] (.+)'
    format: '$1'
```
```sh
rexedia -i video.mp4 -p preset.yml
```

## Contributing

- Found a bug? Open a new [issue](https://github.com/Ktt-Development/rexedia/issues).
- Want to contribute? Create a [fork](https://github.com/Ktt-Development/rexedia/fork) and open a [pull request](https://github.com/Ktt-Development/rexedia/pulls).