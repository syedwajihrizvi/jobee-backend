-- Clean up string "null" values in experiences table
UPDATE experiences 
SET 
    city = CASE WHEN city = 'null' THEN NULL ELSE city END,
    state = CASE WHEN state = 'null' THEN NULL ELSE state END,
    country = CASE WHEN country = 'null' THEN NULL ELSE country END,
    company = CASE WHEN company = 'null' THEN NULL ELSE company END,
    position = CASE WHEN position = 'null' THEN NULL ELSE position END,
    description = CASE WHEN description = 'null' THEN NULL ELSE description END
WHERE 
    city = 'null' OR 
    state = 'null' OR 
    country = 'null' OR 
    company = 'null' OR 
    position = 'null' OR 
    description = 'null';

-- Clean up string "null" values in user_profiles table
UPDATE user_profiles 
SET 
    city = CASE WHEN city = 'null' THEN NULL ELSE city END,
    state = CASE WHEN state = 'null' THEN NULL ELSE state END,
    country = CASE WHEN country = 'null' THEN NULL ELSE country END,
    summary = CASE WHEN summary = 'null' THEN NULL ELSE summary END,
    profile_image_url = CASE WHEN profile_image_url = 'null' THEN NULL ELSE profile_image_url END
WHERE 
    city = 'null' OR 
    state = 'null' OR 
    country = 'null' OR 
    summary = 'null' OR 
    profile_image_url = 'null';

-- Clean up string "null" values in educations table  
UPDATE educations 
SET 
    institution = CASE WHEN institution = 'null' THEN NULL ELSE institution END,
    degree = CASE WHEN degree = 'null' THEN NULL ELSE degree END,
    field_of_study = CASE WHEN field_of_study = 'null' THEN NULL ELSE field_of_study END,
    description = CASE WHEN description = 'null' THEN NULL ELSE description END
WHERE 
    institution = 'null' OR 
    degree = 'null' OR 
    field_of_study = 'null' OR 
    description = 'null';