package de.philipppixel.tweetkov.core;

import java.util.*;
import java.util.logging.Logger;

/**
 * TweetkovChain - the tweet Markov-chain text generator.
 * <p>
 * <code>first second third.</code>: A window size of two takes two subsequent words and maps the following of it:
 * "first second" -&gt; third. "first second" is called a <code>prefix</code> while "third" is the <code>suffix</code>.
 * <p>
 * Multiple occurrences of the same word possible and desired because the selection
 * probability rises later on.
 * <p>
 * The window size determines (also known as the order of a Markov Chain) the structure of the dictionary (that is the
 * map from prefix-&gt;suffix). While a window size of one suffices for a small text base the textual stringency rises
 * with the window size because more prefix tokens are taken into consideration. And while this CAN lead to a better
 * textual stringency it also means that the word histogram MAY look totally different in terms of probable suffix
 * selection. Exactly one suffix for a prefix has a general probability of p=1.0 for selection which in turn leads to
 * a very high probability to re-generate already existing sentences.
 *
 * <p>
 * For a window size of one, dictionary may look like this:
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
 *
 * With a small dictionary you may want to select a window size of 1, while with a larger dictionary a window size of
 * 2 or even 3 may be good choices.
 */
public class TweetkovChain {
    private static final int DEFAULT_WINDOW_SIZE = 2;
    private static final int NULL_SAFE_RETRIES = 5;
    private static final String SENTENCE_DELIMITER = ".";
    private static final String WORD_DELIMITER = " ";
    private static final String EMPTY_RESULT = "";
    private static final Logger LOG = Logger.getLogger(TweetkovChain.class.getName());

    private Map<String, List<String>> trainingMap = new HashMap<>();
    private final int windowSize;

    /**
     * Creates a {@link TweetkovChain} with the default window size
     */
    public TweetkovChain() {
        this(DEFAULT_WINDOW_SIZE);
    }

    public TweetkovChain(int windowSize) {
        this.windowSize = windowSize;
    }

    public void printHistogram() {
        Map<Integer, Integer> histogram = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : trainingMap.entrySet()) {
            int valueSize = entry.getValue().size();
            System.out.printf("%s: %s\n", entry.getKey(), valueSize);
            int numberOfEntriesWithThatSize = histogram.getOrDefault(valueSize, 0);
            numberOfEntriesWithThatSize++;
            histogram.put(valueSize, numberOfEntriesWithThatSize);
        }

        for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
            System.out.printf("Entries with %s prefixes: %s\n", entry.getKey(), entry.getValue());
        }
    }

    public void train(Iterable<String> feeder) {
        for (String line : feeder) {
            trainSingleLine(line);
        }
    }

    private void trainSingleLine(String trainingLine) {
        String[] words = trainingLine.split(WORD_DELIMITER);
        Queue<String> recentWords = new ArrayDeque<>(this.windowSize);

        for (int wordIndex = 0; wordIndex < words.length - 1; wordIndex++) {
            String currentWord = words[wordIndex];
            currentWord = replaceSpecialChars(currentWord);

            LOG.fine("looking at " + currentWord);
            recentWords.add(currentWord); //tail

            LOG.fine("has suff data? " + recentWords.size());
            if (!hasSufficientData(recentWords)) {
                LOG.fine("skip this round");
                continue;
            }

            String prefix = mergeToLowerCasePrefix(recentWords);
            LOG.fine("Looking at prefix: " + prefix);
            List<String> suffixes = trainingMap.getOrDefault(prefix, new ArrayList<>());
            String suffixToAdd = words[wordIndex + 1];
            LOG.fine("looking at suffix to add: " + suffixToAdd);
            suffixes.add(suffixToAdd);
            trainingMap.put(prefix, suffixes);
            LOG.fine("map: " + trainingMap);

            recentWords.remove(); //head
        }
    }

    String replaceSpecialChars(String currentWord) {
        return currentWord
                .replaceAll("&amp;", "&")
                .replaceAll("&gt;", ">")
                .replaceAll("&lt;", "<")
                .replaceAll("\\.\\.\\.", "\\u2026");
    }

    private boolean hasSufficientData(Queue<String> recentWords) {
        int currentPrefixSize = recentWords.size();
        if (currentPrefixSize > this.windowSize) {
            throw new IllegalStateException("Prefix size is too large: " + recentWords);
        }
        return currentPrefixSize == this.windowSize;
    }

    private String mergeToLowerCasePrefix(Queue<String> recentWords) {
        String merged = "";

        for (String word : recentWords) {
            merged += word.toLowerCase() + WORD_DELIMITER;
        }
        return merged.trim();
    }

    public String generate() {
        String prefix = getRandomPrefix();
        String result = prefix;

        int numberOfWordsPerSentence = 32;
        for (int i = 0; i < numberOfWordsPerSentence; i++) {
            String suffix = getRandomSuffix(prefix);
            result += WORD_DELIMITER + suffix;

            if (suffix.equals(EMPTY_RESULT)) {
                break;
            }
            prefix = createNewPrefix(prefix, suffix);
        }
        return result.trim() + SENTENCE_DELIMITER;
    }

    private String createNewPrefix(String prefix, String suffix) {
        String[] prefixes = prefix.split(WORD_DELIMITER);

        Queue<String> prefixesAsQueue = new ArrayDeque<>(Arrays.asList(prefixes));
        prefixesAsQueue.add(suffix); // tail
        prefixesAsQueue.remove(); // head

        return mergeToLowerCasePrefix(prefixesAsQueue);
    }

    private String getRandomSuffix(String prefix) {
        List<String> suffixes = trainingMap.get(prefix);

        if (suffixes == null) {
            return EMPTY_RESULT;
        }
        int index = new Random().nextInt(suffixes.size());
        return suffixes.get(index);
    }

    private String getRandomPrefix() {
        List<String> keys = null;
        int retry = 0;
        while (keys == null) {
            if (++retry >= NULL_SAFE_RETRIES) {
                throw new RuntimeException("Prefix fetching is stuck.");
            }
            keys = new ArrayList<>(trainingMap.keySet());
        }

        int keyIndex = new Random().nextInt(keys.size());
        return keys.get(keyIndex);
    }

    Map<String, List<String>> getTrainingMap() {
        return trainingMap;
    }
}
