package com.example.hycare.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.example.hycare.chatGPT.ChatGPTDto;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.hycare.entity.ApiResult.S3_FAIL;

@Component
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 s3Client;

    // summary S3 저장
    public String upload(File uploadFile, String filePath, String uuid) {
        String fileName = filePath + "/" + uuid + "/" + uuid + "_chatGPT.json";   // S3에 저장될 파일 이름
        String uploadImageUrl = putS3(uploadFile, fileName); // s3로 업로드
        return uploadImageUrl;
    }

    // 결과지 S3 저장
    public String uploadResult(File uploadFile, String filePath, String uuid) {
        String fileName = filePath + "/" + uuid + "/" + uuid + "_result.png";   // S3에 저장될 파일 이름
        String uploadImageUrl = putS3(uploadFile, fileName); // s3로 업로드
        return uploadImageUrl;
    }

    // S3로 업로드
    private String putS3(File uploadFile, String fileName) {
        try{
            amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        }catch (Exception e) {
            return S3_FAIL.getMessage();
        }
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // s3 파일 읽기
    public ChatGPTDto findFile(String storedFileName) throws IOException {
       ChatGPTDto chatGPTDto = new ChatGPTDto();
        S3Object o = s3Client.getObject(new GetObjectRequest(bucket, storedFileName));
        // s3에서 가져온 파일 읽기
        S3ObjectInputStream ois = null;
        BufferedReader br = null;
        ois = o.getObjectContent();
        br = new BufferedReader (new InputStreamReader(ois, "UTF-8"));

        String line = br.readLine();

        // json 객체로 변환
        JSONObject jsonParser = new JSONObject(line);

        // dto에 setting
        chatGPTDto.setStt((String) jsonParser.get("stt"));
        chatGPTDto.setSummary((String) jsonParser.get("summary"));

        return chatGPTDto;
    }
}