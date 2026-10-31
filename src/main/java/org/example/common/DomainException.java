package org.example.common;
import lombok.Getter;
@Getter
public class DomainException extends RuntimeException {
    private final int status;
    private final String code;
    public DomainException(int status, String code, String message) { super(message); this.status=status; this.code=code; }
}
