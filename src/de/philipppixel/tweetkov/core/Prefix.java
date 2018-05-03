package de.philipppixel.tweetkov.core;

import java.util.ArrayDeque;
import java.util.Queue;

class Prefix {
    private static final String TOKEN_DELIMITER = " ";
    private final Queue<String> tokens;
    private final int windowSize;

    /**
     * Creates a prefix with the given window size
     *
     * @param windowSize the window size determines the number of tokens by each prefix.
     */
    Prefix(int windowSize) {
        this.tokens = new ArrayDeque<>(windowSize);
        this.windowSize = windowSize;
    }

    /**
     * copy constructor
     *
     * @param prefixToCopy the prefix to be copied.
     */
    private Prefix(Prefix prefixToCopy) {
        this.tokens = new ArrayDeque<>(prefixToCopy.tokens);
        this.windowSize = prefixToCopy.windowSize;
    }

    /**
     * Append a token to this prefix. The token will be trimmed. <code>Null</code> or empty Strings are not allowed.
     * <p>
     * An exception is thrown if the user attempts to add more tokens than the window is wide.
     * An exception is thrown if token is empty.
     *
     * @param token a single part of this prefix, usually a single word  but can be any non-empty string
     */
    void appendToken(String token) {
        if (tokens.size() >= windowSize) {
            throw new IllegalStateException("Cannot append token '" + token + "'. Prefix size is too large: " + tokens);
        }

        tokens.add(token);
    }

    boolean isSmallerThanWindowSize() {
        return this.tokens.size() < this.windowSize;
    }

    /**
     * creates a copy from this prefix but removes the first token and appends the given suffix.
     *
     * @param currentSuffix the current
     * @return a new prefix from the current prefix and the current suffix
     */
    Prefix shiftWithSuffix(String currentSuffix) {
        Prefix copy = new Prefix(this);
        copy.removeHeadToken();
        copy.appendToken(currentSuffix);
        return copy;
    }

    private void removeHeadToken() {
        this.tokens.remove();
    }


    private String mergeToLowerCase() {
        return merge().toLowerCase();
    }

    private String merge() {
        String result = "";

        for (String token : tokens) {
            result += token + TOKEN_DELIMITER;
        }
        return result.trim();
    }

    @Override
    public String toString() {
        return merge();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Prefix prefix = (Prefix) o;

        return mergeToLowerCase().equals(prefix.mergeToLowerCase());
    }

    @Override
    public int hashCode() {
        return mergeToLowerCase().hashCode();
    }
}
