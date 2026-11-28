package org.example.repository;

import org.example.entity.StoredImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StoredImageRepository extends JpaRepository<StoredImage, String> {
    @Query("select image.bytes from StoredImage image where image.fileRef = :ref")
    Optional<byte[]> findBytesByRef(String ref);
}
