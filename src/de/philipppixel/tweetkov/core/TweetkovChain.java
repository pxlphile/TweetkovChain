package de.philipppixel.tweetkov.core;

import java.util.*;
import java.util.logging.Logger;

/**
 * TweetkovChain - the tweet Markov-chain text generator.
 * <p>
 * <code>first second third.</code>: A window size of two takes two subsequent words and maps the following of it:
 * "first second" -&gt; third. "first second" is called a <code>prefix</code> while "third" is the <code>suffix</code>.
 * <p>Multiple occurrences of the same word possible and desired because the selection</p>
 * probability rises later on.</p>
 * <p>
 * The window size determines (also known as the order of a Markov Chain) the structure of the dictionary (that is the
 * map from prefix-&gt;suffix). While a window size of one suffices for a small text base the textual stringency rises
 * with the window size because more prefix tokens are taken into consideration. And while this CAN lead to a better
 * textual stringency it also means that the word histogram MAY look totally different in terms of probable suffix
 * selection. Exactly one suffix for a prefix has a general probability of p=1.0 for selection which in turn leads to
 * a very high probability to re-generate already existing sentences.</p>
 * <p>For a window size of one, dictionary may look like this:</p>
 * <code>
 * hello -&gt; [again,world,my]<br/>
 * my -&gt; [old,little]<br/>
 * little -&gt; [pony]<br/>
 * old -&gt; [friend]<br/>
 * again -&gt; []<br/>
 * world -&gt; []<br/>
 * friend -&gt; []<br/>
 * pony -&gt; []<br/>
 * </code>
 * while for a window size of two the dictionary may look like this
 * * <code>
 * hello world -&gt; []<br/>
 * hello again -&gt; []<br/>
 * hello my -&gt; [old]<br/>
 * my little -&gt; [pony]<br/>
 * my old -&gt; [friend]<br/>
 * little pony -&gt; []<br/>
 * old friend -&gt; []<br/>
 * </code>
 * <p>
 * With a small dictionary you may want to select a window size of 1, while with a larger dictionary a window size of
 * 2 or even 3 may be good choices.</p>
 */
public class TweetkovChain {
    public static final int DEFAULT_WINDOW_SIZE = 2;
    private static final int MAX_NUMBER_OF_WORDS_PER_SENTENCE = 32;
    private static final int NULL_SAFE_RETRIES = 5;
    private static final String SENTENCE_DELIMITER = ".";
    private static final String WORD_DELIMITER = " ";
    private static final String EMPTY_RESULT = "";
    private static final Logger LOG = Logger.getLogger(TweetkovChain.class.getName());
    private static final int ORIGINAL_START_PREFIX_PROBABILITY_IN_PERCENT = 67;

    private final Map<Prefix, List<String>> trainingMap = new HashMap<>();
    private final List<Prefix> startPrefixes = new ArrayList<>();
    private int windowSize;
    private Random random;

    /**
     * Creates a {@link TweetkovChain} with the default window size
     */
    public TweetkovChain() {
        this(DEFAULT_WINDOW_SIZE);
    }

    public TweetkovChain(int windowSize) {
        setWindowSize(windowSize);
        this.random = new Random();
    }

