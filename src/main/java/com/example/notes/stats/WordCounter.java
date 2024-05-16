package com.example.notes.stats;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class WordCounter {
    private static Pattern tokenizerRegex = Pattern.compile("\\b\\w+\\b");
    public static Map<String, Integer> getWordCount(String text){
        Map<String, Token> wordCount = new HashMap<>();
        Iterator<MatchResult> results = tokenizerRegex.matcher(text).results().iterator();
        while(results.hasNext()){
            MatchResult matchResult = results.next();
            String token = text.substring(matchResult.start(), matchResult.end()).toLowerCase();
            wordCount.put(token, wordCount.getOrDefault(token, new Token(token, 0)).increase());
        }
        List<Token> sortedTokens = new ArrayList<>(wordCount.values());
        sortedTokens.sort(Collections.reverseOrder());
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for(Token t : sortedTokens){
            sortedMap.put(t.getToken(), t.getCount());
        }
        return sortedMap;


    }
}
