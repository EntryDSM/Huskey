package kr.hs.entrydsm.husky.domain.application.service;

import kr.hs.entrydsm.husky.domain.application.dto.*;
import kr.hs.entrydsm.husky.domain.application.exception.ApplicationNotFoundException;
import kr.hs.entrydsm.husky.domain.application.exception.ApplicationTypeUnmatchedException;
import kr.hs.entrydsm.husky.domain.user.exception.UserNotFoundException;
import kr.hs.entrydsm.husky.entities.applications.GEDApplication;
import kr.hs.entrydsm.husky.entities.applications.GeneralApplication;
import kr.hs.entrydsm.husky.entities.applications.GraduatedApplication;
import kr.hs.entrydsm.husky.entities.applications.UnGraduatedApplication;
import kr.hs.entrydsm.husky.entities.applications.repositories.GEDApplicationRepository;
import kr.hs.entrydsm.husky.entities.applications.repositories.GraduatedApplicationRepository;
import kr.hs.entrydsm.husky.entities.applications.repositories.UnGraduatedApplicationRepository;
import kr.hs.entrydsm.husky.entities.users.User;
import kr.hs.entrydsm.husky.entities.users.enums.GradeType;
import kr.hs.entrydsm.husky.entities.users.repositories.UserRepository;
import kr.hs.entrydsm.husky.security.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApplicationService {

    private final UserRepository userRepository;
    private final GEDApplicationRepository gedApplicationRepository;
    private final GraduatedApplicationRepository graduatedApplicationRepository;
    private final UnGraduatedApplicationRepository unGraduatedApplicationRepository;

    private final AuthenticationFacade authenticationFacade;

    public void setIntro(SetDocsRequest request) {
        Integer receiptCode = authenticationFacade.getReceiptCode();
        User user = userRepository.findByReceiptCode(receiptCode)
                .orElseThrow(UserNotFoundException::new);

        user.setSelfIntroduction(request.getContent());
        userRepository.save(user);
    }

    public IntroResponse getIntro() {
        Integer receiptCode = authenticationFacade.getReceiptCode();
        User user = userRepository.findByReceiptCode(receiptCode)
                .orElseThrow(UserNotFoundException::new);

        return new IntroResponse(user.getSelfIntroduction());
    }

    public void setPlan(SetDocsRequest request) {
        Integer receiptCode = authenticationFacade.getReceiptCode();
        User user = userRepository.findByReceiptCode(receiptCode)
                .orElseThrow(UserNotFoundException::new);

        user.setStudyPlan(request.getContent());
        userRepository.save(user);
    }

    public PlanResponse getPlan() {
        Integer receiptCode = authenticationFacade.getReceiptCode();
        User user = userRepository.findByReceiptCode(receiptCode)
                .orElseThrow(UserNotFoundException::new);

        return new PlanResponse(user.getStudyPlan());
    }

    public void setGedScore(SetGedScoreRequest request) {
        Integer receiptCode = authenticationFacade.getReceiptCode();
        User user = userRepository.findByReceiptCode(receiptCode)
                .orElseThrow(UserNotFoundException::new);

        if (!user.isGed())
            throw new ApplicationTypeUnmatchedException();

        GEDApplication application = gedApplicationRepository.findByReceiptCode(receiptCode)
                .orElseThrow(ApplicationNotFoundException::new);

        application.setGedAverageScore(request.getGedAverageScore());
        gedApplicationRepository.save(application);
    }

    public void setScore(SetScoreRequest request) {
        Integer receiptCode = authenticationFacade.getReceiptCode();
        User user = userRepository.findByReceiptCode(receiptCode)
                .orElseThrow(UserNotFoundException::new);

        if (!user.isGraduated() && !user.isUngraduated())
            throw new ApplicationTypeUnmatchedException();

        switch (user.getGradeType()) {
            case GRADUATED: {
                GraduatedApplication graduatedApplication =
                        graduatedApplicationRepository.findByReceiptCode(receiptCode)
                        .orElseThrow(ApplicationNotFoundException::new);

                graduatedApplication.setScore(request.getVolunteerTime(), request.getFullCutCount(),
                        request.getPeriodCutCount(), request.getLateCount(), request.getEarlyLeaveCount(),
                        request.getKorean(), request.getSocial(), request.getHistory(), request.getMath(),
                        request.getScience(), request.getTechAndHome(), request.getEnglish());
                graduatedApplicationRepository.save(graduatedApplication);
                break;
            }

            case UNGRADUATED: {
                UnGraduatedApplication unGraduatedApplication =
                        unGraduatedApplicationRepository.findByReceiptCode(receiptCode)
                        .orElseThrow(ApplicationNotFoundException::new);

                unGraduatedApplication.setScore(request.getVolunteerTime(), request.getFullCutCount(),
                        request.getPeriodCutCount(), request.getLateCount(), request.getEarlyLeaveCount(),
                        request.getKorean(), request.getSocial(), request.getHistory(), request.getMath(),
                        request.getScience(), request.getTechAndHome(), request.getEnglish());
                unGraduatedApplicationRepository.save(unGraduatedApplication);
                break;
            }
        }
    }

    public ScoreResponse getScore() {
        Integer receiptCode = authenticationFacade.getReceiptCode();
        User user = userRepository.findByReceiptCode(receiptCode)
                .orElseThrow(UserNotFoundException::new);

        GeneralApplication generalApplication;

        GradeType gradeType = user.getGradeType();

        switch (gradeType) {
            case GED: {
                GEDApplication gedApplication = gedApplicationRepository.findByReceiptCode(receiptCode)
                        .orElseThrow(ApplicationNotFoundException::new);
                return ScoreResponse.gedResponse(gedApplication, gradeType);
            }

            case UNGRADUATED: {
                generalApplication = unGraduatedApplicationRepository.findByReceiptCode(receiptCode)
                        .orElseThrow(ApplicationNotFoundException::new);
                break;
            }

            case GRADUATED: {
                generalApplication = graduatedApplicationRepository.findByReceiptCode(receiptCode)
                        .orElseThrow(ApplicationNotFoundException::new);
                break;
            }

            default:
                throw new ApplicationNotFoundException();
        }
        return ScoreResponse.response(generalApplication, gradeType);
    }

}
