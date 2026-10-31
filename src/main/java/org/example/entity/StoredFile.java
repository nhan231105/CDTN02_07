package org.example.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="stored_files") @Data @NoArgsConstructor
public class StoredFile {
    @Id @Column(length=36) private String ref;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="owner_id",nullable=false) private User owner;
    @Column(nullable=false,length=255) private String originalName;
    @Column(nullable=false,length=100) private String contentType;
    @Column(nullable=false) private long size;
    @Column(nullable=false,unique=true,length=100) private String storageName;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
}
