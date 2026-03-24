# Bag1-to-Bag2 Testsuite – Dokumentation

Diese Dokumentation beschreibt alle Testfälle der **Bag1-to-Bag2 BenchmarX-Testsuite**. Die Testsuite überprüft die bidirektionale Transformation zwischen zwei Bag-Metamodellen (`Bag1` / `bags1.MyBag` und `Bag2` / `bags2.MyBag`). Die Tests sind in vier Klassen unterteilt, die entlang zweier Dimensionen organisiert sind:

- **Propagationsrichtung**: Forward (fwd) – Änderungen in Bag1 werden nach Bag2 propagiert; Backward (bwd) – Änderungen in Bag2 werden nach Bag1 propagiert.
- **Synchronisationsart**: Batch – der Startzustand ist leer; Alignment-based (inkrementell) – der Startzustand ist nicht leer, und Korrespondenzen zwischen bestehenden Elementen werden berücksichtigt.

Alle Tests sind als JUnit-5-`@ParameterizedTest` implementiert und werden für jedes registrierte BX-Tool (aktuell: BXtend, MediniQVT) ausgeführt.

---

## 1. Batch Forward (`BatchForward`)

**Paket:** `org.benchmarx.examples.bag12bag2.testsuite.batch.fwd`

Batch-Forward-Tests starten mit leeren Modellen und propagieren Änderungen von **Bag1 → Bag2**.

---

### 1.1 `testInitialiseSynchronisation`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Forward (fwd) |
| **Art** | Batch, fixed |
| **Vorbedingung** | Keine (leere Modelle) |

**Was wird getestet?**  
Überprüft, dass nach der Initialisierung des Synchronisationsdialogs korrekte Wurzelelemente in beiden Modellen vorhanden sind – auch ohne jegliche Editierung.

**Ablauf:**
1. Initialisierung des BX-Tools (leere Modelle).
2. Keine Editierung wird durchgeführt.
3. **Erwartetes Ergebnis:** Beide Modelle enthalten je ein Wurzelelement (`RootElementBags1`, `RootElementBags2`).

---

### 1.2 `testCreateElement`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Forward (fwd) |
| **Art** | Batch, fixed |
| **Vorbedingung** | Keine (leere Modelle) |

**Was wird getestet?**  
Erstellt ein einzelnes Element (Beer) in Bag1 und überprüft, ob es korrekt nach Bag2 propagiert wird.

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Quell-Edit: Ein Beer-Element wird in Bag1 erzeugt (`createOneBeer`).
3. Das Edit wird nach Bag2 propagiert (`performAndPropagateSourceEdit`).
4. **Erwartetes Ergebnis:** Bag1 enthält genau 1 Beer (`OneBeerBags1`); Bag2 enthält das entsprechende transformierte Element (`OneBeerBags2`).

---

### 1.3 `testCreateMultipleElements`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Forward (fwd) |
| **Art** | Batch, fixed |
| **Vorbedingung** | Keine (leere Modelle) |

**Was wird getestet?**  
Erstellt mehrere Elemente unterschiedlichen Typs (5 Beers und 1 Beer Glass) in Bag1 und überprüft die korrekte Propagation nach Bag2.

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Quell-Edit: Fünf Beer-Elemente (`createFiveBeers`) und ein Beer-Glass-Element (`createBeerGlass`) werden in Bag1 erzeugt.
3. Das Edit wird nach Bag2 propagiert.
4. **Erwartetes Ergebnis:** Bag1 enthält 5 Beers und 1 Beer Glass (`FiveBeerWithGlassBags1`); Bag2 spiegelt diesen Zustand korrekt wider (`FiveBeerWithGlassBags2`).

---

## 2. Batch Backward (`BatchBackward`)

**Paket:** `org.benchmarx.examples.bag12bag2.testsuite.batch.bwd`

Batch-Backward-Tests starten mit leeren Modellen und propagieren Änderungen von **Bag2 → Bag1**.

---

### 2.1 `testCreateElement`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Backward (bwd) |
| **Art** | Batch, fixed |
| **Vorbedingung** | Keine (leere Modelle) |

**Was wird getestet?**  
Erstellt ein einzelnes Element (Beer) in Bag2 und überprüft, ob es korrekt nach Bag1 propagiert wird.

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Ziel-Edit: Ein Beer-Element wird in Bag2 erzeugt (`createOneBeer`).
3. Das Edit wird nach Bag1 propagiert (`performAndPropagateTargetEdit`).
4. **Erwartetes Ergebnis:** Bag1 enthält das entsprechend transformierte Element (`OneBeerBags1`); Bag2 enthält 1 Beer (`OneBeerBags2`).

