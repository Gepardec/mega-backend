package com.gepardec.mega;

import com.gepardec.mega.aplication.security.SessionUser;
import io.quarkus.test.Mock;

@Mock
public class SessionUserMock extends SessionUser {

    @Override
    public void init(String email, String idToken, int recht) {
        super.init(email, idToken, recht);
    }
}
