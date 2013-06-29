package org.wikapidia.download;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.wikapidia.core.lang.Language;

import java.util.*;

/**
 *
 * @author Ari Weiland
 *
 * This class wraps a complex map of DumpLinkInfo, and provides iteration by language
 * so that during the download process, downloads proceed one language at a time.
 * The Iterator returns Multimaps of LinkMatchers mapped to DumpLinkInfo so that
 * the downloads can be additionally clustered by LinkMatcher.
 *
 */
public class DumpLinkCluster implements Iterable<Multimap<LinkMatcher, DumpLinkInfo>> {

    private final Map<Language, Multimap<LinkMatcher, DumpLinkInfo>> links = new HashMap<Language, Multimap<LinkMatcher, DumpLinkInfo>>();

    public DumpLinkCluster() {}

    public void add(DumpLinkInfo link) {
        Language language = link.getLanguage();
        if (!links.containsKey(language)) {
            Multimap<LinkMatcher, DumpLinkInfo> temp = HashMultimap.create();
            links.put(language, temp);
        }
        links.get(language).put(link.getLinkMatcher(), link);
    }

    public Multimap<LinkMatcher, DumpLinkInfo> get(Language language) {
        return links.get(language);
    }

    public int size() {
        int counter = 0;
        for (Multimap map : this) {
            counter += map.size();
        }
        return counter;
    }

    @Override
    public Iterator<Multimap<LinkMatcher, DumpLinkInfo>> iterator() {
        return links.values().iterator();
    }
}