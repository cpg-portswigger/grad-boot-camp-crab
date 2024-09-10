package org.example;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class BootCampController
{
    @Autowired
    private AppRootPath appRootPath;

    @GetMapping("/")
    public String index()
    {
        return "index";
    }

    @GetMapping("/secure/")
    public String secure(HttpServletRequest request)
    {
        // Commenting this out for testing purposes, I'm sure I'll remember to remove it later...
        String authCookieValue = getCookieValue(request, "authenticated");
        if ("true".equals(authCookieValue))
        {
            return "secure";
        }
        return "redirect:/";
    }

    @GetMapping("/loadImage")
    public ResponseEntity<ByteArrayResource> loadImage(@RequestParam String imagePath) {
        try {
            Path path = appRootPath.getRootPath().resolve(imagePath);
            String content = new String(Files.readAllBytes(path));

            // Load the image as a byte array
            byte[] imageBytes = Files.readAllBytes(path);
            ByteArrayResource resource = new ByteArrayResource(imageBytes);

            // Determine the content type of the image
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // Fallback to binary if type is unknown
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName().toString() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpServletResponse response, HttpSession session)
    {
        if (authenticate(username, password))
        {
            session.setAttribute("authenticated", true);
            Cookie authCookie = new Cookie("authenticated", "true");
            authCookie.setHttpOnly(true);
//            authCookie.setSecure(true);
            response.addCookie(authCookie);
            return "redirect:/secure/";
        }
        else
        {
            return "redirect:/?error=true";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response)
    {
        session.invalidate();
        Cookie authCookie = new Cookie("authenticated", "false");
        authCookie.setMaxAge(0);
        response.addCookie(authCookie);
        return "redirect:/";
    }

    private boolean authenticate(String username, String password)
    {
        // Hardcoded username and password check
        return "user".equals(username) && "pass".equals(password);
    }

    @SuppressWarnings("SameParameterValue")
    private String getCookieValue(HttpServletRequest request, String cookieName)
    {
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookie.getName().equals(cookieName))
                {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
