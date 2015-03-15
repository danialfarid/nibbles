package com.df.nibbles.model;

public class Part {
    public Integer x;
    public Integer y;
    public char d;
    public Part next;

    public Part(int x, int y, char d) {
        this.x = x;
        this.y = y;
        this.d = d;
    }

    public Part() {

    }
}
