package com.gepardec.mega.domain.model;

import jakarta.enterprise.inject.Vetoed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Vetoed
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private User user;
}
