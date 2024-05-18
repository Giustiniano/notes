package com.example.notes.stats;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Token implements Comparable<Token> {
    private String token;
    private int count = 0;

    public Token(String token){
        this.token = token;
    }

    @Override
    public int compareTo(Token o) {
        return this.count - o.count;
    }
    public Token increase(){
        this.count++;
        return this;
    }
}
