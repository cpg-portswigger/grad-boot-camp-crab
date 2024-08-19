package org.example;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BootCampController
{
    @GetMapping("/")
    public String index()
    {
        return "index";
    }

    @GetMapping("/secure/")
    public String secure(HttpServletRequest request)
    {
        String authCookieValue = getCookieValue(request, "authenticated");
        if ("true".equals(authCookieValue))
        {
            return "secure";
        }
        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpServletResponse response, HttpSession session)
    {
        if (authenticate(username, password))
        {
            session.setAttribute("authenticated", true);
            Cookie authCookie = new Cookie("authenticated", "true");
            authCookie.setHttpOnly(true);
            authCookie.setSecure(true);
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
