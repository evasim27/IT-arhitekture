param(
  [switch]$StopAfterCheck
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
Set-Location $repoRoot

Write-Host "[1/5] Starting comparison-service..."
docker compose up -d comparison-service | Out-Host

$containerName = "pricescout-comparison-service"

Write-Host "[2/5] Waiting for container to run..."
$running = $false
for ($i = 0; $i -lt 20; $i++) {
  $status = docker inspect -f "{{.State.Running}}" $containerName 2>$null
  if ($status -eq "true") {
    $running = $true
    break
  }
  Start-Sleep -Seconds 1
}

if (-not $running) {
  throw "Container $containerName did not reach running state."
}

Write-Host "[3/5] Waiting for gRPC server readiness..."
$ready = $false
for ($i = 0; $i -lt 20; $i++) {
  $logs = docker compose logs --no-color --tail 50 comparison-service
  if ($logs -match "Price comparison gRPC server is listening on") {
    $ready = $true
    break
  }
  Start-Sleep -Seconds 1
}

if (-not $ready) {
  throw "gRPC server readiness log line was not detected in time."
}

Write-Host "[4/5] Executing gRPC smoke calls inside container..."
$nodeScript = @'
const grpc = require("@grpc/grpc-js");
const protoLoader = require("@grpc/proto-loader");

const packageDefinition = protoLoader.loadSync("/usr/src/app/proto/comparison.proto", {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
});

const comparison = grpc.loadPackageDefinition(packageDefinition).pricescout.comparison;
const client = new comparison.PriceComparisonService(
  "127.0.0.1:50051",
  grpc.credentials.createInsecure(),
);

function call(method, payload) {
  return new Promise((resolve, reject) => {
    client[method](payload, (err, response) => {
      if (err) {
        reject(err);
        return;
      }
      resolve(response);
    });
  });
}

(async () => {
  try {
    const prices = await call("GetPricesForProduct", { product_id: "1" });
    const lowest = await call("GetLowestPrice", { product_id: "1" });

    console.log("GetPricesForProduct:", JSON.stringify(prices));
    console.log("GetLowestPrice:", JSON.stringify(lowest));

    client.close();
  } catch (error) {
    console.error("gRPC call failed:", error.message);
    process.exit(1);
  }
})();
'@

$nodeScript | docker exec -i $containerName node -

if ($LASTEXITCODE -ne 0) {
  throw "Smoke calls failed."
}

if ($StopAfterCheck) {
  Write-Host "[5/5] Stopping comparison-service..."
  docker compose stop comparison-service | Out-Host
} else {
  Write-Host "[5/5] Done. comparison-service is still running."
}