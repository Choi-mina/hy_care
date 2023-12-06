package com.example.hycare.controller;

import ch.qos.logback.core.model.Model;
import com.example.hycare.dto.DiagnosisDto;
import com.example.hycare.dto.ResultDto;
import com.example.hycare.entity.ResultEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

// 화면 전환 Controller
@Slf4j
@Controller
@CrossOrigin(origins = {"http://18.183.4.163:8080", "http://18.183.4.163:3000"},allowedHeaders = "*")
public class WebController {
    @Value("${server.host.api}")
    private String baseUrl;


    // 홈화면 리다이렉트
    @RequestMapping("/")
    public String mainWeb(Model model, HttpServletRequest request) {
        // 세션 유무/ loginDiv 값에 따라 적절한 화면 return
        HttpSession session = request.getSession();
        if (session.getAttribute("email") != null && session.getAttribute("loginDiv").equals("0")) {    // 의사인 경우
            return "home/doctorHome.html";
        } else if (session.getAttribute("email") != null && session.getAttribute("loginDiv").equals("1")) { // 환자인 경우
            return "home/patientHome.html";
        }
        return "home/home.html";
    }

    // 상담 화면 리다이렉트
    @RequestMapping("/consult")
    public String consultWeb(Model model) {
        return "consult/consultStart.html";
    }

    // 의사 마이페이지 리다이렉트
    @RequestMapping("/doctor-myinfo")
    public String doctorMyInfoWeb(Model model) {
        return "myPage/doctorMyInfo.html";
    }

    @RequestMapping("/patient-myinfo")
    public String patientMyInfoWeb(Model model) {
        return "myPage/patientMyInfo.html";
    }

    // 로그인 화면 리다이렉트
    @RequestMapping("/login")
    public String loginWeb(Model model) {
        return "login/login.html";
    }

    @RequestMapping("/naver-callback")
    public String naverCallback(Model model) {
        return "login/callback.html";
    }

    // 로그인 성공 화면 리다이렉트
    @RequestMapping("/api/auth/login/complete")
    public String loginGoogleGet(@RequestParam String email, String loginDiv, org.springframework.ui.Model model, HttpServletRequest request) {

        HttpSession session = request.getSession();
        session.setAttribute("loginDiv", loginDiv);
        session.setAttribute("email", email);

        if(loginDiv == "0" || loginDiv.equals("0")) {   // 의사인 경우
            return "home/doctorHome.html";
        }
        // 환자인 경우
        return "home/patientHome.html";
    }

    // 로그아웃 (세션 삭제) 리다이렉트
    @RequestMapping("/session-remove")
    public String sessionRemove(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        log.info("session remove success");
        return "home/home.html";
    }

    @GetMapping("/getSessionValues")
    public ResponseEntity<Map<String, Object>> getSessionValues(HttpSession session) {
        Map<String, Object> sessionValues = new HashMap<>();
        sessionValues.put("loginDiv", session.getAttribute("loginDiv"));
        sessionValues.put("email", session.getAttribute("email"));
        return ResponseEntity.ok(sessionValues);
    }

    @GetMapping("/get-uuid-session")
    public ResponseEntity<Map<String, Object>> getUuidSession(HttpSession session) {
        Map<String, Object> sessionValues = new HashMap<>();
        sessionValues.put("loginDiv", session.getAttribute("loginDiv"));
        sessionValues.put("email", session.getAttribute("email"));
        if(session.getAttribute("loginDiv").equals("0")) {
            // UUID 랜덤 생성
            char str[] = new char[1];
            StringBuffer uuid = new StringBuffer();
            for (int i = 0; i < 10; i++) {
                str[0] = (char) ((Math.random() * 26) + 65);
                uuid.append(str);
            }
            // 진료 기록 생성 API 호출
            DiagnosisDto diagnosisDto = new DiagnosisDto();
            diagnosisDto.setDiagId(uuid.toString());
            diagnosisDto.setDoctorName((String) session.getAttribute("email"));
            String url = baseUrl + "/diagnosis/make";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity httpEntity = new HttpEntity<>(diagnosisDto, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ResultEntity> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    ResultEntity.class);

            session.setAttribute("uuid", uuid.toString());
        } else {    // 환자인 경우
            String url = baseUrl + "/diagnosis/find";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity httpEntity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<DiagnosisDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    DiagnosisDto.class);

            session.setAttribute("uuid", response.getBody().getDiagId());
        }
        sessionValues.put("uuid", session.getAttribute("uuid"));
        return ResponseEntity.ok(sessionValues);
    }

    // 결과화면 창 이동
    @PostMapping("/consult-result")
    public String processData(@RequestBody ResultDto resultDto,
                              org.springframework.ui.Model model) {

        // 처리된 데이터를 모델에 추가
        model.addAttribute("summary", resultDto.getSummary());
        model.addAttribute("symptom", resultDto.getSymptom());

        // 다른 페이지로 이동
        return "consult/result";
    }
}
