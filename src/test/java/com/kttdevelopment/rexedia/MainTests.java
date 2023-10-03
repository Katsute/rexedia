package com.kttdevelopment.rexedia;

import com.kttdevelopment.rexedia.format.FFMPEG;
import com.kttdevelopment.rexedia.logger.LoggerFormatter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.*;
import java.util.regex.Pattern;

public class MainTests {

    private static final Path sc = Paths.get("src/test/resources/format/corrupt.mp4");
    private static final Path sv = Paths.get("src/test/resources/format/video.mp4");

    private static final FFMPEG ffmpeg = new FFMPEG("app/bin/ffmpeg.exe", "app/bin/ffprobe.exe");

    @BeforeAll
    public static void setup(){
        // logging
        Logger.getGlobal().setLevel(Level.ALL);
        Logger.getGlobal().setUseParentHandlers(false);
        if(Logger.getGlobal().getHandlers().length == 0)
            Logger.getGlobal().addHandler(new ConsoleHandler() {{
            setLevel(Level.ALL);
            setFormatter(new LoggerFormatter(false, true));
        }});
    }

    @TempDir
    public File dir = new File(String.valueOf(UUID.randomUUID()));

    //

    @SuppressWarnings({"DuplicateExpressions", "RedundantSuppression"})
    @Test
    public void testLogging() throws IOException{
        final File debug    = new File("debug.log");

        Assertions.assertNull(getFile(new File("."), "\\d+\\Q.log\\E"), "The testing directory was not clear. Please remove log files.");

        debug.deleteOnExit();

        final String unique = String.valueOf(UUID.randomUUID());
        final File input    = new File(dir, unique + ".mp4");
        Files.copy(sv, input.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // test none
        Main.main(new String[]{
            "-i", '"' + input.getAbsolutePath() + '"',
            "-m", "\"title\"", "\"(.+)\"", "\"$1\"",
            "-o", "\"(.+)\"", '"' + new File(dir, String.valueOf(UUID.randomUUID())).getAbsolutePath() + ".avi\"",
        });

        Assertions.assertNull(getFile(new File("."),"\\d+\\Q.log\\E"));
        Assertions.assertFalse(debug.exists());

        // test logging
        final File log;
        Main.main(new String[]{
            "-i", '"' + input.getAbsolutePath() + '"',
            "-m", "\"title\"", "\"(.+)\"", "\"$1\"",
            "-o", "\"(.+)\"", '"' + new File(dir, String.valueOf(UUID.randomUUID())).getAbsolutePath() + ".avi\"",
            "-l"
        });

        Assertions.assertNotNull(log = getFile(new File("."),"\\d+\\Q.log\\E"));

        log.deleteOnExit();

        // test debug
        Main.main(new String[]{
            "-i", '"' + input.getAbsolutePath() + '"',
            "-m", "\"title\"", "\"(.+)\"", "\"$1\"",
            "-o", "\"(.+)\"", '"' + new File(dir, String.valueOf(UUID.randomUUID())).getAbsolutePath() + ".avi\"",
            "-d"
        });
        Assertions.assertTrue(debug.exists());
    }

    private File getFile(final File directory, final String regex){
        final Pattern pattern = Pattern.compile(regex);
        for(final File file : Objects.requireNonNullElse(directory.listFiles(), new File[0]))
            if(pattern.matcher(file.getName()).matches())
                return file;
        return null;
    }

    @Test // this method also tests preset as args
    public void testInput() throws IOException{
        // test corrupt
        {
            final String unique = String.valueOf(UUID.randomUUID());
            final File input    = new File(dir, unique + ".mp4");
            Files.copy(sc, input.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Main.main(new String[]{
                "-i", '"' + input.getAbsolutePath() + '"',
                "-m", "\"title\"", "\"(.+)\"", "\"$1\"",
                "-o", "\"(.+)\"", "\"$1.avi\""
            });
            Assertions.assertFalse(new File(dir, unique + ".avi").exists());
        }

        // test walk
        {
            final String unique = String.valueOf(UUID.randomUUID());
            // test no walk
            final File walk = new File(dir, String.valueOf(UUID.randomUUID()));
            Assertions.assertTrue(walk.exists() || walk.mkdirs());
            final File walkv = new File(walk, unique + ".mp4");

            Files.copy(sv, walkv.toPath(), StandardCopyOption.REPLACE_EXISTING);
            walkv.deleteOnExit();

            final File input    = new File(dir, unique + ".mp4");
            Files.copy(sv, input.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Main.main(new String[]{
                "-i", '"' + input.getAbsolutePath() + '"',
                "-m", "\"title\"", "\"(.+)\"", "\"$1\"",
                "-o", "\"(.+)\"", "\"$1.avi\""
            });

            final File output = new File(dir, unique + ".avi");
            final File outputw = new File(walk, unique + ".avi");
            output.deleteOnExit();
            outputw.deleteOnExit();

            Assertions.assertTrue(output.exists());
            Assertions.assertFalse(outputw.exists());
            Assertions.assertEquals(unique, ffmpeg.getMetadata(output).get("title"));

            // test walk

            Main.main(new String[]{
                "-i", '"' + input.getParentFile().getAbsolutePath() + '"',
                "-m", "\"title\"", "\"(.+)\"", "\"$1\"",
                "-o", "\"(.+)\"", "\"$1.avi\"",
                "-w"
            });

            Assertions.assertTrue(output.exists());
            Assertions.assertEquals(unique, ffmpeg.getMetadata(output).get("title"));
            Assertions.assertTrue(outputw.exists());
            Assertions.assertEquals(unique, ffmpeg.getMetadata(outputw).get("title"));
        }
    }

    @Test
    public void testBackup() throws IOException{
        final String unique = String.valueOf(UUID.randomUUID());
        final File input    = new File(dir, unique + ".mp4");
        Files.copy(sv, input.toPath(), StandardCopyOption.REPLACE_EXISTING);

        Main.main(new String[]{
            "-i", '"' + input.getAbsolutePath() + '"',
            "-m", "\"title\"", "\"(.+)\"", "\"$1\"",
            "-o", "\"(.+)\"", "\"$1.avi\""
        });

        Assertions.assertNull(getFile(dir,".+\\Q.backup.\\E.+"));

        Main.main(new String[]{
            "-i", '"' + input.getAbsolutePath() + '"',
            "-m", "\"title\"", "\"(.+)\"", "\"$1\"",
            "-o", "\"(.+)\"", "\"$1.avi\"" +
            "-b"
        });

        Assertions.assertNotNull(getFile(dir,".+\\Q.backup.\\E.+"));
    }

    @Test
    public void testPresetOverrideArgs() throws IOException{
        final String unique = String.valueOf(UUID.randomUUID());
        final File input    = new File(dir, unique + ".mp4");
        Files.copy(sv, input.toPath(), StandardCopyOption.REPLACE_EXISTING);

        final String yml =
            "metadata:\n" +
            "  - meta: 'title'\n" +
            "    regex: '(.+)'\n" +
            "    format: '$1'\n" +
            "output:\n" +
            "  regex: '(.+)'\n" +
            "  format: '$1.avi'\n";

        final File preset = new File(dir, String.valueOf(UUID.randomUUID()));
        Files.write(preset.toPath(), yml.getBytes());

        Main.main(new String[]{
            "-i", '"' + input.getAbsolutePath() + '"',
            "-m", "\"title\"", "\"(.+)\"", "\"$1\"",
            "-p", '"' + preset.getAbsolutePath() + '"'
        });

        final File output = new File(dir, unique + ".avi");
        output.deleteOnExit();
        Assertions.assertEquals(unique, ffmpeg.getMetadata(output).get("title"));
    }

}
