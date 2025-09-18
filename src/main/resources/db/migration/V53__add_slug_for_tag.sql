ALTER TABLE tags ADD COLUMN slug VARCHAR(255);
UPDATE tags SET slug = LOWER(REPLACE(name, ' ', ''));
CREATE UNIQUE INDEX idx_tags_slug ON tags(slug);