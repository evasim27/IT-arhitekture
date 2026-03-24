class PriceComparisonService {
  constructor(storeServiceClient, logger) {
    this.storeServiceClient = storeServiceClient;
    this.logger = logger;
  }

  validateProductId(productId) {
    if (!productId || !String(productId).trim()) {
      throw new Error("product_id must not be empty");
    }
  }

  async getPricesForProduct(productId) {
    this.validateProductId(productId);
    this.logger.info(`Fetching prices for product_id=${productId}`);
    const prices = await this.storeServiceClient.getPricesForProduct(productId);
    this.logger.info(`Fetched ${prices.length} prices for product_id=${productId}`);
    return prices;
  }

  getLowestPrice(prices) {
    if (!prices || prices.length === 0) {
      this.logger.warn("No prices available for lowest-price calculation");
      return null;
    }

    const lowest = prices.reduce((min, current) => (current.price < min.price ? current : min));
    this.logger.info(
      `Calculated lowest price: store=${lowest.store_name} price=${lowest.price} ${lowest.currency}`,
    );
    return lowest;
  }

  async compareProduct(productId) {
    const prices = await this.getPricesForProduct(productId);
    const lowestPrice = this.getLowestPrice(prices);
    return { prices, lowestPrice };
  }
}

module.exports = { PriceComparisonService };