package model.memory;

import java.util.*;

public class MainMemory {
    private final Map<Long, Byte> memory = new HashMap<>();

    public MainMemory() {
        Random rand = new Random();

        for (long i = 0; i < 4096; i++) {
            byte randomValue = (byte) rand.nextInt(256);
            memory.put(i, randomValue);
        }

        memory.put(0L, (byte) 0xAA);
    }
    public byte read(long address) {
        if (memory.containsKey(address)) {
            return memory.get(address);
        }
        return 0;
    }
    public void write(long address, byte value) {
        memory.put(address, value);
    }
}