---

### 2.2 `testCreateMultipleElements`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Backward (bwd) |
| **Art** | Batch, fixed |
| **Vorbedingung** | Keine (leere Modelle) |

**Was wird getestet?**  
Erstellt mehrere Elemente unterschiedlichen Typs (5 Beers und 1 Beer Glass) in Bag2 und überprüft die korrekte Rückpropagation nach Bag1.

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Ziel-Edit: Fünf Beer-Elemente (`createFiveBeer`) und ein Beer-Glass-Element (`createBeerGlass`) werden in Bag2 erzeugt.
3. Das Edit wird nach Bag1 propagiert.
4. **Erwartetes Ergebnis:** Bag1 spiegelt den Zustand korrekt wider (`FiveBeerWithGlassBags1`); Bag2 enthält 5 Beers und 1 Beer Glass (`FiveBeerWithGlassBags2`).

---

## 3. Incremental Forward (`IncrementalForward`)

**Paket:** `org.benchmarx.examples.bag12bag2.testsuite.alignment_based.fwd`

Inkrementelle Forward-Tests starten mit einem **nicht-leeren Startzustand** und propagieren weitere Änderungen von **Bag1 → Bag2**. Korrespondenzen zwischen bestehenden Elementen werden berücksichtigt (alignment-based / corr-based).

---

### 3.1 `testIncrementalInserts`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Forward (fwd) |
| **Art** | Inkrementell (alignment-based), add, fixed |
| **Vorbedingung** | Bag1: 1 Beer; Bag2: 1 Beer mit inkrementeller ID |

**Was wird getestet?**  
Fügt weitere Elemente zu einem bereits befüllten Bag1 hinzu und prüft, ob neue Elemente korrekt zu den bestehenden hinzugefügt werden (Mengensemantik in Bag2).

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Vorbedingung herstellen: 1 Beer in Bag1 erzeugen und nach Bag2 propagieren; inkrementelle ID in Bag2 setzen.
3. Precondition-Check: `OneBeerBags1` / `OneBeerIncrIDBags2`.
4. Quell-Edit: 5 weitere Beers (`createFiveBeers`), 2 Beer Glasses (`createBeerGlass` zweimal) werden in Bag1 hinzugefügt.
5. Das Edit wird nach Bag2 propagiert.
6. **Erwartetes Ergebnis:** Bag1 enthält 6 Beers und 2 Beer Glasses (`SixBeerWithTwoGlassesBags1`); Bag2 spiegelt dies korrekt wider (`SixBeerWithTwoGlassesBags2`).

---

### 3.2 `testIncrementalDeletions`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Forward (fwd) |
| **Art** | Inkrementell, del, corr-based, structural |
| **Vorbedingung** | Bag1: 5 Beers, 1 Beer Glass; Bag2: entsprechend mit inkrementeller ID |

**Was wird getestet?**  
Löscht Elemente aus einem befüllten Bag1 und prüft, ob die Änderungen (Multiplizitätsreduktion bzw. Löschung) korrekt nach Bag2 übertragen werden.

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Vorbedingung herstellen: 5 Beers und 1 Beer Glass in Bag1 erzeugen; inkrementelle ID in Bag2 setzen.
3. Precondition-Check: `FiveBeerWithGlassBags1` / `FiveBeerWithGlassIncrIDBags2`.
4. Quell-Edit: 2 Beers (`deleteBeer` zweimal) und 1 Beer Glass (`deleteBeerGlass`) werden aus Bag1 gelöscht.
5. Das Edit wird nach Bag2 propagiert.
6. **Erwartetes Ergebnis:** Bag1 enthält noch 3 Beers (`ThreeBeerBags1`); in Bag2 wird die Multiplizität von Beer entsprechend reduziert und das Beer-Glass-Element entfernt (`ThreeBeerBags2`).

---

### 3.3 `testIncrementalValueChangeOfOne`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Forward (fwd) |
| **Art** | Inkrementell, attribute, fixed, structural, corr-based |
| **Vorbedingung** | Bag1: 5 Beers, 1 Beer Glass; Bag2: entsprechend mit inkrementeller ID |

