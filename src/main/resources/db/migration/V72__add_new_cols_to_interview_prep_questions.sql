ALTER TABLE interview_preparation_questions
    ADD COLUMN ai_answer TEXT DEFAULT NULL,
    ADD COLUMN user_answer_score SMALLINT DEFAULT NULL;