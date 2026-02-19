package model.cache;

import model.replacement.ReplacementPolicy;
import model.memory.MainMemory;
import model.address.AddressParser;

import java.util.*;

public class CacheSet {
    private final List<CacheBlock> cacheBlocks;
    private final ReplacementPolicy replacement;
    private final int blockSize;

    public CacheSet(int associativity, int blockSize, ReplacementPolicy replacement) {
        this.cacheBlocks = new ArrayList<>(associativity);
        this.blockSize = blockSize;
        this.replacement = replacement;

        for (int i = 0; i < associativity; i++) {
            CacheBlock cb = new CacheBlock(-1, blockSize);
            cb.setValid(false);
            cacheBlocks.add(cb);
        }
    }

    public Optional<CacheBlock> findByTag(long tag) {
        for (CacheBlock cb : cacheBlocks) {
            if (cb.isValid() && cb.getTag() == tag) {
                return Optional.of(cb);
            }
        }
        return Optional.empty();
    }
    public CacheAllocationResult allocateBlock(long tag, int setIndex, int nrSets, MainMemory memory, AddressParser parser) {
        Optional<CacheBlock> result = replacement.choose(cacheBlocks);
        CacheBlock block = result.orElse(cacheBlocks.get(0));

        replacement.onRemove(block);

        CacheBlock evictedBlock = null;

        if (block.isValid() && block.isDirty()) {
            evictedBlock = block;
            long oldTag = block.getTag();
            for (int i = 0; i < blockSize; i++) {
                long memAddress = (oldTag * nrSets + setIndex) * blockSize + i;
                memory.write(memAddress, block.getData()[i]);
            }
        }

        block.setTag(tag);
        block.setValid(true);
        block.setDirty(false);

        for (int i = 0; i < blockSize; i++) {
            long memAddress = (tag * nrSets + setIndex) * blockSize + i;
            block.getData()[i] = memory.read(memAddress);
        }

        replacement.onInsert(block);

        return new CacheAllocationResult(block, evictedBlock);
    }
    public List<CacheBlock> getCacheBlocks() {
        return cacheBlocks;
    }
    public ReplacementPolicy getReplacement() {
        return replacement;
    }
    public static class CacheAllocationResult {
        private final CacheBlock allocatedBlock;
        private final CacheBlock evictedBlock;

        public CacheAllocationResult(CacheBlock allocatedBlock, CacheBlock evictedBlock) {
            this.allocatedBlock = allocatedBlock;
            this.evictedBlock = evictedBlock;
        }

        public CacheBlock getAllocatedBlock() {
            return allocatedBlock;
        }

        public CacheBlock getEvictedBlock() {
            return evictedBlock;
        }
    }
}
