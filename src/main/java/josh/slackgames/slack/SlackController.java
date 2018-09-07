package josh.slackgames.slack;

import josh.slackgames.slack.objects.Action;
import josh.slackgames.slack.objects.Attachment;
import josh.slackgames.slack.objects.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/slack")
public class SlackController {

    @Value("${SLACK_ACCESS_TOKEN}")
    private String accessToken;

    @Value("${SLACK_CLIENT_ID}")
    private String clientId;

    @Value("${SLACK_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${SG_DOMAIN}")
    private String sgDomain;


    /**
     * Handler for slash-command action
     * @param paramMap form-encoded parameters from Slack
     * @return ResponseEntity
     */
    @RequestMapping("/games")
    public ResponseEntity slashCommand(@RequestBody MultiValueMap<String, String> paramMap) {

        String channelId = paramMap.getFirst("channel_id");
        String channelName = paramMap.getFirst("channel_name");
        String text = paramMap.getFirst("text").trim();
        String userId = paramMap.getFirst("user_id");
        String userName = paramMap.getFirst("user_name");

        Action act = new Action.ActionBuilder()
                .text("Minesweeper")
                .type("button")
                .url(String.format("%s/play/minesweeper/%s/%s", sgDomain, channelId, userId))
                .build();

        List<Action> actList = new ArrayList<>();
        actList.add(act);

        Attachment atc = new Attachment.AttachmentBuilder().actions(actList).build();
        List<Attachment> atcList = new ArrayList<>();
        atcList.add(atc);

        Message msg = new Message.MessageBuilder()
                .text("Select a game to start:")
                .attachments(atcList)
                .build();

        return ResponseEntity.ok(msg);
    }


    /**
     * Authroization handler for Slack
     * @param code Auth code
     * @return ResponseEntity
     */
    @RequestMapping("/authorize")
    public ResponseEntity authorize(@RequestParam String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        RestTemplate rt = new RestTemplate();
        String response = rt.postForObject("https://slack.com/api/oauth.access", entity, String.class);

        return ResponseEntity.ok(response);
    }
}
