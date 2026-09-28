from fastapi import FastAPI


app = FastAPI(
    title="Freelance Part-time Platform API",
    description="REST API for connecting students with freelance part-time jobs.",
    version="1.0.0",
)


@app.get("/", tags=["System"])
def read_root() -> dict[str, str]:
    return {"message": "Freelance Part-time Platform API"}


@app.get("/health", tags=["System"])
def health_check() -> dict[str, str]:
    return {"status": "ok"}