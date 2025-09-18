ALTER TABLE educations
        ALTER COLUMN degree DROP NOT NULL,
        ALTER COLUMN institution DROP NOT NULL,
        ALTER COLUMN from_year DROP NOT NULL;
ALTER TABLE user_skills
        ALTER COLUMN experience DROP NOT NULL;
ALTER TABLE experiences
        ALTER COLUMN description DROP NOT NULL;