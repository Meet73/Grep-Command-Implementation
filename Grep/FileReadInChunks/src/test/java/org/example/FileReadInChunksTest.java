package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileReadInChunksTest {

    @Mock
    private FileChannel channel;

    @Mock
    private MyRegexOptions options;

    @Mock
    private LineResult.Builder builder;

    @Mock
    private Result result;

    @Mock
    private List<Pattern> regexPatterns;

    @Mock
    private List<String> files;

    // Class Under Test
    @InjectMocks
    private FileReadInChunks fileReadInChunks;

    @BeforeEach
    public void before() {
        assertNotNull(channel);
        assertNotNull(regexPatterns);
        assertNotNull(builder);
        assertNotNull(result);
        assertNotNull(files);
        assertNotNull(options);
        fileReadInChunks = new FileReadInChunks(channel, 0, 100, 1, regexPatterns, builder, result, files, options);
    }

    @Test
    public void testInitialization() {
        assertNotNull(fileReadInChunks);
    }

    @Test
    public void testRun() throws IOException {
        byte[]testData="line1\nline2\nline3".getBytes();
//        when(channel.read(any(ByteBuffer.class))).thenReturn(testData.length).thenReturn(-1);

//        ByteBuffer buffer = ByteBuffer.allocate(100); // Create a ByteBuffer instance
//        when(channel.read(buffer)).thenReturn(testData.length).thenReturn(-1);

        doReturn(testData.length).when(channel).read(any(ByteBuffer.class), anyLong());
//        doNothing().when(fileReadInChunks).processLine(any(),anyLong(),anyLong());
        fileReadInChunks.run();

        verify(channel,times(1)).read(any(ByteBuffer.class));

        verify(fileReadInChunks,times(1)).processLine(any(),anyLong(),anyLong());
    }
}
