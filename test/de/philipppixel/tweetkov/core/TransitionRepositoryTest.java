package de.philipppixel.tweetkov.core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransitionRepositoryTest {

    private TransitionRepository sut = new TransitionRepository();

    @Test
    void trainShouldAddMappingsToRepository() {
        // given
        Prefix prefix = new Prefix(2);
        prefix.appendToken("My");
        prefix.appendToken("Little");

        // when
        sut.train(prefix, "Pony");

        // then
        Prefix prefixToSearch = prefix("My", "Little");
        assertThat(sut.get(prefixToSearch).getSuffixes()).containsExactly("Pony");
    }

    @Test
    void getRandomStartPrefixShouldReturnRandomPrefixesWithoutError() {
        // given
        Prefix prefix = new Prefix(2);
        prefix.appendToken("Hello");
        prefix.appendToken("World");
        sut.trainAsStartPrefix(prefix, "Something");

        Prefix prefix2 = new Prefix(2);
        prefix2.appendToken("Hello");
        prefix2.appendToken("Pluto");
        sut.trainAsStartPrefix(prefix2, "SomethingElse");

        sut.initializeRandomSeed(6); // pretty random, eh?

        // when
        List<Prefix> actual = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Prefix actualPrefix = sut.getRandomStartPrefix();
            actual.add(actualPrefix);
        }

        // then
        Prefix world = prefix("Hello", "World");
        Prefix pluto = prefix("Hello", "Pluto");

        Prefix[] oracleOfEleven = {pluto, world, pluto, world, pluto, pluto, world, world, world, pluto, pluto};

        assertThat(actual).containsExactly(oracleOfEleven);
    }

    @Test
    void getRandomStartPrefixShouldThrowExceptionForUntrainedPrefixes() {
        // when & then
        assertThrows(IllegalStateException.class, () -> sut.getRandomStartPrefix());
    }

    @Test
    void getRandomPrefixShouldThrowExceptionForUntrainedPrefixes() {
        // when & then
        assertThrows(IllegalStateException.class, () -> sut.getRandomPrefix());
    }

    @Test
    void getRandomPrefixShouldReturnPrefixesFromBothStartListAndDeeperPrefixes() {
        // given
        Prefix startPrefix1 = new Prefix(2);
        startPrefix1.appendToken("Hello");
        startPrefix1.appendToken("World");
        sut.trainAsStartPrefix(startPrefix1, "Europe");

        Prefix deeper1 = startPrefix1.shiftWithSuffix("Europe");
        sut.train(deeper1, "Germany"); // World Europe -> Germany

        Prefix deeper2 = deeper1.shiftWithSuffix("Germany");
        sut.train(deeper2, "Berlin"); // Europe Germany -> Berlin

        Prefix startPrefix2 = new Prefix(2);
        startPrefix2.appendToken("Hello");
        startPrefix2.appendToken("Pluto");
        sut.trainAsStartPrefix(startPrefix2, "Tombaugh");

        Prefix deeper3 = startPrefix2.shiftWithSuffix("Tombaugh");
        sut.train(deeper3, "SputnikPlanum");  // Pluto Tombaugh -> Sputnik

        Prefix deeper4 = deeper3.shiftWithSuffix("SputnikPlanum");
        sut.train(deeper4, "AstridColles"); // Tombaugh Sputnik -> Astrid

        sut.initializeRandomSeed(8); // pretty random, eh?

        // when
        List<Prefix> actual = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Prefix actualPrefix = sut.getRandomPrefix();
            actual.add(actualPrefix);
        }

        // then
        Prefix hellow = prefix("Hello", "World");
        Prefix world = prefix("World", "Europe");
        Prefix europe = prefix("Europe", "Germany");
        Prefix hellop = prefix("Hello", "Pluto");
        Prefix pluto = prefix("Pluto", "Tombaugh");
        Prefix tombaugh = prefix("Tombaugh", "SputnikPlanum");

        Prefix[] oracleOfEleven = {world, world, world, europe, pluto, tombaugh, hellow, hellop, hellop, pluto, pluto};

        assertThat(actual).containsExactly(oracleOfEleven);
    }

    @Test
    void test_getStartPrefixToken() {
        // given
        Prefix startPrefix1 = new Prefix(2);
        startPrefix1.appendToken("Hello");
        startPrefix1.appendToken("World");
        sut.trainAsStartPrefix(startPrefix1, "Europe");

        Prefix inner1 = startPrefix1.shiftWithSuffix("Europe");
        sut.train(inner1, "Germany"); // World Europe -> Germany

        Prefix inner2 = inner1.shiftWithSuffix("Germany");
        sut.train(inner2, "Berlin"); // Europe Germany -> Berlin

        Prefix startPrefix2 = new Prefix(2);
        startPrefix2.appendToken("Hello");
        startPrefix2.appendToken("Pluto");
        sut.trainAsStartPrefix(startPrefix2, "Tombaugh");

        Prefix inner3 = startPrefix2.shiftWithSuffix("Tombaugh");
        sut.train(inner3, "SputnikPlanum");  // Pluto Tombaugh -> Sputnik

        Prefix inner4 = inner3.shiftWithSuffix("SputnikPlanum");
        sut.train(inner4, "AstridColles"); // Tombaugh Sputnik -> Astrid

        sut.initializeRandomSeed(5); // pretty random, eh?

        // when
        List<Prefix> actual = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Prefix actualPrefix = sut.getFirstPrefixToken();
            actual.add(actualPrefix);
        }

        // then
        Prefix hellow = prefix("Hello", "World");
        Prefix world = prefix("World", "Europe");
        Prefix europe = prefix("Europe", "Germany");
        Prefix hellop = prefix("Hello", "Pluto");
        Prefix tombaugh = prefix("Tombaugh", "SputnikPlanum");

        // expect a 1/3 + 2/3 distribution of start prefix and inner prefix
        Prefix[] oracleOfEleven = {world, tombaugh, hellop, hellop, hellow, hellop, hellow, hellop, europe, hellop, hellow};

        assertThat(actual).containsExactly(oracleOfEleven);
    }

    private Prefix prefix(String... tokens) {
        Prefix p = new Prefix(tokens.length);
        for (String token : tokens) {
            p.appendToken(token);
        }
        return p;
    }
}