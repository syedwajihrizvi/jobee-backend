ALTER TABLE interview_preparation_resouces
    RENAME COLUMN resource_name TO title;

ALTER TABLE interview_preparation_resouces
    RENAME COLUMN resource_link TO link;

ALTER TABLE interview_preparation_resouces
    ADD COLUMN type VARCHAR(55);

ALTER TABLE interview_preparation_resouces
    ADD COLUMN description TEXT;
