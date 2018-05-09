package de.philipppixel.tweetkov.core;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a way to avoid original fragments.
 * <p>
 * Sentences are called <code>duplicates</code> when they consist of transitions that would result in a text that is
 * fully or partially an original tweet. Sentences are called <code>alternatives</code> when they used at least one
 * different tokens than the original tweet.
 * <p>
 * Currentely, after the training there is no way to know which transitions the original sentence in real life took.
 * There is still a high chance to render duplicates that are marked as alternative.
 */
class Sentence {
    private static final String SENTENCE_DELIMITER = ".";
    private final String wordDelimiter;
    private List<String> tokens = new ArrayList<>();
    private List<Boolean> duplicateLedger = new ArrayList<>();

    Sentence(String wordDelimiter) {
        this.wordDelimiter = wordDelimiter;
    }

    /**
     * Returns true if all transitions indicate that there was only one choice to generate this sentence thus rendering
     * a duplicate. If at least one alternative transition was found this sentence counts as alternative.
     * <p>
     * Please note that after the training there is no way to know which transitions the original sentence in real life
     * took. So there is still a high chance to render duplicates that are marked as alternative.
     *
     * @return true if all transitions indicate that there was only one choice to generate this sentence thus rendering
     * a duplicate
     */
    boolean isDuplicate() {
        for (boolean isAlternative : duplicateLedger) {
            if (isAlternative) {
                return false;
            }
        }
        return true;
    }

    void addBridge(String suffix, Transition transition) {
        if (suffix.isEmpty()) {
            return;
        }

        if (tokens.isEmpty()) {
            addStartPrefix(transition.getPrefix());
        }
        this.tokens.add(suffix);

        boolean isAlternative = transition.getUniqueSuffixCount() > 1;
        this.duplicateLedger.add(isAlternative);
    }

    private void addStartPrefix(Prefix prefix) {
        this.tokens.add(prefix.toString());
    }

    /**
     * returns the generated sentence as string, ending with {@link #SENTENCE_DELIMITER}.
     *
     * @return the generated sentence, ending with {@link #SENTENCE_DELIMITER}.
     */
    String create() {
        return toString() + SENTENCE_DELIMITER;
    }

    @Override
    public String toString() {
        String result = "";
        for (String token : tokens) {
            result += token + wordDelimiter;
        }
        return result.trim();
    }
}
