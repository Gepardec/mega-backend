package com.gepardec.mega.zep;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

public class ZepRestServiceImplTest {
    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Test
    public void test() {
        regularWorkingTimesService.getRegularWorkingTimesByUsername("001-hwirnsberger");
    }
}
