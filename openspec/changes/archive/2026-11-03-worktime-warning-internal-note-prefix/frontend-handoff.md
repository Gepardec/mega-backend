# Frontend-Handoff: „Intern -“-Warnungen (Gepardec/mega#799)

**Repo:** `mega-frontend-v2`
**Backend-Change:** `openspec/changes/worktime-warning-internal-note-prefix` (mega-backend, Branch `feat/799-mfa-spec-test`)
**Blockiert:** Das Archivieren des Backend-Changes. Erst wenn die Punkte unten umgesetzt und getestet sind, wird archiviert.

## Worum geht's

Die Buchungskontrolle meldet jetzt zwei neue Verstöße gegen die „Intern -“-Konvention, und zwar für Zeit- und Reisebuchungen:

| Warnungstyp (API-Wert) | Bedeutung |
|---|---|
| `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` | Buchung auf einem **fakturierbaren Projekt**, deren Bemerkung mit „Intern -“ beginnt |
| `INTERNAL_PREFIX_MISSING` | Buchung auf einem **internen (nicht fakturierbaren) Projekt**, deren Bemerkung **nicht** mit „Intern -“ beginnt (auch bei leerer Bemerkung) |

Ob eine Buchung intern ist, entscheidet das **Projekt** und nicht das Fakturierbar-Flag der einzelnen Buchung.

## API-Vertrag (keine Schemaänderung)

`GET /worktime/warnings/{payrollMonth}` bleibt unverändert. `type` ist im OpenAPI-Vertrag ein `string`, deshalb musst du den Client **nicht** neu generieren. Neu sind nur zwei mögliche Werte:

```json
[
  { "type": "INTERNAL_PREFIX_MISSING",             "date": "2026-03-02", "hours": null },
  { "type": "INTERNAL_PREFIX_ON_BILLABLE_PROJECT", "date": "2026-03-03", "hours": null }
]
```

- `date` ist immer gesetzt, `hours` ist immer `null`.
- Pro Typ und Tag kommt höchstens ein Eintrag.

Die bestehende Gruppierung (`groupWorkTimeBookingWarnings`) und die Darstellung als Datums-Chips funktionieren generisch. Daran ändert sich nichts.

## Notwendige Änderungen

### 1. Übersetzungs-Keys umbenennen

Auf `main` gibt es die Übersetzungen schon (Commit `3ab6f85`), aber unter anderen Keys. Ohne Umbenennung zeigt die UI den rohen Key an.

Pfad: `employee.dashboard.work-time-booking-warnings.warning.*` in `public/i18n/de.json` **und** `public/i18n/en.json`

| alt | neu |
|---|---|
| `INTERNAL_NOTE_BILLABLE` | `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` |
| `INTERNAL_NOTE_MISSING` | `INTERNAL_PREFIX_MISSING` |

### 2. Texte auf Projekt-Ebene anpassen

Die aktuellen Texte („Fakturierbare Buchung …“ / „Nicht fakturierbare Buchung …“) beschreiben die einzelne Buchung. Geprüft wird aber das Projekt. Vorschlag:

| Key | de | en |
|---|---|---|
| `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` | Buchung auf fakturierbarem Projekt mit Bemerkung „Intern -“ | Booking on a billable project with a note starting with “Intern -” |
| `INTERNAL_PREFIX_MISSING` | Buchung auf internem Projekt ohne Bemerkung „Intern -“ | Booking on an internal project without a note starting with “Intern -” |

Die finale Formulierung stimmst du mit dem PO bzw. der Projektleitung ab.

### 3. Automatisierte Tests

- In `work-time-booking-warnings.component.spec.ts` (oder in der Spec von `work-time-booking-warning`) prüfst du für **beide** neuen Typen, dass der übersetzte Text gerendert wird, nicht der rohe Key, und dass das Datum als Chip ohne Stundenangabe erscheint.
- Optional, aber empfohlen: ein Test, der sicherstellt, dass `de.json` und `en.json` unter `warning.*` dieselben Keys haben.

## Manueller Test (gegen das Backend vom Branch)

Voraussetzung: Backend läuft mit dem Branch, die Projekte sind in MEGA synchronisiert, und es gibt ZEP-Buchungen im Testmonat.

| # | Testdaten (ZEP) | Erwartung im Dashboard „MEGA Zeiten Check“ |
|---|---|---|
| 1 | Buchung auf **internem** Projekt, Bemerkung `Teammeeting` | Warnung „… ohne Bemerkung „Intern -““ mit Datum |
| 2 | Buchung auf **internem** Projekt, **leere** Bemerkung | wie 1 |
| 3 | Buchung auf **internem** Projekt, Bemerkung `Intern - Teammeeting` | keine der beiden Warnungen |
| 4 | Buchung auf **fakturierbarem** Projekt, Bemerkung `Intern - Kickoff` | Warnung „… mit Bemerkung „Intern -““ mit Datum |
| 5 | Buchung auf **fakturierbarem** Projekt, Bemerkung `Kickoff` | keine der beiden Warnungen |
| 6 | Reisebuchung auf **internem** Projekt ohne Präfix | wie 1 |
| 7 | Mehrere Verstöße desselben Typs an verschiedenen Tagen | **eine** Warnung mit mehreren Datums-Chips |
| 8 | Sprache auf EN umstellen | englische Texte, kein roher Key |

Die Präfix-Erkennung ist tolerant: Groß- und Kleinschreibung, Leerzeichen sowie `-`, `–` und `—` werden akzeptiert (z. B. `intern-`, `Intern –`). „Interne Schulung“ zählt **nicht** als Präfix.

## Definition of Done

- [ ] Keys in `de.json` und `en.json` sind umbenannt, die alten Keys sind entfernt
- [ ] Texte sind auf Projekt-Ebene formuliert und abgestimmt
- [ ] Component-Tests für beide Typen sind grün, `npm test` ist grün
- [ ] Manueller Test 1–8 gegen das Backend ist durchgeführt
- [ ] Der Frontend-PR ist gemerged oder zusammen mit dem Backend-PR freigegeben

Melde dich danach bei Mattias Fankhauser. Erst dann wird der Backend-Change archiviert.
