from collections.abc import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, declarative_base, sessionmaker
from sqlalchemy.pool import StaticPool

from app.config import get_settings

settings = get_settings()


def _create_engine(database_url: str):
    if database_url.startswith("sqlite"):
        use_static_pool = database_url in {"sqlite://", "sqlite:///:memory:"}
        return create_engine(
            database_url,
            connect_args={"check_same_thread": False},
            poolclass=StaticPool if use_static_pool else None,
        )

    return create_engine(database_url)


engine = _create_engine(settings.database_url)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()