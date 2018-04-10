package de.philipppixel.tweetkov.core;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class TweetkovChainTest {
    private TweetkovChain sut = new TweetkovChain();

    @Test
    void trainShouldResultInSingleElementList() {
        // given
        List<String> input = Collections.singletonList("First Second Third");

        // when
        sut.train(input);

        // then
        Map<String, List<String>> actual = sut.getTrainingMap();
        assertThat(actual).isNotNull();

        String expectedPrefix = "First Second".toLowerCase();
        ArrayList<String> expectedSuffix = Lists.newArrayList("Third".toLowerCase());
        assertThat(actual.get(expectedPrefix)).isEqualTo(expectedSuffix);
    }

    @Test
    void testTrainOnFullText() {
        // given
        List<String> input = Collections.singletonList("now he is gone she said he is gone for good");

        // when
        sut.train(input);

        // then
        Map<String, List<String>> actual = sut.getTrainingMap();
        assertThat(actual).isNotNull().hasSize(7);
        assertThat(actual.get("now he")).isEqualTo(Lists.newArrayList("is"));
        assertThat(actual.get("he is")).isEqualTo(Lists.newArrayList("gone", "gone"));
        assertThat(actual.get("is gone")).isEqualTo(Lists.newArrayList("she", "for"));
        assertThat(actual.get("gone she")).isEqualTo(Lists.newArrayList("said"));
        assertThat(actual.get("she said")).isEqualTo(Lists.newArrayList("he"));
        assertThat(actual.get("said he")).isEqualTo(Lists.newArrayList("is"));
        assertThat(actual.get("gone for")).isEqualTo(Lists.newArrayList("good"));
    }

    @Test
    void generateShouldCreateNonEmptySentences() {
        // given
        List<String> input = Collections.singletonList("now he is gone she said he is gone for good");
        sut.train(input);

        // when
        String actual = sut.generate();

        // then
        assertThat(actual).matches("[a-zA-Z]+( [a-zA-Z]+)+ ?\\.");
    }

    @Test
    void replaceStringShouldReplaceAllOccurrences() {
        String actual = sut.replaceSpecialChars("&amp;&amp;\"\"&gt;");

        assertThat(actual).isEqualToIgnoringCase("&& >");
    }
}
