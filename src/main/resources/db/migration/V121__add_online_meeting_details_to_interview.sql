ALTER TABLE interviews
DROP COLUMN IF EXISTS meeting_link;

ALTER TABLE interviews
ADD COLUMN online_meeting_information JSONB;