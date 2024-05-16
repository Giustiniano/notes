package com.example.notes.stats;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Token implements Comparable<Token> {
    private String token;
    private int count;


    @Override
    public int compareTo(Token o) {
        return this.count - o.count;
    }
    public void increase(){
        this.count++;
    }
}
