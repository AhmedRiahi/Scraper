package com.pp.crawler.api;


import com.pp.crawler.core.PPCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

@Service
public class CrawlerApiService {

    @Autowired
    private PPCrawler ppCrawler;


    public String downloadImage(String url) throws IOException {
        BufferedImage bufferedImage = this.ppCrawler.downloadImage(new URL(url));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage,"png",baos);
        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(baos.toByteArray());
    }
}
