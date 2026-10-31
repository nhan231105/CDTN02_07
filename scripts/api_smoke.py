"""Repeatable live REST acceptance checks for UC_01..UC_07 (Python stdlib only).

Creates isolated QA accounts/jobs in the running dev H2 database. Never connects
to MySQL, prints tokens, or modifies seed accounts. Exit 0 means all checks pass.
"""
from __future__ import annotations

import argparse
import base64
from concurrent.futures import ThreadPoolExecutor
from datetime import date, datetime, timedelta, timezone
import json
import math
from pathlib import Path
import secrets
import statistics
import sys
import time
import urllib.error
import urllib.parse
import urllib.request


def valid_pdf() -> bytes:
    """Generate a small real PDF with correct object offsets and xref table."""
    stream = b"BT /F1 12 Tf 40 100 Td (QA Student Curriculum Vitae) Tj ET"
    objects = [
        b"<< /Type /Catalog /Pages 2 0 R >>",
        b"<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 150] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>",
        b"<< /Length " + str(len(stream)).encode() + b" >>\nstream\n" + stream + b"\nendstream",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
    ]
    output = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
    offsets = [0]
    for number, obj in enumerate(objects, 1):
        offsets.append(len(output))
        output.extend(f"{number} 0 obj\n".encode() + obj + b"\nendobj\n")
    xref = len(output)
    output.extend(f"xref\n0 {len(offsets)}\n0000000000 65535 f \n".encode())
    for offset in offsets[1:]:
        output.extend(f"{offset:010d} 00000 n \n".encode())
    output.extend(f"trailer\n<< /Size {len(offsets)} /Root 1 0 R >>\nstartxref\n{xref}\n%%EOF\n".encode())
    return bytes(output)


