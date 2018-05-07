package de.philipppixel.tweetkov.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransitionTest {

    @Test
    void mapSuffixShouldAddAnotherSuffix() {
        // given
        Prefix prefix = new Prefix(1);
        prefix.appendToken("hello");
        Transition sut = new Transition(prefix);

        // when
        sut.mapSuffix("world");

        // then
        assertThat(sut.getSuffixes()).contains("world");
    }

    @Test
    void mapSuffixShouldAddSameSuffixSeveralTime() {
        // given
        Prefix prefix = new Prefix(1);
        prefix.appendToken("hello");
        Transition sut = new Transition(prefix);

        // when
        sut.mapSuffix("world");
        sut.mapSuffix("world");

        // then
        assertThat(sut.getSuffixes()).containsOnly("world");
    }

    @Test
    void mapSuffixShouldHandleDifferentSuffixes() {
        // given
        Prefix prefix = new Prefix(1);
        prefix.appendToken("hello");
        Transition sut = new Transition(prefix);

        // when
        sut.mapSuffix("world");
        sut.mapSuffix("kitty");
        sut.mapSuffix("flower");
        sut.mapSuffix("FLOWER");
        sut.mapSuffix("Flower");
        sut.mapSuffix("world");

        // then
        assertThat(sut.getSuffixes())
                .containsOnly("world", "kitty", "flower", "FLOWER", "Flower");
    }

    @Test
    void constructorShouldRejectNull() {
        assertThrows(IllegalArgumentException.class, () -> new Transition(null));
    }

    @Test
    void mappingShouldReturnUniqueSuffixCountForSameSuffix() {
        Prefix prefix = new Prefix(1);
        prefix.appendToken("Bon jour");
        Transition sut = new Transition(prefix);

        // when
        sut.mapSuffix("world");
        sut.mapSuffix("world");
        sut.mapSuffix("world");
        int actual = sut.getUniqueSuffixCount();

        // then
        assertThat(actual).isEqualTo(1);
    }

    @Test
    void mappingShouldReturnUniqueSuffixCountForDifferentSuffixes() {
        Prefix prefix = new Prefix(1);
        prefix.appendToken("Hello");
        Transition sut = new Transition(prefix);

        // when
        sut.mapSuffix("Kitty");
        sut.mapSuffix("is");
        sut.mapSuffix("world");
        int actual = sut.getUniqueSuffixCount();

        // then
        assertThat(actual).isEqualTo(3);
    }

    @Test
    void getRandomSuffixShouldReturnEmptyStringForNoSuffix() {
        Prefix prefix = new Prefix(1);
        prefix.appendToken("Xylophone");
        Transition sut = new Transition(prefix);

        // then
        String actual = sut.getRandomSuffix();

        assertThat(actual).isEmpty();
    }

    @Test
    void getRandomSuffixShouldReturnOnlySuffix() {
        Prefix prefix = new Prefix(1);
        prefix.appendToken("Xylophone");
        Transition sut = new Transition(prefix);
        sut.mapSuffix("Zoo");

        // then
        String actual = sut.getRandomSuffix();

        assertThat(actual).isEqualTo("Zoo");
    }

    @Test
    void calculateSuffixShouldReturnTheOnlySuffix() {
        Prefix prefix = new Prefix(1);
        prefix.appendToken("Xylophone");
        Transition sut = new Transition(prefix);
        sut.mapSuffix("Zoo");

        // then
        Collection<String> actual = sut.getSuffixes();

        assertThat(actual).hasSize(1).containsOnly("Zoo");
    }

    @Test
    void calculateSuffixShouldReturnAveragelyDistributedSuffixes() {
        Prefix prefix = new Prefix(1);
        prefix.appendToken("Xylophone");
        Transition sut = new Transition(prefix);
        sut.initializeRandom(1);

        sut.mapSuffix("Wolperdinger");
        sut.mapSuffix("Xantylope");
        sut.mapSuffix("York");
        sut.mapSuffix("Zoo");

        // then
        List<String> actual = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String suffix = sut.getRandomSuffix();
            actual.add(suffix);
        }

        String w = "Wolperdinger";
        String x = "Xantylope";
        String y = "York";
        String z = "Zoo";

        String[] oracleOfEleven = {y, w, x, x, w, w, x, y, z, y};
        assertThat(actual).containsExactlyInAnyOrder(oracleOfEleven);
    }

    @Test
    void mapSuffixShouldThrowAnException_whiteSpaceOrNull() {
        Prefix prefix = new Prefix(1);
        prefix.appendToken("Error");
        Transition sut = new Transition(prefix);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> sut.mapSuffix(null));
    }
}