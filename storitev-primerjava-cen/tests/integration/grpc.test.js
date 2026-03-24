const { MockStoreServiceClient } = require("../../src/clients/storeServiceClient");
const { createServer } = require("../../src/server");
const { grpc, comparisonProto } = require("../../src/transport/grpc/protoLoader");

function createClient(address) {
  return new comparisonProto.PriceComparisonService(address, grpc.credentials.createInsecure());
}

describe("gRPC PriceComparisonService", () => {
  let server;
  let client;

  beforeAll(async () => {
    const storeClient = new MockStoreServiceClient({
      "1": [
        { store_id: "lidl", store_name: "Lidl", price: 6.49, currency: "EUR" },
        { store_id: "spar", store_name: "Spar", price: 6.99, currency: "EUR" },
      ],
      "3": [],
    });

    const logger = { info: jest.fn(), warn: jest.fn(), error: jest.fn() };
    server = createServer({ storeClient, logger });

    await new Promise((resolve, reject) => {
      server.bindAsync("127.0.0.1:50061", grpc.ServerCredentials.createInsecure(), (err) => {
        if (err) {
          reject(err);
          return;
        }
        server.start();
        resolve();
      });
    });

    client = createClient("127.0.0.1:50061");
  });

  afterAll(async () => {
    if (client) {
      client.close();
    }
    if (server) {
      await new Promise((resolve) => server.tryShutdown(resolve));
    }
  });

  test("GetPricesForProduct returns prices and lowest price", async () => {
    const response = await new Promise((resolve, reject) => {
      client.GetPricesForProduct({ product_id: "1" }, (err, res) => {
        if (err) {
          reject(err);
          return;
        }
        resolve(res);
      });
    });

    expect(response.product_id).toBe("1");
    expect(response.has_prices).toBe(true);
    expect(response.prices).toHaveLength(2);
    expect(response.lowest_price.store_name).toBe("Lidl");
  });

  test("GetLowestPrice returns NOT_FOUND when there are no prices", async () => {
    await expect(
      new Promise((resolve, reject) => {
        client.GetLowestPrice({ product_id: "3" }, (err, res) => {
          if (err) {
            reject(err);
            return;
          }
          resolve(res);
        });
      }),
    ).rejects.toMatchObject({ code: grpc.status.NOT_FOUND });
  });

  test("GetPricesForProduct validates empty product_id", async () => {
    await expect(
      new Promise((resolve, reject) => {
        client.GetPricesForProduct({ product_id: "" }, (err, res) => {
          if (err) {
            reject(err);
            return;
          }
          resolve(res);
        });
      }),
    ).rejects.toMatchObject({ code: grpc.status.INVALID_ARGUMENT });
  });
});