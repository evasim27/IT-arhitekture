# Sistem za primerjanje cen izdelkov

**PriceScout** je mikrostoritvena aplikacija za primerjavo cen izdelkov med različnimi trgovinami.  
Uporabnikom omogoča, da poiščejo izdelek in preverijo, v kateri trgovini je trenutno najnižja cena.

Primer uporabe:

Uporabnik išče izdelek Nutella:
| Izdelek | Trgovina | Cena |
|--------|---------|------|
| Nutella 750g | Lidl | 6.49 € |
| Nutella 750g | Spar | 6.99 € |
| Nutella 750g | Mercator | 7.29 € |

# Arhitektura sistema

Sistem je zasnovan kot **mikrostoritvena arhitektura**, ki vključuje:

- tri neodvisne mikrostoritve 
- spletno aplikacijo kot uporabniški vmesnik

# Mikrostoritve

## storitev-izdelki

Skrbi za upravljanje kataloga izdelkov.

### Funkcionalnosti

- dodajanje izdelkov
- iskanje izdelkov
- pregled podrobnosti izdelka

## storitev-trgovine

Upravlja podatke o trgovinah ter cene izdelkov v posamezni trgovini.

### Funkcionalnosti

- seznam trgovin
- dodajanje cen izdelkov
- posodabljanje cen

## storitev-primerjava-cen

Primerja cene izdelkov v različnih trgovinah in izračuna najnižjo ceno.

### Funkcionalnosti

- pridobivanje cen izdelka
- primerjava cen
- določanje najcenejše trgovine

Na spodnjem diagramu je prikazana arhitektura in komunikacije med mikrostoritvami.

![PriceScout arhitektura](https://www.plantuml.com/plantuml/png/ZP91JiCm44NtSufHzhq02rHIkkWkQa1TJvnHv3gnex4Rf28axi09E0mNuIGu8n1LAr2pYkp_t_pvyZ8cadGOUe1aKy-uQNbF-FdwXfLOmxK9XmH09WN1efJegFVM5KWHImbFaGL0s2JoNNONZsCGQhpzU7SMfu6ZYfy1ysV2C0RFFc6nMvT8OsyT6RidfUsuWHU0RoWk5hTw12CPHrr6_-R1Q6FYOUQUW-kSqoQUT0DxRBbtLl6LXFPWKiqoPSo_jYHTcAndzSqtV8rh53kmx6aYDEpLkyfI4Xk3_m4eeQL43SLvfP3_NPWyOKD7EWrwNrwXrN1PEHUNozBIBeRY3MxNzGFULHk0YrlrAETI_eVwj4hLTzjg1N1sVABC2NN2-KYDlcJVvlVt1G00)

# Struktura repozitorija

```
price-scout
│
├── README.md
├── web-aplikacija
│   └── src
│
├── storitev-izdelki
│   ├── src
│   └── db
│
├── storitev-trgovine
│   ├── src
│   └── db
│
└── storitev-primerjava-cen
    └── src
```