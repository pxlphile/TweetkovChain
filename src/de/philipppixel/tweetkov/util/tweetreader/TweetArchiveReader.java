package de.philipppixel.tweetkov.util.tweetreader;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class parses a given Twitter archive and returns the tweets.
 * <p>
 * <code>
 * tweets = new TweetArchiveReader()
 * .withArchive("/path/to/monthly/archive/2018_03.js")
 * .getTweetTexts();
 * </code>
 */
public class TweetArchiveReader {
    private String pathToArchive;

    public TweetArchiveReader withArchive(String pathToArchive) {
        this.pathToArchive = pathToArchive;
        return this;
    }

    private Tweet[] readArchive() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(pathToArchive));
            reader.readLine(); // this will read the first line

            return mapper.readValue(reader, Tweet[].class);
        } catch (Exception e) {
            throw new RuntimeException("Error while reading archive", e);
        }
    }

    public List<String> getTweetTexts() {
        return Arrays.stream(readArchive())
                .map(tweet -> tweet.text)
                .collect(Collectors.toList());
    }
}
