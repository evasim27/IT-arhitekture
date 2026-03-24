const { PriceComparisonService } = require("../../src/domain/priceComparisonService");

describe("PriceComparisonService", () => {
  const logger = {
    info: jest.fn(),
    warn: jest.fn(),
    error: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test("returns lowest price from a list", () => {
    const service = new PriceComparisonService({}, logger);
    const prices = [
      { store_id: "a", store_name: "A", price: 5.99, currency: "EUR" },
      { store_id: "b", store_name: "B", price: 4.49, currency: "EUR" },
      { store_id: "c", store_name: "C", price: 6.19, currency: "EUR" },
    ];

    const result = service.getLowestPrice(prices);

    expect(result.store_name).toBe("B");
    expect(result.price).toBe(4.49);
  });

  test("returns null when price list is empty", () => {
    const service = new PriceComparisonService({}, logger);

    const result = service.getLowestPrice([]);

    expect(result).toBeNull();
  });

  test("throws for empty product_id", async () => {
    const mockClient = {
      getPricesForProduct: jest.fn(),
    };
    const service = new PriceComparisonService(mockClient, logger);

    await expect(service.getPricesForProduct("")).rejects.toThrow("product_id must not be empty");
  });

  test("compareProduct returns prices and lowest price", async () => {
    const mockClient = {
      getPricesForProduct: jest.fn().mockResolvedValue([
        { store_id: "a", store_name: "A", price: 9.99, currency: "EUR" },
        { store_id: "b", store_name: "B", price: 8.99, currency: "EUR" },
      ]),
    };
    const service = new PriceComparisonService(mockClient, logger);

    const result = await service.compareProduct("1");

    expect(result.prices).toHaveLength(2);
    expect(result.lowestPrice.store_name).toBe("B");
  });
});