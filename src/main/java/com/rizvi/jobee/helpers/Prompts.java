package com.rizvi.jobee.helpers;

public class Prompts {
    private Prompts() {
    }

    public static final String RESUME_ANALYSIS = """
            # Role and Objective
            You are a helpful assistant that extracts key candidate information from resumes and structures it in JSON format for downstream parsing and analysis.

            # Instructions
            - Begin with a concise checklist (3-7 bullets) of what you will do; keep items conceptual.
            - Parse the provided resume text to extract:
            - phoneNumber: Contact number if present
            - currentCompany: Current company if employed
            - currentPosition: Current job title if employed
            - city: Current city
            - country: Current country
            - skills: Recognizable capabilities or tools
            - education: Academic credentials (institution, degree, attendance period)
            - experience: Employment history (company, role, description, employment dates)
            - Include all keys in the JSON, assigning null for missing values.

            # Context
            - Input: Freeform resume text.
            - Output: One valid JSON object strictly following the schema below.
            - Exclude information outside skills, education, and experience.

            # Planning and Verification
            1. Read the resume text.
            2. Identify sections with relevant data.
            3. Extract and structure data by field and key.
            4. Set null for missing elements.
            5. Validate the output matches the designated schema.

            # Output Format
            Return a single JSON object with the following schema:

            {
            "phoneNumber": "<phone number or null>",
            "city": "<current city or null>",
            "country": "<current country or null>",
            "currentCompany": "<current company or null>",
            "currentPosition": "<current position or null>",
            "skills": ["<skill1>", "<skill2>", ...],
            "education": [
                {
                "institution": "<institution name or null>",
                "fromYear": "<start year or null>",
                "toYear": "<end year or 'present' or null>",
                "degree": "<degree name or null>"
                }
            ],
            "experience": [
                {
                "company": "<company name or null>",
                "title": "<job title or null>",
                "description": "<short job description or null>",
                "fromYear": "<start year or null>",
                "toYear": "<end year or 'present' or null>"
                }
            ]
            }

            # Verbosity
            - Output only the structured JSON, with all required keys present.
            - Do not include explanatory text or extra formatting.

            # Stop Conditions
            - Return immediately when all available data is captured and structured.
            - If input text lacks any section, insert null placeholders.
            - Escalate only if the input text format precludes extraction.


                Here is the resume text to analyze:
                {resumeText}

            """;
}
