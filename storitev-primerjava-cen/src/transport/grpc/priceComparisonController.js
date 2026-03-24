const { grpc } = require("./protoLoader");

function toPriceInfo(price) {
  return {
    store_id: price.store_id,
    store_name: price.store_name,
    price: price.price,
    currency: price.currency,
  };
}

function createPriceComparisonController(comparisonService, logger) {
  return {
    async GetPricesForProduct(call, callback) {
      const productId = (call.request.product_id || "").trim();
      logger.info(`Received GetPricesForProduct request for product_id=${productId}`);

      try {
        const { prices, lowestPrice } = await comparisonService.compareProduct(productId);
        callback(null, {
          product_id: productId,
          prices: prices.map(toPriceInfo),
          lowest_price: lowestPrice ? toPriceInfo(lowestPrice) : {},
          has_prices: prices.length > 0,
        });
      } catch (error) {
        logger.error(`GetPricesForProduct failed for product_id=${productId}: ${error.message}`);
        if (error.message.includes("product_id must not be empty")) {
          callback({ code: grpc.status.INVALID_ARGUMENT, message: error.message });
          return;
        }
        callback({ code: grpc.status.INTERNAL, message: "Internal server error" });
      }
    },

    async GetLowestPrice(call, callback) {
      const productId = (call.request.product_id || "").trim();
      logger.info(`Received GetLowestPrice request for product_id=${productId}`);

      try {
        const { prices, lowestPrice } = await comparisonService.compareProduct(productId);
        if (!prices.length || !lowestPrice) {
          callback({ code: grpc.status.NOT_FOUND, message: `No prices found for product_id=${productId}` });
          return;
        }

        callback(null, {
          product_id: productId,
          lowest_price: toPriceInfo(lowestPrice),
          has_price: true,
        });
      } catch (error) {
        logger.error(`GetLowestPrice failed for product_id=${productId}: ${error.message}`);
        if (error.message.includes("product_id must not be empty")) {
          callback({ code: grpc.status.INVALID_ARGUMENT, message: error.message });
          return;
        }
        callback({ code: grpc.status.INTERNAL, message: "Internal server error" });
      }
    },
  };
}

module.exports = { createPriceComparisonController };