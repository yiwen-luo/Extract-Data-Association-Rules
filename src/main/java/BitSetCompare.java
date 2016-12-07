import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

public class BitSetCompare implements Comparator<BitSet> {
    @Override
    public int compare(BitSet o1, BitSet o2) {
        assert (o1.size() == o2.size());
        int bit = 0, bit1, bit2;
        do {
            bit1 = o1.nextSetBit(bit);
            bit2 = o2.nextSetBit(bit);
            if (bit1 != bit2) {
                break;
            }
            bit = bit1 + 1;
        } while (bit1 != -1);
        return bit1 - bit2;
    }
}

class BitSetConfidenceCompare implements Comparator<List<Object>> {
    @Override
    public int compare(List<Object> list1, List<Object> list2) {
        if ((double) list2.get(2) - (double) list1.get(2) > 0) return 1;
        else if ((double) list2.get(2) - (double) list1.get(2) < 0) return -1;
        else return 0;
    }
}
