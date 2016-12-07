import java.util.BitSet;
import java.util.Comparator;

public class BitSetComp implements Comparator<BitSet> {
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
