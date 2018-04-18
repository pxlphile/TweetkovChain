package de.philipppixel.tweetkov;

import de.philipppixel.tweetkov.core.TweetkovChain;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * uses a single text file with one line per sentence for training
 */
public class LinekovRunner {
    private static final int NUMBER_OF_SENTENCES = 400;
    private final TweetkovChain app;
    private final String directory;

    public static void main(String[] args) {
        String directory = args[0];
        new LinekovRunner(directory).run();
    }

    public LinekovRunner(String directory) {
        this.directory = directory;
        this.app = new TweetkovChain();
    }

    private void run() {
        try {
            List<String> pureGold = getActs();
            app.train(pureGold);
            makeItWeird();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void makeItWeird() {
//        app.printHistogram();

        for (int i = 0; i < NUMBER_OF_SENTENCES; i++) {
            String sentence = app.generate();
            if (sentence.split(" ").length < 5) {
                i--;
                continue;
            }
            System.out.println(sentence);
        }
    }

    private List<String> getActs() throws FileNotFoundException {
        Scanner sc = new Scanner(new File(directory + "DieZauberfloete.txt"));
        List<String> lines = new ArrayList<>();
        while (sc.hasNextLine()) {
            lines.add(sc.nextLine());
        }

        return lines;
    }
}
