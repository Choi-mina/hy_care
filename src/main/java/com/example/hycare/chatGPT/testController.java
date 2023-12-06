package com.example.hycare.chatGPT;

import com.example.hycare.Service.DiagnosisService;
import com.example.hycare.dto.DiagnosisDto;
import com.example.hycare.entity.ResultEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flashvayne.chatgpt.service.ChatgptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@CrossOrigin(origins = {"http://18.183.4.163:8080", "http://18.183.4.163:3000"},allowedHeaders = "*")
public class testController {
    private final ChatService chatService;
    private final DiagnosisService diagnosisService;
    public String summary;

    @Value("${server.host.api}")
    private String baseUrl;

    //chat-gpt API 호출
     @PostMapping("/summary")
     public String summary(@RequestBody Map<Integer, String> stt, @RequestParam String email) throws IOException {

        summary = chatService.getChatResponse(stt);

         // json 파일을 저장할 상대 directory 지정
         String path = System.getProperty("user.dir") + "/summary";
         File Folder = new File(path);
         // 해당 디렉토리가 없다면 디렉토리를 생성.
         if (!Folder.exists()) {
             try{
                 Folder.mkdir(); //폴더 생성합니다. ("새폴더"만 생성)
                 log.info("summary folder create success");
             }
             catch(Exception e){
                 e.getStackTrace();
             }
         }else {
             log.info("summary folder exists");
         }

         // ChatGPT 결과 -> json file로 변환해 로컬 저장
         ChatGPTDto chatGPTDto = new ChatGPTDto();
         chatGPTDto.setStt(stt.values().toString());
         chatGPTDto.setSummary(summary);
         ObjectMapper mapper = new ObjectMapper();
         mapper.writeValue(new File(path + "/chatGPTDto.json"), chatGPTDto);


         // S3에 저장할 수 있도록 API 호출
         DiagnosisDto diagnosisDto = diagnosisService.findDiagnosis();
         String url = baseUrl + "/s3-save/" + diagnosisDto.getDiagId() + "?email=" + email;
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         HttpEntity httpEntity = new HttpEntity<>(path, headers);
         RestTemplate restTemplate = new RestTemplate();
         ResponseEntity<ResultEntity> response = restTemplate.exchange(
                 url,
                 HttpMethod.POST,
                 httpEntity,
                 ResultEntity.class);

         if(response.getStatusCode() == HttpStatus.OK) { // API 호출 성공
             log.info("ChatGPT >>> S3 SUCCESS");
         } else {   // API 호출 실패
             log.error("ChatGPT >>> S3 FAIL");
         }

        return summary;

    }

    @PostMapping("/classification")
    public String classification(@RequestBody List<String> symptom) throws IOException {
         log.info("ChatGPT classification API");
        String classification = chatService.getClassification(symptom);

        if(classification.contains("요."))
            classification = classification.replace("요.", "");
        else if(classification.contains("요"))
            classification = classification.replace("요", "");
        else if(classification.contains("\n"))
            classification = classification.replace("\n", "");

        return classification;
    }
}

