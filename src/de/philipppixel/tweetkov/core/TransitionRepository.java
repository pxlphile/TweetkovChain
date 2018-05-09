package de.philipppixel.tweetkov.core;

import java.util.*;

/**
 * This class organizes the TweetkovChain dictionary and helps to decouple for the storage and retrieve part.
 */
class TransitionRepository {
    private static final int ORIGINAL_START_PREFIX_PROBABILITY_IN_PERCENT = 67;

    /**
     * Provides efficient access to all prefixes. Although transitions keep their own prefixes iterating all transitions
     * is not efficient enough to quickly get all prefixes.
     */
    private Map<Prefix, Transition> prefixToTransitions = new HashMap<>();
    private final List<Prefix> startPrefixes = new ArrayList<>();
    private Random random = new Random();

    void train(Prefix prefix, String suffix) {
        Transition mapping = get(prefix);
        if (mapping == null) {
            mapping = new Transition(prefix);
            prefixToTransitions.put(prefix, mapping);
        }
        mapping.mapSuffix(suffix);
    }

    void trainAsStartPrefix(Prefix prefix, String suffix) {
        train(prefix, suffix);
        addToStartTokens(prefix);
    }

    private void addToStartTokens(Prefix prefix) {
        startPrefixes.add(prefix);
    }

    Transition get(Prefix prefix) {
        return prefixToTransitions.get(prefix);
    }

    Collection<Transition> getAllTransitions() {
        return prefixToTransitions.values();

    }

    String getRandomSuffix(Prefix prefix) {
        Transition transition = prefixToTransitions.get(prefix);
        if (transition == null) {
            return "";
        }

        return transition.getRandomSuffix();
    }

    void initializeRandomSeed(long seed) {
        random.setSeed(seed);

        for (Transition transition : prefixToTransitions.values()) {
            transition.initializeRandom(seed);
        }
    }

    Prefix getRandomStartPrefix() {
        if(startPrefixes.isEmpty()) {
            throw new IllegalStateException("Cannot return start prefix because there are no prefixes yet.");
        }
        int keyIndex = random.nextInt(startPrefixes.size());
        return startPrefixes.get(keyIndex);
    }

    Prefix getRandomPrefix() {
        if(prefixToTransitions.isEmpty()) {
            throw new IllegalStateException("Cannot return prefix because there are no prefixes yet.");
        }

        Prefix[] prefixes = prefixToTransitions.keySet().toArray(new Prefix[startPrefixes.size()]);
        int keyIndex = random.nextInt(prefixes.length);
        return prefixes[keyIndex];
    }

    Prefix getFirstPrefixToken() {
        int hundredPercent = 100;
        int originalStartWord = random.nextInt(hundredPercent);
        if (originalStartWord <= ORIGINAL_START_PREFIX_PROBABILITY_IN_PERCENT) {
            return getRandomStartPrefix();
        }
        return getRandomPrefix();
    }
}
