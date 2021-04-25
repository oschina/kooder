package com.gitee.kooder.code;

import com.gitee.kooder.query.QueryException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Source code analyzer
 * @author Winter Lau<javayou@gmail.com>
 */
public class SourceCodeAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String s) {
        return new TokenStreamComponents(new SourceCodeTokenizer());
    }

    /**
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

}
