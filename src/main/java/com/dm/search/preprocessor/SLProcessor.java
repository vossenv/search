package com.dm.search.preprocessor;

import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.util.Assert;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;

public class SLProcessor {

    private static final String KEYWORD_SEARCH = "\\S+\\s*:\\s*\".*?\"|\\S+\\s*:\\s*\\S*";
    private static final String QUOTE_SEARCH = "(?<=\\(|^|\\s)((?<!\\\\)\").+?((?<!\\\\)\")(?=$|\\s|\\))";
    private static final String ESCAPED_CHARS = "\\ + - ! { } [ ] ^ ? /";

    private static final String END_BOOL = "(\\s*(AND|OR|NOT)\\s*)";
    private static final String SKIP_BOOL = String.format("^%s*|%<s*$", END_BOOL);

    @Setter
    private int minFuzzyLen = 3;

    public SLProcessor(int fuzziness) {
        this.minFuzzyLen = fuzziness;
    }

    public SLProcessor() {
    }

    public String format(String originalQuery) throws ParseException {
        Assert.notNull(originalQuery, "Query must not be null");
        String query = originalQuery;
        query = query.replaceAll(SKIP_BOOL, "").trim();
        if (query.isEmpty()) return "*";

        query = newFindEncode(QUOTE_SEARCH, query, false);
        query = newFindEncode(KEYWORD_SEARCH, query, true);
        return decodeQuery(query);
    }

    private String decodeQuery(String query) {

        query = query.replaceAll("\\)", " )");
        StringBuilder sb = new StringBuilder();
        stream(query.split("\\s+")).map(this::addTermSuffix).forEach(sb::append);
        query = decodeString(sb.toString().replaceAll(" \\)", ")"));

        for (String c : ESCAPED_CHARS.split(" ")) {
            query = query.replace(c, "\\" + c);
        }

        return query.trim().replaceAll("\\\\\"", "\\\"");
    }

    private String addTermSuffix(String term) {
     //   if (minFuzzyLen > 0) {
            Matcher m = Pattern.compile("[A-Za-z0-9\"&%#@<>;`_,.](?=$)").matcher(term);
            if (!isBool(term) && m.find() && term.length() > minFuzzyLen) term += "~";
       // }
        return term + " ";
    }

    private String normalizeKeyword(String s) {

        String k = s.split(":")[0].trim();
        if (k.isEmpty() || s.replace(":", "").trim().isEmpty()) {
            return s.trim();
        }
        s = s.replaceFirst("\\s*" + Pattern.quote(k) + "\\s*:\\s*", k + ":").trim();
        return s.replace(Pattern.quote(k), k);
    }

    private String newFindEncode(String regex, String target, boolean isKeyword) throws ParseException {
        Matcher m = Pattern.compile(regex).matcher(target);
        while (m.find()) target = newEncodeTerm(m.group(), target, isKeyword);
        return target;
    }

    private String newEncodeTerm(String term, String target, boolean isKeyword) throws ParseException {
        try {
            String decoded = decodeString(term);
            target = target.replace(term, decoded);
            if (isKeyword) {
                String original = decoded;
                decoded = normalizeKeyword(decoded);
                target = target.replace(original, decoded);
            }
            target = target.replace(decoded, encodeString(decoded));
            return target;
        } catch (Exception e) {
            throw new ParseException("Error encoding term: " + term);
        }
    }

    @SneakyThrows
    private String encodeString(String s) {
        return URLEncoder.encode(s, "UTF-8");
    }

    @SneakyThrows
    private String decodeString(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            throw new ParseException("Error decoding string: " + s);
        }
    }

    private boolean isBool(String s) {
        return s.equals("AND") || s.equals("OR") || s.equals("NOT");
    }
}
