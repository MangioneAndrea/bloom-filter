import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import com.google.common.hash.*;

public class Main {
    public static class Bloom {
        // number of elements to be inserted
        int n = 60000;
        // false positive probability
        public double p = 0.01;
        // size of bit array
        int m = (int) Math.ceil(-n * Math.log(p) / (Math.log(2) * Math.log(2)));
        // number of hash functions
        int k = (int) Math.ceil(-Math.log(p) * Math.log(2));

        Set<Integer> hashCodes = new HashSet<>();

        public Bloom(List<String> words) {
            words.forEach(word -> {
                for (int i = 0; i < k; i++) {
                    hashCodes.add(Hashing.murmur3_32(i).hashString(word, Charset.defaultCharset()).asInt() % m);
                }
            });
        }

        public boolean contains(String word) {
            return IntStream.range(0, k).allMatch(i -> hashCodes.contains(
                    Hashing.murmur3_32(i).hashString(word, Charset.defaultCharset()).asInt() % m));
        }

    }

    public static void main(String[] args) throws IOException {
        // read words. txt into array of strings
        var words = Files.readAllLines(Paths.get("words.txt"));
        var bloom = new Main.Bloom(words);

        // invert words and remove palindromes --> all words are not contained
        var wrongWords = words.stream().map(word -> new StringBuilder(word).reverse().toString())
                .filter(w -> !words.contains(w)).toList();
        var totalWords = wrongWords.size();

        var falsePositives = wrongWords.stream().filter(word -> bloom.contains(word)).count();

        DecimalFormat numberFormat = new DecimalFormat("#.0000000");
        System.out.println("False positives: " + falsePositives + " out of " + totalWords);
        System.out.println("Expected probability: " + bloom.p);
        System.out.println("Actual probability: 0"
                + numberFormat.format((double) falsePositives / (double) totalWords));

    }
}
