# Cache Memory Simulator

##Overview
The **Cache Memory Simulator** is a full-stack web application designed to simulate and analyze the behavior of CPU cache memory. It provides a visual and interactive way to understand how different cache configurations, replacement policies and write policies influence cache performance and memory access patterns.

This educational and analytical tool enables users to configure cache parameters such as cache size, block size and set associativity. It tracks statistical data including cache hits, misses and evictions for both read and write operations, making it an excellent resource for students and professionals studying computer architecture.

## Features
- **Customizable Cache Configuration:** Adjust total cache size, block size and mapping methods to match different architectures.
- **Replacement Policies Supported:**
	- **LRU** (Least Recently Used)
	- **FIFO** (First-In, First-Out)
	- **Random** 
- **Write Policies Supported:**
	- Write-Back /  Write-Through
	- Write-Allocate / No-Write-Allocate
- **Detailed Statistics & Metrics:** Tracks read/write hits and misses, observing how the cache fills and replaces data over time.
- **Detailed Block Tracking:** Inspect the exact state of cache sets and blocks (valid bits, dirty bits, tags and data).
- **Cache Flush:** Ability to manually flush the cache.

# Technologies Used

### Backend
- **Java 21:** Core application logic and memory simulations.
- **Spring Boot:** REST API framework to expose simulation results and state.
- **Maven:** Dependency and build management.

### Frontend
- **React 19:** Modern UI layer providing a dynamic and responsive visualization of the cache memory.
- **React Scripts:** App structure and build pipeline.
- **React Testing Library:** Used for component testing.