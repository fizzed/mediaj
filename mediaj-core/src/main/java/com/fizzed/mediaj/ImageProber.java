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
import com.fizzed.mediaj.core.ByteArrayImageInputStream;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageProber {
 
    // https://stackoverflow.com/questions/672916/how-to-get-image-height-and-width-using-java
    static public Dimension probeDimension(
            KnownMediaType mediaType,
            byte[] data) throws IOException {
        
        try (ImageInputStream input = new ByteArrayImageInputStream(data)) {
            return probeDimension(mediaType, input);
        }
    }
    
    static public Dimension probeDimension(
            KnownMediaType mediaType,
            ImageInputStream input) throws IOException {
        
        Objects.requireNonNull(mediaType, "mediaType was null");
        Objects.requireNonNull(input, "input was null");
        
        Iterator<ImageReader> iter = ImageIO.getImageReadersByMIMEType(mediaType.getLabel());
        if (iter.hasNext()) {
            ImageReader reader = iter.next();
            try {
                reader.setInput(input);
                int width = reader.getWidth(reader.getMinIndex());
                int height = reader.getHeight(reader.getMinIndex());
                return new Dimension(width, height);
            } finally {
                reader.dispose();
            }
        } else {
            throw new IOException("Unable to probe dimension for media type " + mediaType);
        }
    }
    
}