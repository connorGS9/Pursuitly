package com.pursuitly.pursuitly.user.dto;

import com.pursuitly.pursuitly.common.enums.ExperienceLevel;
import com.pursuitly.pursuitly.common.enums.RemotePreference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {
    private String desiredTitle;
    private ExperienceLevel experienceLevel;
    private String location;
    private RemotePreference remotePreference;
    private Integer salaryMin;
    private String summary;
}