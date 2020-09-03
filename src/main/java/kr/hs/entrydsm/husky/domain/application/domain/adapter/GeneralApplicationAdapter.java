package kr.hs.entrydsm.husky.domain.application.domain.adapter;

import kr.hs.entrydsm.husky.domain.application.domain.GraduatedApplication;
import kr.hs.entrydsm.husky.domain.application.domain.UnGraduatedApplication;
import kr.hs.entrydsm.husky.domain.school.domain.School;
import kr.hs.entrydsm.husky.domain.user.domain.User;
import kr.hs.entrydsm.husky.domain.user.domain.enums.GradeType;
import kr.hs.entrydsm.husky.domain.user.dto.SetUserInfoRequest;
import lombok.Getter;

import static kr.hs.entrydsm.husky.domain.user.domain.enums.GradeType.GRADUATED;
import static kr.hs.entrydsm.husky.domain.user.domain.enums.GradeType.UNGRADUATED;

@Getter
public class GeneralApplicationAdapter {

    private GradeType gradeType;
    private UnGraduatedApplication unGraduatedApplication;
    private GraduatedApplication graduatedApplication;

    public void update(School school) {
        if (gradeType.equals(UNGRADUATED) && unGraduatedApplication != null)
            unGraduatedApplication.update(school);
        else if (gradeType.equals(GRADUATED) && graduatedApplication != null)
            graduatedApplication.update(school);
    }

    public void update(SetUserInfoRequest dto) {
        if (gradeType.equals(UNGRADUATED) && unGraduatedApplication != null)
            unGraduatedApplication.update(dto);
        else if (gradeType.equals(GRADUATED) && graduatedApplication != null)
            graduatedApplication.update(dto);
    }

    public GeneralApplicationAdapter(User user) {
        if (user.getGradeType() != null) {
            this.gradeType = user.getGradeType();
            if (user.isUngraduated())
                this.unGraduatedApplication = user.getUnGraduatedApplication();
            else if (user.isGraduated())
                this.graduatedApplication = user.getGraduatedApplication();
        }
    }

    public String getStudentNumber() {
        if (gradeType.equals(UNGRADUATED) && unGraduatedApplication != null)
            return unGraduatedApplication.getStudentNumber();

        if (gradeType.equals(GRADUATED) && graduatedApplication != null)
            return graduatedApplication.getStudentNumber();

        return null;
    }

    public String getSchoolCode() {
        if (gradeType.equals(UNGRADUATED) && unGraduatedApplication != null
                && unGraduatedApplication.getSchool() != null)
            return unGraduatedApplication.getSchool().getSchoolCode();

        if (gradeType.equals(GRADUATED) && graduatedApplication != null
                && graduatedApplication.getSchool() != null)
            return graduatedApplication.getSchool().getSchoolCode();

        return null;
    }

    public String getSchoolTel() {
        if (gradeType.equals(UNGRADUATED) && unGraduatedApplication != null)
            return unGraduatedApplication.getSchoolTel();

        if (gradeType.equals(GRADUATED) && graduatedApplication != null)
            return graduatedApplication.getSchoolTel();

        return null;
    }

}
