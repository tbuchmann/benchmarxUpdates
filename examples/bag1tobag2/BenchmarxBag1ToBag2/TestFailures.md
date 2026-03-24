testIncrementalValueChangeOfAll

- alle Werte werden geändert. Erwartung bei LeastChange: der Algorithmus erkennt das und propagiert nur die Werteänderung
- in der Runtime wird aber erkannt, dass die values auf beiden Seiten nicht mehr zusammen passen. Es wird invalidiert und neu erzeugt (geht vermutlich nicht anders ohne zuviel Aufwand)