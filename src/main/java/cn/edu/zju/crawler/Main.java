package cn.edu.zju.crawler;

import java.nio.file.Files;
import java.io.File;

public class Main {
    public static void main(String[] args) {

        DrugProfessionalInfoCrawler drugProfessionalInfoCrawler = new DrugProfessionalInfoCrawler();
        DrugLabelCrawler drugLabelCrawler = new DrugLabelCrawler();
        DosingGuidelineCrawler dosingGuidelineCrawler = new DosingGuidelineCrawler();

        // comment the step, if you have finished it

        // Step 1
        if(!Files.exists(new File("drugs.data").toPath())) {
            drugLabelCrawler.doCrawlerDrug();
        }

        // Step 2
        if(!Files.exists(new File("drugLabels.data").toPath())) {
            drugLabelCrawler.doCrawlerDrugLabel();
        }

        // Step 4
        if(!Files.exists(new File("drugProfessionalInfo.data").toPath())) {
            drugProfessionalInfoCrawler.doCrawlerDrugProfessionalInfo(true);
        }

        // Step 3
        if(!Files.exists(new File("dosingGuidelines.data").toPath())) {
            dosingGuidelineCrawler.doCrawlerDosingGuidelineList();
        }
    }
}
