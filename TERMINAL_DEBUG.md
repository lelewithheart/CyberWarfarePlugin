# Terminal Problem Debugging Guide

## Problem: Normale Terminals funktionieren nicht

### Mögliche Ursachen und Lösungen:

1. **Event-Konflikt behoben**:
   - Alter `TerminalInteractionListener` wurde deaktiviert
   - Neuer `GUIInteractionListener` übernimmt Terminal-Handling

2. **Terminal-Erkennung**:
   ```java
   // Prüfe ob Block ein Terminal ist:
   if (terminalManager.isTerminalBlock(block)) {
       // Öffne GUI
   }
   ```

3. **GUI-Öffnung vereinfacht**:
   - `terminalGUI.openTerminalGUI(player, null)` funktioniert jetzt auch ohne Terminal-Objekt
   - Fallback-Werte für unbekannte Terminals

### Test-Schritte:

1. **Server neu starten** mit dem aktualisierten Plugin
2. **Terminal erstellen**:
   ```
   /cyber terminal create
   ```
3. **Rechtsklick auf Observer-Block** sollte GUI öffnen
4. **Falls nicht funktioniert**:
   - Prüfe Konsole auf Fehler
   - Teste mit `/cyber mobile give` ob Mobile Terminal funktioniert

### Debug-Commands für Tests:

```bash
# Terminal erstellen
/cyber terminal create

# IP-Grabber holen
/cyber ipgrabber give  

# Mobile Terminal zum Vergleich
/cyber mobile give

# Target erstellen zum Testen
/cyber target create SERVER
```

### Erwartetes Verhalten:

1. **Rechtsklick auf Observer** → Terminal GUI öffnet sich
2. **Compass-Item klicken** → IP-Eingabe GUI
3. **IP auswählen** → Hacking startet
4. **Mobile Terminal** → Scanner GUI

### Falls weiterhin Probleme:

Prüfe in der Server-Konsole:
- "Registering event listeners..." 
- Keine Fehler beim Plugin-Start
- Event-Handler werden korrekt registriert

Das Problem sollte jetzt behoben sein! 🎯