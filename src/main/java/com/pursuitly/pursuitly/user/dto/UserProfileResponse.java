package com.pursuitly.pursuitly.user.dto;

import com.pursuitly.pursuitly.common.enums.ExperienceLevel;
import com.pursuitly.pursuitly.common.enums.RemotePreference;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String desiredTitle;
    private ExperienceLevel experienceLevel;
    private String location;
    private RemotePreference remotePreference;
    private Integer salaryMin;
    private String summary;
}
