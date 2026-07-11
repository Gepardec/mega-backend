package com.gepardec.mega.hexagon.recognition.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.RecognitionEntrySubmissionDto;
import com.gepardec.mega.hexagon.recognition.application.port.inbound.SubmitRecognitionEntryCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface RecognitionRestMapper {

    @Mapping(target = "anonymous", source = "anonymous", defaultValue = "false")
    SubmitRecognitionEntryCommand toCommand(RecognitionEntrySubmissionDto request);
}
