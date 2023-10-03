package com.kttdevelopment.rexedia.preset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

public class PresetParserTests {

    @TempDir
    public File dir = new File(String.valueOf(UUID.randomUUID()));

    @Test
    public void testFile() throws IOException{
        final String yml =
            "cover:\n" +
            "  regex: \"(.+)\"\n" +
            "  format: \"$1\"\n" +
            "metadata:\n" +
            "  - meta: \"name\"\n" +
            "    regex: \"(.+)\"\n" +
            "    format: \"$1\"\n" +
            "  - meta: \"name2\"\n" +
            "    regex: \"(.+)\"\n" +
            "    format: \"$1\"\n" +
            "output:\n" +
            "  regex: \"(.+)\"\n" +
            "  format: \"$1\"";

        final File presetFile = new File(dir, String.valueOf(UUID.randomUUID()));
        Files.write(presetFile.toPath(), yml.getBytes());

        final Preset preset = new PresetParser().parse(presetFile);
        Assertions.assertEquals(new MetadataPreset(null, "(.+)", "$1"), preset.getCoverPreset());
        Assertions.assertEquals(new MetadataPreset("name","(.+)","$1"), preset.getPresets()[0]);
        Assertions.assertEquals(new MetadataPreset("name2","(.+)","$1"), preset.getPresets()[1]);
        Assertions.assertEquals(new MetadataPreset(null,"(.+)","$1"), preset.getOutputPreset());
    }

    @Test
    public void testNoCover() throws IOException{
        final String yaml =
            "metadata:\n" +
            "  - meta: 'name'\n" +
            "    format: '$1'\n" +
            "    regex: '(.+)'\n" +
            "output:\n" +
            "  format: '$1'\n" +
            "  regex: '(.+)'";

        final Preset preset = new PresetParser().parse(yaml);
        Assertions.assertNull(preset.getCoverPreset());
        Assertions.assertEquals(new MetadataPreset("name","(.+)","$1"), preset.getPresets()[0]);
        Assertions.assertEquals(new MetadataPreset(null,"(.+)","$1"), preset.getOutputPreset());
    }

    @Test
    public void testNoMeta() throws IOException{
        final String yaml =
            "cover:\n" +
            "  format: '$1'\n" +
            "  regex: '(.+)'\n" +
            "output:\n" +
            "  format: '$1'\n" +
            "  regex: '(.+)'";

        final Preset preset = new PresetParser().parse(yaml);
        Assertions.assertEquals(new MetadataPreset(null,"(.+)","$1"), new PresetParser().parse(yaml).getCoverPreset());
        Assertions.assertEquals(0, preset.getPresets().length);
        Assertions.assertEquals(new MetadataPreset(null,"(.+)","$1"), preset.getOutputPreset());
    }

    @Test
    public void testNone(){
        final Preset preset = new Preset.Builder().build();
        Assertions.assertNull(preset.getCoverPreset());
        Assertions.assertEquals(0, preset.getPresets().length);
        Assertions.assertNull(preset.getOutputPreset());
    }

    @Test
    public void testMissingCoverFormat(){
        final String yaml =
            "cover:\n" +
            "  regex: '(.+)'";

        Assertions.assertThrows(NullPointerException.class, () -> new PresetParser().parse(yaml).getCoverPreset());
    }

    @Test
    public void testMissingCoverRegex(){
        final String yaml =
            "cover:\n" +
            "  format: '$1'";

        Assertions.assertThrows(NullPointerException.class, () -> new PresetParser().parse(yaml));
    }

    @Test
    public void testMissingOutputFormat(){
        final String yaml =
            "output:\n" +
            "  regex: '(.+)'";

        Assertions.assertThrows(NullPointerException.class, () -> new PresetParser().parse(yaml).getOutputPreset());
    }

    @Test
    public void testMissingOutputRegex(){
        final String yaml =
            "output:\n" +
            "  format: '$1'";

        Assertions.assertThrows(NullPointerException.class, () -> new PresetParser().parse(yaml));
    }

    @Test
    public void testMissingMetaName(){
        final String yaml =
            "metadata:\n" +
            "  - format: '$1'\n" +
            "    regex: '(.+)'";
        Assertions.assertThrows(NullPointerException.class, () -> new PresetParser().parse(yaml));
    }

    @Test
    public void testMissingMetaFormat(){
        final String yaml =
            "metadata:\n" +
            "  - meta: 'name'\n" +
            "    regex: '(.+)'";
        Assertions.assertThrows(NullPointerException.class, () -> new PresetParser().parse(yaml));
    }

    @Test
    public void testMissingMetaRegex(){
        final String yaml =
            "metadata:\n" +
            "  - meta: 'name'\n" +
            "    format: '$1'";
        Assertions.assertThrows(NullPointerException.class, () -> new PresetParser().parse(yaml));
    }

    @Test
    public void testFormat() throws IOException{
        final String yaml =
            "metadata:\n" +
            "  - meta: 'name'\n" +
            "    format: '[$1]'\n" +
            "    regex: '(.+)'";

        Assertions.assertEquals("[format]", new PresetParser().parse(yaml).getPresets()[0].format("format"));
    }

    @Test
    public void testInvalidPattern(){
        final String yaml =
            "metadata:\n" +
            "  - meta: 'name'\n" +
            "    format: '[$1]'\n" +
            "    regex: '(.+'";

        Assertions.assertThrows(PatternSyntaxException.class, () -> new  PresetParser().parse(yaml));
    }

    @Test
    public void testEscapeChar(){
        final String yaml =
            "metadata:\n" +
            "  - meta: 'name'\n" +
            "    format: '\\$1\\'\n" +
            "    regex: '(.+)'";

        Assertions.assertThrows(IllegalArgumentException.class, () -> new PresetParser().parse(yaml).getPresets()[0].format("format"));
    }

    @Test
    public void testEscapedChar() throws IOException{
        final String yaml =
            "metadata:\n" +
            "  - meta: 'name'\n" +
            "    format: '\\\\$1\\\\'\n" +
            "    regex: '(.+)'";

        Assertions.assertEquals("\\format\\", new PresetParser().parse(yaml).getPresets()[0].format("format"));
    }

}
