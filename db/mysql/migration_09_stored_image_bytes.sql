-- Run in the selected CampusWork database after taking a backup.
-- Match the parent column collation, including installations created from older dumps.
SET @image_ref_charset = (
  SELECT character_set_name FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'stored_files' AND column_name = 'ref'
);
SET @image_ref_collation = (
  SELECT collation_name FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'stored_files' AND column_name = 'ref'
);
SET @image_table_ddl = CONCAT(
  'CREATE TABLE IF NOT EXISTS stored_image_bytes (',
  'file_ref VARCHAR(36) CHARACTER SET ', @image_ref_charset, ' COLLATE ', @image_ref_collation,
  ' NOT NULL PRIMARY KEY, image_bytes MEDIUMBLOB NOT NULL, ',
  'CONSTRAINT fk_stored_image_file FOREIGN KEY (file_ref) REFERENCES stored_files(ref) ON DELETE CASCADE',
  ') ENGINE=InnoDB'
);
PREPARE image_table_statement FROM @image_table_ddl;
EXECUTE image_table_statement;
DEALLOCATE PREPARE image_table_statement;
