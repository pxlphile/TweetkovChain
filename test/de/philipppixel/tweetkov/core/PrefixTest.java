package de.philipppixel.tweetkov.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrefixTest {

    @Test
    void toStringShouldReturnAStringWithoutWhiteSpace() {
        Prefix sut = new Prefix(1);
        sut.appendToken("Roxaaaannnneee!");

        String actual = sut.toString();

        String expected = "Roxaaaannnneee!";
        assertThat(actual).isEqualTo(expected);
    }

    @Test()
    void toStringShouldConcatenateStringsWithSpace() {
        Prefix sut = new Prefix(5);
        sut.appendToken("Hello");
        sut.appendToken("Darkness,");
        sut.appendToken("my");
        sut.appendToken("old");
        sut.appendToken("friend.");

        String actual = sut.toString();

        String expected = "Hello Darkness, my old friend.";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void equalsShouldReturnFalseForUnEqualPrefixes() {
        Prefix sut = new Prefix(2);
        sut.appendToken("UPPERCASE");
        sut.appendToken("lowercase");

        Prefix prefix2 = new Prefix(2);
        prefix2.appendToken("lowercase");
        prefix2.appendToken("UPPERCASE"); // note the position

        boolean actual = sut.equals(prefix2);

        assertThat(actual).as("Prefixes should not be equal: %s <=> %s", sut, prefix2).isFalse();
    }

    @Test
    void equalsShouldReturnTrueForEqualPrefixes() {
        Prefix sut = new Prefix(2);
        sut.appendToken("uppercase");
        sut.appendToken("lowercase");

        Prefix prefix2 = new Prefix(2);
        prefix2.appendToken("uppercase");
        prefix2.appendToken("lowercase");

        boolean actual = sut.equals(prefix2);

        assertThat(actual).as("Prefixes should be equal: %s <=> %s", sut, prefix2).isTrue();
    }

    @Test
    void equalsShouldReturnTrueForEqualPrefixes_ignoreCase() {
        Prefix sut = new Prefix(2);
        sut.appendToken("uppercase");
        sut.appendToken("lowercase");

        Prefix prefix2 = new Prefix(2);
        prefix2.appendToken("UPPERCASE");
        prefix2.appendToken("LOWERCASE");

        boolean actual = sut.equals(prefix2);

        assertThat(actual).as("Prefixes should be equal: %s <=> %s", sut, prefix2).isTrue();
    }

    @Test
    void hashcodeShouldBeDifferentForNonEqualPrefixes() {
        Prefix sut = new Prefix(2);
        sut.appendToken("lowercase");
        sut.appendToken("lowercase");

        Prefix prefix2 = new Prefix(2);
        prefix2.appendToken("lowercase");
        prefix2.appendToken("lowercase1"); // note the extra char

        boolean actual = sut.equals(prefix2);

        assertThat(actual).as("Prefixes should not have same hashcode: %s <=> %s", sut, prefix2).isFalse();
    }

    @Test
    void hashcodeShouldBeEqualForEqualPrefixes() {
        Prefix sut = new Prefix(2);
        sut.appendToken("lowercase");
        sut.appendToken("lowercase");

        Prefix prefix2 = new Prefix(2);
        prefix2.appendToken("LOWERCASE");
        prefix2.appendToken("lowercase");

        boolean actual = sut.equals(prefix2);

        assertThat(actual).as("Prefixes should have same hashcode: %s <=> %s", sut, prefix2).isTrue();
    }

    @Test
    void appendTokenShouldThrowExceptionForMoreTokensThanWindowSize() {
        Prefix sut = new Prefix(3);

        sut.appendToken("1");
        sut.appendToken("2");
        sut.appendToken("3");
        assertThrows(IllegalStateException.class, () -> sut.appendToken("4"));
    }

    @Test
    void shiftWithSuffixShouldCreateNewPrefix() {
        Prefix sut = new Prefix(2);
        sut.appendToken("1");
        sut.appendToken("2");

        Prefix actual = sut.shiftWithSuffix("3");

        Prefix expected = new Prefix(2);
        expected.appendToken("2");
        expected.appendToken("3");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shiftedPrefixShouldNotBeTheSameAsOriginalPrefix() {
        Prefix sut = new Prefix(2);
        sut.appendToken("1");
        sut.appendToken("2");

        Prefix actual = sut.shiftWithSuffix("3");

        assertThat(actual).isNotSameAs(sut);
    }
}