**Was wird getestet?**  
Ändert den Wert eines einzelnen Beer-Elements in Bag1 zu Empty Bottle und prüft, ob diese Attributänderung korrekt nach Bag2 propagiert wird.

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Vorbedingung herstellen: 5 Beers und 1 Beer Glass in Bag1; inkrementelle ID in Bag2.
3. Precondition-Check: `FiveBeerWithGlassBags1` / `FiveBeerWithGlassIncrIDBags2`.
4. Quell-Edit: Ein Beer wird zu Empty Bottle geändert (`changeOneBeerToEmptyBottle`); inkrementelle ID in Bag1 aktualisieren.
5. Das Edit wird nach Bag2 propagiert.
6. **Erwartetes Ergebnis:** Bag1 enthält 4 Beers, 1 Empty Bottle, 1 Beer Glass (`FourBeerOneEmptyBottleWithGlassBags1`); Bag2 spiegelt dies mit inkrementeller ID wider (`FourBeerOneEmptyBottleWithGlassIncrIDBags2`).

---

### 3.4 `testIncrementalValueChangeOfAll`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Forward (fwd) |
| **Art** | Inkrementell, attribute, fixed, structural, corr-based |
| **Vorbedingung** | Bag1: 5 Beers, 1 Beer Glass; Bag2: entsprechend mit inkrementeller ID |

**Was wird getestet?**  
Ändert den Wert **aller** Beer-Elemente in Bag1 zu Empty Bottle und prüft, ob die Transformation ein konsolidiertes Element mit Multiplizität 5 in Bag2 erzeugt.

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Vorbedingung herstellen: 5 Beers und 1 Beer Glass in Bag1; inkrementelle ID in Bag2.
3. Precondition-Check: `FiveBeerWithGlassBags1` / `FiveBeerWithGlassIncrIDBags2`.
4. Quell-Edit: Alle Beer-Elemente werden zu Empty Bottle geändert (`changeAllBeerToEmptyBottles`).
5. Das Edit wird nach Bag2 propagiert.
6. **Erwartetes Ergebnis:** Bag1 enthält 5 Empty Bottles und 1 Beer Glass (`FiveEmptyBottlesWithGlassBags1`); Bag2 enthält ein Element mit Wert Empty Bottle und Multiplizität 5 sowie das Beer Glass (`FiveEmptyBottlesWithGlassBags2`).

---

### 3.5 `testStability`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Forward (fwd) |
| **Art** | Inkrementell, fixed (Stabilitätstest) |
| **Vorbedingung** | Bag1: 6 Beers, 2 Beer Glasses; Bag2: entsprechend mit inkrementeller ID |

**Was wird getestet?**  
Stabilitätstest: Überprüft, dass das erneute Ausführen der Transformation nach einem **leeren (idle) Source-Delta** das Zielmodell nicht verändert (Hippokratische Eigenschaft / Stabilität).

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Vorbedingung herstellen: 5 Beers + 1 weiteres Beer + 2 Beer Glasses in Bag1; inkrementelle ID in Bag2.
3. Precondition-Check: `SixBeerWithTwoGlassesBags1` / `SixBeerWithTwoGlassesBags2`.
4. Quell-Edit: Ein leeres (idle) Delta wird auf Bag1 angewendet (`idleDelta`).
5. Das Edit wird nach Bag2 propagiert.
6. **Erwartetes Ergebnis:** Beide Modelle bleiben unverändert – Bag1: `SixBeerWithTwoGlassesBags1`, Bag2: `SixBeerWithTwoGlassesBags2`.

---

## 4. Incremental Backward (`IncrementalBackward`)

**Paket:** `org.benchmarx.examples.bag12bag2.testsuite.alignment_based.bwd`

Inkrementelle Backward-Tests starten mit einem **nicht-leeren Startzustand** und propagieren weitere Änderungen von **Bag2 → Bag1**.

---

### 4.1 `testIncrementalInserts`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Backward (bwd) |
| **Art** | Inkrementell, add, fixed |
| **Vorbedingung** | Bag2: 1 Empty Bottle; Bag1: entsprechend mit inkrementeller ID |

**Was wird getestet?**  
Fügt weitere Elemente zu einem bereits befüllten Bag2 hinzu und prüft, ob neue Elemente korrekt nach Bag1 propagiert werden, ohne die bestehenden Elemente zu verändern.

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Vorbedingung herstellen: 1 Empty Bottle in Bag2 erzeugen und nach Bag1 propagieren; inkrementelle ID in Bag1 setzen.
3. Precondition-Check: `OneEmptyBottleBags1` / `OneEmptyBottleBags2`.
4. Ziel-Edit: 1 Beer Glass (`createBeerGlass`) und 4 Beers (`createFourBeer`) werden in Bag2 hinzugefügt.
5. Das Edit wird nach Bag1 propagiert.
6. **Erwartetes Ergebnis:** Bag1 enthält 4 Beers, 1 Empty Bottle und 1 Beer Glass (`FourBeerOneEmptyBottleWithGlassBags1`); Bag2 spiegelt dies wider (`FourBeerOneEmptyBottleWithGlassBags2`).

