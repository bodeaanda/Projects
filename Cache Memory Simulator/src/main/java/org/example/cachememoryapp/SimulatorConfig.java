package org.example.cachememoryapp;

import controller.SimulatorController;
import model.address.AddressParser;
import model.cache.Cache;
import model.memory.MainMemory;
import model.replacement.*;
import model.statistics.StatisticsTracker;
import model.write.WritePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimulatorConfig {

    public static final int CACHE_SIZE_BYTES = 1024;
    public static final int BLOCK_SIZE = 32;
    public static final int ASSOCIATIVITY = 4;
    public static final int NR_SETS = (CACHE_SIZE_BYTES / BLOCK_SIZE) / ASSOCIATIVITY;

    @Bean
    public MainMemory mainMemory() {
        return new MainMemory();
    }

    @Bean
    public AddressParser addressParser() {
        return new AddressParser(BLOCK_SIZE, NR_SETS);
    }

    @Bean
    public ReplacementPolicy defaultReplacementPolicy() {
        return new FIFOReplacement();
    }

    @Bean
    public Cache cache(ReplacementPolicy policy) {
        return new Cache(CACHE_SIZE_BYTES, BLOCK_SIZE, ASSOCIATIVITY, policy);
    }

    @Bean
    public SimulatorController simulatorController(Cache cache, MainMemory memory, AddressParser parser) {

        return new SimulatorController(
                cache,
                memory,
                parser,
                WritePolicy.WRITE_BACK,
                new StatisticsTracker()
        );
    }
}