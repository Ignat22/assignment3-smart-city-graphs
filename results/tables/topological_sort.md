| Graph | V | E | Input | Avg time (ms) | Edge examinations | Queue ops | Total ops | Vertices ordered | Result |
|---|---|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | random DAG | 0.0004 | 60 | 20 | 80 | 10/10 | valid order |
| Small | 10 | 30 | DAG + 1 back edge | 0.0004 | 52 | 10 | 62 | 5/10 | cycle detected |
| Small | 10 | 30 | random directed (cyclic) | 0.0002 | 30 | 0 | 30 | 0/10 | cycle detected |
| Medium | 100 | 500 | random DAG | 0.0041 | 1000 | 200 | 1200 | 100/100 | valid order |
| Medium | 100 | 500 | DAG + 1 back edge | 0.0037 | 927 | 130 | 1057 | 65/100 | cycle detected |
| Medium | 100 | 500 | random directed (cyclic) | 0.0015 | 500 | 0 | 500 | 0/100 | cycle detected |
| Large | 1000 | 5000 | random DAG | 0.0760 | 10000 | 2000 | 12000 | 1000/1000 | valid order |
| Large | 1000 | 5000 | DAG + 1 back edge | 0.0749 | 9948 | 1900 | 11848 | 950/1000 | cycle detected |
| Large | 1000 | 5000 | random directed (cyclic) | 0.0214 | 5054 | 20 | 5074 | 10/1000 | cycle detected |
