# Presets

If a preset is specified as an [arguments](https://github.com/Ktt-Development/rexedia/blob/main/docs/arguments.md) then the cover art, metadata, and output fields will be ignored. Only a single preset file can be used.

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

# Example Usage

**Example:** Assign video metadata based on the name from preset.
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
```
rexedia -i video.mp4 -p preset.yml
```