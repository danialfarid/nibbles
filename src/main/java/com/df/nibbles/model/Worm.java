package com.df.nibbles.model;

import java.util.Queue;

public abstract class Worm {
    public int number;
    public Part head = new Part();
    public Part tail = new Part();
    public Queue<Character> turns;
    Playground playground;
    public boolean ready = false;

    public Worm(Playground playground, int number, String name, int lives) {
        this.playground = playground;
        this.number = number;
        this.name = name;
        this.lives = lives;
    }

    public void turn(char d) {
        if ((this.turns.isEmpty() || this.turns.peek() != d) && !isOpposite(d, this.head.d)) {
            this.turns.add(d);
        }
    }

    public boolean isOpposite(char d, char d1) {
        switch (d) {
            case 'l': return d1 != 'r';
            case 'r': return d1 != 'l';
            case 'd': return d1 != 'u';
            case 'u': return d1 != 'd';
        }
        return true;
    }

    public Worm init() {
        this.tail.next = this.head;
        this.length = 3;
        this.grow = 1;
        this.alive = true;
        this.step();
        this.step();
        return this;
    }

    public boolean alive = true;
    public String name;
    public int grow = 1;
    public int lives;
    public int score = 0;
    public int length =3;
    public void step() {
        Part newHead = null;
        Character d = this.turns.poll();
        if (d == null) {
            d = this.head.d;
        }
        switch(d) {
            case 'r': newHead = new Part(this.head.x + 1, this.head.y, 'r');
                break;
            case 'l': newHead = new Part(this.head.x - 1, this.head.y, 'l');
                break;
            case 'd': newHead = new Part(this.head.x, this.head.y + 1, 'd');
                break;
            case 'u': newHead = new Part(this.head.x, this.head.y - 1, 'u');
                break;
        }
        this.head.next = newHead;
        this.head = newHead;
        this.eat(this.head.x, this.head.y);
        if (this.alive) {
            playground.set(this.head.x, this.head.y, this);
            if (this.grow > 0) {
                this.grow--;
            } else {
                if (this.tail.x != null) {
                    playground.set(this.tail.x, this.tail.y, null);
                }
                this.tail = this.tail.next;
            }
        }
    }
    public void eat(int x, int y) {
        Object v = playground.get(x, y);
        if (v != null) {
            if (v == Playground.WALL) {
                this.die();
            } else if (v instanceof Worm) {
                Worm w = (Worm) v;
                if (w.head.x == x && w.head.y == y) {
                    w.die();
                } else {
                    if (w.alive) {
                        w.digest(this.length);
                    }
                }
                this.die();
            } else if (v instanceof Food) {
                int f = playground.eat((Food) v);
                this.digest(f);
                this.length += f;
            } else if (v instanceof Life) {
                this.lives += ((Life)v).count;
            }
        }
    }
    public void digest(int v) {
        this.grow += v;
        this.score += v;
        this.scoreCallback(this.number, this.score);
    }

    protected abstract void scoreCallback(int number, int score);

    public void die() {
        this.alive = false;
        this.score = Math.max(this.score - 10, 0);
        this.scoreCallback(this.number, this.score);
        while (this.tail != this.head) {
            if (this.tail.x != null) {
                playground.set(this.tail.x, this.tail.y, null);
            }
            this.tail = this.tail.next;
        }
        if (playground.get(this.head.x, this.head.y) == this) {
            playground.set(this.head.x, this.head.y, null);
        }
    }
}
