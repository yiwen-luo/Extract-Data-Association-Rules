import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Main {

    private static double supp;
    private static double conf;
    private static int dataCount = 0;
    private static Map<Integer, String> attributeMap = new TreeMap<>();
    private static Map<Integer, Set<Integer>> recordMap = new TreeMap<>();

    private static void addAttributes(String[] attrs) {
        for (int i = 0, j = attrs.length; i < j; ++i) {
            attributeMap.put(i, attrs[i]);
        }
    }

    private static void addRecords(int lineNum, String line) {
        String[] items = line.split(",");
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals("Y")) {
                Set<Integer> records;
                if (recordMap.containsKey(i)) {
                    records = recordMap.get(i);
                } else {
                    records = new TreeSet<>();
                }
                records.add(lineNum);
                recordMap.put(i, records);
            }
        }
    }

    private static List<Map<BitSet, Candidate>> apriori(Map<Integer, Set<Integer>> recordMap) {
        List<Map<BitSet, Candidate>> result = new ArrayList<>();
        Map<BitSet, Candidate> prev = new TreeMap<>(new BitSetComp());

        // Reformatting recordMap using BitSet
        for (Map.Entry<Integer, Set<Integer>> currentEntry : recordMap.entrySet()) {
            BitSet bs = new BitSet(attributeMap.size());
            bs.set(currentEntry.getKey());
            prev.put(bs, new Candidate(currentEntry.getValue()));
        }

        for (int candidateCount = 1; candidateCount <= attributeMap.size(); candidateCount++) {
            // If no more qualified data in this and higher levels, end the loop
            if (prev.size() == 0) {
                break;
            }

            // Remove unqualified entries
            Iterator<Map.Entry<BitSet, Candidate>> prevIter = prev.entrySet().iterator();
            while (prevIter.hasNext()) {
                Map.Entry<BitSet, Candidate> currentEntry = prevIter.next();
                if (currentEntry.getValue().records.size() < supp * dataCount ||
                        currentEntry.getValue().supportList.size() < candidateCount) {
                    prevIter.remove();
                }
            }
            result.add(prev);

            // Get the remaining qualified bits in one Bitset
            BitSet prevBits = new BitSet(attributeMap.size());
            for (Map.Entry<BitSet, Candidate> currentEntry : prev.entrySet()) {
                prevBits.or(currentEntry.getKey());
            }

            Map<BitSet, Candidate> curr = new TreeMap<>(new BitSetComp());
            for (Map.Entry<BitSet, Candidate> currentEntry : prev.entrySet()) {
                // Augment includes every bits in prevBits except for the current bit
                BitSet augment = (BitSet) currentEntry.getKey().clone();
                augment.xor(prevBits);

                // For every "true" bit, update the corresponding Candidate
                for (int j = augment.nextSetBit(0); j >= 0; j = augment.nextSetBit(j + 1)) {
                    BitSet next = (BitSet) currentEntry.getKey().clone();
                    next.set(j);
                    Candidate candidate = curr.get(next);
                    if (candidate == null) {
                        candidate = new Candidate(currentEntry.getValue().records);
                    } else {
                        candidate.records.retainAll(currentEntry.getValue().records);
                        candidate.supportList.add(currentEntry.getValue().records.size());
                    }
                    curr.put(next, candidate);
                }
            }
            prev = curr;
        }
        return result;
    }

    private static void printCandidate(BitSet bs, StringBuilder output) {
        output.append("[");
        int i = bs.nextSetBit(0);
        for (; ; ) {
            output.append(attributeMap.get(i));
            i = bs.nextSetBit(i + 1);
            if (i < 0) {
                break;
            }
            output.append(",");
        }
        output.append("], ");
    }

    private static void printCandidate(int b, PrintStream ps) {
        ps.printf("[%s]", attributeMap.get(b));
    }

    private static void pirntFrequentItemsets(List<Map<BitSet, Candidate>> highFreqItems, StringBuilder output) {
        output.append("==Frequent itemsets (min_sup=");
        output.append((int) (supp * 100));
        output.append("%)\n");

        Map<Candidate, BitSet> printMap = new TreeMap<>();
        for (Map<BitSet, Candidate> currentCountMap : highFreqItems) {
            for (Map.Entry<BitSet, Candidate> currentEntry : currentCountMap.entrySet()) {


                printCandidate(currentEntry.getKey(), output);
                System.out.printf(", %d%%\n", (int) ((double) currentEntry.getValue().records.size() / dataCount * 100));
            }
        }
        System.out.println();
    }

    public static void main(String[] args) throws Exception {

        // Validate the input parameters
        if (args.length != 3) {
            throw new IllegalArgumentException("Input Arguments Wrong!");
        }

        // Parse parameters
        supp = Double.valueOf(args[1]);
        conf = Double.valueOf(args[2]);

        // Get the attributes of the data file
        BufferedReader br = new BufferedReader(new FileReader(new File("./data/" + args[0])));
        String[] attributes = br.readLine().split(",");
        addAttributes(attributes);

        // Add all records from the data file
        String line;
        while ((line = br.readLine()) != null) {
            addRecords(dataCount, line);
            dataCount++;
        }

        List<Map<BitSet, Candidate>> highFreqItems = apriori(recordMap);

        StringBuilder output = new StringBuilder();
        pirntFrequentItemsets(highFreqItems, output);


        System.out.printf("==High-confidence association rules (min_conf=%d%%)\n", (int) (conf * 100));
        int level = 0;
        for (Map<BitSet, Candidate> e : highFreqItems) {
            for (Map.Entry<BitSet, Candidate> f : e.entrySet()) {
                assert (f.getValue().supportList.size() == f.getKey().cardinality());
                assert (f.getValue().supportList.size() == level + 1);
                if (f.getValue().supportList.size() > 1) {
                    int i = -1;
                    Iterator<Integer> j;
                    for (j = f.getValue().supportList.iterator(); j.hasNext(); ) {
                        i = f.getKey().nextSetBit(i + 1);
                        int prevsupp = j.next();
                        BitSet prevbits = (BitSet) f.getKey().clone();
                        prevbits.clear(i);
                        assert (highFreqItems.get(level - 1).get(prevbits).records.size() == prevsupp);
                        if ((double) f.getValue().records.size() / prevsupp >= conf) {
                            printCandidate(prevbits, output);
                            System.out.print(" => ");
                            printCandidate(i, System.out);
                            System.out.printf("(Conf: %d%%, Supp: %d%%)\n",
                                    (int) ((double) f.getValue().records.size() / prevsupp * 100),
                                    (int) ((double) f.getValue().records.size() / dataCount * 100));
                        }
                    }
                }
            }
            ++level;
        }
    }


}
