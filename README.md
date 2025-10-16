# CyberWarfare Plugin

A comprehensive Minecraft hacking system plugin for Paper/Spigot 1.21 with terminals, minigames, and database integration.

## Features

- **Interactive Hacking Terminals** - Place terminals in your world that players can interact with
- **Minigame System** - Challenging hacking puzzles with multiple types
- **Player Progression** - Skill levels and statistics tracking  
- **Database Integration** - MySQL and SQLite support with connection pooling
- **Modern APIs** - Uses Adventure API for modern text components
- **Extensible Architecture** - Clean separation of concerns and event API

## Requirements

- **Java 21** or higher
- **Paper/Spigot 1.21+**
- **Maven** for building
- **MySQL** or **SQLite** (for database)

## Installation

1. **Download or Build**
   ```bash
   mvn clean package
   ```

2. **Install Plugin**
   - Copy `target/cyberwarfare-plugin-1.0.0-SNAPSHOT.jar` to your `plugins/` folder
   - Start your server

3. **Configure Database**
   - Edit `plugins/CyberWarfare/config.yml`
   - Configure MySQL connection or leave as SQLite (default)
   - Restart server

## Configuration

### Database Setup (config.yml)
```yaml
database:
  enabled: true
  type: sqlite  # or mysql
  host: localhost
  port: 3306
  database: cyberwarfare
  username: root
  password: ""
```

### Terminal Settings
```yaml
terminals:
  cooldown: 30  # seconds between uses
  max-per-chunk: 3
  security-levels: [1, 2, 3, 4, 5]
```

## Commands

- `/cyberwarfare` - Main command and help
- `/cw reload` - Reload configuration (admin)
- `/cw stats` - Show your hacking statistics

## Permissions

- `cyberwarfare.*` - All permissions (default: op)
- `cyberwarfare.hack` - Basic hacking access (default: true)
- `cyberwarfare.admin` - Admin commands (default: op)
- `cyberwarfare.terminal.use` - Use terminals (default: true)
- `cyberwarfare.terminal.create` - Create terminals (default: op)

## Development

### Building
```bash
mvn clean package
```

### Project Structure
```
src/main/java/de/cyberwarfare/
├── CyberWarfarePlugin.java      # Main plugin class
├── config/                      # Configuration management
├── database/                    # Database and migrations  
├── models/                      # Data models
├── terminals/                   # Terminal system
├── minigames/                   # Minigame system
├── targets/                     # Target system
├── commands/                    # Command handlers
└── listeners/                   # Event listeners
```

### Database Schema
- **hacker_players** - Player statistics and progression
- **terminals** - World terminal locations
- **hack_sessions** - Session history and results
- **hack_targets** - Available hack targets

## API Usage

The plugin provides events and API access for other plugins:

```java
// Get player stats
HackerPlayer player = CyberWarfarePlugin.getInstance()
    .getDatabaseManager()
    .getHackerPlayer(uuid)
    .join();

// Listen for hack events
@EventHandler
public void onHackAttempt(HackAttemptEvent event) {
    // Handle hack attempts
}
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For support, create an issue on the GitHub repository or join our Discord server.

---

**CyberWarfare Plugin** - Bringing advanced hacking gameplay to Minecraft!