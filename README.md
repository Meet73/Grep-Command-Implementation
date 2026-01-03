# Grep Command Implementation

## 📌 Overview
A Java implementation of the `grep` command with multiple approaches to improve **performance and scalability**.  
The project demonstrates the use of **multi-threading, caching (proxy pattern), and file chunking** for efficient text search.

## 🚀 Implementations
1. **Single-Threaded** – Basic grep functionality using regex.  
2. **Multi-Threaded** – Uses concurrency to process large files faster.  
3. **Multi-Threaded with Proxy (Cache)** – Adds a caching layer (1-minute expiry) to avoid redundant searches.  
4. **File Chunks & Search** – Splits large files into chunks and searches in parallel for maximum efficiency.  

## 🏗 Tech Stack
- **Java 21**  
- **NIO FileChannel** for efficient file reading  
- **ExecutorService** for concurrency  
- **Design Patterns:** Builder, Proxy  
