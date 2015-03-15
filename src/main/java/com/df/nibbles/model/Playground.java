package com.df.nibbles.model;

import com.df.nibbles.api.Config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Playground {
    public boolean isPaused = false;
    public Object[][] matrix;
    public Config config;
    public Map<String, Worm> userIdsMap = new ConcurrentHashMap<>();
    public boolean isStarted;

    public Object get(int x, int y) {
        return this.matrix[x][y];
    }

    public void set(int x, int y, Object v) {
        this.matrix[x][y] = v;
        this.draw(x, y, v);
    }

    protected abstract void draw(int x, int y, Object v);

    public List<Worm> worms = new CopyOnWriteArrayList<>();

    public void broadcast(String message) {
        this.pause();
        this.gameOver = message;
        this.gameOverCallback();
    }

    public String gameOver = null;

    public abstract void gameOverCallback();

    public abstract void livesCallback(int number, int lives);

    public abstract void scoreCallback(int number, int score);

    public Point randomLoc() {
        int x, y;
        do {
            x = 3 + (int) (Math.random() * (this.matrix.length - 6));
            y = 3 + (int) (Math.random() * (this.matrix[0].length - 6));
        } while (this.get(x, y) != null);
        return new Point(x, y);
    }

    public char randomDir() {
        switch ((int) (Math.random() * 4)) {
            case 0:
                return 'l';
            case 1:
                return 'u';
            case 2:
                return 'r';
            case 3:
                return 'd';
            default:
                return 'd';
        }
    }

    public Food food = new Food();

    public int eat(Food food) {
        food.isEaten = true;
        this.set(food.loc.x, food.loc.y, null);
        return food.food;
    }

    public void tick() {
        if (this.food.isEaten) {
            this.food.isEaten = false;
            this.food.food++;
            Point loc = this.randomLoc();
            this.food.loc = loc;
            this.set(loc.x, loc.y, this.food);
        }
        for (Worm worm : this.worms) {
            if (!worm.alive) {
                this.die(worm);
            } else {
                worm.step();
            }
        }
    }

    public void die(Worm worm) {
        int n = this.worms.size();
                /* if (punish && this.food.food > 1) {
					this.food.food--;
					this.draw(this.food.loc.x, this.food.loc.y, this.get(this.food.loc.x, this.food.loc.y));
				} */
        this.worms.remove(worm);
        if (worm.lives > 0) {
            worm.lives--;
            this.livesCallback(worm.number, worm.lives);
            positionWorm(worm);
            this.worms.add(worm.init());
        } else {
            if (n == 1) {
                this.broadcast("Level: " + this.food.food + " Score: " + worm.score);
            } else if (this.worms.size() == 1) {
                this.broadcast(this.worms.get(0).name + " Wins! level: " +
                        this.food.food + " score: " + this.worms.get(0).score);
            }
        }
    }

    public void positionWorm(Worm worm) {
        if (worm.head.x == null) {
            switch (worm.number) {
                case 1:
                    worm.head = new Part(config.size.x / 4, config.size.y / 2, 'r');
                    break;
                case 2:
                    worm.head = new Part(3 * config.size.x / 4, config.size.y / 2, 'l');
                    break;
                case 3:
                    worm.head = new Part(config.size.x / 2, config.size.y / 4, 'd');
                    break;
                case 4:
                    worm.head = new Part(config.size.x / 2, 3 * config.size.y / 4, 'u');
                    break;
            }
        } else {
            Point loc = randomLoc();
            worm.head = new Part(loc.x, loc.y, randomDir());
        }
        set(worm.head.x, worm.head.y, worm.number);
    }

    public void addWorm(int n, String name, String userId) {
        assert n > 0 && n < 5;
        Worm worm = new Worm(this, n, name, config.lives) {
            @Override
            protected void scoreCallback(int number, int score) {
                Playground.this.scoreCallback(number, score);
            }
        };
        this.worms.add(worm);
        userIdsMap.put(userId, worm);
    }

    public Playground init(Config config) {
        this.matrix = new Object[config.size.x][config.size.y];
        this.config = config;

        for (int i = 0; i < config.size.x; i++) {
            this.set(i, 0, WALL);
            this.set(i, config.size.y - 1, WALL);
        }
        for (int i = 0; i < config.size.y; i++) {
            this.set(0, i, WALL);
            this.set(config.size.x - 1, i, WALL);
        }

        this.tick();

        return this;
    }

    public void start() {
        this.isStarted = true;
        for (Worm worm : worms) {
            positionWorm(worm);
            worm.init();
        }
        this.tick();
        resume();
    }

//    protected Map<Integer, char[]> keyMap = map(
//            i(1037), new char[]{'l', 'r'}, i(1038), new char[]{'u', 'd'}, i(1039), new char[]{'r', 'l'}, i(1040), new char[]{'d', 'u'},
//            i(2065), new char[]{'l', 'r'}, i(2087), new char[]{'u', 'd'}, i(2068), new char[]{'r', 'l'}, i(2083), new char[]{'d', 'u'},
//            i(3071), new char[]{'l', 'r'}, i(3089), new char[]{'u', 'd'}, i(3074), new char[]{'r', 'l'}, i(3072), new char[]{'d', 'u'},
//            i(4076), new char[]{'l', 'r'}, i(4080), new char[]{'u', 'd'}, i(4222), new char[]{'r', 'l'}, i(4186), new char[]{'d', 'u'});

    public void resume() {
        this.isPaused = false;
        interval = new Timer();
        interval.schedule(new TimerTask() {
            @Override
            public void run() {

            }
        }, 1000 / config.speed);
    }

    public void pause() {
        this.isPaused = true;
        this.interval.cancel();
        this.interval = null;
    }

    public Timer interval;
    public static Object WALL = new Object();


    public void ready(Worm worm) {
        worm.ready = true;
        if (!isStarted) {
            for (Worm w : worms) {
                if (!w.ready) {
                    return;
                }
            }
        }
        start();
    }
}
