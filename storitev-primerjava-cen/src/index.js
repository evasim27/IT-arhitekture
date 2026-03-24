const logger = require("./logger");
const { createServer, startServer } = require("./server");

(async () => {
  try {
    const server = createServer();
    await startServer(server);

    const shutdown = () => {
      logger.info("Shutting down gRPC server");
      server.tryShutdown(() => process.exit(0));
    };

    process.on("SIGINT", shutdown);
    process.on("SIGTERM", shutdown);
  } catch (error) {
    logger.error(`Application startup failed: ${error.message}`);
    process.exit(1);
  }
})();