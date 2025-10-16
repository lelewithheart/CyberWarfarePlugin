# CyberWarfare Plugin - Neue GUI und IP-System Features

## 🎯 Übersicht der implementierten Features

### 1. IP-Grabber System
- **Item**: IP-Grabber (Kompass-Item)
- **Funktion**: Rechtsklick auf Terminals/Targets sammelt deren IP-Adressen
- **Command**: `/cyber ipgrabber give` - Gibt IP-Grabber Item
- **Verwendung**: 
  - IP-Adressen werden im Item gespeichert und in der Lore angezeigt
  - Eindeutige IP-Generierung basierend auf Position
  - Terminals erhalten 192.x.x.x IPs
  - Targets erhalten 10.x.x.x IPs

### 2. Terminal GUI System
- **Aktivierung**: Rechtsklick auf Terminal-Block (Observer)
- **Features**:
  - **IP-Verbindung**: Verbinde zu Targets über gesammelte IP-Adressen
  - **Eigene Targets**: Zeigt alle gehackten Targets die dem Terminal gehören
  - **Terminal Info**: Security Level, Position, Status
  - **Scanner**: Nicht verfügbar (nur für Mobile Terminals)

### 3. Mobile Terminal GUI System  
- **Aktivierung**: Rechtsklick mit Mobile Terminal Item
- **Command**: `/cyber mobile give` - Gibt Mobile Terminal
- **Features**:
  - **Geräte-Scanner**: Scannt alle Terminals und Targets im 50-Block Radius
  - **Hackbare Geräte**: Zeigt nur nicht-gehackte Targets in Reichweite
  - **Direktes Hacking**: Klick auf Target startet sofort Minigame
  - **Terminal Status**: Batterie, Signal, Online-Status

### 4. Ownership System
- **Datenbank**: Neue Spalte `owned_by_terminal` in hack_targets
- **Funktion**: Nach erfolgreichem Hack gehört Target dem verwendeten Terminal
- **GUI Integration**: Terminals zeigen ihre eigenen Targets an

### 5. Enhanced Database Schema
```sql
-- Neue Spalten hinzugefügt:
ALTER TABLE terminals ADD COLUMN ip_address VARCHAR(15) UNIQUE;
ALTER TABLE hack_targets ADD COLUMN ip_address VARCHAR(15) UNIQUE;
ALTER TABLE hack_targets ADD COLUMN owned_by_terminal INTEGER NULL;
```

## 🎮 Gameplay Flow

### Normales Terminal
1. Rechtsklick auf Terminal → GUI öffnet sich
2. IP-Grabber verwenden um Target-IPs zu sammeln
3. Über Terminal GUI → IP-Verbindung → Gespeicherte IP auswählen
4. Hacking-Minigame startet
5. Nach erfolgreichem Hack gehört Target dem Terminal

### Mobile Terminal
1. `/cyber mobile give` für Mobile Terminal Item
2. Rechtsklick mit Item → Mobile GUI öffnet sich  
3. Scanner verwenden → Alle Geräte in 50 Blöcken werden angezeigt
4. Hackbare Geräte → Nur noch nicht gehackte Targets
5. Direkt auf Target klicken → Sofortiges Hacking ohne IP-Sammlung

### IP-Grabber Workflow
1. `/cyber ipgrabber give` für IP-Grabber Item
2. Rechtsklick auf Terminal/Target mit IP-Grabber
3. IP wird gesammelt und in Item-Lore gespeichert
4. In Terminal GUI können gesammelte IPs verwendet werden

## 🔧 Technische Details

### Event Handler
- `GUIInteractionListener`: Behandelt alle GUI-Klicks und IP-Grabber Nutzung
- `TerminalInteractionListener`: Erweitert für neue GUI-Öffnung
- Thread-sichere Operationen mit Bukkit Scheduler

### Neue Manager-Methoden
- `TargetManager`: 
  - `getTargetsInRadius()` - Targets im Umkreis finden
  - `getHackableTargetsInRadius()` - Nur hackbare Targets
  - `getTargetsOwnedByTerminal()` - Eigene Targets abrufen
  - `findTargetByIP()` - Target über IP-Adresse finden
- `TerminalManager`:
  - `getTerminalsInRadius()` - Terminals im Umkreis finden
- `HackingManager`:
  - `startMobileHackingSession(Player, HackTarget)` - Direktes Target-Hacking

### GUI-Klassen
- `TerminalGUI`: Vollständige GUI für normale Terminals
- `MobileTerminalGUI`: Scanner und Proximity-Hacking für Mobile Terminals
- `IPGrabber`: Item-Management für IP-Sammlung

## 📋 Commands Übersicht

```
/cyber mobile give          - Mobile Terminal Item geben
/cyber mobile open          - Mobile Terminal GUI öffnen
/cyber ipgrabber give       - IP-Grabber Item geben
/cyber terminal create      - Terminal erstellen (Admin)
/cyber target create <type> - Target erstellen (Admin)
```

## 🔐 Permissions

```yaml
cyberwarfare.mobile     - Mobile Terminal Commands
cyberwarfare.ipgrabber  - IP-Grabber Commands  
cyberwarfare.admin      - Terminal/Target Creation
```

## 🎯 Nächste Schritte

Das System ist vollständig implementiert und bereit für den Einsatz:

1. ✅ IP-Grabber System
2. ✅ Terminal GUI mit IP-Verbindung
3. ✅ Mobile Terminal GUI mit Scanner  
4. ✅ Ownership System in Datenbank
5. ✅ Event Handling und Commands
6. ✅ Database Migration für bestehende Instanzen

**Status: Bereit für Testing und Deployment! 🚀**