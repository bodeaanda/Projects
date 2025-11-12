package Model.Replacement;

import Model.Cache.CacheBlock;

import java.util.*;

public interface ReplacementPolicy {
    void onAccess(CacheBlock cacheBlock); //for updating metadata
    Optional<CacheBlock> choose(List<CacheBlock> cacheBlocks);
    void onInsert(CacheBlock cacheBlock);
    void onRemove(CacheBlock cacheBlock);
    void clear();
}
