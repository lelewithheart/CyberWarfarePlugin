CREATE TABLE hacker_players (
    player_uuid VARCHAR(36) PRIMARY KEY,
    successful_hacks INT DEFAULT 0,
    failed_hacks INT DEFAULT 0,
    total_trace_score INT DEFAULT 0,
    skill_level INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE terminals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    world_name VARCHAR(50) NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    security_level INT DEFAULT 1,
    created_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_location (world_name, x, y, z)
);

CREATE TABLE hack_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_uuid VARCHAR(36) NOT NULL,
    terminal_id INT,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    success BOOLEAN DEFAULT FALSE,
    minigame_type VARCHAR(20),
    
    FOREIGN KEY (terminal_id) REFERENCES terminals(id)
);

CREATE TABLE hack_targets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    target_type VARCHAR(20) NOT NULL,
    difficulty INT DEFAULT 1,
    cooldown_seconds INT DEFAULT 300,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);