# Testen

## 1. Automatisch (vor jedem Commit/Release)

```bash
./gradlew build                        # muss grün sein, ohne Compiler-Warnungen
./gradlew :fabric:runGametest          # Server-GameTests Fabric   → "All 12 required tests passed"
./gradlew :neoforge:runGameTestServer  # Server-GameTests NeoForge → "All 12 required tests passed"
./gradlew :fabric:runClientGametest    # Client-Smoke-Test (öffnet kurz ein Fenster)
```

Die GameTests (`common/.../gametest/StoneGolemGameTests.java`) prüfen: spawnt ruhend und bleibt stehen · erwacht bei Angriff, ist dabei unverwundbar und nimmt den Angreifer ins Ziel · erwacht beim Steinabbau in der Nähe (nicht bei Holz, nicht aus 6+ Blöcken) · Spitzhacke > Schwert · Projektile ×0,5 · Stampfer trifft und Cooldown greift · Bodenschlag trifft erst beim Aufprall (~8 Schaden) · Felsbeschwörung wirft ~8 Blöcke hoch, Felsen verschwinden · Loot enthält Cobblestone · Biome-Modifier tragen den Golem mit Gewicht/Gruppengröße ein · Spawnregeln (Stein, Höhle, offener Himmel).

Der Client-Smoke-Test prüft Client/Server-Gleichstand der Zustände, dass der echte Spieler von den Felsen 7,3–8,7 Blöcke hochgeworfen wird, und das Spawn-Ei. Screenshots liegen in `fabric/runs/clientGametest/screenshots/`.

## 2. Manuell im Spiel (beide Loader)

Starten mit `./gradlew :fabric:runClient` bzw. `./gradlew :neoforge:runClient`, neue Welt (Kreativ, Cheats an), Schwierigkeit mindestens *Easy*. Hilfreich: **F3+B** (Hitboxen), Optionen → Untertitel an.

### Erscheinen
- [ ] Kreativ-Inventar → Tab *Spawn-Eier*: „Steingolem-Spawn-Ei“ vorhanden, grau-moosiges Ei
- [ ] Tab *Zutaten*: „Golemkern“ mit Tooltip „Pulsiert schwach …“ (grau, kursiv)
- [ ] Ei auf den Boden → Golem erscheint als moosiger Felsbrocken
- [ ] `/summon stonegolem:stone_golem ~ ~ ~3` → ebenso

### Ruhezustand → Erwachen → Kampf → Rückkehr
- [ ] Ruhend: bewegt sich nicht, dreht den Kopf nicht, macht keine Geräusche (Untertitel bleiben leer)
- [ ] `/gamemode survival`, Golem schlagen → Aufstehanimation (~1,25 s), Untertitel „Steingolem erwacht“
- [ ] Während des Aufstehens: weitere Treffer machen keinen Schaden (keine rote Färbung)
- [ ] Größe: stehend ~5,4 Blöcke, doppelt so hoch wie ein Eisengolem (zum Vergleich `/summon minecraft:iron_golem` daneben); ruhend ein ~3 Blöcke hoher Felsbrocken, Hitbox (F3+B) wächst beim Aufstehen mit
- [ ] Danach: langsam, folgt dir; Bodenschlag: beide Fäuste hoch, Aufprall vor ihm mit Staubwolke, ~4 Herzen ohne Rüstung (Normal), weicht man in der Ausholphase zurück, geht er ins Leere
- [ ] Zweiter Spieler bzw. andere Mobs werden ignoriert (neutral, nur der Angreifer ist Ziel)
- [ ] Neuen ruhenden Golem setzen, im Umkreis von ≤ 4 Blöcken **Stein oder Erz** abbauen → er erwacht, greift aber nicht an; Holz/Erde abbauen → nichts
- [ ] Weglaufen (> 24 Blöcke) oder in den Kreativmodus wechseln, ca. 30 s warten → er kauert sich wieder zum Felsbrocken zusammen
- [ ] Welt verlassen und neu laden: ruhende Golems bleiben ruhend, aktive stehen

