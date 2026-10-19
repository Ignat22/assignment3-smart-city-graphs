| Workload | n | Variant | Avg time (ms) | find calls | Pointer steps | Steps per find | Max tree height |
|---|---|---|---|---|---|---|---|
| chain | 1000 | No optimisation | 0.6959 | 2998 | 493380 | 164.57 | 999 |
| chain | 1000 | Union by rank | 0.0273 | 2998 | 1997 | 0.67 | 1 |
| chain | 1000 | Path compression | 0.0259 | 2998 | 1996 | 0.67 | 1 |
| chain | 1000 | Rank + compression | 0.0292 | 2998 | 1997 | 0.67 | 1 |
| chain | 5000 | No optimisation | 15.493 | 14998 | 12596820 | 839.90 | 4999 |
| chain | 5000 | Union by rank | 0.0894 | 14998 | 9996 | 0.67 | 1 |
| chain | 5000 | Path compression | 0.1006 | 14998 | 9995 | 0.67 | 1 |
| chain | 5000 | Rank + compression | 0.0818 | 14998 | 9996 | 0.67 | 1 |
| chain | 20000 | No optimisation | 250.8 | 59998 | 199750514 | 3329.29 | 19999 |
| chain | 20000 | Union by rank | 0.4113 | 59998 | 39997 | 0.67 | 1 |
| chain | 20000 | Path compression | 0.4135 | 59998 | 39996 | 0.67 | 1 |
| chain | 20000 | Rank + compression | 0.3731 | 59998 | 39997 | 0.67 | 1 |
| random | 1000 | No optimisation | 0.1915 | 3000 | 95288 | 31.76 | 130 |
| random | 1000 | Union by rank | 0.0578 | 3000 | 3351 | 1.12 | 5 |
| random | 1000 | Path compression | 0.0484 | 3000 | 4118 | 1.37 | 4 |
| random | 1000 | Rank + compression | 0.0376 | 3000 | 2302 | 0.77 | 3 |
| random | 5000 | No optimisation | 2.784 | 15000 | 2075362 | 138.36 | 558 |
| random | 5000 | Union by rank | 0.2467 | 15000 | 18280 | 1.22 | 5 |
| random | 5000 | Path compression | 0.2512 | 15000 | 22093 | 1.47 | 4 |
| random | 5000 | Rank + compression | 0.2329 | 15000 | 11849 | 0.79 | 3 |
| random | 20000 | No optimisation | 99.655 | 60000 | 31154076 | 519.23 | 2035 |
| random | 20000 | Union by rank | 1.210 | 60000 | 78022 | 1.30 | 7 |
| random | 20000 | Path compression | 1.315 | 60000 | 95449 | 1.59 | 4 |
| random | 20000 | Rank + compression | 0.9533 | 60000 | 47552 | 0.79 | 3 |
