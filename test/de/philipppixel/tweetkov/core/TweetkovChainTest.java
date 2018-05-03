package de.philipppixel.tweetkov.core;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TweetkovChainTest {
    private TweetkovChain sut = new TweetkovChain();

    @Test
    void trainShouldResultInSingleElementList_WindowSize2() {
        // given
        List<String> input = Collections.singletonList("First Second Third");

        // when
        sut.train(input);

        // then
        Map<Prefix, List<String>> actualTraining = sut.getTrainingMap();
        assertThat(actualTraining).isNotNull();

        Prefix expectedPrefix = new Prefix(2);
        expectedPrefix.appendToken("First");
        expectedPrefix.appendToken("Second");
        ArrayList<String> expectedSuffix = Lists.newArrayList("Third");
        List<String> actualSuffix = actualTraining.get(expectedPrefix);
        assertThat(actualSuffix).isEqualTo(expectedSuffix);
    }

    @Test
    void trainShouldResultInSingleElementList_WindowSize1() {
        // given
        sut.setWindowSize(1);
        List<String> input = Collections.singletonList("First Second Third");

        // when
        sut.train(input);

        // then
        Map<Prefix, List<String>> actualTraining = sut.getTrainingMap();
        assertThat(actualTraining).isNotNull();

        Prefix expectedPrefix1 = new Prefix(1);
        expectedPrefix1.appendToken("First");
        ArrayList<String> expectedSuffix = Lists.newArrayList("Second");
        List<String> actualSuffix = actualTraining.get(expectedPrefix1);
        assertThat(actualSuffix).isEqualTo(expectedSuffix);

        Prefix expectedPrefix2 = new Prefix(1);
        expectedPrefix2.appendToken("Second");
        ArrayList<String> expectedSuffix2 = Lists.newArrayList("Third");
        List<String> actualSuffix2 = actualTraining.get(expectedPrefix2);
        assertThat(actualSuffix2).isEqualTo(expectedSuffix2);
    }

    @Test
    void testTrainOnFullText_WindowSize2() {
        // given
        List<String> input = Collections.singletonList("now he is gone she said he is gone for good");

        // when
        sut.train(input);

        // then
        Map<Prefix, List<String>> actual = sut.getTrainingMap();
        assertThat(actual).isNotNull().hasSize(7);
        assertThat(actual.get(createPrefix_WS2("now", "he"))).isEqualTo(Lists.newArrayList("is"));
        assertThat(actual.get(createPrefix_WS2("he", "is"))).isEqualTo(Lists.newArrayList("gone", "gone"));
        assertThat(actual.get(createPrefix_WS2("is", "gone"))).isEqualTo(Lists.newArrayList("she", "for"));
        assertThat(actual.get(createPrefix_WS2("gone", "she"))).isEqualTo(Lists.newArrayList("said"));
        assertThat(actual.get(createPrefix_WS2("she", "said"))).isEqualTo(Lists.newArrayList("he"));
        assertThat(actual.get(createPrefix_WS2("said", "he"))).isEqualTo(Lists.newArrayList("is"));
        assertThat(actual.get(createPrefix_WS2("gone", "for"))).isEqualTo(Lists.newArrayList("good"));
    }

    @Test
    void testTrainOnFullText_WindowSize1() {
        // given
        sut.setWindowSize(1);
        List<String> input = Collections.singletonList("now he is gone she said he is gone for good");

        // when
        sut.train(input);

        // then
        Map<Prefix, List<String>> actual = sut.getTrainingMap();
        assertThat(actual).isNotNull().hasSize(7);
        assertThat(actual.get(createPrefix_WS1("now"))).isEqualTo(Lists.newArrayList("he"));
        assertThat(actual.get(createPrefix_WS1("he"))).isEqualTo(Lists.newArrayList("is", "is"));
        assertThat(actual.get(createPrefix_WS1("is"))).isEqualTo(Lists.newArrayList("gone", "gone"));
        assertThat(actual.get(createPrefix_WS1("gone"))).isEqualTo(Lists.newArrayList("she", "for"));
        assertThat(actual.get(createPrefix_WS1("she"))).isEqualTo(Lists.newArrayList("said"));
        assertThat(actual.get(createPrefix_WS1("said"))).isEqualTo(Lists.newArrayList("he"));
        assertThat(actual.get(createPrefix_WS1("for"))).isEqualTo(Lists.newArrayList("good"));
    }

    @Test
    void testGenerateOnFullText_WindowSize1() {
        // given
        sut.setWindowSize(1);
        List<String> input = Collections.singletonList("now he is gone she said he is gone for good");
        sut.train(input);
        sut.initializeRandom(0);

        String actual = "";
        // when
        for (int i = 0; i < 10; i++) {
            actual += sut.generate();
        }

        // then
        String expected = "now he is gone she said he is gone she said he is gone for good." +
                "gone she said he is gone she said he is gone she said he is gone for good." +
                "now he is gone for good." +
                "now he is gone for good." +
                "now he is gone for good." +
                "now he is gone for good." +
                "now he is gone she said he is gone she said he is gone for good." +
                "for good." +
                "now he is gone she said he is gone for good." +
                "now he is gone she said he is gone she said he is gone she said he is gone for good.";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testGenerateOnFullText_WindowSize2() {
        // given
        sut.setWindowSize(2);
        List<String> input = Collections.singletonList("now he is gone she said he is gone for good");
        sut.train(input);
        sut.initializeRandom(987654321);

        String actual = "";
        // when
        for (int i = 0; i < 10; i++) {
            actual += sut.generate();
        }

        // then
        String expected = "now he is gone she said he is gone she said he is gone she said he is gone for good." +
                "now he is gone she said he is gone for good." +
                "now he is gone for good." +
                "now he is gone she said he is gone she said he is gone she said he is gone she said he is gone for good." +
                "gone for good." +
                "is gone for good." +
                "now he is gone for good." +
                "now he is gone for good." +
                "now he is gone she said he is gone she said he is gone she said he is gone she said he is gone for good." +
                "now he is gone she said he is gone for good.";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void constructorShouldThrowExceptionForNullNegativeWindowSize() {
        assertThrows(IllegalArgumentException.class, () -> sut.setWindowSize(0));
        assertThrows(IllegalArgumentException.class, () -> sut.setWindowSize(-1));
        assertThrows(IllegalArgumentException.class, () -> sut.setWindowSize(-1234));
        assertThrows(IllegalArgumentException.class, () -> sut.setWindowSize(Integer.MIN_VALUE));
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
        String actual = sut.replaceSpecialChars("&amp;&amp;&gt;");

        assertThat(actual).isEqualToIgnoringCase("&&>");
    }

    private Prefix createPrefix_WS2(String... tokens) {
        return createPrefix(2, tokens);
    }

    private Prefix createPrefix_WS1(String... tokens) {
        int windowSize = 1;
        return createPrefix(windowSize, tokens);
    }

    private Prefix createPrefix(int windowSize, String... tokens) {
        Prefix prefix = new Prefix(windowSize);
        for (String token : tokens) {
            prefix.appendToken(token);
        }
        return prefix;
    }
}
