# 🎮 CyberWarfare Commands Guide

## 📋 Befehlsübersicht

Das CyberWarfare Plugin bietet ein umfassendes Befehlssystem mit deutscher Lokalisierung und moderner Adventure API Integration.

### 🔧 Basis Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/cyber help` | Zeigt alle verfügbaren Befehle | `cyberwarfare.command` |
| `/cyber info` | Plugin Informationen und Features | `cyberwarfare.command` |
| `/cyber version` | Plugin Version anzeigen | `cyberwarfare.command` |
| `/cyber stats` | Deine Hacker-Statistiken | `cyberwarfare.hack` |

### 👤 Spieler Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/cyber stats [player]` | Statistiken anzeigen (eigene oder andere) | `cyberwarfare.hack` + `cyberwarfare.admin` (für andere) |

### 🛠️ Admin Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/cyber reload` | Konfiguration neu laden | `cyberwarfare.admin` |
| `/cyber terminal create` | Hacking Terminal erstellen | `cyberwarfare.admin` |
| `/cyber terminal remove` | Hacking Terminal entfernen | `cyberwarfare.admin` |
| `/cyber target create <type>` | Hack-Ziel erstellen | `cyberwarfare.admin` |
| `/cyber target remove` | Hack-Ziel entfernen | `cyberwarfare.admin` |

## 🎯 Command Aliasse

Das Plugin unterstützt mehrere Aliasse für bessere Usability:
- `/cyberwarfare` → `/cyber`, `/cw`, `/hack`

## 🔍 Tab Completion

Alle Commands unterstützen intelligente Tab-Completion:
- **Subcommands**: Automatische Vervollständigung aller verfügbaren Befehle
- **Player Namen**: Bei Stats-Commands werden Online-Spieler vorgeschlagen
- **Target Types**: Bei Target-Creation werden verfügbare Typen angezeigt
- **Permissions**: Nur verfügbare Commands werden vorgeschlagen

## 📊 Statistik System

Das Stats-System zeigt detaillierte Spieler-Informationen:

```
═══════════════════════════════════════
      Hacker Profil: PlayerName
═══════════════════════════════════════
Skill Level: 5 (Intermediate)
Erfolgreiche Hacks: 23
Fehlgeschlagene Hacks: 7
Gesamt Hacks: 30
Erfolgsrate: 76.7%
Trace Punkte: 15
═══════════════════════════════════════
```

### Skill Ranks
- **Level 1**: Novice
- **Level 2-3**: Amateur  
- **Level 4-5**: Intermediate
- **Level 6-8**: Advanced
- **Level 9-10**: Expert
- **Level 11-13**: Master
- **Level 14-15**: Elite
- **Level 16+**: Legendary

## 🎨 Visual Design

Commands nutzen moderne Adventure API Features:
- **Farbcodierung**: Grün für Erfolg, Rot für Fehler, Gelb für Warnungen
- **Formatierung**: Bold-Text für Titel, verschiedene Farben für Kategorien
- **Unicode Symbole**: Moderne Gestaltung mit Linien und Separatoren
- **MiniMessage Support**: Vollständige MiniMessage Format-Unterstützung

## ⚠️ Error Handling

Das System bietet umfassendes Error Handling:
- **Permission Checks**: Automatische Berechtigung-Prüfung
- **Player Validation**: Überprüfung auf Online-Spieler
- **Async Operations**: Sichere asynchrone Datenbank-Operationen
- **Graceful Failures**: Benutzerfreundliche Fehlermeldungen

## 🔮 Geplante Features

Die folgenden Command-Features sind in Entwicklung:
- ✅ **Help System** - Vollständig implementiert
- ✅ **Stats System** - Vollständig implementiert  
- ✅ **Reload System** - Vollständig implementiert
- 🚧 **Terminal Management** - In Entwicklung
- 🚧 **Target Management** - In Entwicklung
- 📋 **GUI Integration** - Geplant
- 📋 **Minigame Commands** - Geplant

## 💡 Beispiele

### Hilfe anzeigen
```
/cyber help
```

### Eigene Stats anzeigen
```
/cyber stats
```

### Stats von anderem Spieler anzeigen (Admin)
```
/cyber stats Notch
```

### Terminal erstellen (Admin)
```
/cyber terminal create
```

### Konfiguration neu laden (Admin)
```
/cyber reload
```

---

**Das Command-System ist bereit für den Einsatz!** 🚀