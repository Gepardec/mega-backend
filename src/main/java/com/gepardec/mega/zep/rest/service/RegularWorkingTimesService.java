package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.util.Paginator;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class RegularWorkingTimesService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    public ZepRegularWorkingTimes getRegularWorkingTimesByUsername(String username) {
        try  {
            List<ZepRegularWorkingTimes> zepRegularWorkingTimes = Paginator.retrieveAll(
                    (page) -> zepEmployeeRestClient.getRegularWorkingTimesByUsername(username, page),
                    ZepRegularWorkingTimes.class);

            if (zepRegularWorkingTimes.size() == 0) {
                throw new ZepServiceException("Data empty");
            }
            return zepRegularWorkingTimes.stream().sorted(Comparator.comparing(ZepRegularWorkingTimes::getStart_date, Comparator.nullsLast(Comparator.reverseOrder()))).findFirst().orElse(null);
        } catch (Exception e) {
            throw new ZepServiceException("Error retrieving the data of /" + username + "/regular-working-times", e);
        }
    }
}
