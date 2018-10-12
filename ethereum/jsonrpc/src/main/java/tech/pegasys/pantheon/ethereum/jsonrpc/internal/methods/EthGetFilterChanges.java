package net.consensys.pantheon.ethereum.jsonrpc.internal.methods;

import net.consensys.pantheon.ethereum.core.Hash;
import net.consensys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import net.consensys.pantheon.ethereum.jsonrpc.internal.filter.FilterManager;
import net.consensys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import net.consensys.pantheon.ethereum.jsonrpc.internal.queries.LogWithMetadata;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import net.consensys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import net.consensys.pantheon.ethereum.jsonrpc.internal.results.LogsResult;

import java.util.List;
import java.util.stream.Collectors;

public class EthGetFilterChanges implements JsonRpcMethod {

  private final FilterManager filterManager;
  private final JsonRpcParameter parameters;

  public EthGetFilterChanges(final FilterManager filterManager, final JsonRpcParameter parameters) {
    this.filterManager = filterManager;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return "eth_getFilterChanges";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    final String filterId = parameters.required(request.getParams(), 0, String.class);

    final List<Hash> blockHashes = filterManager.blockChanges(filterId);
    if (blockHashes != null) {
      return new JsonRpcSuccessResponse(
          request.getId(),
          blockHashes.stream().map(h -> h.toString()).collect(Collectors.toList()));
    }

    final List<Hash> transactionHashes = filterManager.pendingTransactionChanges(filterId);
    if (transactionHashes != null) {
      return new JsonRpcSuccessResponse(
          request.getId(),
          transactionHashes.stream().map(h -> h.toString()).collect(Collectors.toList()));
    }

    final List<LogWithMetadata> logs = filterManager.logsChanges(filterId);
    if (logs != null) {
      return new JsonRpcSuccessResponse(request.getId(), new LogsResult(logs));
    }

    // Filter was not found.
    return new JsonRpcErrorResponse(request.getId(), JsonRpcError.FILTER_NOT_FOUND);
  }
}