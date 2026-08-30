package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;
import com.gepardec.mega.hexagon.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface ProjectLeadDirectoryMapper {

    @Mapping(target = "firstName", source = "name.firstname")
    RecognitionMailRecipient toRecipient(User user);
}
