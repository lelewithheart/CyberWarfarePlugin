# SQLite Syntax Fix - Database Error Resolved

## Problem
```
[ERROR]: [CyberWarfare] Error saving hacker player: [SQLITE_ERROR] SQL error or missing database (near "DUPLICATE": syntax error)
```

## Ursache
Das Plugin verwendete MySQL-spezifische `ON DUPLICATE KEY UPDATE` Syntax, die in SQLite nicht unterstützt wird.

## Lösung
**Vor (MySQL-Syntax):**
```sql
INSERT INTO hacker_players (...) VALUES (...)
ON DUPLICATE KEY UPDATE 
    successful_hacks = VALUES(successful_hacks),
    failed_hacks = VALUES(failed_hacks),
    ...
```

**Nach (SQLite-Syntax):**
```sql
INSERT INTO hacker_players (...) VALUES (...)
ON CONFLICT(player_uuid) DO UPDATE SET
    successful_hacks = excluded.successful_hacks,
    failed_hacks = excluded.failed_hacks,
    ...
```

## Änderungen
- `ON DUPLICATE KEY UPDATE` → `ON CONFLICT(player_uuid) DO UPDATE SET`
- `VALUES(column)` → `excluded.column`
- SQLite UPSERT-Syntax verwendet für bessere Kompatibilität

## Test
Nach dem Fix sollte der `/cyber target create CAMERA 2` Befehl ohne Datenbankfehler funktionieren.

## Status: ✅ Behoben
- Plugin kompiliert erfolgreich
- SQLite-kompatible Syntax implementiert  
- Deployed und bereit zum Testen

Der Datenbankfehler sollte jetzt nicht mehr auftreten! 🎯