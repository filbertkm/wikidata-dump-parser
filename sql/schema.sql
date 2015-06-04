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

CREATE TABLE terms(
	id serial PRIMARY KEY,
	entity_id TEXT NOT NULL,
	term_type TEXT,
	term_language TEXT,
	term_text TEXT
);

CREATE TABLE descriptions(
	id serial PRIMARY KEY,
	entity_id TEXT NOT NULL,
	term_language TEXT,
	term_text TEXT
);
