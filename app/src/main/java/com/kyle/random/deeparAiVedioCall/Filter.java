package com.kyle.random.deeparAiVedioCall;

public class Filter {
    String key;
    String name;
    String imageFilter;
    boolean applied;
    public Filter(String key, String name, String imageFilter, boolean applied) {
        this.key = key;
        this.name = name;
        this.imageFilter = imageFilter;
        this.applied = applied;
    }
}
