package com.nikeshchaudhary.authify.service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.nikeshchaudhary.authify.entity.UserEntity;
import com.nikeshchaudhary.authify.io.ProfileRequest;
import com.nikeshchaudhary.authify.io.ProfileResponse;
import com.nikeshchaudhary.authify.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileServiceImp implements ProfileService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // === User Profile Management ===

    @Override
    public ProfileResponse createProfile(ProfileRequest profileRequest) {
        if (profileRepository.existsByEmail(profileRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserEntity newProfile = convertToUserEntity(profileRequest);
        newProfile = profileRepository.save(newProfile);
        return convertToProFileResponse(newProfile);
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity userEntity = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found " + email));

        return convertToProFileResponse(userEntity);
    }

    // === Password Reset ===

    @Override
    public void sendResetOtp(String email) {
        UserEntity userEntity = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String otp = generateOtp();
        userEntity.setResetPasswordOtp(otp);
        userEntity.setResetPasswordOtpExpiresAt(Instant.now().plusSeconds(5 * 60));

        profileRepository.save(userEntity);

        emailService.sendResetOtpEmail(userEntity.getName(), userEntity.getEmail(), otp);
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity userEntity = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!otp.equals(userEntity.getResetPasswordOtp()) ||
                userEntity.getResetPasswordOtpExpiresAt() == null ||
                userEntity.getResetPasswordOtpExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userEntity.setResetPasswordOtp(null);
        userEntity.setResetPasswordOtpExpiresAt(null);

        profileRepository.save(userEntity);
    }

    // === Account Verification ===

    @Override
    public void sendOtp(String email) {
        UserEntity userEntity = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String otp = generateOtp();
        userEntity.setVerificationOtp(otp);
        userEntity.setVerificationOtpExpiresAt(Instant.now().plusSeconds(5 * 60));

        profileRepository.save(userEntity);

        emailService.sendVerificationOtp(userEntity.getName(), userEntity.getEmail(), otp);
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity userEntity = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!otp.equals(userEntity.getVerificationOtp()) ||
                userEntity.getVerificationOtpExpiresAt() == null ||
                userEntity.getVerificationOtpExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        userEntity.setAccountVerified(true);
        userEntity.setVerificationOtp(null);
        userEntity.setVerificationOtpExpiresAt(null);

        profileRepository.save(userEntity);
    }

    // === Helper Methods ===

    private UserEntity convertToUserEntity(ProfileRequest profileRequest) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .name(profileRequest.getName())
                .email(profileRequest.getEmail())
                .password(passwordEncoder.encode(profileRequest.getPassword()))
                .build();
    }

    private ProfileResponse convertToProFileResponse(UserEntity userEntity) {
        return ProfileResponse.builder()
                .userId(userEntity.getUserId())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .isAccountVerified(userEntity.isAccountVerified())
                .build();
    }

    private String generateOtp() {
        Random random = new Random();
        int otpNumber = 100_000 + random.nextInt(900_000);
        return String.valueOf(otpNumber);
    }
}