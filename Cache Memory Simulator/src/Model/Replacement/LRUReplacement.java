package Model.Replacement;

import Model.Cache.CacheBlock;

import java.util.*;

public class LRUReplacement implements ReplacementPolicy {
    private final LinkedHashSet<Long> order = new LinkedHashSet<>();

    @Override
    public void onAccess(CacheBlock cacheBlock) {
        if (cacheBlock == null || !cacheBlock.isValid()) return;
        long tag = cacheBlock.getTag();
        order.remove(tag);
        order.add(tag);
    }

    @Override
    public Optional<CacheBlock> choose(List<CacheBlock> cacheBlocks) { //to replace
        for (CacheBlock cb : cacheBlocks) {
            if (!cb.isValid()) return Optional.of(cb);
        }
        for (Long tag : order) {
            for (CacheBlock cb : cacheBlocks) {
                if (cb.isValid() && cb.getTag() == tag) {
                    return Optional.of(cb);
                }
            }
        }
        return cacheBlocks.isEmpty() ? Optional.empty() : Optional.of(cacheBlocks.get(0));
    }

    @Override
    public void onInsert(CacheBlock cacheBlock) {
        if(cacheBlock !=null)
            order.add(cacheBlock.getTag());
    }

    @Override
    public void onRemove(CacheBlock cacheBlock) {
        if(cacheBlock != null)
            order.remove(cacheBlock.getTag());
    }

    @Override
    public void clear() {
        order.clear();
    }
}