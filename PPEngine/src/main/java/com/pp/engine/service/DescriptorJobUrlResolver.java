package com.pp.engine.service;

import com.pp.database.model.engine.DescriptorJobCrawlingParams;
import com.pp.engine.exception.URLGeneratorScriptException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DescriptorJobUrlResolver {

    private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    public List<String> resolveJobURLs(DescriptorJobCrawlingParams descriptorJobCrawlingParams){
        List<String> urls = new ArrayList<>();
        if(descriptorJobCrawlingParams.isScriptGeneratedURL()){
            try {
                ScriptObjectMirror result = (ScriptObjectMirror)engine.eval(descriptorJobCrawlingParams.getUrlGeneratorScript());
                if(result != null) {
                   urls.addAll(result.entrySet().stream().map(e -> e.getValue()).map(e -> e.toString()).collect(Collectors.toList()));
                }else {
                    throw new URLGeneratorScriptException();
                }

            } catch (ScriptException e) {
                log.error(e.getMessage(),e);
                throw new URLGeneratorScriptException();
            }
        }else{
            urls.add(descriptorJobCrawlingParams.getUrl());
        }
        return urls;
    }
}
