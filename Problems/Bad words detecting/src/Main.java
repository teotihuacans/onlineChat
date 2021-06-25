import java.util.*;
import java.util.stream.*;

class BadWordsDetector {

    private static Stream<String> createBadWordsDetectingStream(String text, 
                                                                List<String> badWords) {
        // write your code here
        List<String> tlst = List.of(text.split("\\s"));
        /*return Stream.of(text.split("\\s+")).sorted().filter(badWords::contains)
                .collect(Collectors.toSet()).stream();*/
        return badWords.stream().filter(tlst::contains).sorted(Comparator.comparing(String::new));
    }

    /* Do not change the code below */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String[] parts = scanner.nextLine().split(";");

        // the first part is a text
        String text = parts[0];

        // the second part is a bad words dictionary
        List<String> dict = parts.length > 1 ?
                Arrays.asList(parts[1].split(" ")) :
                Collections.singletonList("");

        System.out.println(createBadWordsDetectingStream(text, dict).collect(Collectors.toList()));
    }

}