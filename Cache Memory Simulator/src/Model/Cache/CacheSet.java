package Model.Cache;

import Model.Replacement.ReplacementPolicy;

import java.util.*;

public class CacheSet {
    private final List<CacheBlock> cacheBlocks;
    private final ReplacementPolicy replacement;
    private final int blockSize;

    public CacheSet(int associativity, int blockSize, ReplacementPolicy replacement) {
        this.cacheBlocks = new ArrayList<>(associativity); //capacity
        this.blockSize = blockSize;
        this.replacement = replacement;

        for(int i = 0; i < associativity; i++) {
            CacheBlock cb = new CacheBlock(-1, blockSize);
            cb.setValid(false);
            cacheBlocks.add(cb);
        }
    }

    public Optional<CacheBlock> findByTag(long tag) {
        for(CacheBlock cb : cacheBlocks) {
            if(cb.isValid() && cb.getTag() == tag) {
                replacement.onAccess(cb);
                return Optional.of(cb);
            }
        }
        return Optional.empty();
    }

    public CacheBlock allocateBlock(long tag) {
        Optional<CacheBlock> result = replacement.choose(cacheBlocks);
        CacheBlock opt = result.orElse(cacheBlocks.get(0)); //extract block

        int index = cacheBlocks.indexOf(opt);

        replacement.onRemove(opt);

        CacheBlock newBlock = new CacheBlock(tag, blockSize);

        cacheBlocks.set(index, newBlock);
        replacement.onInsert(newBlock);

        return newBlock;
    }

    public List<CacheBlock> getCacheBlock() {
        return cacheBlocks;
    }
}
