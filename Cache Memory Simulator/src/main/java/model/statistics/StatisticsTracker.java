package model.statistics;

public class StatisticsTracker {
    private int reads = 0;
    private int writes = 0;
    private int hits = 0;
    private int misses = 0;

    public StatisticsTracker() {
        this.reads = 0;
        this.writes = 0;
        this.hits = 0;
        this.misses = 0;
    }
    public int getReads() {
        return reads;
    }
    public int getWrites() {
        return writes;
    }
    public int getHits() {
        return hits;
    }
    public int getMisses() {
        return misses;
    }
    public void recordReads(boolean hit) {
        reads++;
        if(hit) hits++;
        else misses++;
    }
    public void recordWrites(boolean hit) {
        writes++;
        if(hit) hits++;
        else misses++;
    }
    public double hitRate() {
        return (hits + misses) == 0 ? 0.0 : (double) hits / (hits + misses);
    }
}
