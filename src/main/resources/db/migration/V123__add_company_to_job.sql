ALTER TABLE jobs
ADD COLUMN company_id BIGINT;

ALTER TABLE jobs
ADD CONSTRAINT fk_company
FOREIGN KEY (company_id) REFERENCES companies(id);