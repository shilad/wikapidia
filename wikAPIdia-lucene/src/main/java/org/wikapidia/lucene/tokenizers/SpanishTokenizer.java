package org.wikapidia.lucene.tokenizers;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.es.SpanishLightStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.wikapidia.core.lang.Language;
import org.wikapidia.lucene.TokenizerOptions;

import java.io.Reader;

/**
 * @author Ari Weiland
 */
public class SpanishTokenizer extends LanguageTokenizer {

    protected SpanishTokenizer(Version version, TokenizerOptions options, Language language) {
        super(version, options, language);
    }

    @Override
    public TokenStream getTokenStream(Reader reader, CharArraySet stemExclusionSet) {
        TokenStream stream = setTokenizer(reader);
        stream = new StandardFilter(matchVersion, stream);
        if (caseInsensitive)
            stream = new LowerCaseFilter(matchVersion, stream);
        if (useStopWords)
            stream = new StopFilter(matchVersion, stream, SpanishAnalyzer.getDefaultStopSet());
        if (useStem) {
            if (!stemExclusionSet.isEmpty())
                stream = new SetKeywordMarkerFilter(stream, stemExclusionSet);
            stream = new SpanishLightStemFilter(stream);
        }
        return stream;
    }
}