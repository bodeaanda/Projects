package model.cache;

import model.replacement.ReplacementPolicy;

public class Cache {
    private final int blockSize;
    private final int nrSets;
    private final int associativity;
    private final CacheSet[] sets;

    public Cache(int cacheSizeBytes, int blockSize, int associativity, ReplacementPolicy policy) {
        this.blockSize = blockSize;
        this.associativity = associativity;
        this.nrSets = (cacheSizeBytes / blockSize) / associativity;
        this.sets = new CacheSet[nrSets];
        for (int i=0;i<nrSets;i++){
            sets[i] = new CacheSet(associativity, blockSize, policy);
        }
    }
    public CacheSet getSet(int index) {
        return sets[index];
    }
    public int getBlockSize() {
        return blockSize;
    }
    public int getNumSets() {
        return nrSets;
    }
    public int getAssociativity() {
        return associativity;
    }
    public CacheSet[] getSets() {
        return sets;
    }
}
