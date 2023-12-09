package com.trodix.duckcloud.domain.search.services;

import com.trodix.duckcloud.domain.exceptions.ParsingContentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSearchService {

    public String extractFileTextContent(byte[] content) throws ParsingContentException {

        InputStream stream = new ByteArrayInputStream(content);
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();

        try {
            parser.parse(stream, handler, metadata);
            String handlerContent = handler.toString();
            return handlerContent;
        }  catch (IOException | SAXException | TikaException e){
            throw new ParsingContentException("Error while parsing file content", e);
        }

    }

}
