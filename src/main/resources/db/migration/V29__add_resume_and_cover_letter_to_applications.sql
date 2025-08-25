ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS resume_document_id BIGINT NOT NULL,
    ADD COLUMN IF NOT EXISTS cover_letter_document_id BIGINT,
    ADD CONSTRAINT fk_resume_document FOREIGN KEY (resume_document_id) REFERENCES user_documents (id),
    ADD CONSTRAINT fk_cover_letter_document FOREIGN KEY (cover_letter_document_id) REFERENCES user_documents (id);