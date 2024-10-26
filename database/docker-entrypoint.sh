#!/bin/bash

PGPORT=${PGPORT:-5432}

# Démarre PostgreSQL en arrière-plan avec les arguments fournis
docker-entrypoint.sh postgres "$@" &

# Boucle jusqu'à ce que PostgreSQL soit prêt à accepter les connexions
until pg_isready -h localhost -p "$PGPORT"; do
  echo "En attente que PostgreSQL soit prêt..."
  sleep 1
done

echo "PostgreSQL est prêt, exécution du script d'initialisation..."

# Exécute le script d'initialisation
/database/init.sh

# Maintient le processus en cours pour Docker
wait