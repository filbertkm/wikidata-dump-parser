Scripts for parsing and importing a Wikidata dump into a database (e.g. postgres).

Data extracted includes:

* coordinates
* simple key => value snaks (e.g. wikibase-item or url data types)
* labels and aliases ("terms")
* descriptions

# Requirements

* postgresql-9.3
* postgresql-9.3-postgis-2.1
* postgresql-contrib-9.3 (for hstore extension)

More recent versions of postgresql and extensions are probably okay.

# Install

Create a postgresql user and database.

```
sudo -u postgres psql
createuser -s -P username
createdb -E UTF8 -O username wikidata
exit
```

Then, apply the schema ([sql/schema.sql](sql/schema.sql)).

```
psql -U username -f sql/schema.sql wikidata
```

# Use

Put dumps somewhere such as:

```
~/dumps/dumpfiles/wikidatawiki/json-20150601
```

After building a runnable jar, then the tool can be run on the command line:


```
java -jar importer.jar -dbname wikidata -dbuser username -dbpass password -dumpdir /home/wikidata/dumps/
```

Populate geometry:

```
update coordinates set geom = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326);
create index idx_coords_geom ON coordinates using GIST(geom);
```

