package controller;

import model.address.AddressParser;
import model.cache.*;
import model.memory.MainMemory;
import model.statistics.StatisticsTracker;
import model.replacement.*;
import model.write.*;

import java.util.*;

public class SimulatorController {
    private Cache cache;
    private MainMemory memory;
    private AddressParser parser;
    private WritePolicy writePolicy;
    private StatisticsTracker stats;

    public SimulatorController(Cache cache, MainMemory memory, AddressParser parser, WritePolicy writePolicy, StatisticsTracker stats) {
        this.cache = cache;
        this.memory = memory;
        this.parser = parser;
        this.writePolicy = writePolicy;
        this.stats = stats;
    }
    public byte read(long address) {
        long tag = parser.getTag(address);
        int setIndex = parser.getSetIndex(address);
        int offset = (int) parser.getBlockOffset(address);

        CacheSet cacheSet = cache.getSet(setIndex);
        Optional<CacheBlock> blockOpt = cacheSet.findByTag(tag);

        if (blockOpt.isPresent()) {
            CacheBlock block = blockOpt.get();
            cacheSet.getReplacement().onAccess(block);
            stats.recordReads(true);
            return block.getData()[offset];
        } else {
            stats.recordReads(false);
            CacheSet.CacheAllocationResult allocResult = cacheSet.allocateBlock(tag, setIndex, cache.getNumSets(), memory, parser);

            CacheBlock newBlock = allocResult.getAllocatedBlock();
            CacheBlock evictedBlock = allocResult.getEvictedBlock();

            return newBlock.getData()[offset];
        }
    }
    public void write(long address, byte value, WritePolicy policy, WriteMissPolicy missPolicy) {
        long tag = parser.getTag(address);
        int setIndex = parser.getSetIndex(address);
        int offset = (int) parser.getBlockOffset(address);

        CacheSet set = cache.getSet(setIndex);
        Optional<CacheBlock> blockOpt = set.findByTag(tag);

        if (blockOpt.isPresent()) {
            CacheBlock block = blockOpt.get();
            set.getReplacement().onAccess(block);
            block.getData()[offset] = value;

            if (policy == WritePolicy.WRITE_BACK) {
                block.setDirty(true);
            } else {
                memory.write(address, value);
            }
            stats.recordWrites(true);
        } else {
            stats.recordWrites(false);
            if (missPolicy == WriteMissPolicy.WRITE_ALLOCATE) {
                CacheSet.CacheAllocationResult allocResult = set.allocateBlock(tag, setIndex, cache.getNumSets(), memory, parser);
                CacheBlock block = allocResult.getAllocatedBlock();
                CacheBlock evictedBlock = allocResult.getEvictedBlock();

                block.getData()[offset] = value;

                if (policy == WritePolicy.WRITE_BACK) {
                    block.setDirty(true);
                } else {
                    memory.write(address, value);
                }
            } else {
                memory.write(address, value);
            }
        }
    }
    private int findBlockIndexInSet(CacheSet set, long tag) {
        List<CacheBlock> blocks = set.getCacheBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            CacheBlock block = blocks.get(i);
            if (block.isValid() && block.getTag() == tag) {
                return i;
            }
        }
        return -1;
    }
    private boolean isHit(long address) {
        long tag = parser.getTag(address);
        int setIndex = parser.getSetIndex(address);
        return cache.getSet(setIndex).findByTag(tag).isPresent();
    }
    public Map<String, Object> readStats(long address) {
        long tag = parser.getTag(address);
        int setIndex = parser.getSetIndex(address);
        int offset = (int) parser.getBlockOffset(address);
        CacheSet cacheSet = cache.getSet(setIndex);

        Optional<CacheBlock> blockOpt = cacheSet.findByTag(tag);
        boolean hit = blockOpt.isPresent();

        Map<String, Object> result = getAccessDetails(address, "READ");
        result.put("hit", hit);
        result.put("stats", stats);

        if (hit) {
            CacheBlock block = blockOpt.get();
            cacheSet.getReplacement().onAccess(block);
            stats.recordReads(true);

            result.put("value", block.getData()[offset]);
            result.put("wayIndex", findBlockIndexInSet(cacheSet, tag));
            result.put("evicted", false);
        } else {
            stats.recordReads(false);

            CacheSet.CacheAllocationResult allocResult = cacheSet.allocateBlock(tag, setIndex, cache.getNumSets(), memory, parser);
            CacheBlock newBlock = allocResult.getAllocatedBlock();
            CacheBlock evictedBlock = allocResult.getEvictedBlock();

            result.put("value", newBlock.getData()[offset]);
            result.put("wayIndex", findBlockIndexInSet(cacheSet, tag));

            if (evictedBlock != null) {
                result.put("evicted", true);
                result.put("evictedTag", evictedBlock.getTag());
                result.put("evictedDirty", evictedBlock.isDirty());
            } else {
                result.put("evicted", false);
            }
        }

        return result;
    }
    public Map<String, Object> writeStats(long address, byte value, WritePolicy policy, WriteMissPolicy missPolicy) {
        long tag = parser.getTag(address);
        int setIndex = parser.getSetIndex(address);
        int offset = (int) parser.getBlockOffset(address);
        CacheSet cacheSet = cache.getSet(setIndex);

        Optional<CacheBlock> blockOpt = cacheSet.findByTag(tag);
        boolean hit = blockOpt.isPresent();

        Map<String, Object> result = getAccessDetails(address, "WRITE");
        result.put("hit", hit);
        result.put("writePolicy", policy.toString());
        result.put("missPolicy", missPolicy.toString());
        result.put("stats", stats);
        result.put("value", value);

        if (hit) {
            CacheBlock block = blockOpt.get();
            cacheSet.getReplacement().onAccess(block);
            block.getData()[offset] = value;
            if (policy == WritePolicy.WRITE_BACK) block.setDirty(true);
            else memory.write(address, value);

            stats.recordWrites(true);
            result.put("wayIndex", findBlockIndexInSet(cacheSet, tag));
            result.put("evicted", false);
        } else {
            stats.recordWrites(false);

            if (missPolicy == WriteMissPolicy.WRITE_ALLOCATE) {
                CacheSet.CacheAllocationResult allocResult = cacheSet.allocateBlock(tag, setIndex, cache.getNumSets(), memory, parser);
                CacheBlock block = allocResult.getAllocatedBlock();
                CacheBlock evictedBlock = allocResult.getEvictedBlock();

                block.getData()[offset] = value;
                if (policy == WritePolicy.WRITE_BACK) block.setDirty(true);
                else memory.write(address, value);

                result.put("wayIndex", findBlockIndexInSet(cacheSet, tag));

                if (evictedBlock != null) {
                    result.put("evicted", true);
                    result.put("evictedTag", evictedBlock.getTag());
                    result.put("evictedDirty", evictedBlock.isDirty());
                } else {
                    result.put("evicted", false);
                }
            } else {
                memory.write(address, value);
                result.put("wayIndex", -1);
                result.put("evicted", false);
            }
        }
        return result;
    }
    public void flush() {
        for (int i = 0; i < cache.getNumSets(); i++) {
            for (CacheBlock block : cache.getSet(i).getCacheBlocks()) {
                if (block.isValid() && block.isDirty()) {
                    for (int j = 0; j < cache.getBlockSize(); j++) {
                        long address = (block.getTag() * cache.getNumSets() + i) * cache.getBlockSize() + j;
                        memory.write(address, block.getData()[j]);
                    }
                    block.setDirty(false);
                }
            }
        }
    }
    public void reconfigure(int cacheSizeBytes, int blockSize, int associativity, String policyName) {
        Optional<ReplacementPolicy> policyOpt = ChooseReplacement.create(policyName);
        if (policyOpt.isEmpty()) throw new IllegalArgumentException("Invalid replacement policy");

        int nrSets = (cacheSizeBytes / blockSize) / associativity;
        if (nrSets <= 0) throw new IllegalArgumentException("Invalid cache configuration");

        this.cache = new Cache(cacheSizeBytes, blockSize, associativity, policyOpt.get());
        this.parser = new AddressParser(blockSize, nrSets);
        this.stats = new StatisticsTracker();
    }
    public Cache getCache() { return cache; }
    public StatisticsTracker getStats() { return stats; }
    private Map<String, Object> getAccessDetails(long address, String op) {
        Map<String, Object> details = new HashMap<>();
        details.put("address", address);
        details.put("tag", parser.getTag(address));
        details.put("setIndex", parser.getSetIndex(address));
        details.put("offset", parser.getBlockOffset(address));
        details.put("operation", op);
        details.put("hits", stats.getHits());
        details.put("misses", stats.getMisses());
        return details;
    }
    public Map<String, Object> getCacheState() {
        Map<String, Object> state = new HashMap<>();
        CacheSet[] sets = cache.getSets();
        List<Map<String, Object>> setsData = new ArrayList<>();

        for (CacheSet set : sets) {
            List<Map<String, Object>> blocksData = new ArrayList<>();
            for (CacheBlock block : set.getCacheBlocks()) {
                Map<String, Object> blockInfo = new HashMap<>();
                blockInfo.put("valid", block.isValid());
                blockInfo.put("dirty", block.isDirty());
                blockInfo.put("tag", block.getTag());
                blockInfo.put("data", block.getData());
                blocksData.add(blockInfo);
            }
            Map<String, Object> setInfo = new HashMap<>();
            setInfo.put("cacheBlocks", blocksData);
            setsData.add(setInfo);
        }

        state.put("sets", setsData);
        state.put("numSets", cache.getNumSets());
        state.put("blockSize", cache.getBlockSize());
        state.put("associativity", cache.getAssociativity());

        return state;
    }
}
