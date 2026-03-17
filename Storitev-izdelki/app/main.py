from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, HTTPException, Path, Query, Request, Response, status
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session

from app import crud, models, schemas
from app.config import get_settings
from app.database import Base, engine, get_db
from app.logging_rabbitmq import configure_logging

settings = get_settings()
logger = configure_logging(settings)


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    Base.metadata.create_all(bind=engine)
    logger.info("Product service started and database schema ensured")
    yield

app = FastAPI(
    title=settings.app_name,
    version="1.0.0",
    docs_url="/docs",
    lifespan=lifespan,
)


@app.exception_handler(Exception)
async def handle_unexpected_exception(_: Request, exc: Exception) -> JSONResponse:
    logger.exception("Unhandled server error: %s", exc)
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={"detail": "Internal server error"},
    )


@app.post("/products", response_model=schemas.ProductRead, status_code=status.HTTP_201_CREATED)
def create_product(product_in: schemas.ProductCreate, db: Session = Depends(get_db)) -> schemas.ProductRead:
    product = crud.create_product(db, product_in)
    logger.info(
        "Created product",
        extra={"product_id": product.id, "product_name": product.name},
    )
    return product


@app.get("/products", response_model=list[schemas.ProductRead])
def list_products(
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=500),
    db: Session = Depends(get_db),
) -> list[schemas.ProductRead]:
    products = crud.get_products(db, skip=skip, limit=limit)
    logger.info("Fetched product list", extra={"count": len(products)})
    return products


@app.get("/products/search", response_model=list[schemas.ProductRead])
def search_products(
    name: str = Query(..., min_length=1),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=500),
    db: Session = Depends(get_db),
) -> list[schemas.ProductRead]:
    products = crud.search_products_by_name(db, name_query=name, skip=skip, limit=limit)
    logger.info("Searched products", extra={"query": name, "count": len(products)})
    return products


@app.get("/products/{product_id}", response_model=schemas.ProductRead)
def get_product(
    product_id: int = Path(..., ge=1),
    db: Session = Depends(get_db),
) -> schemas.ProductRead:
    product = crud.get_product_by_id(db, product_id)
    if product is None:
        logger.error("Product not found", extra={"product_id": product_id})
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")

    logger.info("Fetched product details", extra={"product_id": product_id})
    return product


@app.put("/products/{product_id}", response_model=schemas.ProductRead)
def update_product(
    product_in: schemas.ProductCreate,
    product_id: int = Path(..., ge=1),
    db: Session = Depends(get_db),
) -> schemas.ProductRead:
    product = crud.get_product_by_id(db, product_id)
    if product is None:
        logger.error("Product not found for update", extra={"product_id": product_id})
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")

    updated_product = crud.update_product(db, product, product_in)
    logger.info("Updated product", extra={"product_id": product_id})
    return updated_product


@app.delete("/products/{product_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_product(
    product_id: int = Path(..., ge=1),
    db: Session = Depends(get_db),
) -> Response:
    product = crud.get_product_by_id(db, product_id)
    if product is None:
        logger.error("Product not found for delete", extra={"product_id": product_id})
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")

    crud.delete_product(db, product)
    logger.info("Deleted product", extra={"product_id": product_id})
    return Response(status_code=status.HTTP_204_NO_CONTENT)