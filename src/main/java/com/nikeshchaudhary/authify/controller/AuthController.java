package com.nikeshchaudhary.authify.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nikeshchaudhary.authify.io.AuthRequest;
import com.nikeshchaudhary.authify.io.ResetPasswordRequest;
import com.nikeshchaudhary.authify.io.VerifyOtpRequest;
import com.nikeshchaudhary.authify.service.ProfileService;
import com.nikeshchaudhary.authify.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;

    // === Authentication Endpoints ===

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            authenticate(authRequest.getEmail(), authRequest.getPassword());

            final String jwtToken = jwtUtil.generateToken(authRequest.getEmail());
            ResponseCookie cookie = buildJwtCookie(jwtToken);

            Map<String, Object> success = buildSuccessResponse("Login successful");
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(success);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildErrorResponse("Incorrect email or password"));
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildErrorResponse("Account is disabled"));
        }
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

    @PostMapping("/send-reset-otp")
    public ResponseEntity<?> sendResetOtp(@RequestParam String email) {
        try {
            profileService.sendResetOtp(email);
            return ResponseEntity.ok(buildSuccessResponse("Reset OTP sent to your email."));
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.badRequest().body(buildErrorResponse("User with this email not found."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            profileService.resetPassword(
                    resetPasswordRequest.getEmail(),
                    resetPasswordRequest.getOtp(),
                    resetPasswordRequest.getNewPassword());

            return ResponseEntity.ok(buildSuccessResponse("Password has been successfully reset."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(buildErrorResponse(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("An unexpected error occurred."));
        }
    }

    // === Account Verification Endpoints ===

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendVerificationOtp(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {
        try {
            profileService.sendOtp(email);
            return ResponseEntity.ok(buildSuccessResponse("Verification OTP sent to your email."));
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.badRequest().body(buildErrorResponse("User with this email not found."));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        try {
            profileService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());
            return ResponseEntity.ok(buildSuccessResponse("Account verified successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(buildErrorResponse(ex.getMessage()));
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.badRequest().body(buildErrorResponse("User with this email not found."));
        }
    }

    // === Helper Methods ===

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    private ResponseCookie buildJwtCookie(String jwtToken) {
        return ResponseCookie.from("jwt", jwtToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Strict")
                .secure(true)
                .build();
    }

    private Map<String, Object> buildSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", false);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("message", message);
        return response;
    }
}