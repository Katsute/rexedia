package com.kttdevelopment.rexedia.config;

import com.kttdevelopment.rexedia.preset.MetadataPreset;
import org.apache.commons.cli.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

import static com.kttdevelopment.rexedia.config.Configuration.*;

public class ConfigurationTests {

    // input

    @Test
    public void testMissingInputArg(){
        Assertions.assertThrows(MissingArgumentException.class, () -> new Configuration("-input"));
    }

    @Test
    public void testMultipleInputs() throws IOException, ParseException{
        Configuration config = new Configuration("-input","file1.mp4","file2.mp4","-meta","","","");
        File[] input = (File[]) config.getConfiguration().get(INPUT);
        Assertions.assertEquals(new File("file1.mp4"), input[0]);
        Assertions.assertEquals(new File("file2.mp4"), input[1]);

        config = new Configuration("-input","file1.mp4","-input","file2.mp4","-meta","","","");
        input = (File[]) config.getConfiguration().get(INPUT);
        Assertions.assertEquals(new File("file1.mp4"), input[0]);
        Assertions.assertEquals(new File("file2.mp4"), input[1]);
    }

    // meta

    @Test
    public void testMissingMeta(){
        Assertions.assertThrows(MissingOptionException.class, () -> new Configuration("-input", "file.mp4"));
    }

    @TempDir
    public File dir = new File(String.valueOf(UUID.randomUUID()));

    @Test
    public void testPresetSubstituteMeta() throws IOException, ParseException{
        final String yml =
            "cover:\n" +
            "  regex: \"(.+)\"\n" +
            "  format: \"$1\"\n" +
            "metadata:\n" +
            "  - meta: \"name\"\n" +
            "    regex: \"(.+)\"\n" +
            "    format: \"$1\"\n" +
            "output:\n" +
            "  regex: \"(.+)\"\n" +
            "  format: \"$1\"";

        final File presetFile = new File(dir, String.valueOf(UUID.randomUUID()));
        Files.write(presetFile.toPath(), yml.getBytes());

        new Configuration("-input", "file.mp4","-preset",'"' + presetFile.getAbsolutePath() + '"');
    }

    @Test
    public void testMissingCoverArgs(){
        Assertions.assertThrows(MissingArgumentException.class, () -> new Configuration("-input", "file.mp4", "-c"));
    }

    @Test
    public void testMissingMetaArgs(){
        Assertions.assertThrows(MissingArgumentException.class, () -> new Configuration("-input", "file.mp4", "-m"));
    }

    @Test
    public void testCoverArgs() throws IOException, ParseException{
        final Configuration config = new Configuration("-input","file.mp4","-c","(.+)","$1");

        Assertions.assertEquals(new MetadataPreset(null,"(.+)","$1"), config.getPreset().getCoverPreset());
    }

    @Test
    public void testMetaArgs() throws IOException, ParseException{
        final Configuration config = new Configuration("-input","file.mp4","-m","name","(.+)","$1");

        Assertions.assertEquals(new MetadataPreset("name","(.+)","$1"), config.getPreset().getPresets()[0]);
    }

    @Test
    public void testInvalidPattern() {
        Assertions.assertThrows(PatternSyntaxException.class, () -> new Configuration("-input","file.mp4","-m","name","(.+","$1"));
    }

    @Test
    public void testMultipleMeta() throws IOException, ParseException{
        Configuration config = new Configuration("-input","file.mp4","-m","name","(.+)","$1","name2","(.+)","$1");
        Assertions.assertEquals(new MetadataPreset("name","(.+)","$1"), config.getPreset().getPresets()[0]);
        Assertions.assertEquals(new MetadataPreset("name2","(.+)","$1"), config.getPreset().getPresets()[1]);

        config = new Configuration("-input","file.mp4","-m","name","(.+)","$1","-meta","name2","(.+)","$1");
        Assertions.assertEquals(new MetadataPreset("name","(.+)","$1"), config.getPreset().getPresets()[0]);
        Assertions.assertEquals(new MetadataPreset("name2","(.+)","$1"), config.getPreset().getPresets()[1]);
    }

    @Test
    public void testOutputArgs() throws IOException, ParseException{
        final Configuration config = new Configuration("-input","file.mp4","-m","name","(.+)","$1","-o","(.+)","$1");

        Assertions.assertEquals(new MetadataPreset(null,"(.+)","$1"), config.getPreset().getOutputPreset());
    }

    // remaining flags

    @Test
    public void testDefault() throws IOException, ParseException{
        final Configuration config = new Configuration("-input","file.mp4","-m","name","(.+)","$1","-o","$1","");
        final String[] args = {WALK, BACKUP, LOGGING, PRECOV, PREMETA};

        for(final String arg : args)
             Assertions.assertNotNull(config.getConfiguration().get(arg));
    }

    // test not needed for malformed (default is false)

    @Test
    public void testArgs() throws IOException, ParseException{
        final Configuration def = new Configuration("-input","file1.mp4","-meta","","","");
        final Configuration config = new Configuration("-input","file1.mp4","-meta","","","",
            '-'+WALK,"true",
            '-'+BACKUP,"true",
            '-'+LOGGING,"true",
            '-'+PRECOV,"false",
            '-'+PREMETA,"true"
        );

        final String[] args = {WALK, BACKUP, LOGGING, PRECOV, PREMETA};

        for(final String arg : args)
             Assertions.assertNotEquals(def.getConfiguration().get(arg), config.getConfiguration().get(arg));
    }

}