---

### 4.2 `testIncrementalDeletions`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Backward (bwd) |
| **Art** | Inkrementell, del, corr-based, structural |
| **Vorbedingung** | Bag2: 4 Beers, 1 Beer Glass, 1 Empty Bottle; Bag1: entsprechend mit inkrementeller ID |

**Was wird getestet?**  
Löscht Elemente aus einem befüllten Bag2 und prüft, ob die Löschungen korrekt nach Bag1 zurückpropagiert werden.

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Vorbedingung herstellen: 1 Beer Glass, 4 Beers und 1 Empty Bottle in Bag2 erzeugen; inkrementelle ID in Bag1 setzen.
3. Precondition-Check: `FourBeerOneEmptyBottleWithGlassBags1` / `FourBeerOneEmptyBottleWithGlassBags2`.
4. Ziel-Edit: Das Beer-Glass-Element (`deleteBeerGlass`) und alle Beers (`deleteAllBeers`) werden aus Bag2 gelöscht.
5. Das Edit wird nach Bag1 propagiert.
6. **Erwartetes Ergebnis:** Bag1 enthält nur noch 1 Empty Bottle (`OneEmptyBottleBags1`); Bag2 enthält nur noch 1 Empty Bottle (`OneEmptyBottleBags2`).

---

### 4.3 `testIncrementalValueChangeOfAll`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Backward (bwd) |
| **Art** | Inkrementell, attribute, fixed, structural, corr-based |
| **Vorbedingung** | Bag2: 4 Beers, 1 Beer Glass, 1 Empty Bottle; Bag1: entsprechend mit inkrementeller ID |

**Was wird getestet?**  
Führt mehrere kombinierte Wert- und Multiplizitätsänderungen in Bag2 durch und prüft, ob alle Änderungen korrekt nach Bag1 propagiert werden:
- Empty Bottle → Broken Bottle
- Multiplizität von Beer wird reduziert
- Beer → Empty Bottle
- Multiplizität von Beer Glass wird erhöht

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Vorbedingung herstellen: 1 Beer Glass, 4 Beers und 1 Empty Bottle in Bag2 erzeugen; inkrementelle ID in Bag1 setzen.
3. Precondition-Check: `FourBeerOneEmptyBottleWithGlassBags1` / `FourBeerOneEmptyBottleWithGlassBags2`.
4. Ziel-Edit (kombiniert):
   - Empty Bottle → Broken Bottle (`changeEmptyBottleToBrokenBottle`)
   - Multiplizität von Beer reduzieren (`changeMultiplicityOfBeer`)
   - Beer → Empty Bottle (`changeBeerToEmptyBottle`)
   - Multiplizität von Beer Glass erhöhen (`changeMultiplicityOfBeerGlass`)
5. Das Edit wird nach Bag1 propagiert.
6. **Erwartetes Ergebnis:** Bag1 enthält 1 Broken Bottle, 2 Empty Bottles und 2 Beer Glasses (`OneBrokenBottleTwoEmptyBottleWithTwoGlassesBags1`); Bag2 spiegelt dies wider (`OneBrokenBottleTwoEmptyBottleWithTwoGlassesBags2`).

---

### 4.4 `testStability`

| Eigenschaft | Beschreibung |
|---|---|
| **Richtung** | Backward (bwd) |
| **Art** | Inkrementell, fixed (Stabilitätstest) |
| **Vorbedingung** | Bag2: 4 Beers, 1 Beer Glass, 1 Empty Bottle; Bag1: entsprechend mit inkrementeller ID |

**Was wird getestet?**  
Stabilitätstest: Überprüft, dass das erneute Ausführen der Transformation nach einem **leeren (idle) Target-Delta** das Quellmodell nicht verändert (Hippokratische Eigenschaft / Stabilität).

