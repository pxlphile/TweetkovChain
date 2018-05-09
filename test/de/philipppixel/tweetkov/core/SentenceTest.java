package de.philipppixel.tweetkov.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SentenceTest {

    private static final String WORD_DELIMITER = " ";
    private Sentence sut = new Sentence(WORD_DELIMITER);

    @Test
    void addPrefixShouldResultInDuplicate() {
        // given
        Prefix prefix = prefix("you", "are");

        Transition transition = new Transition(prefix);
        transition.mapSuffix("so");

        sut.addBridge("so", transition);

        // when
        boolean actual = sut.isDuplicate();

        // then
        assertThat(actual)
                .as("Expected the sentence to be a duplicate: %s", sut.toString())
                .isTrue();
    }

    @Test
    void addPrefixShouldResultInAlternative() {
// given
        Prefix prefix = prefix("you", "are");

        Transition transition = new Transition(prefix);
        transition.mapSuffix("so");
        transition.mapSuffix("not");

        sut.addBridge("so", transition);

        // when
        boolean actual = sut.isDuplicate();

        assertThat(actual)
                .as("Expected the sentence to be an alternative: %s", sut.toString())
                .isFalse();
    }

    private Prefix prefix(String... tokens) {
        Prefix prefix = new Prefix(tokens.length);
        for (String token : tokens) {
            prefix.appendToken(token);
        }
        return prefix;
    }
}