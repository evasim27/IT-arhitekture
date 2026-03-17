from functools import lru_cache
from pathlib import Path

from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


ROOT_ENV_FILE = Path(__file__).resolve().parents[2] / ".env"


class Settings(BaseSettings):
    app_name: str = "PriceScout Product Service"
    app_env: str = "development"

    database_url: str

    rabbitmq_enabled: bool = True
    rabbitmq_url: str | None = None
    rabbitmq_queue: str = "product_logs"

    @model_validator(mode="after")
    def validate_rabbitmq_settings(self) -> "Settings":
        if self.rabbitmq_enabled and not self.rabbitmq_url:
            raise ValueError("RABBITMQ_URL must be set when RABBITMQ_ENABLED is true")
        return self

    model_config = SettingsConfigDict(
        env_file=ROOT_ENV_FILE,
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()