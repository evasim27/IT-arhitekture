from app import crud, schemas


def test_create_and_get_product(db_session):
    product_in = schemas.ProductCreate(
        name="Nutella 750g",
        description="Lešnikov namaz",
        category="Napoji in namazi",
    )

    created = crud.create_product(db_session, product_in)
    fetched = crud.get_product_by_id(db_session, created.id)

    assert created.id is not None
    assert fetched is not None
    assert fetched.name == "Nutella 750g"


def test_search_products_by_partial_name(db_session):
    crud.create_product(db_session, schemas.ProductCreate(name="Nutella 350g", category="Namazi"))
    crud.create_product(db_session, schemas.ProductCreate(name="Bio Nutella Mix", category="Namazi"))
    crud.create_product(db_session, schemas.ProductCreate(name="Marmelada", category="Namazi"))

    results = crud.search_products_by_name(db_session, "nutella")

    assert len(results) == 2
    assert all("nutella" in product.name.lower() for product in results)