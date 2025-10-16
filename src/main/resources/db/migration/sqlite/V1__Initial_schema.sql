CREATE TABLE hacker_players (
    player_uuid VARCHAR(36) PRIMARY KEY,
    successful_hacks INTEGER DEFAULT 0,
    failed_hacks INTEGER DEFAULT 0,
    total_trace_score INTEGER DEFAULT 0,
    skill_level INTEGER DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE terminals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    world_name VARCHAR(50) NOT NULL,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    z INTEGER NOT NULL,
    security_level INTEGER DEFAULT 1,
    created_by VARCHAR(36),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(world_name, x, y, z)
);

CREATE TABLE hack_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_uuid VARCHAR(36) NOT NULL,
    terminal_id INTEGER,
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME,
    success BOOLEAN DEFAULT FALSE,
    minigame_type VARCHAR(20),
    
    FOREIGN KEY (terminal_id) REFERENCES terminals(id)
);

CREATE TABLE hack_targets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    target_type VARCHAR(20) NOT NULL,
    difficulty INTEGER DEFAULT 1,
    cooldown_seconds INTEGER DEFAULT 300,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);