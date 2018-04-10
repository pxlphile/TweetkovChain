package de.philipppixel.tweetkov.util.tweetreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TweetRefiner {
    private static final String URL_PATTERN = "https?://[a-zA-Z./0-9#_\\-+%&]+";

    public static List<String> process(List<String> tweets) {
        List<String> filtered = tweets.stream()
                .filter(TweetRefiner::filterOwn)
                .collect(Collectors.toList());

        List<String> sentences = new ArrayList<>();
        for (String tweet : filtered) {
            List<String> someSentences = refine(tweet);
            sentences.addAll(someSentences);
        }
        return sentences;
    }

    private static List<String> refine(String tweet) {
        tweet = replaceUrls(tweet);
        List<String> myTweetsAsSentences = convertToSentences(tweet);
        return refineSentences(myTweetsAsSentences);
    }

    private static String replaceUrls(String tweet) {
        return tweet.replaceAll(URL_PATTERN, "");
    }

    private static List<String> convertToSentences(String fullTweet) {
        String[] split = fullTweet.split("(\n|/\n|\\. *|! *|\\? *)");

        return new ArrayList<>(Arrays.asList(split));
    }

    private static List<String> refineSentences(List<String> sentences) {
        ArrayList<String> result = new ArrayList<>();
        for (String sentence : sentences) {
            String newSentence = refineSentence(sentence);

            if (newSentence.isEmpty()) {
                continue;
            }

            result.add(sentence);
        }

        return result;
    }

    private static String refineSentence(String sentence) {
        sentence = sentence.replace("*", " * ");
        sentence = sentence.replace("&amp;", "&");
        sentence = sentence.replace("&gt;", ">");
        sentence = sentence.replace("&lt;", "<");
        sentence = sentence.replace(".", " . ");
        sentence = sentence.replace(",", " , ");
        sentence = sentence.replace(":", " : ");
        sentence = sentence.replace("\"", " ");
        sentence = sentence.replace("(", " ");
        sentence = sentence.replace(")", " ");
        sentence = sentence.replace("[", " ");
        sentence = sentence.replace("]", " ");
        return sentence.trim();
    }

    public static boolean filterOwn(String rawTweet) {
        return !(
                rawTweet.trim().isEmpty() ||
                        rawTweet.startsWith("RT ") ||
                        rawTweet.startsWith("@") ||
                        rawTweet.trim().matches(URL_PATTERN)
        );
    }
}
