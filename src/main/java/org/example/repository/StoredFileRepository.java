package org.example.repository;
import org.example.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StoredFileRepository extends JpaRepository<StoredFile,String> { }
