package com.nikeshchaudhary.authify.io;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyOtpRequest {

    private String otp;
    private String email;

}
