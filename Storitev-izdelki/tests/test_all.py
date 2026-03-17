from app import crud, schemas


def test_create_product(client):
    response = client.post(
        "/products",
        json={
            "name": "Nutella 750g",
            "description": "Lesnikov namaz",
            "category": "Namazi",
        },
    )

    assert response.status_code == 201
    payload = response.json()
    assert payload["id"] > 0
    assert payload["name"] == "Nutella 750g"


def test_get_products_and_get_by_id(client):
    create_response = client.post(
        "/products",
        json={"name": "Cokolada", "description": "Mlecna", "category": "Sladkarije"},
    )
    product_id = create_response.json()["id"]

    list_response = client.get("/products")
    item_response = client.get(f"/products/{product_id}")

    assert list_response.status_code == 200
    assert len(list_response.json()) == 1
    assert item_response.status_code == 200
    assert item_response.json()["id"] == product_id


def test_search_products(client):
    client.post("/products", json={"name": "Nutella 350g", "description": "", "category": "Namazi"})
    client.post("/products", json={"name": "Nutella B-ready", "description": "", "category": "Prigrizki"})
    client.post("/products", json={"name": "Kava", "description": "", "category": "Pijace"})

    response = client.get("/products/search", params={"name": "nutella"})

    assert response.status_code == 200
    assert len(response.json()) == 2


def test_get_product_not_found(client):
    response = client.get("/products/9999")

    assert response.status_code == 404
    assert response.json()["detail"] == "Product not found"


def test_create_product_validation_empty_name(client):
    response = client.post(
        "/products",
        json={"name": "", "description": "Opis", "category": "Test"},
    )

    assert response.status_code == 422


def test_update_product(client):
    create_response = client.post(
        "/products",
        json={"name": "Nutella 350g", "description": "Opis", "category": "Namazi"},
    )
    product_id = create_response.json()["id"]

    update_response = client.put(
        f"/products/{product_id}",
        json={"name": "Nutella 750g", "description": "Nov opis", "category": "Akcija"},
    )

    assert update_response.status_code == 200
    payload = update_response.json()
    assert payload["id"] == product_id
    assert payload["name"] == "Nutella 750g"
    assert payload["description"] == "Nov opis"
    assert payload["category"] == "Akcija"


def test_delete_product(client):
    create_response = client.post(
        "/products",
        json={"name": "Kava", "description": "Arabica", "category": "Pijace"},
    )
    product_id = create_response.json()["id"]

    delete_response = client.delete(f"/products/{product_id}")
    get_deleted_response = client.get(f"/products/{product_id}")

    assert delete_response.status_code == 204
    assert get_deleted_response.status_code == 404


def test_create_and_get_product(db_session):
    product_in = schemas.ProductCreate(
        name="Nutella 750g",
        description="Lesnikov namaz",
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