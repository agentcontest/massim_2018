package massim.util;

import java.util.Comparator;

/**
 * Comparator for comparing strings by length first, then lexicographically. (for names with attached numbers)
 */
public class NameComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        if (o1.length() > o2.length()) return 1;
        else if (o1.length() < o2.length()) return -1;
        return o1.compareTo(o2);
    }
}
