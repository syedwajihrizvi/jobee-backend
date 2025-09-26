ALTER TABLE interview_preparation_questions
    ADD COLUMN IF NOT EXISTS question_audio_url VARCHAR(255);

ALTER TABLE interview_preparation_questions
    ADD COLUMN IF NOT EXISTS answer_audio_url VARCHAR(255);