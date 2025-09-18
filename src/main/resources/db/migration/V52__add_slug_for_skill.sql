ALTER TABLE skills ADD COLUMN slug VARCHAR(255);
UPDATE skills SET slug = LOWER(REPLACE(name, ' ', '-'));
ALTER TABLE skills ALTER COLUMN slug SET NOT NULL;
CREATE UNIQUE INDEX idx_skills_slug ON skills(slug);