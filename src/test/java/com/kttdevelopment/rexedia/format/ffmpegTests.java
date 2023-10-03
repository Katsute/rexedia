package com.kttdevelopment.rexedia.format;

import com.kttdevelopment.rexedia.logger.LoggerFormatter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.logging.*;

@SuppressWarnings("SpellCheckingInspection")
public class ffmpegTests {

    private static final FFMPEG ffmpeg = new FFMPEG("app/bin/ffmpeg.exe", "app/bin/ffprobe.exe");

    @BeforeAll
    public static void initFFMPEG() throws IOException{
        Logger.getGlobal().setLevel(Level.ALL);
        Logger.getGlobal().setUseParentHandlers(false);
        if(Logger.getGlobal().getHandlers().length == 0)
            Logger.getGlobal().addHandler(new ConsoleHandler() {{
                setLevel(Level.ALL);
                setFormatter(new LoggerFormatter(false, true));
            }});

        if(!input.exists())
            Files.copy(new File("src/test/resources/format/video.mp4").toPath(), input.toPath());
    }

    @AfterAll
    public static void denitFFMPEG(){
        try{ Files.delete(input.toPath());
        }catch(final Throwable ignored){ }
        try{ Files.delete(cover2.toPath());
        }catch(final Throwable ignored){ }
        try{ Files.delete(cover3.toPath());
        }catch(final Throwable ignored){ }
        try{ Files.delete(cover4.toPath());
        }catch(final Throwable ignored){ }
    }

    @TempDir
    public File dir = new File(String.valueOf(UUID.randomUUID()));

    @Test
    public void testVerify(){
        Assertions.assertTrue(ffmpeg.verifyFileIntegrity(new File("src/test/resources/format/video.mp4"), 3, 0));
        Assertions.assertFalse(ffmpeg.verifyFileIntegrity(new File("src/test/resources/format/corrupt.mp4"), 3, 0));
        Assertions.assertFalse(ffmpeg.verifyFileIntegrity(new File("src/test/resources/format/apply/cover.png"), 3, 0));
    }

    private static final File input = new File("src/test/resources/format/apply/clean.mp4");
    private static final File cover = new File("src/test/resources/format/apply/cover.png");

    private static final File cover2 = new File("src/test/resources/format/apply/cover2.png");
    private static final File cover3 = new File("src/test/resources/format/apply/cover3.png");
    private static final File cover4 = new File("src/test/resources/format/apply/cover4.png");

    @Test
    public void missingArgs(){
       Assertions.assertThrows(FileNotFoundException.class, () -> ffmpeg.apply(null, null, false, null, false, null));
       Assertions.assertThrows(FileNotFoundException.class, () -> ffmpeg.apply(new File("null"), null, false, null, false, null));
       Assertions.assertThrows(FileNotFoundException.class, () -> ffmpeg.apply(input, null, false, null, false, null));
    }

    @Test
    public void testApplyNone() throws IOException{
        final File out = new File(dir, String.valueOf(UUID.randomUUID()));

        ffmpeg.apply(input, null, true, null, true, out);
        Assertions.assertEquals(input.length(), out.length());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testApplyCover() throws IOException{
        final File out  = new File(dir, UUID.randomUUID() + ".mp4");
        final File out2 = new File(dir, UUID.randomUUID() + ".mp4");
        final File out3 = new File(dir, UUID.randomUUID() + ".mp4");

        ffmpeg.apply(input, cover, false, null, false, out);
        Assertions.assertEquals(cover.length(), ffmpeg.getCoverArt(out, cover2).length());

        // test preserve
        ffmpeg.apply(out, null, true, null, false, out2);
        Assertions.assertEquals(cover.length(), ffmpeg.getCoverArt(out2, cover3).length());

        // test remove
        ffmpeg.apply(out, null, false, null, false, out3);
        Assertions.assertNull(ffmpeg.getCoverArt(out3, cover4));
    }

    @Test
    public void testOversizedCover(){
        final File cover = new File("src/test/resources/format/apply/oversized.png");
        final File out = new File(dir, String.valueOf(UUID.randomUUID()));

        Assertions.assertThrows(OutOfMemoryError.class, () -> ffmpeg.apply(input, cover, false, null, false, out));
    }

    @Test
    public void testApplyMetadata() throws IOException{
        final File out  = new File(dir, UUID.randomUUID() + ".mp4");
        final File out2 = new File(dir, UUID.randomUUID() + ".mp4");
        final File out3 = new File(dir, UUID.randomUUID() + ".mp4");
        final Map<String, String> metadata = Map.of("title","value","date", String.valueOf(UUID.randomUUID()));

        ffmpeg.apply(input, null, false, metadata, false,out);
        Assertions.assertEquals(metadata.get("title"),ffmpeg.getMetadata(out).get("title"));
        Assertions.assertEquals(metadata.get("date"),ffmpeg.getMetadata(out).get("date"));

        // test preserve
        ffmpeg.apply(out,null,false,null,true,out2);
        Assertions.assertEquals(metadata.get("title"),ffmpeg.getMetadata(out2).get("title"));
        Assertions.assertEquals(metadata.get("date"),ffmpeg.getMetadata(out2).get("date"));

        // test remove
        ffmpeg.apply(out,null,false,null,false,out3);
        Assertions.assertNull(ffmpeg.getMetadata(out3).get("title"));
        Assertions.assertNull(ffmpeg.getMetadata(out3).get("date"));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testApplyAll() throws IOException{
        final File out  = new File(dir, UUID.randomUUID() + ".mp4");
        final File out2 = new File(dir, UUID.randomUUID() + ".mp4");
        final File out3 = new File(dir, UUID.randomUUID() + ".mp4");
        final Map<String,String> metadata = Map.of("title","value","date",String.valueOf(UUID.randomUUID()));

        ffmpeg.apply(input,cover,false,metadata,false,out);
        Assertions.assertNotEquals(0,ffmpeg.getCoverArt(out,cover2).length());
        Assertions.assertEquals(metadata.get("title"),ffmpeg.getMetadata(out).get("title"));
        Assertions.assertEquals(metadata.get("date"),ffmpeg.getMetadata(out).get("date"));

        // test preserve
        ffmpeg.apply(out,cover,false,metadata,false,out2);
        Assertions.assertNotEquals(0,ffmpeg.getCoverArt(out2,cover3).length());
        Assertions.assertEquals(metadata.get("title"),ffmpeg.getMetadata(out2).get("title"));
        Assertions.assertEquals(metadata.get("date"),ffmpeg.getMetadata(out2).get("date"));

        // test remove
        ffmpeg.apply(out,null,false,null,false,out3);
        Assertions.assertNull(ffmpeg.getCoverArt(out3,cover4));
        Assertions.assertNull(ffmpeg.getMetadata(out3).get("title"));
        Assertions.assertNull(ffmpeg.getMetadata(out3).get("date"));
    }

}
