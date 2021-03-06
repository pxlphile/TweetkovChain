package de.philipppixel.tweetkov;

import de.philipppixel.tweetkov.core.TweetkovChain;
import de.philipppixel.tweetkov.util.tweetreader.TweetArchiveReader;
import de.philipppixel.tweetkov.util.tweetreader.TweetRefiner;

import java.util.List;

/**
 * uses a (extracted) Tweet archives for training
 */
public class TweetkovRunner {
    private static final int NUMBER_OF_SENTENCES = 400;
    private final String tweetDirectory;
    private final TweetkovChain app;

    public static void main(String[] args) {
        if(args.length == 0) {
            throw new IllegalArgumentException("No tweet archive directory given. Expected something like " +
                    "C:\\YourTwitterHistory\\data\\js\\tweets\\");
        }
        String directory = args[0];
        new TweetkovRunner(directory).run();
    }

    public TweetkovRunner(String directory) {
        tweetDirectory = directory;
        app = new TweetkovChain();
    }

    private void run() {
        TweetArchiveReader tweetArchiveReader = new TweetArchiveReader();

        String[] archives = getTweetArchives();

        for (String file : archives) {
            String pathToArchive = tweetDirectory + file;
            System.out.println("importing " + pathToArchive);
            List<String> tweetMonth = tweetArchiveReader
                    .withArchive(pathToArchive)
                    .getTweetTexts();

            List<String> pureGold = refineTweets(tweetMonth);
            app.train(pureGold);
        }

        makeItWeird();
    }

    private String[] getTweetArchives() {
        return new String[]{
                "2018_03.js",
                "2018_02.js",
                "2018_02.js",
                "2018_01.js",
                "2017_12.js",
                "2017_11.js",
                "2017_10.js",
                "2017_09.js",
                "2017_08.js",
                "2017_07.js",
                "2017_06.js",
                "2017_05.js",
                "2017_04.js",
                "2017_03.js",
                "2017_02.js",
                "2017_01.js",
                "2016_12.js",
                "2016_11.js",
                "2016_10.js",
                "2016_09.js",
                "2016_08.js",
                "2016_07.js",
                "2016_06.js",
                "2016_05.js",
                "2016_04.js",
                "2016_03.js",
                "2016_02.js",
                "2016_01.js",
                "2015_12.js",
                "2015_11.js",
                "2015_10.js",
                "2015_09.js",
                "2015_08.js",
                "2015_07.js",
                "2015_06.js",
                "2015_05.js",
                "2015_04.js",
                "2015_03.js",
                "2015_02.js",
                "2015_01.js"
        };
    }

    private List<String> refineTweets(List<String> fullTweets) {
        return TweetRefiner.process(fullTweets);
    }

    private void makeItWeird() {
//        app.createHistogram();

        for (int i = 0; i < NUMBER_OF_SENTENCES; i++) {
            String sentence = app.generateWithoutDuplicates();
            if(sentence.split(" ").length < 5) {
                i--;
                continue;
            }
            System.out.println(sentence);
        }
    }
}
