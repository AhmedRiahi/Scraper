package com.pp.labtest;



import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.json.GsonJsonParser;

import java.util.List;

public class Scraper {

    public static void main(String[] args){
        String str = "{\n" +
                "    \"login\": \"Moham3dRiahi\",\n" +
                "    \"id\": 28678908,\n" +
                "    \"node_id\": \"MDQ6VXNlcjI4Njc4OTA4\",\n" +
                "    \"avatar_url\": \"https://avatars3.githubusercontent.com/u/28678908?v=4\",\n" +
                "    \"gravatar_id\": \"\",\n" +
                "    \"url\": \"https://api.github.com/users/Moham3dRiahi\",\n" +
                "    \"html_url\": \"https://github.com/Moham3dRiahi\",\n" +
                "    \"followers_url\": \"https://api.github.com/users/Moham3dRiahi/followers\",\n" +
                "    \"following_url\": \"https://api.github.com/users/Moham3dRiahi/following{/other_user}\",\n" +
                "    \"gists_url\": \"https://api.github.com/users/Moham3dRiahi/gists{/gist_id}\",\n" +
                "    \"starred_url\": \"https://api.github.com/users/Moham3dRiahi/starred{/owner}{/repo}\",\n" +
                "    \"subscriptions_url\": \"https://api.github.com/users/Moham3dRiahi/subscriptions\",\n" +
                "    \"organizations_url\": \"https://api.github.com/users/Moham3dRiahi/orgs\",\n" +
                "    \"repos_url\": \"https://api.github.com/users/Moham3dRiahi/repos\",\n" +
                "    \"events_url\": \"https://api.github.com/users/Moham3dRiahi/events{/privacy}\",\n" +
                "    \"received_events_url\": \"https://api.github.com/users/Moham3dRiahi/received_events\",\n" +
                "    \"type\": \"User\",\n" +
                "    \"site_admin\": false,\n" +
                "    \"name\": \"Mohamed Riahi\",\n" +
                "    \"company\": null,\n" +
                "    \"blog\": \"\",\n" +
                "    \"location\": \"Tunisia\",\n" +
                "    \"email\": null,\n" +
                "    \"hireable\": null,\n" +
                "    \"bio\": \"G33k \uD83D\uDC64, programmer \uD83D\uDCBB, Pentester ⚠ , Defacer \uD83D\uDC68\\u200d\uD83D\uDCBB, Crypto fan \uD83D\uDD12, Malware lover ☢, security researcher \uD83D\uDD0E, open source fan ❤, AGE: 22\\r\\n\",\n" +
                "    \"public_repos\": 4,\n" +
                "    \"public_gists\": 0,\n" +
                "    \"followers\": 294,\n" +
                "    \"following\": 0,\n" +
                "    \"created_at\": \"2017-05-13T22:10:17Z\",\n" +
                "    \"updated_at\": \"2019-01-17T15:59:12Z\"\n" +
                "}";
        JSONObject json = new JSONObject(str);
        String xml = XML.toString(json,"body");
        System.out.println(xml);
    }
}
