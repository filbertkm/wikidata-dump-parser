CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TABLE coordinates(
	id serial PRIMARY KEY,
	entity_id TEXT NOT NULL,
	globe TEXT default NULL,
	precision double precision,
	latitude double precision,
	longitude double precision
);

SELECT AddGeometryColumn ('public', 'coordinates', 'geom', 4326, 'POINT', 2);

CREATE TABLE value_snaks(
	id serial PRIMARY KEY,
	entity_id VARCHAR,
	values HSTORE
);
