-- Remove circular foreign key dependency between interviews and interview_preparations
-- Keep interview_id in interview_preparations (child references parent)
-- Remove preparation_id from interviews (parent should not reference child)

ALTER TABLE interviews DROP COLUMN IF EXISTS preparation_id;
