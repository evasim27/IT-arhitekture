import os

# Test environment is configured before importing app modules.
os.environ["DATABASE_URL"] = "sqlite://"
os.environ["RABBITMQ_ENABLED"] = "false"

from fastapi.testclient import TestClient
import pytest

from app.main import app
from app.database import Base, SessionLocal, engine
from app.models import Product


Base.metadata.create_all(bind=engine)


@pytest.fixture(autouse=True)
def clear_products_table() -> None:
    db = SessionLocal()
    try:
        db.query(Product).delete()
        db.commit()
    finally:
        db.close()


@pytest.fixture()
def db_session():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@pytest.fixture()
def client():
    with TestClient(app) as test_client:
        yield test_client