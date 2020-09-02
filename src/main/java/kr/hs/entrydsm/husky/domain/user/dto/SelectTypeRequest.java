package kr.hs.entrydsm.husky.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectTypeRequest {

    @NotEmpty @NotBlank
    private String gradeType;
    @NotEmpty @NotBlank
    private String applyType;
    @NotEmpty @NotBlank
    private String additionalType;
    private Boolean isDaejeon;
    @Pattern(regexp = "2[0-9][0-9][0-9]-[0-1][0-9]")
    private String graduatedDate;
    @Pattern(regexp = "2[0-9][0-9][0-9]-[0-1][0-9]")
    private String gedPassDate;

}
