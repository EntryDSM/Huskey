package kr.hs.entrydsm.husky.service.user;

import kr.hs.entrydsm.husky.domains.request.AccountRequest;
import kr.hs.entrydsm.husky.domains.request.VerifyCodeRequest;
import kr.hs.entrydsm.husky.domains.request.ChangePasswordRequest;
import kr.hs.entrydsm.husky.entities.verification.EmailVerification;
import kr.hs.entrydsm.husky.entities.verification.EmailVerificationStatus;
import kr.hs.entrydsm.husky.entities.verification.EmailVerificationRepository;
import kr.hs.entrydsm.husky.entities.users.User;
import kr.hs.entrydsm.husky.entities.users.repositories.UserRepository;
import kr.hs.entrydsm.husky.exceptions.*;
import kr.hs.entrydsm.husky.security.AuthenticationFacade;
import kr.hs.entrydsm.husky.service.email.EmailService;
import kr.hs.entrydsm.husky.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    private final AuthenticationFacade authenticationFacade;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void signUp(AccountRequest accountRequest) {
        String email = accountRequest.getEmail();
        String password = passwordEncoder.encode(accountRequest.getPassword());

        emailVerificationRepository.findById(email)
                .filter(EmailVerification::isVerified)
                .orElseThrow(ExpiredAuthCodeException::new);

        userRepository.save(
            User.builder()
                .email(email)
                .password(password)
                .createdAt(LocalDateTime.now())
                .build()
        );
    }

    @Override
    public void sendSignUpEmail(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException();
                });

        String code = randomCode();
        emailService.sendEmail(email, code);
        emailVerificationRepository.save(
            EmailVerification.builder()
                .email(email)
                .authCode(code)
                .status(EmailVerificationStatus.UNVERIFIED)
                .build()
        );
    }

    @Override
    public void authEmail(VerifyCodeRequest verifyCodeRequest) {
        String email = verifyCodeRequest.getEmail();
        String code = verifyCodeRequest.getAuthCode();
        EmailVerification emailVerification = emailVerificationRepository.findById(email)
                .orElseThrow(InvalidAuthEmailException::new);

        if (!emailVerification.getAuthCode().equals(code))
            throw new InvalidAuthCodeException();

        emailVerification.verify();
        emailVerificationRepository.save(emailVerification);
    }

    @Override
    public void sendPasswordChangeEmail(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        String code = randomCode();
        emailService.sendEmail(email, code);
        emailVerificationRepository.save(
                EmailVerification.builder()
                        .email(email)
                        .authCode(code)
                        .status(EmailVerificationStatus.UNVERIFIED)
                        .build()
        );
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        String email = changePasswordRequest.getEmail();
        String password = passwordEncoder.encode(changePasswordRequest.getPassword());

        emailVerificationRepository.findById(email)
                .filter(EmailVerification::isVerified)
                .orElseThrow(ExpiredAuthCodeException::new);

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        user.setPassword(password);
        userRepository.save(user);
    }

    private String randomCode() {
        StringBuilder result = new StringBuilder();
        String[] codes = "QWERTYUIOPASDFGHJKLZXCVBNM0123456789".split("");
        for (int i = 0; i < 6; i++) {
            result.append(codes[(int) (Math.random() % codes.length)]);
        }
        return result.toString();
    }

}
