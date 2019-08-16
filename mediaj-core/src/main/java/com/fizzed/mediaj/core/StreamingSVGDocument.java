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

import com.fizzed.crux.util.Size;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author jjlauer
 */
public class StreamingSVGDocument {

    final private XMLStreamReader streamReader;
    private boolean readHeader;
    private String heightAttr;
    private String widthAttr;
    private String viewBoxAttr;
    
    private StreamingSVGDocument(
            XMLStreamReader streamReader) {
        
        this.streamReader = streamReader;
    }
    
    static public StreamingSVGDocument load(
            InputStream input) throws IOException {

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            // these help immensely with speed...
            xmlInputFactory.setProperty("javax.xml.stream.isValidating", false);
            xmlInputFactory.setProperty("javax.xml.stream.isNamespaceAware", true);
            xmlInputFactory.setProperty("javax.xml.stream.isReplacingEntityReferences", false);
            xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
            xmlInputFactory.setProperty("javax.xml.stream.supportDTD", false);

            XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(input, "UTF-8");
            
            return new StreamingSVGDocument(streamReader);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
    
    public Size getSize() throws IOException {
        if (!readHeader) {
            this.readHeader();
        }
        
        double width = 0.0f;
        double height = 0.0f;
        
        if (this.heightAttr != null) {
            height = parseSize(this.heightAttr);
        }
        
        if (this.widthAttr != null) {
            width = parseSize(this.widthAttr);
        }
        
        if (height <= 0 || width <= 0) {
            // try viewbox now
            if (this.viewBoxAttr != null) {
                double[] values = parseViewBox(this.viewBoxAttr);
                if (values != null) {
                    width = values[2] - values[0];  // width - minx
                    height = values[3] - values[1]; // height - miny
                }
            }
        }
        
        if (height > 0 && width > 0) {
            return new Size(width, height);
        }
        
        return null;
    }

    /**
     * The value of the viewBox attribute is a list of four numbers min-x, min-y,
     * width and height, separated by whitespace and/or a comma, which specify a
     * rectangle in user space which is mapped to the bounds of the viewport
     * established for the associated element.
     */
    static private double[] parseViewBox(String value) {
        // e.g. 0 0 0 0
        if (value == null) {
            return null;
        }
        
        // space or comma apparently...
        String[] tokens = value.split("[ ,]+", 4);
        
        if (tokens == null || tokens.length != 4) {
            return null;
        }
        
        double[] values = new double[4];
        
        for (int i = 0; i < values.length; i++) {
            values[i] = parseSize(tokens[i]);
        }
        
        return values;
    }
    
    static private double parseSize(String value) {
        // e.g. 476 or 576px
        if (value == null) {
            return -1;
        }
        
        int posOfUnit = -1;
        
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c) || c == '-' || c == '.' || c == ' ') {
                // okay
            }
            else {
                posOfUnit = i;
                break;
            }
        }
        
        if (posOfUnit >= 0) {
            return Double.parseDouble(value.substring(0, posOfUnit).trim());
        }
        
        return Double.parseDouble(value.trim());
    }
    
    private void readHeader() throws IOException {
        try {
            while (streamReader.hasNext()) {
                streamReader.next();
                if (streamReader.isStartElement()) {
                    if (streamReader.getLocalName().equals("svg")) {
                        int attrs = streamReader.getAttributeCount();
                        for (int i = 0; i < attrs; i++) {
                            String name = streamReader.getAttributeLocalName(i);
                            if (name.equalsIgnoreCase("height")) {
                                this.heightAttr = streamReader.getAttributeValue(i);
                            }
                            else if (name.equalsIgnoreCase("width")) {
                                this.widthAttr = streamReader.getAttributeValue(i);
                            }
                            else if (name.equalsIgnoreCase("viewBox")) {
                                this.viewBoxAttr = streamReader.getAttributeValue(i);
                            }
                        }
                        this.readHeader = true;
                        return;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

}
