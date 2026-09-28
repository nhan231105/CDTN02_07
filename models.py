from __future__ import annotations

from datetime import datetime
from decimal import Decimal
from enum import Enum

from sqlalchemy import Boolean, DateTime, Enum as SqlEnum, ForeignKey, Numeric, String, Text, UniqueConstraint, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from database import Base


class UserRole(str, Enum):
    ADMIN = "ROLE_ADMIN"
    EMPLOYER = "ROLE_EMPLOYER"
    STUDENT = "ROLE_STUDENT"


class JobStatus(str, Enum):
    OPEN = "OPEN"
    IN_PROGRESS = "IN_PROGRESS"
    CLOSED = "CLOSED"


class ApplicationStatus(str, Enum):
    PENDING = "PENDING"
    ACCEPTED = "ACCEPTED"
    REJECTED = "REJECTED"


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(primary_key=True)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True)
    password_hash: Mapped[str] = mapped_column(String(255))
    role: Mapped[UserRole] = mapped_column(
        SqlEnum(UserRole, name="user_role", values_callable=lambda enum: [item.value for item in enum]),
        default=UserRole.STUDENT,
    )
    enabled: Mapped[bool] = mapped_column(Boolean, default=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now(), onupdate=func.now())

    profile: Mapped[Profile | None] = relationship(back_populates="user", uselist=False, cascade="all, delete-orphan")
    jobs: Mapped[list[Job]] = relationship(back_populates="employer", foreign_keys="Job.employer_id")
    applications: Mapped[list[Application]] = relationship(back_populates="student", foreign_keys="Application.student_id")


class Profile(Base):
    __tablename__ = "profiles"

    id: Mapped[int] = mapped_column(primary_key=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), unique=True)
    full_name: Mapped[str] = mapped_column(String(150))
    phone: Mapped[str | None] = mapped_column(String(20))
    avatar_url: Mapped[str | None] = mapped_column(String(500))
    cv_url: Mapped[str | None] = mapped_column(String(500))
    company_name: Mapped[str | None] = mapped_column(String(255))
    company_description: Mapped[str | None] = mapped_column(Text)
    company_website: Mapped[str | None] = mapped_column(String(500))
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now(), onupdate=func.now())

    user: Mapped[User] = relationship(back_populates="profile")


class Job(Base):
    __tablename__ = "jobs"

    id: Mapped[int] = mapped_column(primary_key=True)
    employer_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="RESTRICT"), index=True)
    title: Mapped[str] = mapped_column(String(255))
    description: Mapped[str] = mapped_column(Text)
    salary: Mapped[Decimal | None] = mapped_column(Numeric(15, 2))
    location: Mapped[str | None] = mapped_column(String(255))
    status: Mapped[JobStatus] = mapped_column(
        SqlEnum(JobStatus, name="job_status", values_callable=lambda enum: [item.value for item in enum]),
        default=JobStatus.OPEN,
        index=True,
    )
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now(), onupdate=func.now())

    employer: Mapped[User] = relationship(back_populates="jobs", foreign_keys=[employer_id])
    applications: Mapped[list[Application]] = relationship(back_populates="job", cascade="all, delete-orphan")


class Application(Base):
    __tablename__ = "applications"
    __table_args__ = (UniqueConstraint("student_id", "job_id", name="uk_applications_student_job"),)

    id: Mapped[int] = mapped_column(primary_key=True)
    student_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), index=True)
    job_id: Mapped[int] = mapped_column(ForeignKey("jobs.id", ondelete="CASCADE"), index=True)
    status: Mapped[ApplicationStatus] = mapped_column(
        SqlEnum(ApplicationStatus, name="application_status", values_callable=lambda enum: [item.value for item in enum]),
        default=ApplicationStatus.PENDING,
    )
    applied_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now(), onupdate=func.now())

    student: Mapped[User] = relationship(back_populates="applications", foreign_keys=[student_id])
    job: Mapped[Job] = relationship(back_populates="applications")