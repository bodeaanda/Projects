package org.example.cachememoryapp;

import controller.SimulatorController;
import model.statistics.StatisticsTracker;
import model.write.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
@CrossOrigin(origins = "*")
public class CacheController {

    private final SimulatorController controller;

    public CacheController(SimulatorController controller) {
        this.controller = controller;
    }

    @PostMapping("/config")
    public ResponseEntity<String> configureCache(
            @RequestParam int cacheSizeBytes,
            @RequestParam int blockSize,
            @RequestParam int associativity,
            @RequestParam String replacementPolicy) {
        try {
            controller.reconfigure(cacheSizeBytes, blockSize, associativity, replacementPolicy);
            return ResponseEntity.ok("Cache successfully reconfigured and statistics reset.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Configuration Error: " + e.getMessage());
        }
    }

    @GetMapping("/read")
    public Map<String, Object> read(@RequestParam long address) {
        return controller.readStats(address);
    }

    @PostMapping("/write")
    public Map<String, Object> write(
            @RequestParam long address,
            @RequestParam int value,
            @RequestParam WritePolicy writePolicy,
            @RequestParam WriteMissPolicy missPolicy) {

        return controller.writeStats(address, (byte) value, writePolicy, missPolicy);
    }

    /*@GetMapping("/state")
    public Object getCacheState() {
        return controller.getCache();
    }*/

    @GetMapping("/state")
    public Map<String, Object> getCacheState() {
        return controller.getCacheState();
    }

    @GetMapping("/stats")
    public StatisticsTracker getStats() {
        return controller.getStats();
    }

    @PostMapping("/flush")
    public ResponseEntity<String> flushCache() {
        controller.flush();
        return ResponseEntity.ok("Cache flushed successfully. Dirty blocks written back to memory.");
    }
}