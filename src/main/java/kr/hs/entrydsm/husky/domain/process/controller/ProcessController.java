package kr.hs.entrydsm.husky.domain.process.controller;

import kr.hs.entrydsm.husky.domain.process.dto.ProcessResponse;
import kr.hs.entrydsm.husky.domain.process.service.ProcessServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/process/me")
@RestController
public class ProcessController {

    private final ProcessServiceImpl processService;

    @GetMapping
    public ProcessResponse getProcess() {
        return processService.getProcess();
    }

}
