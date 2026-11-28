package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stored_image_bytes")
public class StoredImage {
    @Id
    @Column(name = "file_ref", length = 36)
    private String fileRef;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_ref", foreignKey = @ForeignKey(name = "fk_stored_image_file"))
    private StoredFile file;

    @Lob
    @Column(name = "image_bytes", nullable = false, length = 4194304)
    private byte[] bytes;

    protected StoredImage() {}

    public StoredImage(StoredFile file, byte[] bytes) {
        this.file = file;
        this.bytes = bytes;
    }
}
