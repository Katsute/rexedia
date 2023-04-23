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
import org.apache.commons.exec.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CommandExecutor {

    private final String[] args;

    public CommandExecutor(){
        this(new String[0]);
    }

    public CommandExecutor(final String... args){
        this.args = args;
    }

    //

    public final String execute() throws IOException{
        return execute(new String[0]);
    }

    public final String execute(final String... args) throws IOException{
        final Logger logger = Logger.getGlobal();

        final List<String> a = new ArrayList<>();
        a.addAll(Arrays.asList(this.args));
        a.addAll(Arrays.asList(args));

        final String asString = String.join(" ", a);

        logger.log(Level.FINER,"Executing args:\n" + asString);

        logger.log(Level.FINER,"--- [ START EXECUTION ] ---");

        final CommandLine cmd           = CommandLine.parse(asString);
        final DefaultExecutor executor  = new DefaultExecutor();
        final ByteArrayOutputStream OUT = new ByteArrayOutputStream();

        String result;

        executor.setWatchdog(new ExecuteWatchdog(120 * 1000));
        executor.setStreamHandler(new PumpStreamHandler(OUT));
        try{
            executor.execute(cmd);
        }catch(final Throwable e){
            result = OUT.toString().trim();
            logger.severe(result.isEmpty() ? "Execution timed out" : '\n' + result);
            logger.log(Level.FINER,"--- [ END EXECUTION ] ---");
            throw e;
        }
        result = OUT.toString().trim();
        logger.finer('\n' + result);
        logger.log(Level.FINER,"--- [ END EXECUTION ] ---");
        return result;
    }

    //

    @Override
    public String toString(){
        return new ToStringBuilder(getClass().getSimpleName())
            .addObject("args", args)
            .toString();
    }

}
