package com.example.notes.stats;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.example.notes.stats.WordCounter.getWordCount;

public class WordCounterTest {
    @Test
    public void testDescendingOrder(){
        String text = "hello Hello world!!";
        Map<String, Integer> wordCount = WordCounter.getWordCount(text);
        assert wordCount.size() == 2;
        assert wordCount.get("hello").equals(2);
        assert wordCount.get("world").equals(1);
        int idx = 0;
        for(Map.Entry<String, Integer> entry : wordCount.entrySet()){
            if(idx == 0){
                assert entry.getKey().equals("hello") && entry.getValue().equals(2);
            }
            if(idx ==1){
                assert entry.getKey().equals("world") && entry.getValue().equals(1);
            }
            idx++;
        }


    }
}