    public void printHistogram() {
        Map<Integer, Integer> histogram = new HashMap<>();
        for (Map.Entry<Prefix, List<String>> entry : trainingMap.entrySet()) {
            int valueSize = entry.getValue().size();
            System.out.printf("%s: %s\n", entry.getKey().toString(), valueSize);
            int numberOfEntriesWithThatSize = histogram.getOrDefault(valueSize, 0);
            numberOfEntriesWithThatSize++;
            histogram.put(valueSize, numberOfEntriesWithThatSize);
        }

        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            System.out.printf("Entries with %s prefixes: %s\n", entry.getKey().toString(), entry.getValue());
        }
    }

    /**
     * Takes a collection or array of sentences and creates a mapping from prefixes to suffixes for each one
     * for later generation of sentences using the Markov property.
     *
     * @param sentences a collection or array of sentences
     */
    public void train(Iterable<String> sentences) {
        for (String sentence : sentences) {
            trainSingleLine(sentence);
        }
    }

    /**
     * Takes a sentence and creates a mapping from prefix(es) to suffix using a sliding-window. While the number of
     * tokens in a single prefix is determined by the windows size, the suffix consists maximally of one token. Thus,
     * the quality of the generated sentence correlates strongly with the size of the window.
     *
     * @param trainingLine the sentence that is subject to be tokenized into prefix(es) and suffix
     */
    private void trainSingleLine(String trainingLine) {
        String[] tokens = trainingLine.split(WORD_DELIMITER);
        Prefix currentPrefix = new Prefix(this.windowSize);

        for (int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++) {
            String currentToken = tokens[tokenIndex];
            currentToken = replaceSpecialChars(currentToken);

            if (currentPrefix.isSmallerThanWindowSize()) {
                LOG.fine("looking at " + currentToken);
                currentPrefix.appendToken(currentToken);
                LOG.fine("skip this round");
                continue;
            }

            LOG.fine("Looking at prefix: " + currentPrefix);
            List<String> suffixes = trainingMap.getOrDefault(currentPrefix, new ArrayList<>());

            String suffix = currentToken;
            LOG.fine("looking at suffix to add: " + suffix);
            suffixes.add(suffix);
            trainingMap.put(currentPrefix, suffixes);
            LOG.fine("map: " + trainingMap);

            if (tokenIndex == this.windowSize) {
                fillStartTokenList(currentPrefix);
            }

            currentPrefix = currentPrefix.shiftWithSuffix(suffix);
        }
    }

    private void fillStartTokenList(Prefix prefix) {
        this.startPrefixes.add(prefix);
    }

    String replaceSpecialChars(String currentToken) {
        return currentToken
                .replaceAll("&amp;", "&")
                .replaceAll("&gt;", ">")
                .replaceAll("&lt;", "<")
                .replaceAll("\\.\\.\\.", "\\u2026");
    }

    public String generate() {
        Prefix prefix = getStartPrefixToken();
        String generatedSentence = prefix.toString();

        for (int i = 0; i < MAX_NUMBER_OF_WORDS_PER_SENTENCE; i++) {
            String suffix = getRandomSuffix(prefix);
            generatedSentence += WORD_DELIMITER + suffix;

            if (suffix.equals(EMPTY_RESULT)) {
                break;
            }
            prefix = prefix.shiftWithSuffix(suffix);
        }
        return generatedSentence.trim() + SENTENCE_DELIMITER;
    }

    private Prefix getStartPrefixToken() {
        int hundredPercent = 100;
        int originalStartWord = random.nextInt(hundredPercent);
        if (originalStartWord <= ORIGINAL_START_PREFIX_PROBABILITY_IN_PERCENT) {
            return getPrefixFromStartPrefixList();
        }
        return getRandomStartPrefix();
    }

    private Prefix getPrefixFromStartPrefixList() {
        int keyIndex = random.nextInt(startPrefixes.size());
        return startPrefixes.get(keyIndex);
    }

    private Prefix getRandomStartPrefix() {
        List<Prefix> keys = new ArrayList<>(trainingMap.keySet());
        int keyIndex = random.nextInt(keys.size());
        return keys.get(keyIndex);
    }

    private String getRandomSuffix(Prefix prefix) {
        List<String> suffixes = trainingMap.get(prefix);

        if (suffixes == null) {
            return EMPTY_RESULT;
        }
        int index = random.nextInt(suffixes.size());
        return suffixes.get(index);
    }

    Map<Prefix, List<String>> getTrainingMap() {
        return trainingMap;
    }

    /**
     * This makes the tester happy
     * @param seed this value initializes the pseudo-random generator. For the same seed the generated values are
     *             predictable, thus enabling testing.
     */
    void initializeRandom(long seed) {
        random.setSeed(seed);
    }

    /**
     * Changes the number of the tokens per prefix. The change of the window size drastically changes the structure of
     * the generated sentences. This is because usually the number of mappings <code>prefix-&gt;suffix</code> decrease
     * when window size is increased, and vice versa.
     * <p>
     * Hint: For a small base of sentences (less than 10000) a window size of 1 is a good choice.
     *
     * @param windowSize the window size be at least 1 (one); otherwise an exception is thrown
     * @see #DEFAULT_WINDOW_SIZE
     */
    public void setWindowSize(int windowSize) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("Window size must not be smaller than 1. Given: " + windowSize);
        }
        this.windowSize = windowSize;
    }
}
