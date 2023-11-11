package com.example.hycare.controller;

import com.example.hycare.dto.DiagnosisDto;
import com.example.hycare.Service.DiagnosisService;
import com.example.hycare.entity.ResultEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/diagnosis")
public class DiagnosisController {
    private final DiagnosisService diagnosisService;

    @Value("${server.host.api}")
    private String baseUrl;

    @PostMapping("/save")
    public ResponseEntity<ResultEntity> saveDiagnosis (@RequestBody DiagnosisDto diagnosisDto) {
        try {
            diagnosisService.saveDiagnosis(diagnosisDto);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * id 타입 변경해야함*/
    @GetMapping("/find/{id}")
    public ResultEntity<DiagnosisDto> findById (@PathVariable("id") Long id) {
        try {
            DiagnosisDto diagnosisDto = diagnosisService.findData(id);
            return new ResultEntity<>(diagnosisDto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * id 타입 변경해야함*/
    @GetMapping("/find-diagText/{id}")
    public ResultEntity<Object> findDiagText (@PathVariable("id") Long id) {
        try {
            DiagnosisDto diagnosisDto = diagnosisService.findData(id);

            String[] diagUrl = diagnosisDto.getDiagLink().split("/");

            String[] s3find = diagUrl[4].split("_");
            Long diagId =  Long.parseLong(s3find[0]);


            // s3 조회 api 호출
            String url = baseUrl + "/s3-find/" + diagId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity httpEntity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ResultEntity> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    ResultEntity.class);


            return new ResultEntity<>(response.getBody().getData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
