import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Candidate {
    List<Integer> supportList;
    Set<Integer> records;

    public Candidate(Set<Integer> records) {
        this.supportList = new LinkedList<>();
        this.supportList.add(records.size());
        this.records = new TreeSet<>(records);
    }
}

class CandidateCompare implements Comparator<Candidate> {
    @Override
    public int compare(Candidate c1, Candidate c2) {

    }
}
