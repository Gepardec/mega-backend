package com.gepardec.mega.zep.service.impl;

import com.gepardec.mega.aplication.security.ForbiddenException;
import com.gepardec.mega.aplication.security.SessionUser;
import com.gepardec.mega.aplication.security.UnauthorizedException;
import com.gepardec.mega.zep.service.api.UserService;
import com.gepardec.mega.zep.soap.ZepSoapProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import de.provantis.zep.MitarbeiterType;
import de.provantis.zep.ReadMitarbeiterRequestType;
import de.provantis.zep.ReadMitarbeiterResponseType;
import de.provantis.zep.ZepSoapPortType;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.Optional;

@RequestScoped
public class UserServiceImpl implements UserService {

    @Inject
    ZepSoapPortType zepSoapPortType;

    @Inject
    GoogleIdTokenVerifier tokenVerifier;

    @Inject
    ZepSoapProvider zepSoapProvider;

    @Inject
    SessionUser sessionUser;

    @Override
    public MitarbeiterType login(String idToken) {
        String email;

        try {
            email = Optional.ofNullable(tokenVerifier.verify(idToken))
                    .orElseThrow(() -> new UnauthorizedException("IdToken was invalid"))
                    .getPayload()
                    .getEmail();
        } catch (Exception e) {
            throw new IllegalStateException("Could not verify idToken", e);
        }

        // get ZEP employee
        final ReadMitarbeiterRequestType empl = new ReadMitarbeiterRequestType();
        empl.setRequestHeader(zepSoapProvider.createRequestHeaderType());

        final ReadMitarbeiterResponseType rmrt = zepSoapPortType.readMitarbeiter(empl);
        //TODO: contact ZEP for enabling search-criteria for mail-address
        final MitarbeiterType mt = rmrt.getMitarbeiterListe().getMitarbeiter().stream()
                .filter(emp -> email.equals(emp.getEmail()))
                .findFirst().orElse(null);

        if (mt == null) {
            throw new ForbiddenException(String.format("'%s' is not an employee in ZEP", email));
        }

        // Could be re-logged in with different user within the same session
        sessionUser.init(email, idToken, mt.getRechte());

        return mt;
    }
}