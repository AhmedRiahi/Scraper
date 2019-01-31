package com.pp.dashboard.controller;

import com.pp.dashboard.service.DWDPService;
import com.pp.database.model.scrapper.descriptor.listeners.ScrapedContent;
import com.pp.database.model.semantic.individual.PPIndividual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dwdp")
public class DWDPController {

    @Autowired
    private DWDPService dwdpService;

    @RequestMapping(path = "/{dwdpId}/scrapedContents/",method = RequestMethod.GET)
    public List<ScrapedContent> getScapedContents(@PathVariable  String dwdpId){
        return this.dwdpService.getScrapedContents(dwdpId);
    }

    @RequestMapping(path = "/{dwdpId}/individuals")
    public List<PPIndividual> getIndividuals(@PathVariable String dwdpId){
        return this.dwdpService.getIndividuals(dwdpId);
    }
}
