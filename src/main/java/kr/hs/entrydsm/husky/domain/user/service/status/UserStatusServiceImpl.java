package kr.hs.entrydsm.husky.domain.user.service.status;

import kr.hs.entrydsm.husky.domain.application.domain.CalculatedScore;
import kr.hs.entrydsm.husky.domain.grade.service.GradeCalcService;
import kr.hs.entrydsm.husky.domain.process.service.ProcessService;
import kr.hs.entrydsm.husky.domain.schedule.dao.ScheduleRepository;
import kr.hs.entrydsm.husky.domain.schedule.domain.Schedule;
import kr.hs.entrydsm.husky.domain.user.dto.UserPassResponse;
import kr.hs.entrydsm.husky.domain.user.dto.UserStatusResponse;
import kr.hs.entrydsm.husky.domain.user.exception.NotCompletedProcessException;
import kr.hs.entrydsm.husky.domain.user.exception.NotStartedScheduleException;
import kr.hs.entrydsm.husky.domain.user.exception.ScheduleNotFoundException;
import kr.hs.entrydsm.husky.domain.user.exception.UserNotFoundException;
import kr.hs.entrydsm.husky.domain.user.domain.Status;
import kr.hs.entrydsm.husky.domain.user.domain.User;
import kr.hs.entrydsm.husky.domain.user.domain.repositories.StatusRepository;
import kr.hs.entrydsm.husky.domain.user.domain.repositories.UserRepository;
import kr.hs.entrydsm.husky.global.config.security.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserStatusServiceImpl implements UserStatusService {

    private final AuthenticationFacade authFacade;

    private final UserRepository userRepository;
    private final StatusRepository statusRepository;
    private final ScheduleRepository scheduleRepository;

    private final ProcessService processService;
    private final GradeCalcService gradeCalcService;

    @Override
    public UserStatusResponse getStatus() {
        Integer receiptCode = authFacade.getReceiptCode();
        User user = userRepository.findById(receiptCode)
                .orElseThrow(UserNotFoundException::new);

        Status status = statusRepository.findById(receiptCode)
                .orElseGet(() -> statusRepository.save(new Status(receiptCode)));

        return UserStatusResponse.response(user, status);
    }

    @Override
    public UserStatusResponse finalSubmit() {
        Integer receiptCode = authFacade.getReceiptCode();
        User user = userRepository.findById(receiptCode)
                .orElseThrow(UserNotFoundException::new);

        Status status = statusRepository.findById(receiptCode)
                .orElseGet(() -> statusRepository.save(new Status(receiptCode)));

        CalculatedScore score = gradeCalcService.calcStudentGrade(user);

        if (!processService.allCheck(user, score))
            throw new NotCompletedProcessException();

        status.finalSubmit();
        statusRepository.save(status);

        return UserStatusResponse.response(user, status);
    }

    @Override
    public UserPassResponse isPassedFirst() {
        Integer receiptCode = authFacade.getReceiptCode();

        Schedule schedule = scheduleRepository.findById("first_notice")
                .orElseThrow(ScheduleNotFoundException::new);

        if(LocalDateTime.now().compareTo(schedule.getStartDate()) < 0)
            throw new NotStartedScheduleException();

        Status status = statusRepository.findById(receiptCode)
                .orElseGet(() -> statusRepository.save(new Status(receiptCode)));

        return UserPassResponse.builder()
                .isPassed(status.isPassedFirstApply())
                .build();
    }

    @Override
    public UserPassResponse isPassedFinal() {
        Integer receiptCode = authFacade.getReceiptCode();

        Schedule schedule = scheduleRepository.findById("notice")
                .orElseThrow(ScheduleNotFoundException::new);

        if(LocalDateTime.now().compareTo(schedule.getStartDate()) < 0)
            throw new NotStartedScheduleException();

        Status status = statusRepository.findById(receiptCode)
                .orElseGet(() -> statusRepository.save(new Status(receiptCode)));

        return UserPassResponse.builder()
                .isPassed(status.isPassedInterview())
                .build();
    }

}
