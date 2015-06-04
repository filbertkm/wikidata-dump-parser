Scripts for parsing and importing a Wikidata dump into a database (e.g. postgres).

It imports the current page revisions into a table, with the following columns:

* id (numeric entity id, integer)
* page id (integer)
* content (text)
* rev id (integer)
* entity type (text)

The dump is then accessible and can then be further processed with whatever tools one likes.

# Install

Create a postgresql database and apply the schema ([sql/schema.sql](sql/schema.sql)).
