package com.admin.portal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class JobShareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testShareJob() throws Exception {
        mockMvc.perform(get("/share/jobs/1?redirect=http%3A%2F%2Flocalhost%3A5173%2Fcareers"))
               .andDo(print());
    }
}
