package com.kyle.random.matches;

public class Match {
   public String userId;
    public Integer matchRatio;
    public boolean blocked;

    public Match(String userId, int matchRatio, boolean blocked) {
        this.userId = userId;
        this.matchRatio = matchRatio;
        this.blocked = blocked;

    }
}
