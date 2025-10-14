ALTER TABLE user_profiles
ADD COLUMN profile_views INT DEFAULT 0;

CREATE TABLE favorite_companies (
    user_profile_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    PRIMARY KEY (user_profile_id, company_id),
    CONSTRAINT fk_favorite_companies_user_profile FOREIGN KEY (user_profile_id) REFERENCES user_profiles(id),
    CONSTRAINT fk_favorite_companies_company FOREIGN KEY (company_id) REFERENCES companies(id)
);