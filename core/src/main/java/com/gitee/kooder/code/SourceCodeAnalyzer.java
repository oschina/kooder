package com.gitee.kooder.code;

import com.gitee.kooder.query.QueryException;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Source code analyzer
 * @author Winter Lau<javayou@gmail.com>
 */
public class SourceCodeAnalyzer extends Analyzer {

    private final static Formatter hl_fmt = new SimpleHTMLFormatter("<em class='highlight'>", "</em>");

    @Override
    protected TokenStreamComponents createComponents(String s) {
        return new TokenStreamComponents(new SourceCodeTokenizer());
    }

    /**
     * extract text content to tokens
     * @param code
     * @return
     */
    public List<String> tokens(String code) {
        List<String> tokens = new ArrayList<>();
        try (TokenStream stream = tokenStream(null, new StringReader(code))){
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                tokens.add(termAtt.toString());
            }
            stream.end();
        } catch (IOException e) {
            throw new QueryException("Failed to tokens: " + code, e);
        }
        return tokens;
    }

    /**
     * Highlight code
     * @param code
     * @param key
     * @return
     * @throws Exception
     */
    public String highlight(String code, String key) {
        try {
            QueryParser parser = new QueryParser(null, this);
            Query query = parser.parse(key);
            QueryScorer scorer = new QueryScorer(query);
            Highlighter hig = new Highlighter(hl_fmt, scorer);
            TokenStream tokens = tokenStream(null, new StringReader(code));
            String[] fragments = hig.getBestFragments(tokens, code, hig.getMaxDocCharsToAnalyze());

            return String.join("", fragments);
        } catch (ParseException e) {
            return this.highlight(code, QueryParser.escape(key));
        } catch (IOException | InvalidTokenOffsetsException e) {
            return code;
        }
    }

    public static void main(String[] args) throws Exception {
        String code = "Select * from osc_users where id = 12342";
        String key = "se2lect";
        System.out.println(new SourceCodeAnalyzer().highlight(code, key));
    }

}