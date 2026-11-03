-- Chạy MỘT LẦN trong MySQL Workbench trên DB freelance_marketplace.
USE freelance_marketplace;

ALTER TABLE employer_profiles
  ADD COLUMN cover_ref VARCHAR(255),
  ADD COLUMN tagline VARCHAR(150),
  ADD COLUMN founded_year INT,
  ADD COLUMN benefits VARCHAR(600);