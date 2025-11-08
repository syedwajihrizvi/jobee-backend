ALTER TABLE invitations
    DROP COLUMN IF EXISTS invite_token,
    DROP COLUMN IF EXISTS qr_code_url;