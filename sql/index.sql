create index idx_label on label using btree (entity_id);
create index idx_alias on alias using btree (entity_id);
create index idx_description on description using btree (entity_id);
create index idx_claim_coordinate on claim_coordinate using btree (entity_id);
create index idx_claim_datetime on claim_datetime using btree (entity_id);
create index idx_claim_entity on claim_entity using btree (entity_id);
create index idx_claim_string on claim_string using btree (entity_id);
create index idx_sitelink on sitelink using btree (entity_id);