class Smoke:
    def __init__(self, base_url: str):
        self.base = base_url.rstrip("/")
        self.results: list[dict] = []

    def request(self, method: str, path: str, token: str | None = None,
                data: dict | None = None, upload: tuple[str, str, bytes] | None = None):
        headers = {"Accept": "application/json"}
        body = None
        if token:
            headers["Authorization"] = "Bearer " + token
        if data is not None:
            headers["Content-Type"] = "application/json"
            body = json.dumps(data).encode()
        if upload:
            filename, media_type, content = upload
            boundary = "qa-" + secrets.token_hex(12)
            headers["Content-Type"] = "multipart/form-data; boundary=" + boundary
            body = (f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; filename=\"{filename}\"\r\n"
                    f"Content-Type: {media_type}\r\n\r\n").encode() + content + f"\r\n--{boundary}--\r\n".encode()
        start = time.perf_counter()
        request = urllib.request.Request(self.base + path, data=body, headers=headers, method=method)
        try:
            response = urllib.request.urlopen(request, timeout=20)
        except urllib.error.HTTPError as error:
            response = error
        raw = response.read()
        content_type = response.headers.get("Content-Type", "")
        parsed = json.loads(raw) if raw and "json" in content_type else raw
        return response.code, parsed, round((time.perf_counter() - start) * 1000, 2)

    def record(self, name: str, passed: bool, **details):
        self.results.append({"name": name, "status": "PASS" if passed else "FAIL", **details})
        print(("PASS " if passed else "FAIL ") + name)

    def expect(self, name: str, method: str, path: str, expected: int | tuple[int, ...], **kwargs):
        status, body, elapsed = self.request(method, path, **kwargs)
        allowed = (expected,) if isinstance(expected, int) else expected
        details = {"method": method, "path": path, "expectedHttp": list(allowed), "actualHttp": status,
                   "durationMs": elapsed}
        if status not in allowed and isinstance(body, dict):
            details["errorCode"] = body.get("code")
            details["message"] = body.get("message")
        self.record(name, status in allowed, **details)
        if status not in allowed:
            raise AssertionError(f"{name}: HTTP {status}, expected {allowed}")
        return body

    def assert_that(self, name: str, condition: bool):
        self.record(name, bool(condition))
        if not condition:
            raise AssertionError(name)

    def login(self, identifier: str, password: str, name: str):
        body = self.expect(name, "POST", "/auth/login", 200,
                           data={"identifier": identifier, "password": password})
        user = body.get("data", body)
        self.assert_that(name + ": token metadata", bool(user.get("accessToken")) and bool(user.get("expiresAt")))
        return user

    def run(self, samples: int):
        password = "DemoPass123!"
        suffix = secrets.token_hex(5)
        phone = lambda: "0" + str(secrets.randbelow(900000000) + 100000000)
        student_data = {"fullName": "QA Student " + suffix, "email": f"qa.student.{suffix}@example.test",
                        "phone": phone(), "password": password, "role": "STUDENT"}
        employer_data = {"fullName": "QA Employer " + suffix, "email": f"qa.employer.{suffix}@example.test",
                         "phone": phone(), "password": password, "role": "EMPLOYER"}
        self.expect("Health endpoint", "GET", "/auth/health", 200)
        self.expect("UC01 missing JWT", "GET", "/me/profile", 401)
        self.expect("UC01 invalid JWT", "GET", "/me/profile", 401, token="invalid.jwt.value")
        self.expect("UC01 wrong password", "POST", "/auth/login", 401,
                    data={"identifier": "student.a@demo.local", "password": "WrongPass123!"})
        self.expect("UC02 reject ADMIN registration", "POST", "/auth/register", 400,
                    data={**student_data, "role": "ADMIN"})
        self.expect("UC02 reject weak password", "POST", "/auth/register", 400,
                    data={**student_data, "password": "123456"})
        self.expect("UC02 register student", "POST", "/auth/register", 201, data=student_data)
        self.expect("UC02 duplicate email", "POST", "/auth/register", 409,
                    data={**student_data, "phone": phone()})
        self.expect("UC02 duplicate phone", "POST", "/auth/register", 409,
                    data={**student_data, "email": f"duplicate.{suffix}@example.test"})
        self.expect("UC02 register employer", "POST", "/auth/register", 201, data=employer_data)
        student = self.login(student_data["email"], password, "UC01 student email login")
        employer = self.login(employer_data["phone"], password, "UC01 employer phone login")
        student_b = self.login("student.b@demo.local", password, "UC01 Student B seed login")
        employer_b = self.login("employer.b@demo.local", password, "UC01 Employer B seed login")
        admin = self.login("admin@demo.local", password, "UC01 admin seed login")
        st, et, sb, eb = (student["accessToken"], employer["accessToken"],
                          student_b["accessToken"], employer_b["accessToken"])
        profile = self.expect("UC03 profile exists after register", "GET", "/me/profile", 200, token=st)
        self.assert_that("UC03 own profile identity", profile["id"] == student["id"])
        self.expect("UC03 update profile", "PUT", "/me/profile", 200, token=st,
                    data={"fullName": "QA Updated Student", "bio": "Saved through the real REST API and H2"})
        saved = self.expect("UC03 profile reload", "GET", "/me/profile", 200, token=st)
        self.assert_that("UC03 profile persisted", saved["fullName"] == "QA Updated Student" and "real REST" in saved["bio"])
        self.expect("UC03 cross-account legacy profile forbidden", "GET", f"/profiles/students/{student['id']}", 403, token=sb)
        self.expect("UC03 reject fake PDF", "POST", "/me/cv", 415, token=st,
                    upload=("fake.pdf", "application/pdf", b"this is not a PDF"))
        self.expect("UC03 reject wrong media type", "POST", "/me/cv", 415, token=st,
                    upload=("cv.txt", "text/plain", b"not PDF"))
        self.expect("UC03 reject oversized CV", "POST", "/me/cv", 413, token=st,
                    upload=("large.pdf", "application/pdf", b"%PDF-1.4\n" + b"x" * (5 * 1024 * 1024)))
        cv = self.expect("UC03 upload valid PDF", "POST", "/me/cv", 201, token=st,
                         upload=("qa-cv.pdf", "application/pdf", valid_pdf()))
        self.assert_that("UC03 CV metadata", bool(cv.get("ref")) and cv.get("size", 0) > 0)
        downloaded = self.expect("UC03 download own CV", "GET", "/me/cv", 200, token=st)
        self.assert_that("UC03 actual PDF download", isinstance(downloaded, bytes) and downloaded.startswith(b"%PDF-"))
        png = base64.b64decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jNioAAAAASUVORK5CYII=")
        self.expect("UC03 upload avatar", "POST", "/me/avatar", 201, token=st,
                    upload=("avatar.png", "image/png", png))
        self.expect("UC03 download avatar", "GET", "/me/avatar", 200, token=st)
        listing = self.expect("UC04 public job list", "GET", "/jobs?size=100", 200)
        self.assert_that("UC04 max 12 jobs per page", listing["size"] <= 12 and len(listing["content"]) <= 12)
        self.assert_that("UC04 seeded pagination", listing["totalElements"] > 12 and listing["totalPages"] > 1)
        self.assert_that("UC04 public jobs are OPEN", all(j["status"] == "OPEN" for j in listing["content"]))
        second = self.expect("UC04 second page", "GET", "/jobs?page=1&size=12", 200)
        self.assert_that("UC04 pages do not overlap", not ({j["id"] for j in listing["content"]} & {j["id"] for j in second["content"]}))
        job_data = {"title": "QA Opportunity " + suffix, "description": "Real integration testing opportunity for a student developer.",
                    "requirements": "Basic web development", "benefits": "Mentoring", "category": "Development",
                    "location": "QA City", "workSchedule": "Part-time", "salary": 150000,
                    "deadline": str(date.today() + timedelta(days=30))}
        self.expect("UC05 student cannot create job", "POST", "/jobs", 403, token=st, data=job_data)
        job = self.expect("Employer creates support job", "POST", "/jobs", 201, token=et, data=job_data)
        jid = job["id"]
        self.expect("UC04 public job detail", "GET", f"/jobs/{jid}", 200)
        params = urllib.parse.urlencode({"keyword": suffix, "category": "Development", "location": "QA City",
                                       "minSalary": 100000, "maxSalary": 200000, "workSchedule": "Part-time"})
        filtered = self.expect("UC04 combined filters", "GET", "/jobs?" + params, 200)
        self.assert_that("UC04 combined filters exact result", filtered["totalElements"] == 1 and filtered["content"][0]["id"] == jid)
        wrong = self.expect("UC04 filters use AND", "GET", "/jobs?" + params.replace("QA+City", "Missing+City"), 200)
        self.assert_that("UC04 incompatible location returns empty", wrong["totalElements"] == 0)
        app_data = {"fullName": "QA Updated Student", "email": student_data["email"], "phone": student_data["phone"],
                    "coverLetter": "I would like to apply for this opportunity.", "cvFileRef": cv["ref"], "consentDataUsage": True}
        self.expect("UC05 missing JWT cannot apply", "POST", f"/jobs/{jid}/applications", 401, data=app_data)
        self.expect("UC05 employer cannot apply", "POST", f"/jobs/{jid}/applications", 403, token=et, data=app_data)
        self.expect("UC05 consent required", "POST", f"/jobs/{jid}/applications", 400, token=st,
                    data={**app_data, "consentDataUsage": False})
        self.expect("UC05 another student's CV forbidden", "POST", f"/jobs/{jid}/applications", 403, token=sb, data=app_data)
        application = self.expect("UC05 apply with own CV", "POST", f"/jobs/{jid}/applications", 201, token=st, data=app_data)
        aid = application["id"]
        self.expect("UC05 duplicate application", "POST", f"/jobs/{jid}/applications", 409, token=st, data=app_data)
        mine = self.expect("UC05 own application history", "GET", "/me/applications", 200, token=st)
        self.assert_that("UC05 history persisted", any(a["id"] == aid for a in mine["content"]))
        self.expect("UC05 other employer cannot list applicants", "GET", f"/jobs/{jid}/applications", 403, token=eb)
        apps = self.expect("Employer lists own applicants", "GET", f"/jobs/{jid}/applications", 200, token=et)
        self.assert_that("Employer sees saved application", any(a["id"] == aid for a in apps["content"]))
        self.expect("UC03 unrelated employer cannot download CV", "GET", f"/applications/{aid}/cv", 403, token=eb)
        self.expect("UC03 own employer downloads application CV", "GET", f"/applications/{aid}/cv", 200, token=et)
        self.expect("UC06 review before approval forbidden", "POST", f"/applications/{aid}/reviews", 409,
                    token=st, data={"rating": 5, "content": "Premature"})
        self.expect("Other employer cannot decide", "PATCH", f"/applications/{aid}/decision", 403,
                    token=eb, data={"decision": "APPROVED"})
        self.expect("Employer approves application", "PATCH", f"/applications/{aid}/decision", 200,
                    token=et, data={"decision": "APPROVED"})
        self.expect("Employer cannot decide twice", "PATCH", f"/applications/{aid}/decision", 409,
                    token=et, data={"decision": "REJECTED"})
        notifications = self.expect("UC07 notification list", "GET", "/me/notifications", 200, token=st)
        matching = [n for n in notifications["content"] if n.get("relatedId") == aid]
        self.assert_that("UC07 decision created exactly one notification", len(matching) == 1)
        nid = matching[0]["id"]
        self.expect("UC07 Student B cannot read A notification", "GET", f"/me/notifications/{nid}", 403, token=sb)
        self.expect("UC07 Student B cannot modify A notification", "PATCH", f"/me/notifications/{nid}/read", 403, token=sb)
        unread = self.expect("UC07 unread count before read", "GET", "/me/notifications/count/unread", 200, token=st)
        self.expect("UC07 mark own notification read", "PATCH", f"/me/notifications/{nid}/read", 200, token=st)
        after = self.expect("UC07 unread count after read", "GET", "/me/notifications/count/unread", 200, token=st)
        self.assert_that("UC07 badge decreases", after == unread - 1)
        reread = self.expect("UC07 read state persists", "GET", f"/me/notifications/{nid}", 200, token=st)
        self.assert_that("UC07 stored READ status", reread["status"] == "READ")
        self.expect("UC06 review before completion forbidden", "POST", f"/applications/{aid}/reviews", 409,
                    token=st, data={"rating": 5, "content": "Still premature"})
        self.expect("Employer invalid transition OPEN to COMPLETED", "PATCH", f"/jobs/{jid}/status", 409,
                    token=et, data={"status": "COMPLETED"})
        self.expect("Employer transitions OPEN to FILLED", "PATCH", f"/jobs/{jid}/status", 200,
                    token=et, data={"status": "FILLED"})
        self.expect("UC05 closed job rejects application", "POST", f"/jobs/{jid}/applications", 409, token=st, data=app_data)
        self.expect("Employer transitions FILLED to COMPLETED", "PATCH", f"/jobs/{jid}/status", 200,
                    token=et, data={"status": "COMPLETED"})
        self.expect("UC06 Student B cannot review A application", "POST", f"/applications/{aid}/reviews", 403,
                    token=sb, data={"rating": 5, "content": "Not my application"})
        self.expect("UC06 rating outside 1..5 rejected", "POST", f"/applications/{aid}/reviews", 400,
                    token=st, data={"rating": 6, "content": "Invalid rating"})
        self.expect("UC06 create employer review", "POST", f"/applications/{aid}/reviews", 201,
                    token=st, data={"rating": 5, "content": "Clear expectations and supportive employer."})
        self.expect("UC06 duplicate review rejected", "POST", f"/applications/{aid}/reviews", 409,
                    token=st, data={"rating": 4, "content": "Duplicate"})
        reviews = self.expect("UC06 reload real reviews", "GET", f"/reviews/jobs/{jid}", 200)
        self.assert_that("UC06 review saved in backend", reviews["totalElements"] == 1)
        # Replacing the profile CV must preserve the file referenced by an application.
        self.expect("UC03 replace current CV", "POST", "/me/cv", 201, token=st,
                    upload=("replacement.pdf", "application/pdf", valid_pdf()))
        self.expect("UC03 application CV snapshot still downloadable", "GET", f"/applications/{aid}/cv", 200, token=et)
        race_job = self.expect("Concurrency creates isolated job", "POST", "/jobs", 201, token=et,
                               data={**job_data, "title": "QA Race " + suffix})
        with ThreadPoolExecutor(max_workers=2) as pool:
            futures = [pool.submit(self.request, "POST", f"/jobs/{race_job['id']}/applications", st, app_data) for _ in range(2)]
            statuses = sorted(f.result()[0] for f in futures)
        self.assert_that("UC05 concurrent duplicate gives one 201 and one 409", statuses == [201, 409])
        race_apps = self.expect("Concurrency reload application", "GET", f"/jobs/{race_job['id']}/applications", 200, token=et)
        self.assert_that("UC05 one application row after concurrency", race_apps["totalElements"] == 1)
        race_aid = race_apps["content"][0]["id"]
        with ThreadPoolExecutor(max_workers=2) as pool:
            futures = [pool.submit(self.request, "PATCH", f"/applications/{race_aid}/decision", et, {"decision": decision})
                       for decision in ("APPROVED", "REJECTED")]
            statuses = sorted(f.result()[0] for f in futures)
        self.assert_that("Concurrent decision gives one 200 and one 409", statuses == [200, 409])
        notifications = self.expect("Concurrent decision notification reload", "GET", "/me/notifications", 200, token=st)
        self.assert_that("Concurrent decision creates exactly one notification",
                         len([n for n in notifications["content"] if n.get("relatedId") == race_aid]) == 1)
        self.expect("UC03 account requires current password", "PATCH", "/me/account", 400, token=st,
                    data={"email": f"changed.{suffix}@example.test"})
        self.expect("UC03 account contact change", "PATCH", "/me/account", 200, token=st,
                    data={"email": f"changed.{suffix}@example.test", "currentPassword": password})
        self.expect("UC03 account 48-hour rate limit", "PATCH", "/me/account", 409, token=st,
                    data={"phone": phone(), "currentPassword": password})
        self.expect("Admin route requires role", "GET", "/auth/admin/stats", 403, token=st)
        self.expect("Admin statistics role access", "GET", "/auth/admin/stats", 200, token=admin["accessToken"])
        durations = [self.request("GET", "/jobs?size=12")[2] for _ in range(samples)]
        p95 = sorted(durations)[max(0, math.ceil(samples * .95) - 1)]
        self.record("NFR04 live API list latency p95 <= 3 seconds", p95 <= 3000,
                    samples=samples, meanMs=round(statistics.mean(durations), 2), p95Ms=p95, maxMs=max(durations),
                    environment="Loopback HTTP, running Java Spring Boot dev-h2, current machine; excludes browser render")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://localhost:8082/api")
    parser.add_argument("--report", default="docs/evidence/api-smoke.json")
    parser.add_argument("--benchmark-samples", type=int, default=30)
    args = parser.parse_args()
    if not 1 <= args.benchmark_samples <= 1000:
        parser.error("--benchmark-samples must be between 1 and 1000")
    smoke = Smoke(args.base_url)
    try:
        smoke.run(args.benchmark_samples)
    except Exception as error:
        smoke.record("Scenario completed without interruption", False, message=str(error))
    passed = sum(result["status"] == "PASS" for result in smoke.results)
    failed = len(smoke.results) - passed
    report = {"timestampUtc": datetime.now(timezone.utc).isoformat(), "baseUrl": args.base_url,
              "database": "dev-h2 expected; verify backend startup log", "passed": passed, "failed": failed,
              "notes": "Creates isolated QA users/jobs. Does not test MySQL or browser rendering. No tokens stored.",
              "tests": smoke.results}
    path = Path(args.report)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"RESULT: {passed} PASS, {failed} FAIL. Evidence: {path}")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
