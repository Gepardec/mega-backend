package com.gepardec.mega.zep.rest.util;

import com.gepardec.mega.zep.util.ZepRestUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jose4j.json.JsonUtil.parseJson;

public class ZepRestUtilTest {

    @Test
    public void getEmptyJson_thenThrowException() {
        String json = "";
        assertThatThrownBy(() -> {
            ZepRestUtil.parseJson(json, "/data", List.class);
        }).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Missing JSON-Path: /data");
    }

    @Test
    public void getData_thenReturnObject() {
        String json = "{" +
                "\"data\": {" +
                "   \"id\": 1," +
                "   \"name\": \"mega\"" +
                "}" +
                "}";

        Optional<Demo> demoOpt = ZepRestUtil.parseJson(json, "/data", Demo.class);
        assertThat(demoOpt.get().id).isEqualTo(1);
        assertThat(demoOpt.get().name).isEqualTo("mega");
    }
    @Test
    public void getDataUnderOtherFields_thenReturnObject() {
        String json = "{" +
                "\"data\": {" +
                "   \"id\": 1," +
                "   \"name\": \"mega\"" +
                "}," +
                "\"otherData\": {" +
                "   \"id\": 2," +
                "   \"name\": \"mega2\"}," +
                "\"data3\": {" +
                "   \"id\": 3," +
                "   \"name\": \"mega3\"" +
                "}" +
                "}";

        Optional<Demo> demoOpt = ZepRestUtil.parseJson(json, "/data3", Demo.class);
        assertThat(demoOpt.get().id).isEqualTo(3);
        assertThat(demoOpt.get().name).isEqualTo("mega3");
    }
    @Test
    public void getEmptyData_thenReturnEmpty() {
        String json = "{\"data\": {}}";

        Optional<Demo> demoOpt = ZepRestUtil.parseJson(json, "/data", Demo.class);
        assertThat(demoOpt.isEmpty()).isTrue();
    }
    @Test
    public void getEmptyArr_thenReturnEmpty() {
        String json = "{\"data\": []}";

        Optional<Demo> demoOpt = ZepRestUtil.parseJson(json, "/data", Demo.class);
        assertThat(demoOpt.isEmpty()).isTrue();
    }
    @Test
    public void getArr_thenReturnArr() {
        String json = "{\"data\": [" +
                "{\"id\": 1, \"name\": \"MEGA\"}," +
                "{\"id\": 2, \"name\": \"GEMA\"}" +
                "]}";

        List<Demo> list = List.of(ZepRestUtil.parseJson(json, "/data", Demo[].class).get());
        assertThat(list.get(0).id).isEqualTo(1);
        assertThat(list.get(0).name).isEqualTo("MEGA");
        assertThat(list.get(1).id).isEqualTo(2);
        assertThat(list.get(1).name).isEqualTo("GEMA");
    }



    private static class Demo {
        public int id;
        public String name;
    }
}
