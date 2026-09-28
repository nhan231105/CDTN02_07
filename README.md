# Freelance Part-time Platform API

Backend REST API sử dụng FastAPI, SQLAlchemy và MySQL/PostgreSQL.

## Chạy local

1. Tạo môi trường ảo và cài dependencies:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

2. Sao chép `.env.example` thành `.env` và cập nhật `DATABASE_URL`.

3. Khởi động API:

```powershell
uvicorn main:app --reload
```

Tài liệu Swagger có tại `http://127.0.0.1:8000/docs`.
