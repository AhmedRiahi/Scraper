package com.pp.framework.kafka;

public class KafkaTopics {

	public static final String IN = ".in";
	public static final String OUT = ".out";
	public static final String ERROR = ".error";
	public static final String ENV = "";

	public static class Mozart{
		public static final String PROCESS_DESCRIPTOR = "mozart.process_descriptor"+ENV;
        public static final String PROCESS_DESCRIPTOR_JOIN = "mozart.process_descriptor_join"+ENV;
	}
	
	public static class Crawler{
		public static final String DOWNLOAD = "crawler.download"+ENV;
	}

	public static class Renderer{
		public static final String DOWNLOAD = "renderer.download"+ENV;
	}
	
	public static class Scraper{
		public static final String MATCH_DESCRIPTOR = "scrapper.match_descriptor"+ENV;
	}
	
	public static class Analytics{
		public static final String ANALYSE_STANDALONE_DESCRITOR_POPULATION = "analytics.analyse_standalone_descriptor_population"+ENV;
		public static final String ANALYSE_JOINED_DESCRITOR_POPULATION = "analytics.analyse_joined_descriptor_population"+ENV;
        public static final String ANALYSE_JOINER_DESCRITOR_POPULATION = "analytics.analyse_joiner_descriptor_population"+ENV;
		public static final String ANALYSE_INDIVIDUAL = "analytics.analyse_individual"+ENV;
	}
	
	public static class Subscription{
		public static final String SCAN_DESCRIPTOR_POPULATION_SUBSCRIPTION = "subscription.scan_descriptor_population_subscription"+ENV;
	}
	
	public static class Cleaner{
		public static final String CLEAN = "cleaner.clean"+ENV;
	}

	public static class Engine {
		public static final String LAUNCH_JOB = "engine.launch_job"+ENV;
	}
}
