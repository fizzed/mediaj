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
package com.fizzed.mediaj;

import com.fizzed.crux.mediatype.KnownMediaType;
import com.fizzed.crux.util.Resources;
import com.fizzed.crux.util.Size;
import com.fizzed.crux.util.StopWatch;
import java.io.IOException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageProberTest {
    static private final Logger log = LoggerFactory.getLogger(ImageProberTest.class);
    
    @Test
    public void probeDimensionJpeg() throws IOException {
        byte[] data = Resources.readAllBytes("/fixtures/sample1.jpg");
        
        StopWatch timer = StopWatch.timeMillis();
        
        Size size = ImageProber.probeSize(
            KnownMediaType.IMAGE_JPEG, data);
        
        log.debug("probed jpeg size in {}", timer);
        
        assertThat(size.getWidth(), is(1000.0d));
        assertThat(size.getHeight(), is(672.0d));
    }
    
    @Test
    public void probeDimensionPng() throws IOException {
        byte[] data = Resources.readAllBytes("/fixtures/sample1.png");
        
        StopWatch timer = StopWatch.timeMillis();
        
        Size size = ImageProber.probeSize(
            KnownMediaType.IMAGE_PNG, data);
        
        log.debug("probed png size in {}", timer);
        
        assertThat(size.getWidth(), is(650.0d));
        assertThat(size.getHeight(), is(341.0d));
    }
    
    @Test
    public void probeDimensionSvg() throws IOException {
        byte[] data = Resources.readAllBytes("/fixtures/sample1.svg");
        
        StopWatch timer = StopWatch.timeMillis();
        
        Size size = ImageProber.probeSize(
            KnownMediaType.IMAGE_SVG_XML, data);
        
        log.debug("probed svg size in {}", timer);
        
        assertThat(size.getWidth(), is(472.0d));
        assertThat(size.getHeight(), is(392.0d));
    }
    
    @Test
    public void probeDimensionSvgLarge() throws IOException {
        byte[] data = Resources.readAllBytes("/fixtures/sample2-large.svg");
        
        StopWatch timer = StopWatch.timeMillis();
        
        Size size = ImageProber.probeSize(
            KnownMediaType.IMAGE_SVG_XML, data);
        
        log.debug("probed svg (large) size in {}", timer);
        
        assertThat(size.getWidth(), is(2045.0d));
        assertThat(size.getHeight(), is(1720.0d));
    }
    
}