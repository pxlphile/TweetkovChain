package de.philipppixel.tweetkov;

import de.philipppixel.tweetkov.util.tweetreader.TweetRefiner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TweetRefinerTest {

    @Test
    void filterRetweets() {
        assertThat(TweetRefiner.filterOwn("RT some retweet stuff")).isFalse();
    }

    @Test
    void filterReplies() {
        assertThat(TweetRefiner.filterOwn("@Dude hey dude")).isFalse();
    }

    @Test
    void filterEmpty() {
        assertThat(TweetRefiner.filterOwn("")).isFalse();
    }

    @Test
    void filterLinksOnly() {
        assertThat(TweetRefiner.filterOwn("http://t.co/somePicture")).isFalse();
    }

    @Test
    void tokenizePoems() {
        // given
        String tweet = "Too often an end with a sigh/\n" +
                "So do not despair/\n" +
                "Gain some positive air/\n" +
                "Maybe with help from a sugary high.";

        // when
        List<String> actual = TweetRefiner.process(Collections.singletonList(tweet));

        // then
        List<String> expectedSentences = new ArrayList<>();
        expectedSentences.add("Too often an end with a sigh");
        expectedSentences.add("So do not despair");
        expectedSentences.add("Gain some positive air");
        expectedSentences.add("Maybe with help from a sugary high");
        assertThat(actual).isEqualTo(expectedSentences);
    }
}