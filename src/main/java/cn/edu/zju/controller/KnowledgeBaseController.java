package cn.edu.zju.controller;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.bean.DrugProfessionalInfo;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.DrugProfessionalInfoDao;
import cn.edu.zju.servlet.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class KnowledgeBaseController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseController.class);

    private DrugDao drugDao = new DrugDao();
    private DrugLabelDao drugLabelDao = new DrugLabelDao();
    private DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();
    private DrugProfessionalInfoDao drugProfessionalInfoDao = new DrugProfessionalInfoDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/drugs", this::drugs);
        dispatcher.registerGetMapping("/drugLabels", this::drugLabels);
        dispatcher.registerGetMapping("/dosingGuideline", this::dosingGuideline);
        dispatcher.registerGetMapping("/drugProfessionalInfo", this::drugProfessionalInfo);
    }

    public void drugs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<Drug> drugs;
        if (keyword != null && !keyword.trim().isEmpty()) {
            drugs = drugDao.findByKeyword(keyword);
        } else {
            drugs = drugDao.findAll();
        }
        request.setAttribute("drugs", drugs);
        request.getRequestDispatcher("/views/drugs.jsp").forward(request, response);
    }

    public void drugLabels(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<DrugLabel> drugLabels;
        if (keyword != null && !keyword.trim().isEmpty()) {
            drugLabels = drugLabelDao.findByKeyword(keyword);
        } else {
            drugLabels = drugLabelDao.findAll();
        }
        request.setAttribute("drugLabels", drugLabels);
        request.getRequestDispatcher("/views/drug_labels.jsp").forward(request, response);
    }

    public void dosingGuideline(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<DosingGuideline> dosingGuidelines;
        if (keyword != null && !keyword.trim().isEmpty()) {
            dosingGuidelines = dosingGuidelineDao.findByKeyword(keyword);
        } else {
            dosingGuidelines = dosingGuidelineDao.findAll();
        }
        request.setAttribute("dosingGuidelines", dosingGuidelines);
        request.getRequestDispatcher("/views/dosing_guideline.jsp").forward(request, response);
    }

    public void drugProfessionalInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = trimToNull(request.getParameter("keyword"));
        String sourceType = trimToNull(request.getParameter("sourceType"));
        String evidenceLevel = trimToNull(request.getParameter("evidenceLevel"));
        List<DrugProfessionalInfo> drugProfessionalInfos = drugProfessionalInfoDao.findByFilters(keyword, sourceType, evidenceLevel);
        request.setAttribute("drugProfessionalInfos", drugProfessionalInfos);
        request.setAttribute("sourceTypeOptions", drugProfessionalInfoDao.findAllSourceTypes());
        request.setAttribute("evidenceLevelOptions", drugProfessionalInfoDao.findAllEvidenceLevels());
        request.setAttribute("selectedKeyword", keyword == null ? "" : keyword);
        request.setAttribute("selectedSourceType", sourceType == null ? "" : sourceType);
        request.setAttribute("selectedEvidenceLevel", evidenceLevel == null ? "" : evidenceLevel);
        request.getRequestDispatcher("/views/drug_professional_info.jsp").forward(request, response);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
