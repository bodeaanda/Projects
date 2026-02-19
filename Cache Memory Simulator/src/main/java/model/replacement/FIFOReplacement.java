package model.replacement;

import model.cache.CacheBlock;

import java.util.*;

public class FIFOReplacement implements ReplacementPolicy {
    private final Queue<CacheBlock> queue = new ArrayDeque<>();

    @Override
    public void onAccess(CacheBlock cacheBlock) {}

    @Override
    public Optional<CacheBlock> choose(List<CacheBlock> cacheBlocks) {
        for (CacheBlock cb : cacheBlocks) {
            if (!cb.isValid()) {
                return Optional.of(cb);
            }
        }
        CacheBlock opt = queue.poll();
        return opt != null ? Optional.of(opt) : (cacheBlocks.isEmpty() ? Optional.empty() : Optional.of(cacheBlocks.get(0)));
    }

    @Override
    public void onInsert(CacheBlock cacheBlock) {
        if (cacheBlock != null)
            queue.add(cacheBlock);
    }

    @Override
    public void onRemove(CacheBlock cacheBlock) {
        if (cacheBlock != null)
            queue.remove(cacheBlock);
    }

    @Override
    public void clear() {
        queue.clear();
    }
}
