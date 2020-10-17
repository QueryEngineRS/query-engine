package com.wumbo.queryengine;

import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.netty.NettyServerBuilder;

import io.grpc.stub.StreamObserver;
import net.runelite.api.Client;
import org.wumbo.queryengine.grpc.QueryCoordinatesRandomRequest;
import org.wumbo.queryengine.grpc.QueryCoordinatesRequest;
import org.wumbo.queryengine.grpc.QueryCoordinatesResponse;
import org.wumbo.queryengine.grpc.QueryEngineServiceGrpc.*;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class QueryEngineServer {
    private Server server;
    final String host = "";
    final int port = 0;

    private final QueryEnginePlugin plugin;
    private final QueryEngineConfig config;
    private final Client client;

    @Inject
    public QueryEngineServer(QueryEnginePlugin plugin, QueryEngineConfig config, Client client) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;
    }

    public void start() throws IOException {
        server = NettyServerBuilder.forAddress(new InetSocketAddress(host, port))
                        .addService(new QueryEngineServiceImpl(this.client, this.plugin))
                        .build()
                        .start();

        log.info("Query Engine Server started, listening on " + port);
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread() {
                            @Override
                            public void run() {
                                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                                System.err.println("*** shutting down gRPC server");
                                QueryEngineServer.this.stop();
                                System.err.println("*** server shut down");
                            }
                        });
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public static class QueryEngineServiceImpl extends QueryEngineServiceImplBase {
        private final QueryEnginePlugin plugin;
        private final Client client;

        public QueryEngineServiceImpl(Client client, QueryEnginePlugin plugin) {
            this.client = client;
            this.plugin = plugin;
        }

        @Override
        public void queryCoordinates(QueryCoordinatesRequest request, StreamObserver<QueryCoordinatesResponse> responseObserver) {
            var objects = this.plugin.getObjects();
            var obj = objects.getOrDefault(request.getObjectId(), null);

            if (obj == null) {
                throw new NullPointerException("object does not exist");
            }

            if (obj.getPlane() != this.client.getPlane()) {
                throw new RuntimeException("object is not on the same plane");
            }

            var clickBox = obj.getClickbox();
            if (clickBox == null) {
                throw new NullPointerException("object does not have a clickbox");
            }

            var bounds = clickBox.getBounds();
            var x = (int) bounds.getCenterX();
            var y = (int) bounds.getCenterY();

            QueryCoordinatesResponse resp = QueryCoordinatesResponse.newBuilder().setX(x).setY(y).build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        }

        @Override
        public void queryCoordinatesRandom(QueryCoordinatesRandomRequest request, StreamObserver<QueryCoordinatesResponse> responseObserver) {
            QueryCoordinatesResponse resp = QueryCoordinatesResponse.newBuilder().setX(0).setY(0).build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        }
    }
}

