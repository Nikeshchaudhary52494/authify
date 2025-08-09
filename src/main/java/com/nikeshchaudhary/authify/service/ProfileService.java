package com.nikeshchaudhary.authify.service;

import com.nikeshchaudhary.authify.io.ProfileRequest;
import com.nikeshchaudhary.authify.io.ProfileResponse;

public interface ProfileService {

    ProfileResponse createProfile(ProfileRequest profileRequest);

    ProfileResponse getProfile(String email);

}
