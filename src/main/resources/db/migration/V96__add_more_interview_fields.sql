ALTER TABLE interviews
DROP COLUMN IF EXISTS location;

ALTER TABLE interviews
ADD COLUMN street_address VARCHAR(255),
ADD COLUMN building_name VARCHAR(255),
ADD COLUMN parking_info TEXT,
ADD COLUMN contact_instructions TEXT;

ALTER TABLE interviews
ADD COLUMN meeting_platform VARCHAR(100);