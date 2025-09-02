ALTER TABLE interviews
    DROP CONSTRAINT interviews_interviewer_id_fkey;

ALTER TABLE interviews
    DROP COLUMN interviewer_id;