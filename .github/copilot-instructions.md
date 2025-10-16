# CyberWarfare Plugin - Minecraft Hacking System

This workspace contains a complete Minecraft plugin for Paper/Spigot 1.21 that implements a comprehensive hacking system with terminals, minigames, and database integration.

## Project Structure
- **Java 21** - Modern Java features and compatibility
- **Paper API 1.21** - Latest Minecraft server platform
- **Maven** - Build system and dependency management
- **HikariCP** - High-performance database connection pooling
- **Flyway** - Database migration management
- **Adventure API** - Modern text components and messaging
- **MySQL/SQLite** - Database backend options

## Key Features
- Interactive hacking terminals placed in the world
- GUI-based minigame system with multiple challenge types
- Player progression and skill system
- Target system with 10+ different effect types
- Configuration and localization support
- Event API for extensibility
- Command system for administration

## Development Guidelines
- Follow modern Minecraft plugin development practices
- Use Adventure API for all text components (no legacy ChatColor)
- Implement async database operations with CompletableFuture
- Use proper package naming conventions (lowercase)
- Maintain clean separation of concerns between systems

## Build Process
1. Maven compilation with `mvn clean compile`
2. Package with `mvn clean package` 
3. Deploy JAR from `target/` directory to server plugins folder

## Database Setup
- Plugin auto-creates tables via Flyway migrations
- Configure connection in `plugins/CyberWarfare/config.yml`
- Supports both MySQL and SQLite backends