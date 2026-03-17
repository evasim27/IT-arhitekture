from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models import Product
from app.schemas import ProductCreate


def create_product(db: Session, product_in: ProductCreate) -> Product:
    product = Product(
        name=product_in.name,
        description=product_in.description,
        category=product_in.category,
    )
    db.add(product)
    db.commit()
    db.refresh(product)
    return product


def get_products(db: Session, skip: int = 0, limit: int = 100) -> list[Product]:
    stmt = select(Product).offset(skip).limit(limit).order_by(Product.id.asc())
    return list(db.scalars(stmt).all())


def get_product_by_id(db: Session, product_id: int) -> Product | None:
    return db.get(Product, product_id)


def search_products_by_name(db: Session, name_query: str, skip: int = 0, limit: int = 100) -> list[Product]:
    pattern = f"%{name_query}%"
    stmt = (
        select(Product)
        .where(Product.name.ilike(pattern))
        .offset(skip)
        .limit(limit)
        .order_by(Product.id.asc())
    )
    return list(db.scalars(stmt).all())


def update_product(db: Session, product: Product, product_in: ProductCreate) -> Product:
    product.name = product_in.name
    product.description = product_in.description
    product.category = product_in.category

    db.commit()
    db.refresh(product)
    return product


def delete_product(db: Session, product: Product) -> None:
    db.delete(product)
    db.commit()