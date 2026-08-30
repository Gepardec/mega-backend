package com.gepardec.mega.hexagon.recognition.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.RecognitionApi;
import com.gepardec.mega.hexagon.generated.model.RecognitionEntrySubmissionDto;
import com.gepardec.mega.hexagon.recognition.application.port.inbound.SubmitRecognitionEntryUseCase;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.ForbiddenException;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.EMPLOYEE)
public class RecognitionResource implements RecognitionApi {

    private final SubmitRecognitionEntryUseCase submitRecognitionEntryUseCase;
    private final RecognitionRestMapper recognitionRestMapper;
    private final AuthenticatedActorContext authenticatedActorContext;

    @Inject
    public RecognitionResource(
            SubmitRecognitionEntryUseCase submitRecognitionEntryUseCase,
            RecognitionRestMapper recognitionRestMapper,
            AuthenticatedActorContext authenticatedActorContext
    ) {
        this.submitRecognitionEntryUseCase = submitRecognitionEntryUseCase;
        this.recognitionRestMapper = recognitionRestMapper;
        this.authenticatedActorContext = authenticatedActorContext;
    }

    @Override
    public Response submitRecognitionEntry(RecognitionEntrySubmissionDto request) {
        if (authenticatedActorContext.user().isExternal()) {
            throw new ForbiddenException("external users must not submit recognition entries");
        }
        submitRecognitionEntryUseCase.submit(recognitionRestMapper.toCommand(request), authenticatedActorContext.userId());
        return Response.status(Response.Status.CREATED).build();
    }
}
