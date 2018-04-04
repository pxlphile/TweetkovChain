package de.philipppixel.markov;

import java.util.*;
import java.util.logging.Logger;

/**
 * A sliding window markov chain text generator.
 * <p>
 * <code>first second third.</code>: A window size of two takes two subsequent words and maps the following of it:
 * "first second" -&gt; third. "first second" is called a <code>prefix</code> while "third" is the <code>suffix</code>.
 *
 * Multiple occurrences of the same word possible and desired because the selection
 * probability rises later on.
 *
 * What is
 */
public class Markov2 {
    private static final int WINDOW_SIZE = 2;
    private static final int NULL_SAFE_RETRIES = 5;
    private static final int NUMBER_OF_SENTENCES = 150;
    private static final String SENTENCE_DELIMITER = ".";
    private static final String WORD_DELIMITER = " ";
    private static final String EMPTY_RESULT = "";
    private static final Logger LOG = Logger.getLogger(Markov2.class.getName());

    private Map<String, List<String>> trainingMap = new HashMap<>();

    public static void main(String[] args) {
        String filePath = args[0];
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Please call with absolute file path as only argument");
        }

        Markov2 app = new Markov2();
        LineFeeder feeder = new ListFeeder(filePath);
        app.train(feeder);

        LOG.info("============ histogram " + app.trainingMap);

        for (int i = 0; i < NUMBER_OF_SENTENCES; i++) {
            System.out.println(app.generate());
        }
    }

    public void train(LineFeeder feeder) {
        for (String line : feeder) {
            trainSingleLine(line.toLowerCase());
        }
    }

    private void trainSingleLine(String trainingLine) {
        String[] words = trainingLine.split(WORD_DELIMITER);
        Queue<String> recentWords = new ArrayDeque<>(WINDOW_SIZE);

        for (int wordIndex = 0; wordIndex < words.length - 1; wordIndex++) {
            String currentWord = words[wordIndex];
            LOG.info("looking at " + currentWord);
            recentWords.add(currentWord); //tail

            LOG.info("has suff data? " + recentWords.size());
            if (!hasSufficientData(recentWords)) {
                LOG.info("skip this round");
                continue;
            }

            String prefix = mergePrefix(recentWords);
            LOG.info("Looking at prefix: " + prefix);
            List<String> suffixes = trainingMap.getOrDefault(prefix, new ArrayList<>());
            String suffixToAdd = words[wordIndex + 1];
            LOG.info("looking at suffix to add: " + suffixToAdd);
            suffixes.add(suffixToAdd);
            trainingMap.put(prefix, suffixes);
            LOG.info("map: " + trainingMap);

            recentWords.remove(); //head
        }
    }

    private boolean hasSufficientData(Queue<String> recentWords) {
        int currentPrefixSize = recentWords.size();
        if (currentPrefixSize > WINDOW_SIZE) {
            throw new IllegalStateException("Prefix size is too large: " + recentWords);
        }
        return currentPrefixSize == WINDOW_SIZE;
    }

    private String mergePrefix(Queue<String> recentWords) {
        String merged = "";

        for (String word : recentWords) {
            merged += word + WORD_DELIMITER;
        }
        return merged.trim();
    }

    String generate() {
        String prefix = getRandomPrefix();
        String result = prefix;

        int numberOfWordsPerSentence = 16;
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

        return mergePrefix(prefixesAsQueue);
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
