import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Main {

    private static double minSupp;
    private static double minConf;
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
        Map<BitSet, Candidate> prev = new TreeMap<>(new BitSetCompare());

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
                if (currentEntry.getValue().records.size() < minSupp * dataCount ||
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

            Map<BitSet, Candidate> curr = new TreeMap<>(new BitSetCompare());
            for (Map.Entry<BitSet, Candidate> prevEntry : prev.entrySet()) {
                // Augment includes every bits in prevBits except for the current bit
                BitSet augment = (BitSet) prevEntry.getKey().clone();
                augment.xor(prevBits);

                // For every "true" bit, update the corresponding Candidate
                for (int j = augment.nextSetBit(0); j >= 0; j = augment.nextSetBit(j + 1)) {
                    BitSet next = (BitSet) prevEntry.getKey().clone();
                    next.set(j);
                    Candidate candidate = curr.get(next);
                    if (candidate == null) {
                        candidate = new Candidate(prevEntry.getValue().records);
                    } else {
                        candidate.records.retainAll(prevEntry.getValue().records);
                        candidate.supportList.add(prevEntry.getValue().records.size());
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
        while (true) {
            output.append(attributeMap.get(i));
            i = bs.nextSetBit(i + 1);
            if (i < 0) {
                break;
            }
            output.append(",");
        }
        output.append("]");
    }

    private static void pirntFrequentItemsets(List<Map<BitSet, Candidate>> highFreqItems, StringBuilder output) {
        output.append("==Frequent itemsets (min_sup=");
        output.append((int) (minSupp * 100));
        output.append("%)\n");

        Map<Candidate, BitSet> printMap = new TreeMap<>(new CandidateCompare());
        for (Map<BitSet, Candidate> currentCountMap : highFreqItems) {
            for (Map.Entry<BitSet, Candidate> currentEntry : currentCountMap.entrySet()) {
                printMap.put(currentEntry.getValue(), currentEntry.getKey());
            }
        }

        for (Map.Entry<Candidate, BitSet> currentEntry : printMap.entrySet()) {
            printCandidate(currentEntry.getValue(), output);
            output.append(", ");
            output.append((int) (currentEntry.getKey().records.size() * 100.0 / dataCount)); // Calculating the support
            output.append("%\n");
        }
        output.append("\n");
    }

    private static void printAssocRules(List<Map<BitSet, Candidate>> highFreqItems, StringBuilder output) {
        output.append("==High-confidence association rules (min_conf=");
        output.append((int) (minConf * 100));
        output.append("%)\n");

        Map<List<Object>, Double> printMap = new TreeMap<>(new BitSetConfidenceCompare());
        for (Map<BitSet, Candidate> currentCountMap : highFreqItems) {
            for (Map.Entry<BitSet, Candidate> currentEntry : currentCountMap.entrySet()) {
                if (currentEntry.getValue().supportList.size() > 1) {
                    int i = -1;
                    for (int currentSupport : currentEntry.getValue().supportList) {
                        i = currentEntry.getKey().nextSetBit(i + 1);
                        BitSet prevBits = (BitSet) currentEntry.getKey().clone();
                        prevBits.clear(i);
                        double currentConfidence = (double) currentEntry.getValue().records.size() / currentSupport;
                        if (currentConfidence >= minConf) {
                            List<Object> printList = new ArrayList<>();
                            printList.add(prevBits);
                            printList.add(attributeMap.get(i));
                            printList.add(currentConfidence);
                            printMap.put(printList, (double) currentEntry.getValue().records.size() / dataCount);
                        }
                    }
                }
            }
        }

        for (Map.Entry<List<Object>, Double> currentEntry : printMap.entrySet()) {
            printCandidate((BitSet) currentEntry.getKey().get(0), output);
            output.append(" => [");
            output.append((String) currentEntry.getKey().get(1));
            output.append("](Conf: ");
            output.append((int) ((double) currentEntry.getKey().get(2) * 100));
            output.append("%, Supp: ");
            output.append((int) (currentEntry.getValue() * 100));
            output.append("%)\n");
        }
    }

    public static void main(String[] args) throws Exception {

        // Validate the input parameters
        if (args.length != 3) {
            throw new IllegalArgumentException("Input Arguments Wrong!");
        }

        // Parse parameters
        minSupp = Double.valueOf(args[1]);
        minConf = Double.valueOf(args[2]);

        // Get the attributes of the data file
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(new File("./data/" + args[0])));
        } catch (Exception e) {
            br = new BufferedReader(new FileReader(new File(args[0])));
        }
        String[] attributes = br.readLine().split(",");
        addAttributes(attributes);

        // Add all records from the data file
        String line;
        while ((line = br.readLine()) != null) {
            addRecords(dataCount, line);
            dataCount++;
        }

        // Obtain high frequent items by Apriori Algorithm
        List<Map<BitSet, Candidate>> highFreqItems = apriori(recordMap);

        // Print results to the StringBuilder
        StringBuilder output = new StringBuilder();
        pirntFrequentItemsets(highFreqItems, output);
        printAssocRules(highFreqItems, output);

        // Outputs
        System.out.print(output.toString());
        try (PrintWriter out = new PrintWriter("output.txt")) {
            out.println(output.toString());
        }
    }
}
