package de.philipppixel.tweetkov.core;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.*;

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
        TransitionRepository repo = sut.getTransitions();
        assertThat(repo).isNotNull();

        Prefix expectedPrefix = new Prefix(2);
        expectedPrefix.appendToken("First");
        expectedPrefix.appendToken("Second");
        Collection<String> actualSuffixes = repo.get(expectedPrefix).getSuffixes();

        ArrayList<String> expectedSuffix = Lists.newArrayList("Third");
        assertThat(actualSuffixes).hasSize(1).containsAll(expectedSuffix);
    }

    @Test
    void trainShouldResultInSingleElementList_WindowSize1() {
        // given
        sut.setWindowSize(1);
        List<String> input = Collections.singletonList("First Second Third");

        // when
        sut.train(input);

        // then
        TransitionRepository actualTraining = sut.getTransitions();
        assertThat(actualTraining).isNotNull();

        Prefix expectedPrefix1 = new Prefix(1);
        expectedPrefix1.appendToken("First");
        Collection<String> actualSuffixes = actualTraining.get(expectedPrefix1).getSuffixes();

        ArrayList<String> expectedSuffix = Lists.newArrayList("Second");
        assertThat(actualSuffixes).hasSize(1).containsAll(expectedSuffix);

        Prefix expectedPrefix2 = new Prefix(1);
        expectedPrefix2.appendToken("Second");
        Collection<String> actualSuffixes2 = actualTraining.get(expectedPrefix2).getSuffixes();

        ArrayList<String> expectedSuffix2 = Lists.newArrayList("Third");
        assertThat(actualSuffixes2).hasSize(1).containsAll(expectedSuffix2);
    }

    @Test
    void testTrainOnFullText_WindowSize2() {
        // given
        List<String> input = Collections.singletonList("now he is gone she said he is gone for good");

        // when
        sut.train(input);

        // then
        TransitionRepository actual = sut.getTransitions();
        assertThat(actual).isNotNull();
        assertThat(actual.getAllTransitions()).hasSize(7);
        assertThat(actual.get(createPrefix_WS2("now", "he")).getSuffixes()).containsExactly("is");
        assertThat(actual.get(createPrefix_WS2("he", "is")).getSuffixes()).containsExactly("gone", "gone");
        assertThat(actual.get(createPrefix_WS2("is", "gone")).getSuffixes()).containsExactly("she", "for");
        assertThat(actual.get(createPrefix_WS2("gone", "she")).getSuffixes()).containsExactly("said");
        assertThat(actual.get(createPrefix_WS2("she", "said")).getSuffixes()).containsExactly("he");
        assertThat(actual.get(createPrefix_WS2("said", "he")).getSuffixes()).containsExactly("is");
        assertThat(actual.get(createPrefix_WS2("gone", "for")).getSuffixes()).containsExactly("good");
    }

    @Test
    void testTrainOnFullText_WindowSize1() {
        // given
        sut.setWindowSize(1);
        List<String> input = Collections.singletonList("now he is gone she said he is gone for good");

        // when
        sut.train(input);

        // then
        TransitionRepository actual = sut.getTransitions();
        assertThat(actual).isNotNull();
        assertThat(actual.getAllTransitions()).hasSize(7);
        assertThat(actual.get(prefix_WS1("now")).getSuffixes()).containsExactly("he");
        assertThat(actual.get(prefix_WS1("he")).getSuffixes()).containsExactly("is", "is");
        assertThat(actual.get(prefix_WS1("is")).getSuffixes()).containsExactly("gone", "gone");
        assertThat(actual.get(prefix_WS1("gone")).getSuffixes()).containsExactly("she", "for");
        assertThat(actual.get(prefix_WS1("she")).getSuffixes()).containsExactly("said");
        assertThat(actual.get(prefix_WS1("said")).getSuffixes()).containsExactly("he");
        assertThat(actual.get(prefix_WS1("for")).getSuffixes()).containsExactly("good");
    }

    @Test
    void testGenerateOnFullText_WindowSize1() {
        // given
        sut.setWindowSize(1);
        List<String> input = Collections.singletonList("now he is gone she said he is gone for good");
        sut.train(input);
        sut.initializeRandom(0);
        String newline = "\n";

        String actual = "";
        // when
        for (int i = 0; i < 10; i++) {
            actual += sut.generate() + newline;
        }

        // then
        String expected = "now he is gone for good.\n" +
                "now he is gone for good.\n" +
                "now he is gone she said he is gone for good.\n" +
                "now he is gone for good.\n" +
                "now he is gone she said he is gone for good.\n" +
                "for good.\n" +
                "for good.\n" +
                "she said he is gone she said he is gone for good.\n" +
                "she said he is gone for good.\n" +
                "now he is gone she said he is gone she said he is gone she said he is gone for good.\n";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testGenerateOnFullText_WindowSize2() {
        // given
        sut.setWindowSize(2);
        List<String> input = Collections.singletonList("now he is gone she said he is gone for good");
        sut.train(input);
        sut.initializeRandom(987654321);
        String newline = "\n";

        String actual = "";
        // when
        for (int i = 0; i < 10; i++) {
            actual += sut.generate() + newline;
        }

        // then
        String expected = "now he is gone she said he is gone for good.\n" +
                "now he is gone for good.\n" +
                "now he is gone she said he is gone she said he is gone she said he is gone for good.\n" +
                "gone for good.\n" +
                "now he is gone she said he is gone she said he is gone she said he is gone for good.\n" +
                "now he is gone she said he is gone she said he is gone for good.\n" +
                "now he is gone she said he is gone she said he is gone for good.\n" +
                "now he is gone she said he is gone she said he is gone for good.\n" +
                "he is gone for good.\n" +
                "now he is gone for good.\n";
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
        sut.initializeRandom(0);

        // when
        String actual = sut.generate();

        // then
        assertThat(actual).matches("[a-zA-Z]+( [a-zA-Z]+)+ ?\\.");
    }

    @Test
    void generateSentenceShouldCreateDuplicateSentence() {
        // given
        List<String> input = Collections.singletonList("now he is gone");
        sut.train(input);
        sut.initializeRandom(0);

        // when
        Sentence actual = sut.generateSentence();

        // then
        assertThat(actual.isDuplicate())
                .as("expected sentence '%s' to be a duplicate.", actual.create())
                .isTrue();
    }

    @Test
    void generateSentenceShouldCreateAlternativeSentence() {
        // given
        List<String> input = Arrays.asList("now he is gone", "now he went insane");
        sut.train(input);
        sut.initializeRandom(0);

        // when
        Sentence actual = sut.generateSentence();

        // then
        assertThat(actual.isDuplicate())
                .as("expected sentence '%s' to be a duplicate.", actual.create())
                .isFalse();
    }

    @Test
    void generateWithoutDuplicatesShouldQuitAfterThresholdAttempts() {
        // given
        List<String> input = Collections.singletonList("now he is gone");
        sut.train(input);
        sut.initializeRandom(0);

        // when
        String actual = sut.generateWithoutDuplicates();

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void generateWithoutDuplicatesShouldCreateSentence() {
        // given
        List<String> input = Arrays.asList("now he is gone", "now he went insane", "now is the thime");
        sut.train(input);

        // when
        String actual = sut.generateWithoutDuplicates();

        // then
        assertThat(actual).isNotEmpty();
    }

    @Test
    void replaceStringShouldReplaceAllOccurrences() {
        String actual = sut.replaceSpecialChars("&amp;&amp;&gt;");

        assertThat(actual).isEqualToIgnoringCase("&&>");
    }

    private Prefix createPrefix_WS2(String... tokens) {
        return createPrefix(2, tokens);
    }

    private Prefix prefix_WS1(String... tokens) {
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

    @Test
    void isStartPrefixShouldReturnTrueOnFirst_windowsSize1() {
        sut.setWindowSize(1);
        boolean actual = sut.isStartPrefix(1);

        assertThat(actual)
                .as("expected value to be a start prefix for window size 1")
                .isTrue();
    }

    @Test
    void isStartPrefixShouldReturnTrueOnFirst_windowsSize2() {
        sut.setWindowSize(2);
        boolean actual = sut.isStartPrefix(2);

        assertThat(actual)
                .as("expected value to be a start prefix for window size 2")
                .isTrue();
    }

    @Test
    void createHistogramShouldReturn4Rows() {
        // given
        sut.setWindowSize(1);
        String[] input = {"First Second Third", "First Lady Second That"};
        sut.train(Arrays.asList(input));

        // when
        String actual = sut.createHistogram();

        // then
        String expected = "Lady: 1\n"
                + "First: 2\n"
                + "Second: 2\n"
                + "Entries with 1 prefixes: 1\n"
                + "Entries with 2 prefixes: 2\n";
        assertThat(actual).isEqualTo(expected);
    }
}
