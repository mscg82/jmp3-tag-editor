package com.mp3.ui.parsers;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

public class FilenameParserProvider {

    private static Map<String, FilenamePatternParser> parsers;

    public static synchronized Map<String, FilenamePatternParser> getParsers() throws ExistingParserIDException {
        if(parsers == null) {
            initParsers();
        }
        return parsers;
    }

    private static void initParsers() throws ExistingParserIDException {
        ServiceLoader<FilenamePatternParser> serviceLoader = ServiceLoader.load(FilenamePatternParser.class);
        parsers = new TreeMap<>();
        for(FilenamePatternParser parser : serviceLoader) {
            String parserID = parser.getParserID();
            if(parsers.containsKey(parserID))
                throw new ExistingParserIDException("A parser with ID \"" + parserID + "\" has been " +
                		                            "already registered");
            parsers.put(parserID, parser);
        }
    }
}
