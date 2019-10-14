package com.pp.scrapper.core.requester;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Decoder;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PPCrawlerRequester {

    @Value("${crawler.url}")
    private String crawlerUrl;
    @Autowired
    private RestTemplate restTemplate;

    public String downloadImage(String url){
        return this.restTemplate.getForObject(this.crawlerUrl+"/downloadImage?url="+url,String.class);
    }



    public void init() throws IOException {

        String imageStr = this.downloadImage("https://images.sftcdn.net/images/t_app-cover-l,f_auto/p/befbcde0-9b36-11e6-95b9-00163ed833e7/260663710/the-test-fun-for-friends-screenshot.jpg");
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] imageByte = decoder.decodeBuffer(imageStr);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
        BufferedImage image = ImageIO.read(bis);
        bis.close();

// write the image to a file
        File outputfile = new File("image.png");
        ImageIO.write(image, "png", outputfile);

    }

}
