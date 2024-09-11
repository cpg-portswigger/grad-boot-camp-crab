package org.example;

import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BootCampControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoginSuccess() throws Exception {
        MockHttpServletRequestBuilder request = post("/login")
                .param("username", "user")
                .param("password", "pass");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/secure/"))
                .andExpect(cookie().value("authenticated", "true"));
    }

    @Test
    void testLoginFailure() throws Exception {
        MockHttpServletRequestBuilder request = post("/login")
                .param("username", "user")
                .param("password", "wrongpass");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/?error=true"))
                .andExpect(cookie().doesNotExist("authenticated"));
    }

    @Test
    void testAccessSecurePageWithoutLogin() throws Exception {
        mockMvc.perform(get("/secure/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/"));
    }

    @Test
    void testAccessSecurePageWithLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authenticated", true);

        mockMvc.perform(get("/secure/").session(session).cookie(new Cookie("authenticated", "true")))
                .andExpect(status().isOk())
                .andExpect(view().name("secure"));
    }

    @Test
    void testLogout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authenticated", true);

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION, "/"))
                .andExpect(cookie().value("authenticated", "false"));
    }

//    @ParameterizedTest
//    @CsvFileSource(resources = "/filePathTraversal.csv", numLinesToSkip = 0)
//    void testFilePathTraversalVulnerability(String attack) throws Exception {
//        MockHttpSession session = new MockHttpSession();
//        session.setAttribute("authenticated", true);
//
//        mockMvc.perform(get("/loadImage")
//                        .param("imagePath",attack)
//                        .session(session))
//
//                .andExpect(status().isNotFound());
//
//    }
    @Test
    void testFilePathTraversalAttack() throws Exception {
        MockHttpServletRequestBuilder request = get("/loadImage")
                .param("imagePath", "etc/passwd");

        mockMvc.perform(request).andExpect(status().is2xxSuccessful());
    }

    @Test
    void testXSSAttack() throws Exception {
        MockHttpServletRequestBuilder request = get("/xssattack")
                .param("name", "<script>alert(1)</script>");

        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(Matchers.containsString("<script>alert(1)</script>")));
    }
}