const { MockStoreServiceClient } = require("./clients/storeServiceClient");
const config = require("./config");
const logger = require("./logger");
const { PriceComparisonService } = require("./domain/priceComparisonService");
const { grpc, comparisonProto } = require("./transport/grpc/protoLoader");
const { createPriceComparisonController } = require("./transport/grpc/priceComparisonController");

function createServer(options = {}) {
  const server = new grpc.Server();
  const storeClient = options.storeClient || new MockStoreServiceClient();
  const serviceLogger = options.logger || logger;
  const comparisonService = new PriceComparisonService(storeClient, serviceLogger);
  const controller = createPriceComparisonController(comparisonService, serviceLogger);

  server.addService(comparisonProto.PriceComparisonService.service, controller);
  return server;
}

function startServer(server = createServer()) {
  const bindAddress = `${config.grpcHost}:${config.grpcPort}`;

  return new Promise((resolve, reject) => {
    server.bindAsync(bindAddress, grpc.ServerCredentials.createInsecure(), (error) => {
      if (error) {
        logger.error(`Failed to bind gRPC server on ${bindAddress}: ${error.message}`);
        reject(error);
        return;
      }

      server.start();
      logger.info(`Price comparison gRPC server is listening on ${bindAddress}`);
      resolve(server);
    });
  });
}

module.exports = { createServer, startServer };