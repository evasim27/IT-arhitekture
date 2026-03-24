const path = require("path");
const grpc = require("@grpc/grpc-js");
const protoLoader = require("@grpc/proto-loader");

const protoPath = path.join(__dirname, "..", "..", "..", "proto", "comparison.proto");

const packageDefinition = protoLoader.loadSync(protoPath, {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
});

const comparisonProto = grpc.loadPackageDefinition(packageDefinition).pricescout.comparison;

module.exports = { grpc, comparisonProto };