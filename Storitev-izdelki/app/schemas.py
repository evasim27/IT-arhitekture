from typing import Annotated

from pydantic import BaseModel, ConfigDict, StringConstraints


class ProductCreate(BaseModel):
    name: Annotated[str, StringConstraints(strip_whitespace=True, min_length=1)]
    description: str | None = None
    category: str | None = None


class ProductRead(BaseModel):
    id: int
    name: str
    description: str | None = None
    category: str | None = None

    model_config = ConfigDict(from_attributes=True)