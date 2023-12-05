package com.example.hycare.chatGPT;

import io.github.flashvayne.chatgpt.service.ChatgptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatgptService chatgptService;


    @GetMapping("")
    public String getChatResponse(Map<Integer, String> stt) {

        // ChatGPT에게 질문
        return chatgptService.sendMessage(stt.values() + "병원 진료 대화 내용 3줄로 요약해줘.");

    }

    public String getClassification(List<String> symptom) {
        return chatgptService.sendMessage(symptom + "이 단어들 중 증상과 관련된 단어들만 뽑아서 최종적으로 어떤 증상에 해당하는지 단어로 알려줘");
    }


}
