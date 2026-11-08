package de.learnlib.benchmarking.util;

import java.util.Collection;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import lombok.Getter;
import net.automatalib.automaton.concept.SuffixOutput;

@Getter
public class SimCountOracle<M extends SuffixOutput<I, D>, I, D> implements MembershipOracle<I, D> {

    private final M model;
    private int numberOfQueries;
    private int numberOfSymbols;

    public SimCountOracle(M model) {
        this.model = model;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        numberOfQueries += queries.size();

        for (Query<I, D> q : queries) {
            numberOfSymbols += q.length();
            q.answer(model.computeSuffixOutput(q.getPrefix(), q.getSuffix()));
        }
    }
}
