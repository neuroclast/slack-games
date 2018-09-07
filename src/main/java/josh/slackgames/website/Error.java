package josh.slackgames.website;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class Error implements ErrorController {

    @RequestMapping("/error")
    @ResponseBody
    public String errorHandler(HttpServletRequest request) {
        return "Woops! An error occured!";
    }


    @Override
    public String getErrorPath() {
        return "/error";
    }
}
