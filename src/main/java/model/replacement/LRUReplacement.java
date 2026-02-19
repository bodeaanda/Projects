package model.replacement;

import model.cache.CacheBlock;

import java.util.*;

public class LRUReplacement implements ReplacementPolicy {
    private final LinkedHashSet<CacheBlock> order = new LinkedHashSet<>();

    @Override
    public void onAccess(CacheBlock cb) {
        if (cb == null || !cb.isValid()) return;
        order.remove(cb);
        order.add(cb);
    }

    @Override
    public Optional<CacheBlock> choose(List<CacheBlock> blocks) {
        for (CacheBlock cb : blocks) {
            if (!cb.isValid()) return Optional.of(cb);
        }
        return order.stream().findFirst();
    }

    @Override
    public void onInsert(CacheBlock cb) {
        order.add(cb);
    }

    @Override
    public void onRemove(CacheBlock cb) {
        order.remove(cb);
    }

    @Override
    public void clear() {
        order.clear();
    }
}
