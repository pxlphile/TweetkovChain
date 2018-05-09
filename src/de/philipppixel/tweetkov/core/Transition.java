package de.philipppixel.tweetkov.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * This class maps the possible transitions of prefix to any number of suffixes. While it is possible to append any
 * number of suffixes, the prefix is fixed once the transition is created. <p>This class is not thread-safe.</p>
 */
class Transition {

    private final Prefix prefix;
    private List<String> suffixes = new ArrayList<>();
    private Random random = new Random();

    /**
     * Creates a transition. The prefix will be fixed to this transition and cannot be changed.
     *
     * @param prefix the prefix identifies the start of the transition. It must not be <code>null</code>
     */
    Transition(Prefix prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Cannot create Transition. Prefix must not be null");
        }
        this.prefix = prefix;
    }

    /**
     * maps the suffix to the prefix. The same suffix can be mapped several times which increases its overall
     * probability of being picked during the sentence generation.
     *
     * @param suffixToken any string which will be trimmed. An exception will be thrown if <code>null</code>. Empty
     *                    strings are allowed because the algorithm may have arrived at the last prefix (usually the end
     *                    of the sentence).
     */
    void mapSuffix(String suffixToken) {
        if (suffixToken == null) {
            throw new IllegalArgumentException("Could not map suffix to prefix '" + prefix
                    + "'. Suffix must not be empty or null");
        }

        suffixes.add(suffixToken);
    }

    /**
     * returns the current suffixes.
     *
     * @return the current collection of suffixes. The collection is unmodifiable. If you want to add a suffix, please
     * use {@link #mapSuffix(String)}.
     */
    Collection<String> getSuffixes() {
        return Collections.unmodifiableCollection(suffixes);
    }

    /**
     * returns the prefix for this transition
     *
     * @return the prefix for this transition
     */
    Prefix getPrefix() {
        return prefix;
    }

    int getUniqueSuffixCount() {
        return new HashSet<>(suffixes).size();
    }

    private int getTotalSuffixCount() {
        return suffixes.size();
    }

    String getRandomSuffix() {
        if (getTotalSuffixCount() == 0) {
            return "";
        }

        int selectedSuffixIndex = this.random.nextInt(getTotalSuffixCount());
        return suffixes.get(selectedSuffixIndex);
    }

    void initializeRandom(long seed) {
        random.setSeed(seed);
    }
}
