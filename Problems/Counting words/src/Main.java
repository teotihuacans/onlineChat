import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;

class MapUtils {

    //public static SortedMap<String, Integer> wordCount(String[] strings) {
    public static SortedMap<String, Long> wordCount(String[] strings) {
        // write your code here

        Comparator<String> c = Comparator.comparing(String::new);
        return Arrays.stream(strings).sorted().collect(Collectors.groupingBy(String::new,
                () -> new TreeMap<>(c), counting()));

        /*return Arrays.stream(strings)
                .collect(Collectors.toMap(s -> s, s -> 1, Integer::sum, TreeMap::new));*/
    }

    //public static void printMap(Map<String, Integer> map) {
    public static void printMap(Map<String, Long> map) {
        // write your code here
        map.forEach((k,v) -> System.out.println(k + " : " + v));
    }

}

/* Do not change code below */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String[] words = scanner.nextLine().split(" ");
        MapUtils.printMap(MapUtils.wordCount(words));
    }
}