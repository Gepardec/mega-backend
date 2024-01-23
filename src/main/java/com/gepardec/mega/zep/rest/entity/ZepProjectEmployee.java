package com.gepardec.mega.zep.rest.entity;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ZepProjectEmployee {
    private String username;
    private boolean lead;

    private ZepProjectEmployeeType type;
}
