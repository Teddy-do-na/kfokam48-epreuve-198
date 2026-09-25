# KFOKAM48 — Suivi de présence et relecture par les pairs

## Stack
- Backend : Java 17, Spring Boot, Maven, Flyway
- Frontend : React (Vite)
- Base de données : PostgreSQL

## Démarrage (3 commandes, testées depuis un clone vierge)

```bash
docker compose up -d          # démarre PostgreSQL
cd backend && mvn spring-boot:run   # démarre l'API sur :8080 (Java 17+, Maven)
cd frontend && npm install && npm run dev   # démarre le front sur :5173 (Node.js)
```

Le frontend proxifie `/api` vers `http://localhost:8080` (voir `frontend/vite.config.js`).

## Données de démonstration
Au premier démarrage, Flyway crée automatiquement une promotion et trois étudiants de démonstration, ainsi qu’une session ouverte avec le code `DEMO48` (valable 15 minutes).

## Documentation
- Cahier des charges : `docs/CAHIER_DES_CHARGES.md`
- Diagrammes : `docs/diagrammes/`
- Contrat d'API : `api/contrat.yaml`
- Journal de bord : `docs/JOURNAL.md`
