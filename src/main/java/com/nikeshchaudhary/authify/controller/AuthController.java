package com.nikeshchaudhary.authify.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nikeshchaudhary.authify.io.AuthRequest;
import com.nikeshchaudhary.authify.io.ResetPasswordRequest;
import com.nikeshchaudhary.authify.io.VerifyOtpRequest;
import com.nikeshchaudhary.authify.service.AuthService;
import com.nikeshchaudhary.authify.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ProfileService profileService;

    // === Authentication Endpoints ===

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthRequest authRequest) {
        String jwtToken = authService.login(authRequest.getEmail(), authRequest.getPassword());
        ResponseCookie cookie = buildJwtCookie(jwtToken, Duration.ofHours(1));

        Map<String, Object> success = authService.buildSuccessResponse("Login successful");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(success);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        ResponseCookie cookie = buildJwtCookie(null, Duration.ZERO);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authService.buildSuccessResponse("Logout successful"));
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.ok(false);
        } else {
            return ResponseEntity.ok(true);
        }
    }

    // === Password Reset Endpoints ===

    @PostMapping("/password/request")
    public ResponseEntity<Map<String, Object>> sendResetOtp(@RequestParam String email) {
        profileService.sendResetOtp(email);
        return ResponseEntity.ok(authService.buildSuccessResponse("Reset OTP sent to your email."));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        profileService.resetPassword(
                resetPasswordRequest.getEmail(),
                resetPasswordRequest.getOtp(),
                resetPasswordRequest.getNewPassword());

        return ResponseEntity.ok(authService.buildSuccessResponse("Password has been successfully reset."));
    }

    // === Account Verification Endpoints ===

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendVerificationOtp(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {
        profileService.sendOtp(email);
        return ResponseEntity.ok(authService.buildSuccessResponse("Verification OTP sent to your email."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        profileService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());
        return ResponseEntity.ok(authService.buildSuccessResponse("Account verified successfully."));
    }

    // === Helper Methods ===

    private ResponseCookie buildJwtCookie(String jwtToken, Duration maxAge) {
        return ResponseCookie.from("jwt", jwtToken)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict")
                .secure(true)
                .build();
    }
}