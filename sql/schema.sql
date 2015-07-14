CREATE TABLE label(
	id serial PRIMARY KEY,
	entity_id TEXT NOT NULL,
	label_language TEXT,
	label_text TEXT
);

CREATE TABLE alias(
	id serial PRIMARY KEY,
	entity_id TEXT NOT NULL,
	alias_language TEXT,
	alias_text TEXT
);

CREATE TABLE description(
	id serial PRIMARY KEY,
	entity_id TEXT NOT NULL,
	description_language TEXT,
	description_text TEXT
);

CREATE TABLE sitelink(
	id serial PRIMARY KEY,
	entity_id TEXT NOT NULL,
	site_key TEXT,
	page_title TEXT
);

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE claim_coordinate(
	id serial PRIMARY KEY,
	entity_id TEXT NOT NULL,
        property_id TEXT NOT NULL,
	globe TEXT default NULL,
	precision double precision,
	latitude double precision,
	longitude double precision
);

SELECT AddGeometryColumn ('public', 'claim_coordinate', 'geom', 4326, 'POINT', 2);

CREATE TABLE claim_datetime(
	id serial PRIMARY KEY,
	entity_id TEXT NOT NULL,
        property_id TEXT NOT NULL,
	calendar TEXT default NULL,
	year text,
	month text,
	day text,
	hour text,
	minute text,
	second text,
	precision text,
	tolerance_before text,
	tolerance_after text
);

CREATE TABLE claim_entity(
	id serial PRIMARY KEY,
	entity_id VARCHAR,
        property_id VARCHAR,
	value TEXT
);

CREATE TABLE claim_string(
	id serial PRIMARY KEY,
	entity_id VARCHAR,
        property_id VARCHAR,
	value TEXT
);
