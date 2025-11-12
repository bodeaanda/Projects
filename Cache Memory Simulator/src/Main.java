import Model.Cache.Cache;
import Model.Cache.CacheBlock;
import Model.Cache.CacheSet;
import Model.Replacement.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("DEMO");
        System.out.println("\nFIFO:");
        testCache(new FIFOReplacement());

        System.out.println("\nLRU:");
        testCache(new LRUReplacement());

        System.out.println("\nRandom:");
        testCache(new RandomReplacement());
    }

    public static void testCache(ReplacementPolicy policy) {
        Cache cache = new Cache(256, 64, 2, policy);

        System.out.println("Cache: 256 bytes, 64 bytes/bloc, 2-way associative");
        System.out.println("Nr seturi: " + cache.getNumSets());
        System.out.println("Block size: " + cache.getBlockSize() + " bytes\n");

        CacheSet set = cache.getSet(0);

        long[] tagsToAccess = {100, 200, 100, 300, 400};

        int hits = 0;
        int misses = 0;

        for (int i = 0; i < tagsToAccess.length; i++) {
            long tag = tagsToAccess[i];
            System.out.println("Access " + (i + 1) + ": Tag " + tag);
            Optional<CacheBlock> result = set.findByTag(tag);

            if (result.isEmpty()) {
                misses++;
                System.out.println("  > CACHE MISS!");

                if (i >= 2) {
                    System.out.println("  > Cache full");
                }

                CacheBlock newBlock = set.allocateBlock(tag);
                System.out.println("  > Block allocated with tag: " + newBlock.getTag());
            } else {
                hits++;
                System.out.println("  > CACHE HIT! Block found with tag: " + result.get().getTag());
            }
            displaySetState(set);
        }

        int validBlocks = 0;
        for (CacheBlock block : set.getCacheBlock()) {
            if (block.isValid()) {
                validBlocks++;
            }
        }
        System.out.println("Valid Blocks: " + validBlocks + "/2");
        System.out.println("Cache Hits: " + hits);
        System.out.println("Cache Misses: " + misses);
        System.out.println("Hit Rate: " + String.format("%.1f%%", (hits * 100.0 / tagsToAccess.length)));
    }

    public static void displaySetState(CacheSet set) {
        System.out.println("  Set State:");
        int index = 0;
        for (CacheBlock block : set.getCacheBlock()) {
            if (block.isValid()) {
                System.out.println("    [Block " + index + "] Tag: " + block.getTag() +
                        ", Valid: " + block.isValid());
            } else {
                System.out.println("    [Block " + index + "] FREE, Valid: " + block.isValid());
            }
            index++;
        }
    }
}
