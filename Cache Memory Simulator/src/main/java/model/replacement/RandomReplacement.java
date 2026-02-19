package model.replacement;

import model.cache.CacheBlock;

import java.util.*;

public class RandomReplacement implements ReplacementPolicy {
    private final Random rand = new Random();

    @Override
    public void onAccess(CacheBlock cacheBlock) {}

    @Override
    public Optional<CacheBlock> choose(List<CacheBlock> cacheBlocks) {
        for(CacheBlock cb : cacheBlocks) {
            if(!cb.isValid()) {
                return Optional.of(cb);
            }
        }

        if(cacheBlocks.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(cacheBlocks.get(rand.nextInt(cacheBlocks.size())));
    }

    @Override
    public void onInsert(CacheBlock cacheBlock) {}

    @Override
    public void onRemove(CacheBlock cacheBlock) {}

    @Override
    public void clear() {}
}
