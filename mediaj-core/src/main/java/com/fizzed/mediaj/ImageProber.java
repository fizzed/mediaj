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
import com.fizzed.crux.util.Base16;
import com.fizzed.crux.util.Size;
import com.fizzed.mediaj.core.ByteArrayImageInputStream;
import com.fizzed.mediaj.core.StreamingSVGDocument;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

public class ImageProber {
 
    static private final int MAGIC_MAX_LENGTH;
    static private final Map<byte[],KnownMediaType> MAGIC_NUMBERS;
    static {
        MAGIC_NUMBERS = new LinkedHashMap<>();
        MAGIC_NUMBERS.put(Base16.decode("ffd8ff"), KnownMediaType.IMAGE_JPEG);
        MAGIC_NUMBERS.put(Base16.decode("89504e470d0a1a0a"), KnownMediaType.IMAGE_PNG);
        MAGIC_NUMBERS.put(Base16.decode("25504446"), KnownMediaType.APPLICATION_PDF);
        MAGIC_NUMBERS.put(Base16.decode("474946383961"), KnownMediaType.IMAGE_GIF);
        MAGIC_NUMBERS.put(Base16.decode("474946383761"), KnownMediaType.IMAGE_GIF);
        MAGIC_NUMBERS.put(Base16.decode("524946460000000057454250"), KnownMediaType.IMAGE_WEBP);

        // what's the max number of bytes we need to analyze?
        MAGIC_MAX_LENGTH = MAGIC_NUMBERS.keySet().stream()
            .mapToInt(v -> v.length)
            .max()
            .orElse(10);
    }
    
    static public KnownMediaType probeMediaType(
            byte[] data) throws IOException {
        
        if (data == null || data.length == 0) {
            return null;
        }
        
        try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            return probeMediaType(input);
        }
    }
    
    static public KnownMediaType probeMediaType(
            InputStream input) throws IOException {
        
        // https://en.wikipedia.org/wiki/Magic_number_%28programming%29#Magic_numbers_in_files
        // jpeg: FF D8 FF
        // png: 89 50 4E 47 0D 0A 1A 0A
        // pdf: 25 50 44 46
        // gif: 47 49 46 38 39 61   OR    47 49 46 38 37 61
        
        byte[] bytes = new byte[MAGIC_MAX_LENGTH];
        input.mark(bytes.length);     // 10 bytes should do it
        try {
            for (int i = 0; i < bytes.length; i++) {
                int read = input.read(bytes, i, 1);
                if (read < 0) {
                    return null;        // unable to detect media type
                }
                MAGIC_LOOP:
                for (Map.Entry<byte[],KnownMediaType> entry : MAGIC_NUMBERS.entrySet()) {
                    // only evaluate entries that we have a full length for
                    if (entry.getKey().length - 1 == i) {
                        // does it match?
                        for (int j = 0; j < entry.getKey().length; j++) {
                            // skip validating 0x00?
                            if (bytes[j] != (byte)0 && entry.getKey()[j] == (byte)0) {
                                // wildcard, allow it like it matched
                            }
                            else if (bytes[j] != entry.getKey()[j]) {
                                continue MAGIC_LOOP;  // move onto next
                            }
                        }
                        // it MUST have matched
                        return entry.getValue();
                    }
                }
            }
        } finally {
            input.reset();
        }
        
        return null;
    }
    
    
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