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

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author jjlauer
 */
public class StreamingSVGDocument {

    final private XMLEventReader xmlEventReader;
    private boolean svgElemRead;
    private Attribute heightAttr;
    private Attribute widthAttr;
    private Attribute viewBoxAttr;
    
    private StreamingSVGDocument(
            XMLEventReader xmlEventReader) {
        
        this.xmlEventReader = xmlEventReader;
    }
    
    static public StreamingSVGDocument load(
            InputStream input) throws IOException {

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(input);
            
            return new StreamingSVGDocument(xmlEventReader);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
    
    public Rectangle2D getSize() throws IOException {
        if (!svgElemRead) {
            this.readSvgElem();
        }
        
        // prever height/width vs. viewBox?
        
        return null;
    }

    private void readSvgElem() throws IOException {
        try {
            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    if (startElement.getName().getLocalPart().equals("svg")) {
                        this.heightAttr = startElement.getAttributeByName(new QName("height"));
                        this.widthAttr = startElement.getAttributeByName(new QName("width"));
                        this.viewBoxAttr = startElement.getAttributeByName(new QName("viewBox"));
                        this.svgElemRead = true;
                        return;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

}
