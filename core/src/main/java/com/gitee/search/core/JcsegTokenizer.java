package com.gitee.search.core;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.lionsoul.jcseg.ISegment;
import org.lionsoul.jcseg.IWord;
import org.lionsoul.jcseg.dic.ADictionary;
import org.lionsoul.jcseg.segmenter.SegmenterConfig;

import java.io.IOException;

/**
 * Using jcseg tokenizer
 * @author Winter Lau (javayou@gmail.com)
 */
public class JcsegTokenizer extends Tokenizer {

    private final ISegment segmentor;
    private final CharTermAttributeImpl termAtt = (CharTermAttributeImpl)this.addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = (OffsetAttribute)this.addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = (TypeAttribute)this.addAttribute(TypeAttribute.class);
    private int fieldOffset = 0;

    public JcsegTokenizer(ISegment.Type type, SegmenterConfig config, ADictionary dic) throws IOException {
        this.segmentor = type.factory.create(config, dic);
        this.segmentor.reset(this.input);
    }

    public final boolean incrementToken() throws IOException {
        this.clearAttributes();
        IWord word = this.segmentor.next();
        if (word == null) {
            this.fieldOffset = this.offsetAtt.endOffset();
            return false;
        } else {
            this.termAtt.clear();
            this.termAtt.append(word.getValue());
            this.offsetAtt.setOffset(this.correctOffset(this.fieldOffset + word.getPosition()), this.correctOffset(this.fieldOffset + word.getPosition() + word.getLength()));
            this.typeAtt.setType("word");
            return true;
        }
    }

    public void end() throws IOException {
        super.end();
        this.offsetAtt.setOffset(this.correctOffset(this.fieldOffset), this.correctOffset(this.fieldOffset));
        this.fieldOffset = 0;
    }

    public void reset() throws IOException {
        super.reset();
        this.segmentor.reset(this.input);
    }

}
