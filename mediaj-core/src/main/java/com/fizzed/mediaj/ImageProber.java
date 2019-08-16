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
import com.fizzed.crux.util.Size;
import com.fizzed.mediaj.core.ByteArrayImageInputStream;
import com.fizzed.mediaj.core.StreamingSVGDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

public class ImageProber {
 
    // https://stackoverflow.com/questions/672916/how-to-get-image-height-and-width-using-java
    static public Size probeSize(
            KnownMediaType mediaType,
            byte[] data) throws IOException {
        
        Objects.requireNonNull(mediaType, "mediaType was null");
        
        // special handling for svg
        if (mediaType == KnownMediaType.IMAGE_SVG_XML) {
            try (InputStream byteInput = new ByteArrayInputStream(data)) {
                return StreamingSVGDocument.load(byteInput).getSize();
            }
        }
        
        // fallback to ImageIO
        try (ImageInputStream imageInput = new ByteArrayImageInputStream(data)) {
            return probeSize(mediaType, imageInput);
        }
    }
    
    static public Size probeSize(
            KnownMediaType mediaType,
            InputStream input) throws IOException {
        
        Objects.requireNonNull(mediaType, "mediaType was null");
        
        // special handling for svg
        if (mediaType == KnownMediaType.IMAGE_SVG_XML) {
            return StreamingSVGDocument.load(input).getSize();
        }
        
        try (ImageInputStream imageInput = new MemoryCacheImageInputStream(input)) {
            return probeSize(mediaType, imageInput);
        }
    }
    
    
    
    static private Size probeSize(
            KnownMediaType mediaType,
            ImageInputStream imageInput) throws IOException {
        
        Objects.requireNonNull(mediaType, "mediaType was null");
        Objects.requireNonNull(imageInput, "imageInput was null");
        
        Iterator<ImageReader> iter = ImageIO.getImageReadersByMIMEType(mediaType.getLabel());
        if (iter.hasNext()) {
            ImageReader reader = iter.next();
            try {
                reader.setInput(imageInput);
                int width = reader.getWidth(reader.getMinIndex());
                int height = reader.getHeight(reader.getMinIndex());
                return new Size(width, height);
            } finally {
                reader.dispose();
            }
        } else {
            throw new IOException("Unable to probe dimension for media type " + mediaType);
        }
    }
    
}