### Bodenstampfer
- [ ] Im Überlebensmodus nah am aktiven Golem bleiben → er lehnt sich zurück, Arme hoch (sichtbares Ausholen, ~0,75 s), dann Schlag auf den Boden
- [ ] Beim Aufschlag: Blockbruch-Partikel des Untergrunds, dumpfer Amboss-Sound, du wirst leicht hochgeschleudert
- [ ] Nächster Stampfer frühestens nach ~8 s (dazwischen nur Faustschläge)

### Felsbeschwörung
- [ ] 6–15 Blöcke Abstand zum aktiven Golem halten → er hebt eine Hand, unter dir bröckelt der Boden (Partikel)
- [ ] Stehen bleiben → nach ~1 s brechen ein großer und zwei kleine Felsen hervor, du fliegst ~8 Blöcke hoch (auch in Rüstung)
- [ ] Die Felsen versinken langsam, du fällst herunter und nimmst normalen Fallschaden (Federfall/Wasser helfen)
- [ ] Bei der Vorwarnung zur Seite gehen → die Felsen verfehlen dich
- [ ] Nächste Beschwörung frühestens nach ~10 s; näher als 4 Blöcke gibt es stattdessen Bodenschlag/Stampfer

### Waffen-Schwäche (Tags)
- [ ] Eisenspitzhacke vs. Eisenschwert auf zwei frische Golems (je ein Schlag nach dem Erwachen): Spitzhacke nimmt deutlich mehr Leben
- [ ] Pfeile machen spürbar wenig Schaden
- [ ] Optional: Datapack mit `data/stonegolem/tags/item/effective_against_stone_golem.json` (`"values": ["minecraft:stick"]`) → Stock wird zur „Spitzhacke“, ohne Code-Änderung

### Immunitäten
- [ ] `/summon stonegolem:stone_golem ~ ~20 ~` → fällt ohne Schaden
- [ ] Golem in tiefes Wasser setzen (aufwecken) → ertrinkt nicht
- [ ] Rückstoß (Schwert mit Rückstoß, Explosion) verschiebt ihn nicht

### Drops
- [ ] `/kill @e[type=stonegolem:stone_golem]` → 4–8 Cobblestone, 0–2 Moosblöcke
- [ ] Im Überlebensmodus töten → zusätzlich XP; mit Plünderung III (`/enchant @s minecraft:looting 3`) öfter einen Golemkern (Basis ~10 %)

### Natürliches Spawning (erhöhtes Testgewicht)
```bash
./gradlew :fabric:runClient -PspawnWeight=500     # bzw. :neoforge:runClient
```
Im Log steht dann `Spawn weight override active: 500`.
- [ ] Neue Welt, Überlebensmodus, `/effect give @s minecraft:night_vision infinite`
- [ ] Höhle unter Y 40 erkunden → Golems stehen ruhend auf Stein (nie auf Erde/Gras); wegen der Größe nur in Höhlen mit ≥ 6 Blöcken Platz
- [ ] `/locate biome minecraft:stony_peaks`, hinteleportieren → Golems auch an der Oberfläche auf Stein
- [ ] In einem Ebenen-/Waldbiom an der Oberfläche → keine Golems
- [ ] Nie mehr als einer pro Gruppe
- [ ] Im Nether/End → keine Golems

### Lokalisierung
- [ ] Sprache Deutsch/Englisch umschalten: Mob-Name, Ei, Golemkern, Tooltip und alle Untertitel übersetzt (keine rohen Schlüssel wie `entity.stonegolem…`)

## Bekannte Grenzen (Phase 1)
- Modell, Textur und Animationen sind ein erster Entwurf (Blockbench-Überarbeitung geplant).
- Die Sounds sind tief gepitchte Vanilla-Sounds über eigene Sound-Events (`assets/stonegolem/sounds.json`).
- Natürliche Spawns sind Monster-Spawns: Auf *Friedlich* spawnt der Golem nicht natürlich.
