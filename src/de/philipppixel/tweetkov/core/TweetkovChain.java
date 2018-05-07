package de.philipppixel.tweetkov.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * TweetkovChain - the tweet Markov-chain text generator.
 * <p>
 * <code>first second third.</code>: A window size of two takes two subsequent words and maps the following of it:
 * "first second" -&gt; third. "first second" is called a <code>prefix</code> while "third" is the <code>suffix</code>.
 * <p>Multiple occurrences of the same word possible and desired because the selection</p>
 * probability rises later on.</p>
 * <p>
 * The window size (also known as the order of a Markov Chain) determines the number of tokens in the prefix which are
 * examined for the search of an existing suffix. The mapping from prefix-&gt;suffix is called a dictionary.</p>
 * <p>While a window size of one suffices for a small text base the textual stringency rises
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
    private static final int DEFAULT_WINDOW_SIZE = 2;
    private static final int MAX_NUMBER_OF_WORDS_PER_SENTENCE = 32;
    private static final String WORD_DELIMITER = " ";
    private static final String EMPTY_RESULT = "";
    private static final int DUPLICATE_TIMEOUT = 50;
    private static final Logger LOG = Logger.getLogger(TweetkovChain.class.getName());

    private final TransitionRepository transitionRepo = new TransitionRepository();
    private int windowSize;
    private Random random;

    /**
     * Creates a {@link TweetkovChain} with the default window size
     */
    public TweetkovChain() {
        this(DEFAULT_WINDOW_SIZE);
    }

    /**
     * Creates a {@link TweetkovChain} with a selectable window size
     *
     * @param windowSize determines the number of prefix tokens in a transition to a suffix
     */
    public TweetkovChain(int windowSize) {
        setWindowSize(windowSize);
        this.random = new Random();
    }

    /**
     * Creates a histogram output of the trained transitions cardinals.
     *
     * @return a histogram output of the trained transitions cardinals.
     */
    public String createHistogram() {
        Collection<Transition> transitions = transitionRepo.getAllTransitions();
        Map<Integer, Integer> histogram = new HashMap<>();
        StringBuilder result = new StringBuilder(transitions.size());

        for (Transition entry : transitions) {
            int valueSize = entry.getUniqueSuffixCount();

            result.append(String.format("%s: %s\n", entry.getPrefix().toString(), valueSize));

            int numberOfEntriesWithThatSize = histogram.getOrDefault(valueSize, 0);
            numberOfEntriesWithThatSize++;
            histogram.put(valueSize, numberOfEntriesWithThatSize);
        }

        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            result.append(String.format("Entries with %s prefixes: %s\n", entry.getKey().toString(), entry.getValue()));
        }
        return result.toString();
    }

    /**
     * Takes a collection or array of sentences and creates a mapping from prefixes to suffixes for each one for later
     * generation of sentences using the Markov property.
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
                currentPrefix.appendToken(currentToken);
                continue;
            }

            String suffix = currentToken;

            if (isStartPrefix(tokenIndex)) {
                transitionRepo.trainAsStartPrefix(currentPrefix, suffix);
            } else {
                transitionRepo.train(currentPrefix, suffix);
            }

            currentPrefix = currentPrefix.shiftWithSuffix(suffix);
        }
    }

    /**
     * returns true if the index of a given token points to
     *
     * @param tokenIndex the index of the current token. Currently, the index is one larger than the window size because
     *                   the prefix building happens after incrementing the tokenIndex.
     * @return true if the tokenIndex indicates that the prefix at hand is a start of a sentence
     */
    boolean isStartPrefix(int tokenIndex) {
        return tokenIndex == this.windowSize;
    }

    String replaceSpecialChars(String currentToken) {
        return currentToken
                .replaceAll("&amp;", "&")
                .replaceAll("&gt;", ">")
                .replaceAll("&lt;", "<")
                .replaceAll("\\.\\.\\.", "\\u2026");
    }

    Sentence generateSentence() {
        Prefix prefix = transitionRepo.getFirstPrefixToken();
        Sentence sentence = new Sentence(WORD_DELIMITER);

        for (int i = 0; i < MAX_NUMBER_OF_WORDS_PER_SENTENCE; i++) {
            String suffix = transitionRepo.getRandomSuffix(prefix);
            Transition transition = transitionRepo.get(prefix);
            sentence.addBridge(suffix, transition);

            if (suffix.equals(EMPTY_RESULT)) {
                break;
            }
            prefix = prefix.shiftWithSuffix(suffix);
        }
        return sentence;
    }

    public String generate() {
        return generateSentence().create();
    }

    /**
     * Returns a sentence that is less likely to be a duplicate (although there is a chance).
     * <p>
     * This method may return an empty string (f. i. for low quality training data) when there have been
     * attempted {@link #DUPLICATE_TIMEOUT} retries without success.
     *
     * @return a sentence that is less likely to be a duplicate
     */
    public String generateWithoutDuplicates() {
        Sentence sentence = generateSentence();

        int retryCounter = 0;
        while (sentence.isDuplicate() && retryCounter < DUPLICATE_TIMEOUT) {
            sentence = generateSentence();
            retryCounter++;
        }

        if (retryCounter == DUPLICATE_TIMEOUT) {
            LOG.warning("Could not generate sentence without duplicate. Returning empty string.");
            return "";
        }

        return sentence.create();
    }

    TransitionRepository getTransitions() {
        return transitionRepo;
    }

    /**
     * This makes the tester happy
     *
     * @param seed this value initializes the pseudo-random generator. For the same seed the generated values are
     *             predictable, thus enabling testing.
     */
    void initializeRandom(long seed) {
        random.setSeed(seed);
        transitionRepo.initializeRandomSeed(seed);
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
