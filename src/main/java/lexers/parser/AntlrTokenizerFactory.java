/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package lexers.parser;

import core.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AntlrTokenizerFactory implements Analysis.TokenizerFactory {

	private static final Logger log = LogManager
			.getLogger(AntlrTokenizerFactory.class);

	static HashMap<String, AntlrTokenizer> tokenizers;

	static void initTokenizers() {
        if (tokenizers != null) return;

        tokenizers = new HashMap<>();
        AntlrTokenizer java = new AntlrTokenizer("lexers.Java", "compilationUnit");
        tokenizers.put("java", java);
        AntlrTokenizer cpp14 = new AntlrTokenizer("lexers.CPP14", "translationunit");
        tokenizers.put("(c|cpp|cxx|h)", cpp14);
    }

	@Override
    public Tokenizer getTokenizerFor(Submission[] subs) {
        initTokenizers();

        HashMap<Tokenizer, Integer> votes = new HashMap<>();
        Tokenizer empty = new NullTokenizer();
        votes.put(empty, 0);
        Tokenizer best = empty;
        for (Submission sub : subs) {
            for (int i = sub.getSources().size()-1; i >= 0; i-- ) {
                String name = sub.getSourceName(i);
                String suffix = name.substring(name.lastIndexOf('.')+1);
                boolean found = false;
                for (Map.Entry<String, AntlrTokenizer> te : tokenizers.entrySet()) {
                    Tokenizer current = te.getValue();
                    if (suffix.matches(te.getKey())) {
                        found = true;
                        Integer v = votes.get(current);
                        int t = (v == null) ? 1 : v+1;
                        votes.put(current, t);
                        if (t > votes.get(best)) {
                            best = current;
                            log.debug("best is " + best + " with " + t);
                        }
                    }
                }
                if ( ! found) {
                    int t = votes.get(empty) + 1;
                    votes.put(empty, t);
                    if (t > votes.get(best)) {
                        best = empty;
                        log.debug("best is " + best + " with " + t);
                    }
                }
            }
        }
        log.info("chosen tokenizer: " + best + " with " + votes.get(best));
        return best;
    }
}
