FROM postgres:9.6.17

# Set up user and DB to be created.
ENV POSTGRES_USER=kalkiuser
ENV POSTGRES_PASSWORD=kalkipass
ENV POSTGRES_DB=kalkidb
ENV PGDATA=/var/lib/postgres/data/pgdata

# Copy init scripts to be automatically executed by PG entrypoint.
ADD sql/init /docker-entrypoint-initdb.d/

# Copy device type scripts to be automatically executed by PG entrypoint.
ADD sql/device_types/to_load /docker-entrypoint-initdb.d/
