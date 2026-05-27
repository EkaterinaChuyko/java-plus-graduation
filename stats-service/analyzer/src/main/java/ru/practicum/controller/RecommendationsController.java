package ru.practicum.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.service.AnalyzerService;
import ru.practicum.stats.grpc.dashboard.RecommendationsControllerGrpc;
import ru.practicum.stats.grpc.recommendation.InteractionsCountRequestProto;
import ru.practicum.stats.grpc.recommendation.RecommendedEventProto;
import ru.practicum.stats.grpc.recommendation.SimilarEventsRequestProto;
import ru.practicum.stats.grpc.recommendation.UserPredictionsRequestProto;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final AnalyzerService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Request to get recommendations for user {}", request.getUserId());

            recommendationService.getRecommendationsForUser(request)
                    .forEach(responseObserver::onNext);

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while calculating recommendations for user {}", request.getUserId(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Request for similar events for event {}", request.getEventId());

            recommendationService.getSimilarEvents(request)
                    .forEach(responseObserver::onNext);

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while calculating similar events for eventId={}", request.getEventId(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Request for interactions count for events {}", request.getEventIdList());

            recommendationService.getInteractionsCount(request)
                    .forEach(responseObserver::onNext);

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error while fetching interactions count for events {}", request.getEventIdList(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
