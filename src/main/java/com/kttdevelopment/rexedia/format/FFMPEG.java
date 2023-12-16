/*
 * Copyright (C) 2021 Ktt Development
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.kttdevelopment.rexedia.format;

import com.kttdevelopment.rexedia.utility.ToStringBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("SpellCheckingInspection")
public final class FFMPEG {

    private final FFMPEGExecutor executor;

    public FFMPEG(final String pathToFFMPEG, final String pathToFFPROBE){
        executor = new FFMPEGExecutor(pathToFFMPEG,pathToFFPROBE);
    }

// ffprobe

    private final Pattern frames = Pattern.compile("^\\Qframe=\\E *(\\d+)\\Q fps=\\E.+$", Pattern.MULTILINE);

    private int getFrames(final File input){
        if(input == null) return -1;

        final String[] args = new String[]{
            "-i", '"' + input.getAbsolutePath() + '"',
            "-f", "null -" // destroy output
        };

        try{
            final String output   = executor.executeFFMPEG(args);
            final Matcher matcher = frames.matcher(output);

            int frames = -1;
            while(matcher.find()){
                int stream = Integer.parseInt(matcher.group(1));
                if(stream > frames)
                    frames = stream;
            }

            return frames;
        }catch(final IOException ignored){
            return -1;
        }
    }

    private final Pattern framerate = Pattern.compile("\\Q[STREAM]\\E\\r?\\n\\Qr_frame_rate=\\E(\\d+)/(\\d+)\\r?\\n\\Qduration=\\E(\\d+\\.\\d+)\\r?\\n\\Q[/STREAM]\\E", Pattern.MULTILINE);
    public final boolean verifyFileIntegrity(final File input, final int verify, final int verifyDiscrepancy){
        if(!input.exists()) return false;
        if(verify == 0) return true;

        final String[] args = new String[]{
            "-i", '"' + input.getAbsolutePath() + '"',
            "-v", "0",
            "-select_streams", "v", // video stream only
            "-show_entries", "stream=r_frame_rate,duration", // get framerate, and duration
        };

        try{
            final Logger logger   = Logger.getGlobal();
            final String result   = executor.executeFFPROBE(args);
            final Matcher matcher = framerate.matcher(result);

            if(!matcher.find()) // video stream is typically first, a fix is not needed at the moment
                return false;

            final int framerate     = Integer.parseInt(matcher.group(1)) / Integer.parseInt(matcher.group(2));
            final float duration    = Float.parseFloat(matcher.group(3));

            final int calculated = (int) Math.ceil(framerate * duration);
            final int actual     = getFrames(input);

            final int diff = actual - calculated;

            logger.finest("Expected: " + calculated);
            logger.finest("Actual: " + actual);
            logger.finest("Diff: " + diff);

            if(actual == -1)
                return false;
            else
                switch(verify){
                    default: // should never occur
                        return false;
                    case 1: // if file frames within range
                        return Math.abs(diff) <= verifyDiscrepancy;
                    case 2: // if file frames exceed calculated within range
                        return diff >= 0 && diff <= verifyDiscrepancy;
                    case 3: // if frames exact
                        return diff == 0;
                }
        }catch(final IOException | NumberFormatException ignored){
            return false;
        }
    }

    final Pattern metadata = Pattern.compile("^\\QTAG:\\E(.+)=(.+)$",Pattern.MULTILINE);
    public final Map<String,String> getMetadata(final File input){
        if(!input.exists()) return Collections.emptyMap();

        final String[] args = new String[]{
            "-i", '"' + input.getAbsolutePath() + '"',
            "-v", "0",
            "-show_entries", "format_tags", // get all format tags
        };

        try{
            final String result = executor.executeFFPROBE(args);
            final Matcher matcher = metadata.matcher(result);

            final Map<String,String> metadata = new HashMap<>();
            while(matcher.find())
                metadata.put(matcher.group(1), matcher.group(2));
            return metadata;
        }catch(final IOException ignored){ }
        return Collections.emptyMap();
    }

    // this method is used for testing, so it's ok to hardcode the video stream number
    public final File getCoverArt(final File input, final File output){
        final String[] args = {
            "-i", '"' + input.getAbsolutePath() + '"',
            "-map", "0:2", // get second stream
            "-frames:v 1", // get image
            "-c","copy",
            "-y",
            '"' + output.getAbsolutePath() + '"'
        };

        try{
            executor.executeFFMPEG(args);
        }catch(final IOException e){
            e.printStackTrace();
        }
        return output.exists() ? output : null;
    }

    final Pattern disp = Pattern.compile("\\Q[STREAM]\\E\\r?\\nindex=(\\d+)\\r?\\n\\QDISPOSITION:attached_pic=\\E(\\d+)\\r?\\n\\Q[/STREAM]\\E", Pattern.MULTILINE);
    public final List<Integer> getCoverArtStream(final File input){
        final String[] args = new String[]{
            "-i", '"' + input.getAbsolutePath() + '"',
            "-v", "0",
            "-select_streams", "v", // video stream only
            "-show_entries", "stream_disposition=attached_pic", // get attached pic property
            "-show_entries", "stream=index" // get index
        };

        try{
            final String result   = executor.executeFFPROBE(args);
            final Matcher matcher = disp.matcher(result);

            final ArrayList<Integer> remove = new ArrayList<>();
            while(matcher.find())
                if(matcher.group(2).equals("1"))
                    remove.add(Integer.parseInt(matcher.group(1)));
            return Collections.unmodifiableList(remove);
        }catch(final IOException | NumberFormatException ignored){
            return Collections.emptyList();
        }
    }

// ffmpeg

    public final void apply(
        final File INPUT,
        final File cover, final boolean preserveCover,
        final Map<String,String> metadata, final boolean preserveMeta,
        final File OUT) throws IOException{

        if(INPUT == null)
            throw new FileNotFoundException();
        else if(!INPUT.exists())
            throw new FileNotFoundException(INPUT.getAbsolutePath());
        else if(OUT == null)
            throw new FileNotFoundException();
        else if(!OUT.getParentFile().exists() && !OUT.getParentFile().mkdirs())
            throw new FileNotFoundException(OUT.getParentFile().getAbsolutePath());
        else if(INPUT.getAbsolutePath().equals(OUT.getAbsolutePath()))
            throw new IllegalArgumentException("FFMPEG can not write to the file it is reading from");
        else if(cover != null && cover.exists() && cover.length() > 1e+7)
            throw new OutOfMemoryError("Cover art files exceeding 10MB will corrupt video file");

        if(((cover == null || !cover.exists()) && preserveCover) && ((metadata == null || metadata.isEmpty())) && preserveMeta){ // skip if no changes and preserve
            Files.copy(INPUT.toPath(), OUT.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        final List<String> args = new ArrayList<>();
        args.add("-i"); // input
            args.add('"' + INPUT.getAbsolutePath() + '"');

        // map can not be added here because it would cause the cover map order to be incorrect

        if(cover != null && cover.exists()){ // if cover exists
            args.add("-i"); // cover input
                args.add('"' + cover.getAbsolutePath() + '"');
            args.add("-map"); // all streams from cover input (this must come first)
                args.add("1");
            args.add("-map"); // all streams from first input (this must come last)
                args.add("0");
            args.add("-disposition:0"); // set stream as an attached_pic
               args.add("attached_pic");
        }else if((cover == null || !cover.exists()) && !preserveCover){ // if no cover and no preserve (remove cover)
            final List<Integer> covart = getCoverArtStream(INPUT);

            args.add("-map");
                args.add("0");

            for(final Integer index : covart){
                args.add("-map"); // remove stream index
                    args.add("-0:" + index);
            }
        }else{
            args.add("-map");
            args.add("0");
        }

        args.add("-y"); // override ? "-y" : "-n"

        args.add("-c"); // copy codec
            args.add("copy");

        if(!preserveMeta){ // if no preserve (remove previous metadata)
            args.add("-map_metadata"); // remove all metadata
                args.add("-1");
        }
        if(metadata != null && !metadata.isEmpty())
            metadata.forEach((k,v) -> {
                args.add("-metadata"); // add metadata
                    args.add(String.format("\"%s\"=\"%s\"",k,v));
            });

        args.add('"' + OUT.getAbsolutePath() + '"');

        executor.executeFFMPEG(args.toArray(new String[0]));
    }

    //

    @Override
    public String toString(){
        return new ToStringBuilder(getClass().getSimpleName())
            .addObject("executor", executor)
            .addObject("frames_regexp", frames.pattern())
            .addObject("framerate_regexp", framerate.pattern())
            .addObject("metadata_regexp", metadata.pattern())
            .toString();
    }

}
