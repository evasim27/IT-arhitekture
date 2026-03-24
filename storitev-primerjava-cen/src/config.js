const config = {
  grpcHost: process.env.GRPC_HOST || "0.0.0.0",
  grpcPort: Number(process.env.GRPC_PORT || 50051),
  logLevel: process.env.LOG_LEVEL || "info",
};

module.exports = config;