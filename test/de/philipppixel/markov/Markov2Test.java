package de.philipppixel.markov;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class Markov2Test {
    private Markov2 sut = new Markov2();

    @Test
    void trainShouldResultInSingleElementList() {
        // given
        TestFeeder feeder = new TestFeeder("First Second Third");

        // when
        sut.train(feeder);

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
        TestFeeder feeder = new TestFeeder("now he is gone she said he is gone for good");

        // when
        sut.train(feeder);

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
        TestFeeder feeder = new TestFeeder("now he is gone she said he is gone for good");
        sut.train(feeder);

        // when
        String actual = sut.generate();

        // then
        assertThat(actual).matches("[a-zA-Z]+( [a-zA-Z]+)+ ?\\.");
    }

    private class TestFeeder implements LineFeeder {
        private List<String> line;

        TestFeeder(String line) {
            this.line = Collections.singletonList(line);
        }

        @Override
        public Iterator<String> iterator() {
            return line.iterator();
        }

        @Override
        public void forEach(Consumer<? super String> action) {
            //
        }

        @Override
        public Spliterator<String> spliterator() {
            return null;
        }
    }
}
