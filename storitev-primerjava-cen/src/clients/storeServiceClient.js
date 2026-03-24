class MockStoreServiceClient {
  constructor(pricesByProduct = null) {
    this.pricesByProduct =
      pricesByProduct || {
        "1": [
          { store_id: "lidl", store_name: "Lidl", price: 6.49, currency: "EUR" },
          { store_id: "spar", store_name: "Spar", price: 6.99, currency: "EUR" },
          { store_id: "mercator", store_name: "Mercator", price: 7.29, currency: "EUR" },
        ],
        "2": [
          { store_id: "tus", store_name: "Tus", price: 2.19, currency: "EUR" },
          { store_id: "spar", store_name: "Spar", price: 2.29, currency: "EUR" },
        ],
        "3": [],
      };
  }

  async getPricesForProduct(productId) {
    if (productId === "error") {
      throw new Error("Simulated store-service failure");
    }
    return this.pricesByProduct[productId] ? [...this.pricesByProduct[productId]] : [];
  }
}

module.exports = { MockStoreServiceClient };