**Ablauf:**
1. Initialisierung des BX-Tools.
2. Vorbedingung herstellen: 1 Beer Glass, 4 Beers und 1 Empty Bottle in Bag2 erzeugen; inkrementelle ID in Bag1 setzen.
3. Precondition-Check: `FourBeerOneEmptyBottleWithGlassBags1` / `FourBeerOneEmptyBottleWithGlassBags2`.
4. Ziel-Edit: Ein leeres (idle) Delta wird auf Bag2 angewendet (`idleDelta`).
5. Das Edit wird nach Bag1 propagiert.
6. **Erwartetes Ergebnis:** Beide Modelle bleiben unverändert – Bag1: `FourBeerOneEmptyBottleWithGlassBags1`, Bag2: `FourBeerOneEmptyBottleWithGlassBags2`.

---

## Übersicht aller Testfälle

| # | Klasse | Testmethode | Richtung | Art | Kernaspekt |
|---|---|---|---|---|---|
| 1 | `BatchForward` | `testInitialiseSynchronisation` | fwd | Batch | Initialisierung / Wurzelelemente |
| 2 | `BatchForward` | `testCreateElement` | fwd | Batch | Einfügen eines Elements |
| 3 | `BatchForward` | `testCreateMultipleElements` | fwd | Batch | Einfügen mehrerer Elemente |
| 4 | `BatchBackward` | `testCreateElement` | bwd | Batch | Einfügen eines Elements (rückwärts) |
| 5 | `BatchBackward` | `testCreateMultipleElements` | bwd | Batch | Einfügen mehrerer Elemente (rückwärts) |
| 6 | `IncrementalForward` | `testIncrementalInserts` | fwd | Inkrementell | Hinzufügen zu bestehendem Modell |
| 7 | `IncrementalForward` | `testIncrementalDeletions` | fwd | Inkrementell | Löschen aus bestehendem Modell |
| 8 | `IncrementalForward` | `testIncrementalValueChangeOfOne` | fwd | Inkrementell | Einzelne Attributänderung |
| 9 | `IncrementalForward` | `testIncrementalValueChangeOfAll` | fwd | Inkrementell | Alle Attributwerte ändern |
| 10 | `IncrementalForward` | `testStability` | fwd | Inkrementell | Stabilität (idle Delta) |
| 11 | `IncrementalBackward` | `testIncrementalInserts` | bwd | Inkrementell | Hinzufügen zu bestehendem Modell (rückwärts) |
| 12 | `IncrementalBackward` | `testIncrementalDeletions` | bwd | Inkrementell | Löschen aus bestehendem Modell (rückwärts) |
| 13 | `IncrementalBackward` | `testIncrementalValueChangeOfAll` | bwd | Inkrementell | Kombinierte Wert-/Multiplizitätsänderungen |
| 14 | `IncrementalBackward` | `testStability` | bwd | Inkrementell | Stabilität (idle Delta, rückwärts) |

---

## Modellzustände (Referenzbezeichner)

Die folgenden Bezeichner referenzieren Referenzmodellzustände, die in den Ressourcendateien des Projekts hinterlegt sind:

| Bezeichner | Beschreibung |
|---|---|
| `RootElementBags1` / `RootElementBags2` | Leere Bag-Modelle mit nur dem Wurzelelement |
| `OneBeerBags1` / `OneBeerBags2` | Je 1 Beer-Element |
| `FiveBeerWithGlassBags1` / `FiveBeerWithGlassBags2` | 5 Beers + 1 Beer Glass |
| `OneBeerIncrIDBags2` | 1 Beer mit inkrementeller ID in Bag2 |
| `FiveBeerWithGlassIncrIDBags2` | 5 Beers + 1 Beer Glass mit inkrementeller ID in Bag2 |
| `SixBeerWithTwoGlassesBags1` / `SixBeerWithTwoGlassesBags2` | 6 Beers + 2 Beer Glasses |
| `ThreeBeerBags1` / `ThreeBeerBags2` | 3 Beers |
| `FourBeerOneEmptyBottleWithGlassBags1` / `...Bags2` | 4 Beers + 1 Empty Bottle + 1 Beer Glass |
| `FiveEmptyBottlesWithGlassBags1` / `...Bags2` | 5 Empty Bottles + 1 Beer Glass |
| `OneEmptyBottleBags1` / `OneEmptyBottleBags2` | 1 Empty Bottle |
| `OneBrokenBottleTwoEmptyBottleWithTwoGlassesBags1` / `...Bags2` | 1 Broken Bottle + 2 Empty Bottles + 2 Beer Glasses |
| `FourBeerOneEmptyBottleWithGlassBags1` / `...Bags2` | 4 Beers + 1 Empty Bottle + 1 Beer Glass |
