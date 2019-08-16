/*
 * Copyright 2019 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.mediaj.core;

import com.fizzed.crux.util.Resources;
import com.fizzed.crux.util.Size;
import com.fizzed.crux.util.StopWatch;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.input.CountingInputStream;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjlauer
 */
public class StreamingSVGDocumentTest {
    static private final Logger log = LoggerFactory.getLogger(StreamingSVGDocumentTest.class);
    
    @Test
    public void getSize() throws IOException {
        InputStream input = Resources.newInputStream("/fixtures/sample1.svg");

        StreamingSVGDocument svg = StreamingSVGDocument.load(input);
        
        StopWatch timer = StopWatch.timeMillis();
        
        Size size = svg.getSize();
        
        log.debug("getSize in {}", timer);
        
        assertThat(size.getWidth(), is(472.0d));
        assertThat(size.getHeight(), is(392.0d));
    }
    
    @Test
    public void getSizeWidthAndHeightOnly() throws IOException {
        InputStream input = Resources.newInputStream("/fixtures/sample2-large.svg");

        StreamingSVGDocument svg = StreamingSVGDocument.load(input);
        
        StopWatch timer = StopWatch.timeMillis();
        
        Size size = svg.getSize();
        
        log.debug("getSize in {}", timer);
        
        assertThat(size, is(not(nullValue())));
        assertThat(size.getWidth(), is(2045.0d));
        assertThat(size.getHeight(), is(1720.0d));
    }
 
    @Test
    public void getSizeViewBoxOnlyWithNegativeValues() throws IOException {
        InputStream input = Resources.newInputStream("/fixtures/sample3-no-wh.svg");

        StreamingSVGDocument svg = StreamingSVGDocument.load(input);
        
        Size size = svg.getSize();
        
        assertThat(size.getWidth(), is(473.3d));
        assertThat(size.getHeight(), is(394.7d));
    }
    
}