package com.pp.labtest;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.util.List;
import java.util.Properties;

public class Synthesis {


    public static void main(String[] args){

        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,parse,natlog");
        // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
        props.setProperty("tokenize.language", "fr");
        props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz");
        props.setProperty("pos.model" ,"edu/stanford/nlp/models/pos-tagger/french/french-ud.tagger");
        props.setProperty("depparse.model" ,"edu/stanford/nlp/models/parser/nndep/UD_French.gz");

        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String text="Dans le cadre de son développement, IID cherche à renforcer ses équipes avec des jeunes :\n" +
                "\n" +
                "Analystes développeurs Web (C#, ASP.NET, ASP)\n" +
                "\n" +
                "Analystes développeurs grand système (Cobol, C, IMS, DB2, SQL)\n" +
                "\n" +
                " \n" +
                "\n" +
                "Vos principales missions seront :\n" +
                "\n" +
                "L’analyse des cahiers des charges\n" +
                "\n" +
                "L’analyse et la conception technique\n" +
                "\n" +
                "La participation au développement et au suivi post-production\n" +
                "\n" +
                " \n" +
                "\n" +
                "Vous serez en relation avec les équipes de développement et techniques du groupe.\n" +
                "\n" +
                "Vous pourrez vous appuyer sur le capital humain et technologique des équipes informatiques du groupe et évoluerez au sein d’une équipe dynamique pour relever les nombreux challenges de demain.";
        CoreDocument document = new CoreDocument(text);

        pipeline.annotate(document);

        document.sentences().stream().forEach(s -> {
            List<String> posTags = s.posTags();
            for(int i=0;i<posTags.size();i++){
                System.out.println(s.tokens().get(i)+" : "+posTags.get(i));
            }
        });


        Annotation doc = new Annotation(text);
        pipeline.annotate(doc);
        List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                System.out.println(pos);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
            System.out.println(dependencies);
        }



    }
}
