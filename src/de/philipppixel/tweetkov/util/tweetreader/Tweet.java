package de.philipppixel.tweetkov.util.tweetreader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet {
    public String text;
}
