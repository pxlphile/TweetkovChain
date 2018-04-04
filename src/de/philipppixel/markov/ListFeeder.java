package de.philipppixel.markov;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListFeeder implements LineFeeder {
    private String filePath;

    public ListFeeder(String filePath) {
        if (filePath == null || filePath.isEmpty()) throw new IllegalArgumentException("filePath must not be empty.");
        this.filePath = filePath;
    }

    private List<String> readFile() {
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Exception during reading file " + filePath, e);
        }
    }

    @Override
    public Iterator<String> iterator() {
        return readFile().iterator();
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        readFile().forEach(action);
    }

    @Override
    public Spliterator<String> spliterator() {
        return readFile().spliterator();
    }
}
