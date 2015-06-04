Scripts for parsing and importing a Wikidata dump into a database (e.g. postgres).

It imports the current page revisions into a table, with the following columns:

* id (numeric entity id, integer)
* page id (integer)
* content (text)
* rev id (integer)
* entity type (text)

The dump is then accessible and can then be further processed with whatever tools one likes.

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

Set the appropriate postgresql permissions in pg_hba.conf:

```
local   all  all  md5
```


Then, apply the schema ([sql/schema.sql](sql/schema.sql)).

```
psql -U username -f sql/schema.sql wikidata
```
