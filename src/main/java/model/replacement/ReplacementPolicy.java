package model.replacement;

import model.cache.CacheBlock;

import java.util.*;

public interface ReplacementPolicy {
    void onAccess(CacheBlock cacheBlock);
    Optional<CacheBlock> choose(List<CacheBlock> cacheBlocks);
    void onInsert(CacheBlock cacheBlock);
    void onRemove(CacheBlock cacheBlock);
    void clear();
}
