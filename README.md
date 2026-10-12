# Gamba Mod – Fehlerbehebungen

Fabric-Client-Mod für Minecraft 1.21.11.

## Änderungen

- Während die Endertruhe öffnet, verschwindet ihr Symbol sofort und die Gewinnanimation ist sichtbar.
- Die Slot-Rollen dauern zufällig 2 bis 3 Sekunden. Die Symbolwechsel werden am Ende langsamer, damit die Drehung weich ausläuft.
- Die Symbole sind Kupferbarren, Eisenbarren, Goldbarren, Diamanten und Netheritbarren. Netherit ist am seltensten und bringt den höchsten Dreiergewinn.
- Gewertet werden ausschließlich drei gleiche Symbole waagerecht in einer Reihe. Zwei gleiche Symbole, senkrechte Reihen und Diagonalen zahlen nicht aus.
- Der Mindesteinsatz beträgt 1 Münze. Ein Einsatz von 0 oder weniger wird sowohl in der Oberfläche als auch beim Abziehen der Münzen blockiert.

## Gewinne pro Reihe

- Zwei gleiche Symbole: kein Gewinn
- Drei Kupferbarren: 100 Münzen
- Drei Eisenbarren: 150 Münzen
- Drei Goldbarren: 250 Münzen
- Drei Diamanten: 500 Münzen
- Drei Netheritbarren: 1.000 Münzen

## Build über GitHub

Die GitHub-Action unter `.github/workflows/build.yml` baut das Projekt automatisch bei einem Push auf `main` oder `master`. Du kannst sie außerdem im Reiter **Actions** manuell starten. Lade anschließend bei einem erfolgreichen Lauf das Artifact `GambaMod-Fixed-Minecraft-1.21.11-v1.0.2` herunter.

Für einen lokalen Build benötigst du Java 21. Der Gradle-Wrapper lädt Gradle 9.2.1 automatisch herunter. Im Projektordner lautet der Befehl:

```text
Windows: .\gradlew.bat clean build
macOS/Linux: ./gradlew clean build
```

Die fertige Mod-Datei heißt `gambamod-1.0.2.jar` und liegt im Ordner `build/libs`.

Das Guthaben ist lokal und virtuell; die Mod verändert kein Server-Guthaben und verwendet kein echtes Geld.
