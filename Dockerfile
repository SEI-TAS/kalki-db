FROM postgres:9.5.19

# Set up user and DB to be created.
ENV POSTGRES_USER=kalkiuser
ENV POSTGRES_PASSWORD=kalkipass
ENV POSTGRES_DB=kalkidb

# Copy init scripts to be automatically executed by PG entrypoint.
ADD sql/init /docker-entrypoint-initdb.d/

# Copy device type scripts to be automatically executed by PG entrypoint.
ADD sql/device_types /docker-entrypoint-initdb.d/
