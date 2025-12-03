package kr.ac.dankook.ace.healthy_meal_backend.grpc;

import kr.ac.dankook.ace.healthy_meal_backend.proto.AnalyzeResult;
import kr.ac.dankook.ace.healthy_meal_backend.proto.ImageRequest;
import kr.ac.dankook.ace.healthy_meal_backend.proto.MealAnalyzeServiceGrpc;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import kr.ac.dankook.ace.healthy_meal_backend.service.MealRecordService;
import kr.ac.dankook.ace.healthy_meal_backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


import java.util.Base64;
import java.util.List;

@RequiredArgsConstructor
@GrpcService
public class MealAnalyzeGrpcService extends MealAnalyzeServiceGrpc.MealAnalyzeServiceImplBase{

    private final StorageService storageService;
    private final MealRecordService mealRecordService;

    private static final Logger logger = LoggerFactory.getLogger(MealAnalyzeGrpcService.class);

    @Override
    public void uploadImage(ImageRequest request, StreamObserver<AnalyzeResult> responseObserver) {
        try {
            String originalFileName = request.getFilename();
            String filetype = request.getFiletype();
            ByteString imageData =  request.getImagedata();
            byte[] rawBytes = imageData.toByteArray();

            MultipartFile multipartFile = new GrpcMultipartFile("file", originalFileName, filetype, rawBytes);
            String filename = storageService.storeTemp(multipartFile);

            String base64Image = Base64.getEncoder().encodeToString(rawBytes);
            List<String> result = mealRecordService.gptAnalyzeImage(base64Image);
            List<Integer> weights = mealRecordService.getFoodWeight(result);

            AnalyzeResult analyzeResult = AnalyzeResult.newBuilder()
                    .setFilename(filename)
                    .addAllFoodResult(result)
                    .addAllFoodWeight(weights)
                    .build();
            responseObserver.onNext(analyzeResult);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("gRPC API Handling 오류 : {}", e.getMessage());
            responseObserver.onError(e);
        }
    }

}
