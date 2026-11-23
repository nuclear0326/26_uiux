# v45 validation
- JDK 21 build successful; no game Main, JFrame or EXE launched.
- 45 save checks: milestone round-trips, reward/consumption retention, room and environment state, damage/backup recovery, missing primary, reset backup, write failures, checksum/version rejection, lighting restore, new-game confirmation.
- Separate headless JVM processes write, exit, reload, continue through future and ending. Only test folders under outputs/v45-release-tests were used.
- Regression: 51 progression, 15 Rush Hour completion, 44 mission/puzzle, 48 notebook, 34 game-menu checks. Total 237 passed.
- Runtime save/continue integration was reviewed statically; no live window interaction was performed.
- Windows bundle reuses the prior Java launcher/runtime with the v45 JAR and configuration. No launcher was executed.
- Asset hashes unchanged; game-menu/research-note PNGs match v44 byte-for-byte. Packaged JAR matches build; launcher header/runtime/config and both ZIPs verified.
