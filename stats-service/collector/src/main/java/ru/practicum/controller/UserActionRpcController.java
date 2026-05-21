package ru.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.stats.grpc.user.UserActionControllerGrpc;
import ru.practicum.stats.proto.user.UserActionProto;
import ru.practicum.user.UserActionHandler;


@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionRpcController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionHandler userActionHandlerMap;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            userActionHandlerMap.handle(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}